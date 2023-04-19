/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot.redis;

/*
 * a member of a recovery manager fail-over group
 * the pair <failoverGroupId>:<nodeId> are managed by one Recovery Manager
 */
public class CloudId {
    String nodeId;
    // keys in the same failover group support migrate semantics within the group
    String failoverGroupId;
    String description;
    String id; // The pair <failoverGroupId>:<nodeId> must be unique in a give Redis cluster
    String keyPattern;

    public CloudId(String nodeId) {
        this(nodeId, "0");
    }

    public CloudId(String nodeId, String failoverGroupId) {
        this(nodeId, failoverGroupId, null);
    }

    public CloudId(String nodeId, String failoverGroupId, String description) {
        this.failoverGroupId = failoverGroupId;
        this.nodeId = nodeId;
        this.id = String.format("%s:%s", failoverGroupId, nodeId);
        this.description = description;
        this.keyPattern = String.format("{%s}:%s:*", failoverGroupId, nodeId); // matches all keys if this failover group
    }

    /**
     * @return a pattern that matches all keys that share the same failoverGroupId and nodeId
     */
    public String allKeysPattern() {
        return keyPattern;
    }
}
