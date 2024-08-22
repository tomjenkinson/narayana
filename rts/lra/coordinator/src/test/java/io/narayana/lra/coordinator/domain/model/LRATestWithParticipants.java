/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package io.narayana.lra.coordinator.domain.model;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import io.narayana.lra.LRAData;
import io.narayana.lra.client.NarayanaLRAClient;
import io.narayana.lra.coordinator.api.Coordinator;
import io.narayana.lra.coordinator.domain.service.LRAService;
import io.narayana.lra.coordinator.internal.LRARecoveryModule;
import io.narayana.lra.filter.ServerLRAFilter;
import io.narayana.lra.logging.LRALogger;
import io.narayana.lra.provider.ParticipantStatusOctetStreamProvider;
import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.test.TestPortProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static io.narayana.lra.LRAConstants.COORDINATOR_PATH_NAME;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_PARENT_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_RECOVERY_HEADER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LRATestWithParticipants {

    private static UndertowJaxrsServer server;
    private static LRAService service;
    static final AtomicInteger compensateCount = new AtomicInteger(0);
    static final AtomicInteger completeCount = new AtomicInteger(0);
    static final AtomicInteger forgetCount = new AtomicInteger(0);
    static final long LRA_SHORT_TIMELIMIT = 10L;
    private static LRAStatus status = LRAStatus.Active;
    private static final AtomicInteger acceptCount = new AtomicInteger(0);
    private NarayanaLRAClient lraClient;
    private Client client;
    private String coordinatorPath;
    @Rule
    public TestName testName = new TestName();
    private static Object lock = new Object();
    private static boolean joinAttempted;
    private static boolean compensateCalled;

    @Path("/test")
    public static class Participant {

        private Logger log = Logger.getLogger(getClass());
        private Response getResult(boolean cancel, URI lraId) {
            Response.Status status = cancel ? Response.Status.INTERNAL_SERVER_ERROR : Response.Status.OK;
            return Response.status(status).entity(lraId.toASCIIString()).build();
        }

        @GET
        @Path("start-end")
        @LRA(value = LRA.Type.REQUIRED)
        public Response doInLRA(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI contextId,
                @DefaultValue("0") @QueryParam("accept") Integer acceptCount,
                @DefaultValue("false") @QueryParam("cancel") Boolean cancel) {
            LRATestWithParticipants.acceptCount.set(acceptCount);
            return getResult(cancel, contextId);
        }

        @GET
        @Path("start")
        @LRA(value = LRA.Type.REQUIRED, end = false)
        public Response startInLRA(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI contextId,
                @HeaderParam(LRA_HTTP_PARENT_CONTEXT_HEADER) URI parentLRA,
                @DefaultValue("0") @QueryParam("accept") Integer acceptCount,
                @DefaultValue("false") @QueryParam("cancel") Boolean cancel) {
            LRATestWithParticipants.acceptCount.set(acceptCount);
            return getResult(cancel, contextId);
        }

        @PUT
        @Path("end")
        @LRA(value = LRA.Type.MANDATORY, cancelOnFamily = Response.Status.Family.SERVER_ERROR)
        public Response endLRA(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI contextId,
                @HeaderParam(LRA_HTTP_PARENT_CONTEXT_HEADER) URI parentLRA,
                @DefaultValue("0") @QueryParam("accept") Integer acceptCount,
                @DefaultValue("false") @QueryParam("cancel") Boolean cancel) {
            LRATestWithParticipants.acceptCount.set(acceptCount);
            return getResult(cancel, contextId);
        }

        @GET
        @Path("time-limit")
        @Produces(MediaType.APPLICATION_JSON)
        @LRA(value = LRA.Type.REQUIRED, timeLimit = 500, timeUnit = ChronoUnit.MILLIS)
        public Response timeLimit(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
            try {
                // sleep for longer than specified in the attribute 'timeLimit'
                // (go large, ie 2 seconds, to avoid time issues on slower systems)
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                LRALogger.logger.debugf("Interrupted because time limit elapsed", e);
            }
            return Response.status(Response.Status.OK).entity(lraId.toASCIIString()).build();
        }

        @GET
        @Path("timed-action")
        @LRA(value = LRA.Type.REQUIRED, end = false, timeLimit = LRA_SHORT_TIMELIMIT) // the default unit is SECONDS
        public Response actionWithLRA(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI contextId,
                @DefaultValue("false") @QueryParam("cancel") Boolean cancel) {
            status = LRAStatus.Active;
            server.stop(); // simulate a server crash
            return getResult(cancel, contextId);
        }

        @GET
        @Path("status")
        public Response getStatus() {
            return Response.ok(status.name()).build();
        }

        @PUT
        @Path("/complete")
        @Complete
        public Response complete(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI contextLRA,
                @HeaderParam(LRA_HTTP_PARENT_CONTEXT_HEADER) URI parentLRA) {
            if (acceptCount.getAndDecrement() <= 0) {
                completeCount.incrementAndGet();
                acceptCount.set(0);
                return Response.status(Response.Status.OK).entity(ParticipantStatus.Completed).build();
            }
            return Response.status(Response.Status.ACCEPTED).entity(ParticipantStatus.Completing).build();
        }

        @PUT
        @Path("/compensate")
        @Compensate
        public Response compensate(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI contextLRA,
                @HeaderParam(LRA_HTTP_PARENT_CONTEXT_HEADER) URI parentLRA) {
            synchronized (lock) {
                compensateCalled = true;
                lock.notify();
            }
            synchronized (lock) {
                while (!joinAttempted) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        fail("Could not wait");
                    }
                }
            }
            if (acceptCount.getAndDecrement() <= 0) {
                compensateCount.incrementAndGet();
                acceptCount.set(0);
                return Response.status(Response.Status.OK).entity(ParticipantStatus.Compensated).build();
            }
            return Response.status(Response.Status.ACCEPTED).entity(ParticipantStatus.Compensating).build();
        }
    }
    @ApplicationPath("service1")
    public static class Service1 extends Application {

        @Override
        public Set<Class<?>> getClasses() {
            HashSet<Class<?>> classes = new HashSet<>();
            classes.add(Participant.class);
            classes.add(ServerLRAFilter.class);
            classes.add(ParticipantStatusOctetStreamProvider.class);
            return classes;
        }
    }
    @ApplicationPath("service2")
    public static class Service2 extends Application {

        @Override
        public Set<Class<?>> getClasses() {
            HashSet<Class<?>> classes = new HashSet<>();
            classes.add(Participant.class);
            classes.add(ServerLRAFilter.class);
            classes.add(ParticipantStatusOctetStreamProvider.class);
            return classes;
        }
    }
    @ApplicationPath("service3")
    public static class Service3 extends Application {

        @Override
        public Set<Class<?>> getClasses() {
            HashSet<Class<?>> classes = new HashSet<>();
            classes.add(Participant.class);
            classes.add(ServerLRAFilter.class);
            classes.add(ParticipantStatusOctetStreamProvider.class);
            return classes;
        }
    }
    @ApplicationPath("service4")
    public static class Service4 extends Application {

        @Override
        public Set<Class<?>> getClasses() {
            HashSet<Class<?>> classes = new HashSet<>();
            classes.add(Participant.class);
            classes.add(ServerLRAFilter.class);
            classes.add(ParticipantStatusOctetStreamProvider.class);
            return classes;
        }
    }
    @ApplicationPath("/")
    public static class LRACoordinator extends Application {

        @Override
        public Set<Class<?>> getClasses() {
            HashSet<Class<?>> classes = new HashSet<>();
            classes.add(Coordinator.class);
            return classes;
        }
    }
    @BeforeClass
    public static void start() {
        System.setProperty("lra.coordinator.url", TestPortProvider.generateURL('/' + COORDINATOR_PATH_NAME));
    }

    @Before
    public void before() {
        LRALogger.logger.debugf("Starting test %s", testName);
        server = new UndertowJaxrsServer().start();
        clearObjectStore();
        lraClient = new NarayanaLRAClient();
        compensateCount.set(0);
        completeCount.set(0);
        forgetCount.set(0);
        client = ClientBuilder.newClient();
        coordinatorPath = TestPortProvider.generateURL('/' + COORDINATOR_PATH_NAME);
        server.deploy(LRACoordinator.class);
        server.deployOldStyle(Service2.class);
        server.deployOldStyle(Service3.class);
        service = LRARecoveryModule.getService();
    }

    @After
    public void after() {
        LRALogger.logger.debugf("Finished test %s", testName);
        lraClient.close();
        client.close();
        clearObjectStore();
        server.stop();
    }

    @Test
    public void testJoinAfterTimeout() {
        // 1. Service 1 calls POST /lra-coordinator/start to start a Saga.
        URI lraId = lraClient.startLRA(null, "testTimeLimit", 1000L, ChronoUnit.MILLIS);
        // 2. Service 2 calls PUT /lra-coordinator/{LraId} to join the Saga.
        lraClient.joinLRA(lraId, null, URI.create("http://localhost:8081/service2/test"), null);
        // 3. Service 3 calls PUT /lra-coordinator/{LraId} to join the same Saga.
        lraClient.joinLRA(lraId, null, URI.create("http://localhost:8081/service3/test"), null);
        // 4. A timeout exception occurs in Service 1, leading it to call PUT
        // /lra-coordinator/{LraId}/cancel to cancel the Saga.
        // 5. The LRA Coordinator calls the compensation API /saga/compensate registered
        // by Service 2 and Service 3.
        try {
            TimeUnit.SECONDS.sleep(2);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 6. Service 2 receives the /saga/compensate call and begins compensating,
        // which takes more than 2 seconds.
        // 7. Before step 6 is completed, Service 4 calls PUT /lra-coordinator/{LraId}
        // to attempt to join the Saga.
        synchronized (lock) {
            while (!compensateCalled) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    fail("Could not wait");
                }
            }
            try {
                // 8. The LRA Coordinator crashes
                lraClient.joinLRA(lraId, null, URI.create("http://localhost:8081/service4/test/compensate"), null);
                fail("I presume it's not expected to be able to joinLRA when the LRA is compensating");
            } catch (WebApplicationException e) {

            } finally {
                joinAttempted = true;
                lock.notify();
            }
        }
        // test the coordinator is still alive
        lraClient.getRecoveryUrl();
    }

    private void clearObjectStore() {
        final String objectStorePath = arjPropertyManager.getObjectStoreEnvironmentBean().getObjectStoreDir();
        final File objectStoreDirectory = new File(objectStorePath);
        clearDirectory(objectStoreDirectory);
    }

    private void clearDirectory(final File directory) {
        final File[] files = directory.listFiles();
        if (files != null) {
            for (final File file : Objects.requireNonNull(directory.listFiles())) {
                if (file.isDirectory()) {
                    clearDirectory(file);
                }
                if (!file.delete()) {
                    LRALogger.logger.infof("%s: unable to delete file %s", testName, file.getName());
                }
            }
        }
    }
}