/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.arjuna.ats.internal.arjuna.objectstore.slot;

import com.arjuna.ats.arjuna.common.CoreEnvironmentBean;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Connection;
import redis.clients.jedis.ConnectionPool;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static redis.clients.jedis.params.ScanParams.SCAN_POINTER_START;

/**
 * Redis backed implementation of the SlotStore backend.
 * Ensure that your Redis installation is configured for
 */
public class RedisSlots implements BackingSlots, SharedSlots {
    private CloudId cloudId;
    private byte[][] slots = null;
    private boolean clustered;
    private JedisPool jedisPool; // Java API for Redis
    private JedisCluster jedisCluster; // Java API for Redis TODO do we need a pool
    private HostAndPort hostAndPort;

    @Override
    public void init(SlotStoreEnvironmentBean slotStoreConfig) throws IOException {
        if (slots != null) {
            throw new IllegalStateException("already initialized");
        }

        RedisStoreEnvironmentBean env = BeanPopulator.getDefaultInstance(RedisStoreEnvironmentBean.class);
        String nodeId = BeanPopulator.getDefaultInstance(CoreEnvironmentBean.class).getNodeIdentifier();

        slots = new byte[slotStoreConfig.getNumberOfSlots()][];
        cloudId = new CloudId(nodeId, env.getFailoverId());
        hostAndPort = new HostAndPort(env.getRedisHost(), env.getRedisPort());
        clustered = env.isClustered();

        Set<String> keys;

        if (clustered) {
//            jedisCluster = new JedisCluster(hostAndPort);
            initJedis();
            keys = loadClustered();
        } else {
            // nb jedis instances are single threaded
            jedisPool = new JedisPool(env.getRedisURI()); // TODO pass in JedisClientConfig()
            keys = loadSingle();
        }

        load(keys);
    }
    private void initJedis() {
        // provide one of the master instances (the others will be auto discovered)
        jedisCluster = new JedisCluster(new HostAndPort("127.0.0.1", 30001));
    }

    private void initJedis2() {
        Set<HostAndPort> jedisClusterNodes = new HashSet<> ();

        jedisClusterNodes.add(new HostAndPort("127.0.0.1", 30001));
        jedisClusterNodes.add(new HostAndPort("127.0.0.1", 30002));
        jedisClusterNodes.add(new HostAndPort("127.0.0.1", 30003));

        GenericObjectPoolConfig<Connection> jedisPoolConfig = new GenericObjectPoolConfig<> ();
//        jedisPoolConfig.setsetMaxWaitMillis(10);
        JedisClientConfig jedisClientConfig = DefaultJedisClientConfig.builder().timeoutMillis(50).build();

        jedisCluster = new JedisCluster(jedisClusterNodes,jedisClientConfig,2,jedisPoolConfig);
    }

    public void fini() {
        slots = null;

        if (clustered) {
            jedisCluster.close();
        } else {
            jedisPool.close(); // probably needs to be synchronised unless we can control when close gets called
        }
    }

    private Set<String> loadClustered() {
        int i = 0;
        Set<String> keys = new HashSet<>();

        for (ConnectionPool node : jedisCluster.getClusterNodes().values()) {
            try (Jedis j = new Jedis(node.getResource())) {
                // load keys matching this recovery manager
//                String pattern = String.format("{%s}:%s:*", cloudId.failoverGroupId, cloudId.nodeId);
                Set<String> candidates = j.keys(cloudId.allKeysPattern()); //cloudId.keyPattern);
                // filter out candidates that don't match this managers node id
//"{0}:migration-node:6"
//                Collection actuals = candidates.stream().filter(s -> s.matches(pattern)).collect(Collectors.toList());
                keys.addAll(candidates);
            }
        }

        return keys;
    }

    private Set<String> loadSingle() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.keys(cloudId.keyPattern);
        }
    }

    private void load(Set<String> keys) {
        int i = 0;

        for (String key : keys) {
            if (i < slots.length) {
                slots[i] = key.getBytes(StandardCharsets.UTF_8);
                i += 1;
            } else {
                tsLogger.logger.info("Too many redis keys: ignoring remaining keys from " + key);
                break;
            }
        }

        // initialise the remaining slots
        while (i < slots.length) {
            // prefix the slot key with the cloudId and force keys for nodeId + failoverId into the same hash slot
            // (using the curly brace notation) so that they will be stored on the same redis node
            // In this way we can perform multikey operations on a slot
            // see https://redis.io/docs/reference/cluster-spec/ section "Key distribution model" for more info
//            slots[i] = String.format("{%s}:%d", cloudId.id, i).getBytes(StandardCharsets.UTF_8);
            slots[i] = String.format("{%s}:%s:%d", cloudId.failoverGroupId, cloudId.nodeId, i).getBytes(StandardCharsets.UTF_8);
            i += 1;
        }
    }

    @Override
    public void write(int slot, byte[] data, boolean sync) throws IOException {
        if (!clustered) {
            try (Jedis jedis = jedisPool.getResource()) { // or use JedisPooled to avoid the try with resources
                String ok = jedis.set(slots[slot], data);

                if (ok == null) { // ok == "OK" implies success
                    throw new IOException("redis write failed for slot " + slot);
                }
            }
        } else {
            String ok = jedisCluster.set(slots[slot], data);
            System.out.printf("write: %s%n", ok);
//            try (JedisCluster jedis = new , JedisCluster(hostAndPort)) {
//                String ok = jedis.set(slots[slot], data);

                /*
                 * Replication is asynchronous by default, but we need it to be synchronous (to satisfy consistency
                 * and partition tolerance) so do not return until we know a majority of replicas have received the
                 * update.
                 *
                 * TODO as well as using synchronous replication we need to verify that there has not been a failover event.
                 *    We also need to pass in the correct replica count, here I use 1 to just get the durability
                 * guarantee since the wait won't return until the write has been made durable.
                 * This store is experimental since more analysis needs to be carried out to determine whether or not
                 * Redis is a suitable back end for storing transaction logs - ie is it safe with respect to CAP
                 *
                 * NB enable clustering and AOF in /etc/redis/redis.conf
                 * cluster-enabled yes
                 * appendonly yes
                 */
//                long replicaResponses = jedis.waitReplicas(slots[slot], 1, 0);

//                if (replicaResponses == 0) {
//                    System.out.printf("failed%n");
//                }
//            }
        }
    }

    @Override
    public byte[] read(int slot) throws IOException {
        if (clustered) {
                    return jedisCluster.get(slots[slot]);
/*            try (JedisCluster jedis = new JedisCluster(hostAndPort)) {
                try {
                    return jedis.get(slots[slot]);
                } catch (Exception e) {
                    throw new IOException(e);
                }
            }*/
        } else {
            try (Jedis jedis = jedisPool.getResource()) {
                return jedis.get(slots[slot]);
            }
        }
    }

    @Override
    public void clear(int slot, boolean sync) throws IOException {
        if (clustered) {
            try (JedisCluster jedis = new JedisCluster(hostAndPort)) {
                jedis.del(slots[slot]);
            }
        } else {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.del(slots[slot]);
            }
        }
    }

    @Override
    public String getNodeId() {
        return cloudId.nodeId;
    }

    @Override
    public boolean migrate(CloudId to) {
        return migrate(cloudId, to);
    }

    @Override
    public boolean migrate(CloudId from, CloudId to) {
        if (!clustered) {
            // in Cluster mode, only keys in the same hash slot (ie they have the same hash tag) can be reliably renamed
            throw new UnsupportedOperationException("migrating logs is only supported by Redis Cluster");
        }

        if (from.failoverGroupId != to.failoverGroupId) {
            throw new UnsupportedOperationException("migrating logs is only supported if they belong to the same failover group");
        }

        String keyPattern = from.allKeysPattern();

        try (JedisCluster jedis = new JedisCluster(hostAndPort)) {
            for (String key : getKeys(keyPattern)) {
                String newKey = key.replace(from.nodeId, to.nodeId);

                try {
                    String res = jedis.rename(key, newKey);
                    System.out.printf("%s%n", res);
                } catch (JedisException e) {
                    System.out.printf("%s%n", e.getMessage());
                    return false;
                }
            }
        }

        return true;
    }

    private void getKeys(Jedis node, String keyPattern, Set<String> keySet) {
        ScanParams scanParams = new ScanParams().count(100).match(keyPattern);
        String cursor = SCAN_POINTER_START;

        do {
            ScanResult<String> scanResult = node.scan(cursor, scanParams);
            List<String> keys = scanResult.getResult();
            keySet.addAll(keys);
            cursor = scanResult.getCursor();
        } while (!cursor.equals(SCAN_POINTER_START));
    }

    private Set<String> getKeys(String keyPattern) {
        try (JedisCluster jedisCluster = new JedisCluster(hostAndPort)) {
            Set<String> keySet = new HashSet<>();

            for (ConnectionPool node : jedisCluster.getClusterNodes().values()) {
                try (Jedis jedis = new Jedis(node.getResource())) {
                    keySet.addAll(jedis.keys(keyPattern));
                }
            }

            return keySet;
        }
    }
}
