/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: xidcheck.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.jts.jca;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.Assert;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreIterator;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.internal.jta.Implementationsx;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.TransactionImporter;
import com.arjuna.ats.internal.jta.transaction.jts.TransactionImple;
import com.arjuna.ats.internal.jta.transaction.jts.jca.TransactionImporterImple;
import com.arjuna.ats.internal.jta.utils.jts.XidUtils;
import com.arjuna.ats.internal.jts.ControlWrapper;
import com.arjuna.ats.internal.jts.Implementations;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.ServerTransaction;
import com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteServerTransaction;
import com.arjuna.ats.jta.xa.XidImple;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.ats.jts.extensions.AtomicTransaction;
import com.hp.mwtests.ts.jta.jts.common.TestBase;
import com.hp.mwtests.ts.jta.subordinate.TestXAResource;

public class TransactionImporterUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        TransactionImporterImple importer = new TransactionImporterImple();
        
        try
        {
            importer.importTransaction(null);
            
            fail();
        }
        catch (final IllegalArgumentException ex)
        {
        }
        
        try
        {
            importer.recoverTransaction(null);
            
            fail();
        }
        catch (final IllegalArgumentException ex)
        {
        }
        
        try
        {
            importer.recoverTransaction(new Uid());
            
            fail();
        }
        catch (final IllegalArgumentException ex)
        {
        }
        
        try
        {
            importer.getImportedTransaction(null);
            
            fail();
        }
        catch (final IllegalArgumentException ex)
        {
        }
        
        try
        {
            importer.removeImportedTransaction(null);
            
            fail();
        }
        catch (final IllegalArgumentException ex)
        {
        }
    }

    @Test
    public void testDifferentInstanceFromRecovery() throws Exception {
        Uid uid = new Uid();
        XidImple xid = new XidImple(uid);

        SubordinateTransaction subordinateTransaction = SubordinationManager.getTransactionImporter().importTransaction(xid);

        Uid subordinateTransactionUid = getImportedSubordinateTransactionUid(subordinateTransaction);
        Xid subordinateTransactionXid = subordinateTransaction.baseXid();

        SubordinateTransaction importedTransaction = SubordinationManager.getTransactionImporter().getImportedTransaction(subordinateTransactionXid);
        assertTrue (subordinateTransaction == importedTransaction);
        subordinateTransaction.enlistResource(new XAResource() {

            @Override
            public void commit(Xid xid, boolean b) throws XAException {

            }

            @Override
            public void end(Xid xid, int i) throws XAException {

            }

            @Override
            public void forget(Xid xid) throws XAException {

            }

            @Override
            public int getTransactionTimeout() throws XAException {
                return 0;
            }

            @Override
            public boolean isSameRM(XAResource xaResource) throws XAException {
                return false;
            }

            @Override
            public int prepare(Xid xid) throws XAException {
                return 0;
            }

            @Override
            public Xid[] recover(int i) throws XAException {
                return new Xid[0];
            }

            @Override
            public void rollback(Xid xid) throws XAException {

            }

            @Override
            public boolean setTransactionTimeout(int i) throws XAException {
                return false;
            }

            @Override
            public void start(Xid xid, int i) throws XAException {

            }
        });
        subordinateTransaction.doPrepare();

        Implementationsx.initialise();
        SubordinateTransaction subordinateTransaction1 = SubordinationManager.getTransactionImporter().recoverTransaction(subordinateTransactionUid);
        assertTrue(subordinateTransaction != subordinateTransaction1);
        SubordinateTransaction importedTransaction1 = SubordinationManager.getTransactionImporter().getImportedTransaction(subordinateTransactionXid);
        assertTrue(importedTransaction != importedTransaction1);
        SubordinateTransaction importedTransaction2 = SubordinationManager.getTransactionImporter().getImportedTransaction(subordinateTransactionXid);
        assertTrue(importedTransaction1 == importedTransaction2);
        importedTransaction2.doCommit();
    }

    @Test
    public void testPreparedTransactionMovedToAssumedCompleted() throws Exception {
        Implementations.initialise();
        Implementationsx.initialise();

        Uid uid = new Uid();
        Xid xid = XidUtils.getXid(uid, false);

        TransactionImporter importer = SubordinationManager.getTransactionImporter();
        SubordinateTransaction subordinateTransaction = importer.importTransaction(xid);

        Uid subordinateTransactionUid = getImportedSubordinateTransactionUid(subordinateTransaction);

        TestXAResource xares = new TestXAResource();
        xares.setCommitException(new XAException(XAException.XAER_RMFAIL));
        subordinateTransaction.enlistResource(xares);
        subordinateTransaction.doPrepare();
        boolean commitFailed = subordinateTransaction.doCommit();
        Assert.assertFalse("Commit should fail as XAResource defined XAException on commit being thrown", commitFailed);

        int assumedCompletedRetryOriginalValue = jtsPropertyManager.getJTSEnvironmentBean().getCommitedTransactionRetryLimit();
        jtsPropertyManager.getJTSEnvironmentBean().setCommitedTransactionRetryLimit(1);
        try {
            importer.recoverTransaction(subordinateTransactionUid); // reply commit to move txn status to StatusCommitted
            importer.recoverTransaction(subordinateTransactionUid); // first check if txn could not be assumed to be completed
            importer.recoverTransaction(subordinateTransactionUid); // moving to assumed completed
        } finally {
            jtsPropertyManager.getJTSEnvironmentBean().setCommitedTransactionRetryLimit(assumedCompletedRetryOriginalValue);
        }

        try {
            importer.recoverTransaction(subordinateTransactionUid);
            Assert.fail("Transaction '" + subordinateTransaction + "' should fail to recover as it should be moved "
                + "to category AssumedCompleteServerTrasactions");
        } catch (IllegalArgumentException expected) {
        }

        ObjectStoreIterator objectStoreIterator = new ObjectStoreIterator(StoreManager.getRecoveryStore(),
            AssumedCompleteServerTransaction.typeName());

        List<Uid> assumedCompletedUids = new ArrayList<Uid>();
        Uid iteratedUid = objectStoreIterator.iterate();
        while(Uid.nullUid().notEquals(iteratedUid)) {
            assumedCompletedUids.add(iteratedUid);
            iteratedUid = objectStoreIterator.iterate();
        }
        Assert.assertTrue("the subordinate transaction has to be moved under assumed completed in object store",
            assumedCompletedUids.contains(subordinateTransactionUid));
    }

    /**
     * This is required because it JTS records are stored with a dynamic _savingUid
     * Normally they are recovered using XATerminator but for this test I would like to stick to testing
     * transaction importer
     */
    private Uid getImportedSubordinateTransactionUid(SubordinateTransaction subordinateTransaction) throws Exception {
        Field field = TransactionImple.class.getDeclaredField("_theTransaction");
        field.setAccessible(true);
        Object o = field.get(subordinateTransaction);
        field = AtomicTransaction.class.getDeclaredField("_theAction");
        field.setAccessible(true);
        o = field.get(o);
        field = ControlWrapper.class.getDeclaredField("_controlImpl");
        field.setAccessible(true);
        o = field.get(o);
        field = ControlImple.class.getDeclaredField("_transactionHandle");
        field.setAccessible(true);
        o = field.get(o);
        field = ServerTransaction.class.getDeclaredField("_savingUid");
        field.setAccessible(true);
        Uid subordinateTransactionUid = (Uid) field.get(o);
        return subordinateTransactionUid;
    }
}
