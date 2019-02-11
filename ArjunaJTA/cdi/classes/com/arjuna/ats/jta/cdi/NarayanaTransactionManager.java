/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013-2019, Red Hat, Inc., and individual contributors
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

package com.arjuna.ats.jta.cdi;

import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;

import javax.enterprise.event.Event;

import javax.enterprise.inject.CreationException;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import javax.inject.Inject;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionScoped;

import com.arjuna.ats.jta.common.jtaPropertyManager;

/**
 * A {@link DelegatingTransactionManager} in {@linkplain
 * ApplicationScoped application scope} that uses the return value
 * that results from invoking the {@link
 * com.arjuna.ats.jta.TransactionManager#transactionManager()} method
 * as its backing implementation.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see com.arjuna.ats.jta.TransactionManager#transactionManager()
 */
@ApplicationScoped
public class NarayanaTransactionManager extends DelegatingTransactionManager {

  private final Event<Transaction> transactionScopeInitializedBroadcaster;

  private final Event<Object> transactionScopeDestroyedBroadcaster;
  
  /**
   * Creates a new {@link NarayanaTransactionManager}.
   *
   * @param beanManager a {@link BeanManager} to use to find a
   * relevant {@link TransactionManager} to which to delegate all
   * operations
   *
   * @param transactionScopeInitializedBroadcaster an {@link Event}
   * for broadcasting the {@linkplain Initialized initialization} of
   * the {@linkplain TransactionScoped transaction scope}; may be
   * {@code null}
   *
   * @param transactionScopeDestroyedBroadcaster an {@link Event} for
   * broadcasting the {@linkplain Destroyed destruction} of the {@link
   * TransactionScoped transaction scope}; may be {@code null}
   *
   * @see com.arjuna.ats.jta.TransactionManager#transactionManager()
   *
   * @see #begin()
   *
   * @see #commit()
   *
   * @see #rollback()
   *
   * @see TransactionScoped
   */
  @Inject
  public NarayanaTransactionManager(final BeanManager beanManager,
                                    @Initialized(TransactionScoped.class)
                                    final Event<Transaction> transactionScopeInitializedBroadcaster,
                                    @Destroyed(TransactionScoped.class)
                                    final Event<Object> transactionScopeDestroyedBroadcaster) {
    super(getDelegate(beanManager));
    this.transactionScopeInitializedBroadcaster = transactionScopeInitializedBroadcaster;
    this.transactionScopeDestroyedBroadcaster = transactionScopeDestroyedBroadcaster;
  }

  private static final TransactionManager getDelegate(final BeanManager beanManager) {
    Objects.requireNonNull(beanManager);
    final TransactionManager returnValue;
    Set<Bean<?>> beans = beanManager.getBeans(TransactionManager.class);
    if (beans == null || beans.isEmpty()) {
      // There were no beans registered with TransactionManager as
      // their bean type, *including this class*.  This is an edge
      // case, but not as edgy as you might think.  For example, if
      // someone's portable extension vetoes us but creates us by
      // hand, we might be in this situation.
      //
      // Action: find a TransactionManager in JNDI if possible.

      final InitialContext ic;
      beans = beanManager.getBeans(InitialContext.class);
      if (beans == null || beans.isEmpty()) {
        // There was no bean registered with InitialContext among its
        // bean types.
        //
        // Action: create a new InitialContext by hand with no
        // arguments.
        
        InitialContext temp = null;
        try {
          temp = new InitialContext();
        } catch (final NamingException namingException) {
          // TODO: log
        } finally {
          ic = temp;
        }
      } else {
        // There was a bean registered with InitialContext among its
        // bean types, so use it to get an InitialContext from which
        // we can look up a TransactionManager.
        //
        // Action: resolve the bean and get a contextual reference.
        final Bean<?> initialContextBean = beanManager.resolve(beans);
        assert initialContextBean != null;
        ic = (InitialContext)beanManager.getReference(initialContextBean, InitialContext.class, beanManager.createCreationalContext(initialContextBean));
      }

      // Now we have an InitialContext; let's use it.
      
      TransactionManager temp = null;
      try {
        temp = (TransactionManager)ic.lookup(jtaPropertyManager.getJTAEnvironmentBean().getTransactionManagerJNDIContext());
      } catch (final NamingException namingException) {
        throw new CreationException(namingException.getMessage(), namingException);
      }
      if (temp == null) {
        // There was no TransactionManager registered in JNDI.  Use a
        // default instead.
        returnValue = com.arjuna.ats.jta.TransactionManager.transactionManager();
      } else {
        returnValue = temp;
      }
    } else {
      // There were beans registered with TransactionManager among
      // their bean types.  Resolve the set.
      assert beans != null;
      assert !beans.isEmpty();
      final Bean<?> bean = beanManager.resolve(beans);
      assert bean != null;
      if (NarayanaTransactionManager.class.equals(bean.getBeanClass())) {
        // If the bean we found is *us*, then, well, here we are, so
        // we need to use a default.
        returnValue = com.arjuna.ats.jta.TransactionManager.transactionManager();
      } else {
        // If the bean we found is *not us*, then use it as a
        // delegate.
        returnValue = (TransactionManager)beanManager.getReference(bean, TransactionManager.class, beanManager.createCreationalContext(bean));
      }
    }
    return returnValue;
  }

  /**
   * Overrides {@link DelegatingTransactionManager#begin()} to
   * additionally {@linkplain Event#fire(Object) fire} an {@link
   * Object} representing the {@linkplain Initialized initialization}
   * of the {@linkplain TransactionScoped transaction scope}.
   *
   * @exception NotSupportedException if the thread is already
   * associated with a transaction and this {@link TransactionManager}
   * implementation does not support nested transactions
   *
   * @exception SystemException if this {@link TransactionManager}
   * encounters an unexpected error condition
   *
   * @see DelegatingTransactionManager#begin()
   *
   * @see Event#fire(Object)
   *
   * @see Initialized
   *
   * @see TransactionScoped
   */
  @Override
  public void begin() throws NotSupportedException, SystemException {
    super.begin();
    if (this.transactionScopeInitializedBroadcaster != null) {
      this.transactionScopeInitializedBroadcaster.fire(this.getTransaction());
    }
  }

  /**
   * Overrides {@link DelegatingTransactionManager#commit()} to
   * additionally {@linkplain Event#fire(Object) fire} an {@link
   * Object} representing the {@linkplain Destroyed destruction}
   * of the {@linkplain TransactionScoped transaction scope}.
   *
   * @exception RollbackException if the transaction has been rolled
   * back rather than committed
   *
   * @exception HeuristicMixedException if a heuristic decision was
   * made and that some relevant updates have been committed while
   * others have been rolled back
   *
   * @exception HeuristicRollbackException if a heuristic decision was
   * made and all relevant updates have been rolled back
   *
   * @exception SecurityException if the thread is not allowed to
   * commit the transaction
   *
   * @exception IllegalStateException if the current thread is not
   * associated with a transaction
   *
   * @exception SystemException if this {@link TransactionManager}
   * encounters an unexpected error condition
   *
   * @see DelegatingTransactionManager#commit()
   *
   * @see Event#fire(Object)
   *
   * @see Destroyed
   *
   * @see TransactionScoped
   */
  @Override
  public void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SystemException {
    try {
      super.commit();
    } finally {
      if (this.transactionScopeDestroyedBroadcaster != null) {
        this.transactionScopeDestroyedBroadcaster.fire(this.toString());
      }
    }
  }

  /**
   * Overrides {@link DelegatingTransactionManager#rollback()} to
   * additionally {@linkplain Event#fire(Object) fire} an {@link
   * Object} representing the {@linkplain Destroyed destruction}
   * of the {@linkplain TransactionScoped transaction scope}.
   *
   * @exception SecurityException if the thread is not allowed to roll
   * back the transaction
   *
   * @exception IllegalStateException if the current thread is not
   * associated with a transaction
   *
   * @exception SystemException if this {@link TransactionManager}
   * encounters an unexpected error condition
   *
   * @see DelegatingTransactionManager#rollback()
   *
   * @see Event#fire(Object)
   *
   * @see Destroyed
   *
   * @see TransactionScoped
   */
  @Override
  public void rollback() throws SystemException {
    try {
      super.rollback();
    } finally {
      if (this.transactionScopeDestroyedBroadcaster != null) {
        this.transactionScopeDestroyedBroadcaster.fire(this.toString());
      }
    }
  }

}
