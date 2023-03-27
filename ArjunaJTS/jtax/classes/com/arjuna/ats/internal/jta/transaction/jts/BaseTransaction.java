/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: BaseTransaction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.transaction.jts;

import com.arjuna.ats.internal.jta.Implementationsx;
import jakarta.transaction.NotSupportedException;

import org.omg.CORBA.TRANSACTION_UNAVAILABLE;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Coordinator;

import com.arjuna.ats.internal.jta.utils.jtaxLogger;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jts.OTSManager;

/**
 * Some common methods for UserTransaction and TransactionManager.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: BaseTransaction.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.1.
 */

public class BaseTransaction
{

	public void begin () throws jakarta.transaction.NotSupportedException,
			jakarta.transaction.SystemException
	{
		if (jtaxLogger.logger.isTraceEnabled()) {
            jtaxLogger.logger.trace("BaseTransaction.begin");
        }

		/*
		 * We can supported subtransactions, so should have the option to let
		 * programmer use them. Strict conformance will always say no.
		 */

		boolean alreadyAssociated;

		try
		{
			alreadyAssociated = checkTransactionState();
			if (alreadyAssociated && this._supportSubtransactions)
			{
					throw new NotSupportedException("BaseTransaction.begin - " +
							jtaxLogger.i18NLogger.get_jtax_transaction_jts_alreadyassociated());
			}
		}
		catch (NotSupportedException e1) {
			throw e1;
		}
		catch (org.omg.CORBA.SystemException e2)
		{
			jakarta.transaction.SystemException systemException = new jakarta.transaction.SystemException(e2.toString());
			systemException.initCause(e2);
			throw systemException;
		}
		catch (Exception e3)
		{
			jakarta.transaction.SystemException systemException = new jakarta.transaction.SystemException(e3.toString());
			systemException.initCause(e3);
			throw systemException;
		}

		try
		{
			/*
			 * If we are here, it means that either there isn't any transaction associated to this thread
			 * or sub-transactions are allowed. If support for sub-transactions is enabled and there is already
			 * a transaction associated with this thread then Implementationsx.getAllowTransactionCreation()
			 * is overruled
			 */
			if (Implementationsx.getAllowTransactionCreation() || (_supportSubtransactions && alreadyAssociated)) {
				TransactionImple.putTransaction(new TransactionImple());
			}
		}
		catch (org.omg.CosTransactions.SubtransactionsUnavailable e4)
		{
			// shouldn't happen if we get here from the previous checks!

            NotSupportedException notSupportedException = new NotSupportedException(e4.getMessage());
            notSupportedException.initCause(e4);
            throw notSupportedException;
		}
		catch (org.omg.CORBA.SystemException e5)
		{
            jakarta.transaction.SystemException systemException = new jakarta.transaction.SystemException(e5.toString());
            systemException.initCause(e5);
            throw systemException;
		}
	}

	/**
	 * We will never throw a HeuristicRollbackException because if we get a
	 * HeuristicRollback from a resource, and can successfully rollback the
	 * other resources, this is then the same as having simply been forced to
	 * rollback the transaction during phase 1. The OTS interfaces do not allow
	 * a differentiation.
	 */

	public void commit () throws jakarta.transaction.RollbackException,
			jakarta.transaction.HeuristicMixedException,
			jakarta.transaction.HeuristicRollbackException,
			java.lang.SecurityException, java.lang.IllegalStateException,
			jakarta.transaction.SystemException
	{
		if (jtaxLogger.logger.isTraceEnabled()) {
            jtaxLogger.logger.trace("BaseTransaction.commit");
        }

		TransactionImple theTransaction = TransactionImple.getTransaction();

		try
		{
			theTransaction.commitAndDisassociate();
		}
		catch (NullPointerException ex)
		{
			ex.printStackTrace();

			throw new IllegalStateException(
                    "BaseTransaction.commit - "
                            + jtaxLogger.i18NLogger.get_jtax_transaction_jts_notxe()
							+ ex, ex);
		}

		checkTransactionState();
	}

	public void rollback () throws java.lang.IllegalStateException,
			java.lang.SecurityException, jakarta.transaction.SystemException
	{
		if (jtaxLogger.logger.isTraceEnabled()) {
            jtaxLogger.logger.trace("BaseTransaction.rollback");
        }

		TransactionImple theTransaction = TransactionImple.getTransaction();

		try
		{
			theTransaction.rollbackAndDisassociate();
		}
		catch (NullPointerException ex)
		{
			throw new IllegalStateException(ex);
		}

		checkTransactionState();
	}

	public void setRollbackOnly () throws java.lang.IllegalStateException,
			jakarta.transaction.SystemException
	{
		if (jtaxLogger.logger.isTraceEnabled()) {
            jtaxLogger.logger.trace("BaseTransaction.setRollbackOnly");
        }

		TransactionImple theTransaction = TransactionImple.getTransaction();

		try
		{
			theTransaction.setRollbackOnly();
		}
		catch (NullPointerException ex)
		{
			throw new IllegalStateException(
                    jtaxLogger.i18NLogger.get_jtax_transaction_jts_nosuchtx(), ex);
		}
	}

	public int getStatus () throws jakarta.transaction.SystemException
	{
		if (jtaxLogger.logger.isTraceEnabled()) {
            jtaxLogger.logger.trace("BaseTransaction.getStatus");
        }

		TransactionImple theTransaction = null;
	
		try {
		theTransaction = TransactionImple.getTransaction();
		} catch (TRANSACTION_UNAVAILABLE e) {
		    if (e.minor == 1) {
	            return jakarta.transaction.Status.STATUS_NO_TRANSACTION;
		    }
		}
		
		if (theTransaction == null) {
		    return jakarta.transaction.Status.STATUS_NO_TRANSACTION;
		}

		try
		{
			return theTransaction.getStatus();
		}
		catch (NullPointerException ex)
		{
			return jakarta.transaction.Status.STATUS_NO_TRANSACTION;
		}
		catch (Exception e)
		{
            jakarta.transaction.SystemException systemException = new jakarta.transaction.SystemException(e.toString());
            systemException.initCause(e);
            throw systemException;
		}
	}

	public void setTransactionTimeout (int seconds)
			throws jakarta.transaction.SystemException
	{
		try
		{
			OTSImpleManager.current().set_timeout(seconds);
		}
		catch (Exception e)
		{
            jakarta.transaction.SystemException systemException = new jakarta.transaction.SystemException(e.toString());
            systemException.initCause(e);
            throw systemException;
		}
	}

	public int getTimeout () throws jakarta.transaction.SystemException
	{
		try
		{
			return OTSImpleManager.current().get_timeout();
		}
		catch (Exception e)
		{
            jakarta.transaction.SystemException systemException = new jakarta.transaction.SystemException(e.toString());
            systemException.initCause(e);
            throw systemException;
		}
	}

	protected BaseTransaction ()
	{
	}

	/**
	 * Called when we want to make sure this thread does not already have a
	 * transaction associated with it.
	 */

	final boolean checkTransactionState () throws IllegalStateException,
			jakarta.transaction.SystemException
	{
		// false => no transaction is currently associated to this thread
		boolean returnValue = false;

		try
		{
			Control cont = OTSManager.get_current().get_control();

			/*
			 * Control may not be null, but its coordinator may be.
			 */

			if (cont != null)
			{
				Coordinator coord = cont.get_coordinator();

				if (coord != null)
				{
					if ((coord.get_status() == org.omg.CosTransactions.Status.StatusActive)
							&& (!_supportSubtransactions))
					{
						returnValue = true;
					}
				}

				cont = null;
			}
		}
		catch (org.omg.CORBA.SystemException e1)
		{
            jakarta.transaction.SystemException systemException = new jakarta.transaction.SystemException(e1.toString());
            systemException.initCause(e1);
            throw systemException;
		}
		catch (org.omg.CosTransactions.Unavailable e2)
		{
			// ok, no transaction currently associated with thread.
		}
		catch (NullPointerException ex)
		{
			// ok, no transaction currently associated with thread.
		}

		return returnValue;
	}

	private static boolean _supportSubtransactions = jtaPropertyManager.getJTAEnvironmentBean()
            .isSupportSubtransactions();
}
