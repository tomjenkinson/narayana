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
package com.hp.mwtests.ts.jta.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import jakarta.transaction.NotSupportedException;
import jakarta.transaction.Status;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import org.junit.Test;

/*
 * Copyright (C) 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JTATransactionCommitTest2.java 2342 2006-03-30 13:06:17Z  $
 */

public class JTATransactionCommitTest2
{
    @Test
    public void test() throws Exception
    {
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        Transaction tx = tm.getTransaction();

        tx.commit();

        assertEquals(Status.STATUS_COMMITTED, tm.getStatus());

        try {
            tm.begin();

            fail("Begin call completed successfully - this shouldn't have happened");
        }
        catch (NotSupportedException e)
        {
            System.out.println("NotSupportedException \""+e.getMessage()+"\" occurred this is expected and correct");
        }
    }
}
