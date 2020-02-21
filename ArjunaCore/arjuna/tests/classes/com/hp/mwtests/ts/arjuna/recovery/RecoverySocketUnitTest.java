/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2020, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.hp.mwtests.ts.arjuna.recovery;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryDriver;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test cases which work with a direct connection to socket where RecoveryManager listens at.
 */
public class RecoverySocketUnitTest {
    private static final Logger log = Logger.getLogger(RecoverySocketUnitTest.class);

    private static boolean socketRecoveryListenerInitialState;
    private static int periodicRecoveryPeriodInitialState, recoveryBackoffPeriodInitialState;

    private InetAddress recoveryManagerHost = null;
    private int recoveryManagerPort = 0;
    private RecoveryManager recoveryManager;

    @BeforeClass
    public static void getInitialState() {
        socketRecoveryListenerInitialState = recoveryPropertyManager.getRecoveryEnvironmentBean().isRecoveryListener();
        periodicRecoveryPeriodInitialState =  recoveryPropertyManager.getRecoveryEnvironmentBean().getPeriodicRecoveryPeriod();
        recoveryBackoffPeriodInitialState = recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryBackoffPeriod();
    }

    @AfterClass
    public static void returnBackInitialState() {
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryListener(socketRecoveryListenerInitialState);
        recoveryPropertyManager.getRecoveryEnvironmentBean().setPeriodicRecoveryPeriod(periodicRecoveryPeriodInitialState);
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(recoveryBackoffPeriodInitialState);
    }

    @Before
    public void enableRecoveryListener() throws InterruptedException{
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryListener(true);
        recoveryPropertyManager.getRecoveryEnvironmentBean().setPeriodicRecoveryPeriod(1);
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(1);
        recoveryManager = RecoveryManager.manager();
        // wait for the socket would be established
        Thread.sleep(50);
    }

    @After
    public void returnStateOfRecoveryListener() {
        try {
            recoveryManager.terminate();
        } catch (IllegalStateException ise) {
            // cannot terminate cleanly which may happen because some of the tests which terminated recovery manager before
            log.debugf(ise,"Cannot terminate recovery manager. This is probably not a problem as the test may stopped it already. Check the prior errors.");
        }
    }

    @Test
    public void socketPing() throws Exception {
        try (Socket connectorSocket = getSocket()) {
            // streams to and from the RecoveryManager listener
            BufferedReader fromServer = new BufferedReader(new InputStreamReader(connectorSocket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter toServer = new PrintWriter(new OutputStreamWriter(connectorSocket.getOutputStream(), StandardCharsets.UTF_8));

            toServer.println(RecoveryDriver.PING);
            toServer.flush();
            String stringResponse = fromServer.readLine();
            assertEquals("Expecting the correct response string for command " + RecoveryDriver.PING, RecoveryDriver.PONG, stringResponse);
        } catch (final SocketTimeoutException stex) {
            failOnSocketTimeout(stex, RecoveryDriver.PING);
        }
    }

    @Test
    public void socketScan() throws Exception {
        try (Socket connectorSocket = getSocket()) {
            // streams to and from the RecoveryManager listener
            BufferedReader fromServer = new BufferedReader(new InputStreamReader(connectorSocket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter toServer = new PrintWriter(new OutputStreamWriter(connectorSocket.getOutputStream(), StandardCharsets.UTF_8));

            toServer.println(RecoveryDriver.SCAN);
            toServer.flush();
            String stringResponse = fromServer.readLine();
            assertEquals("Expecting SCAN to be processed correctly", "DONE", stringResponse);
        } catch (final SocketTimeoutException stex) {
            failOnSocketTimeout(stex, RecoveryDriver.SCAN);
        }
    }

    @Test
    public void socketScanVerbose() throws Exception {
        try (Socket connectorSocket = getSocket()) {
            // streams to and from the RecoveryManager listener
            BufferedReader fromServer = new BufferedReader(new InputStreamReader(connectorSocket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter toServer = new PrintWriter(new OutputStreamWriter(connectorSocket.getOutputStream(), StandardCharsets.UTF_8));

            toServer.println(RecoveryDriver.VERBOSE_SCAN);
            toServer.flush();
            String stringResponse = fromServer.readLine();
            assertEquals("Expecting VERBOSE SCAN to be processed correctly", "DONE", stringResponse);
        } catch (final SocketTimeoutException stex) {
            failOnSocketTimeout(stex, RecoveryDriver.VERBOSE_SCAN);
        }
    }

    @Test
    public void socketNullWrite() throws Exception {
        try (Socket connectorSocket = getSocket()) {
            PrintWriter toServer = new PrintWriter(new OutputStreamWriter(connectorSocket.getOutputStream(), StandardCharsets.UTF_8));
            toServer.println("PING");
            // no flush + waiting for getting NPE
            Thread.sleep(500);
        } catch (final SocketTimeoutException stex) {
            failOnSocketTimeout(stex, RecoveryDriver.SCAN);
        }
    }

    @Test
    public void socketScanTerminateAbruptly() throws Exception {
        try (Socket connectorSocket = getSocket()) {
            BufferedReader fromServer = new BufferedReader(new InputStreamReader(connectorSocket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter toServer = new PrintWriter(new OutputStreamWriter(connectorSocket.getOutputStream(), StandardCharsets.UTF_8));
            String stringResponse = fromServer.readLine();
        } catch (final SocketTimeoutException stex) {
            failOnSocketTimeout(stex, RecoveryDriver.SCAN);
        }
    }

    private Socket getSocket() {
        try {
            recoveryManagerHost = RecoveryManager.getRecoveryManagerHost();
            recoveryManagerPort = RecoveryManager.getRecoveryManagerPort();
            Socket connectorSocket = new Socket(recoveryManagerHost, recoveryManagerPort);
            connectorSocket.setSoTimeout(RecoveryDriver.DEFAULT_SO_TIMEOUT);
            return connectorSocket;
        } catch (UnknownHostException uhe) {
            throw new IllegalStateException("Cannot obtain the data for recovery manager host", uhe);
        } catch (IOException ioe) {
            throw new IllegalStateException("Cannot create a socket for " + recoveryManagerHost + ":" + recoveryManagerPort, ioe);
        }
    }

    private void failOnSocketTimeout(SocketTimeoutException stex, String failedSocketCommand) {
        log.errorf(stex, "Cannot finish with the socket operation at %s:%d because of a timeout%n",
                recoveryManagerHost, recoveryManagerPort);
        Assert.fail(String.format("Socket operation '%s' timed out", failedSocketCommand));
    }
}
