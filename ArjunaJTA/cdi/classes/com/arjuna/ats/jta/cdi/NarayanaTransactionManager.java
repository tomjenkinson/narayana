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

import javax.enterprise.inject.Any;
import javax.enterprise.inject.CreationException;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import javax.enterprise.util.AnnotationLiteral;

import javax.inject.Inject;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionScoped;

import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.common.JTAEnvironmentBean;

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
class NarayanaTransactionManager extends DelegatingTransactionManager {

  private final Event<Transaction> transactionScopeInitializedBroadcaster;

  private final Event<Object> transactionScopeDestroyedBroadcaster;

  /**
   * Creates a new, possibly <strong>non-functional</strong> {@link
   * NarayanaTransactionManager}.
   *
   * <p>This constructor exists only to conform with <a
   * href="http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#unproxyable">section
   * 3.15 of the CDI specification</a>.</p>
   *
   * @deprecated This constructor exists only to conform with <a
   * href="http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#unproxyable">section
   * 3.15 of the CDI specification</a>.
   */
  @Deprecated
  NarayanaTransactionManager() {
    super(null);
    this.transactionScopeDestroyedBroadcaster = null;
    this.transactionScopeInitializedBroadcaster = null;
  }
  
  /**
   * Creates a new {@link NarayanaTransactionManager}.
   *
   * @param beanManager a {@link BeanManager} to use to find a
   * relevant {@link TransactionManager} to which to delegate all
   * operations; may be {@code null} in which case JNDI and other
   * mechanisms may be used instead
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
  private NarayanaTransactionManager(final BeanManager beanManager,
                                     @Initialized(TransactionScoped.class)
                                     final Event<Transaction> transactionScopeInitializedBroadcaster,
                                     @Destroyed(TransactionScoped.class)
                                     final Event<Object> transactionScopeDestroyedBroadcaster) {
    super(getDelegate(beanManager));
    this.transactionScopeInitializedBroadcaster = transactionScopeInitializedBroadcaster;
    this.transactionScopeDestroyedBroadcaster = transactionScopeDestroyedBroadcaster;
  }

  private static final TransactionManager getDelegate(final BeanManager beanManager) {
    final TransactionManager returnValue;
    
    Set<Bean<?>> beans;
    if (beanManager == null) {
      beans = null;
    } else {
      beans = beanManager.getBeans(TransactionManager.class, AnyLiteral.INSTANCE);
    }
    
    Bean<?> transactionManagerBean = null;
    final int size = beans == null || beans.isEmpty() ? 0 : beans.size();
    if (size == 2) {
      // There are exactly two beans in the container that have
      // TransactionManager among their bean types.  One of them is
      // going to be us (most likely).  If there's exactly one other,
      // use it as our delegate, regardless of its qualifiers.
      for (final Bean<?> bean : beans) {
        if (bean != null && !NarayanaTransactionManager.class.equals(bean.getBeanClass())) {
          transactionManagerBean = bean;
          break;
        }
      }
    } else {
      transactionManagerBean = beanManager.resolve(beans);
      if (transactionManagerBean != null && NarayanaTransactionManager.class.equals(transactionManagerBean.getBeanClass())) {
        transactionManagerBean = null;
      }
    }
    
    if (transactionManagerBean == null) {

      // Time to try JNDI, since we didn't find any beans implementing
      // TransactionManager other than, perhaps, ourselves.
      
      // Try to acquire an InitialContext by looking in CDI first,
      // then by creating one by hand.
      final Context initialContext;
      if (beanManager == null) {
        beans = null;
      } else {
        beans = beanManager.getBeans(InitialContext.class);
      }
      final boolean closeContext;
      if (beans == null || beans.isEmpty()) {
        Context temp = null;
        try {
          temp = new InitialContext();
        } catch (final NoInitialContextException noInitialContextException) {
          // Expected in certain combinations of JNDI
          // implementations and CDI SE situations.
        } catch (final NamingException namingException) {
          throw new CreationException(namingException.getMessage(), namingException);
        } finally {
          closeContext = temp != null;
          initialContext = temp;
        }
      } else {        
        final Bean<?> initialContextBean = beanManager.resolve(beans);
        assert initialContextBean != null;
        initialContext = (InitialContext)beanManager.getReference(initialContextBean, InitialContext.class, beanManager.createCreationalContext(initialContextBean));
        closeContext = false;
      }
      
      TransactionManager candidateTransactionManager = null;
      if (initialContext != null) {
        
        // Acquire a JTAEnvironmentBean which will give us what name
        // to use to look up a TransactionManager in JNDI.
        final JTAEnvironmentBean jtaEnvironmentBean;
        if (beanManager == null) {
          beans = null;
        } else {
          beans = beanManager.getBeans(JTAEnvironmentBean.class);
        }
        if (beans == null || beans.isEmpty()) {
          jtaEnvironmentBean = jtaPropertyManager.getJTAEnvironmentBean();
        } else {
          final Bean<?> bean = beanManager.resolve(beans);
          assert bean != null;
          jtaEnvironmentBean = (JTAEnvironmentBean)beanManager.getReference(bean, JTAEnvironmentBean.class, beanManager.createCreationalContext(bean));
        }
        assert jtaEnvironmentBean != null;
        
        // Do the JNDI lookup.
        try {
          candidateTransactionManager = (TransactionManager)initialContext.lookup(jtaEnvironmentBean.getTransactionManagerJNDIContext());
        } catch (final NoInitialContextException noInitialContextException) {
          // Expected in standalone CDI SE situations.
        } catch (final NamingException namingException) {
          throw new CreationException(namingException.getMessage(), namingException);
        }

        // If we were responsible for creating the InitialContext
        // instance, close it to be a good citizen.  Otherwise it came
        // from a CDI bean so leave closing up to the bean.
        if (closeContext) {
          try {
            initialContext.close();
          } catch (final NamingException namingException) {
            throw new CreationException(namingException.getMessage(), namingException);
          }
        }

      }
      
      // If JNDI failed, or there was no InitialContext that could be
      // interrogated at all, fall back to the last possible backup
      // strategy.  This is a common case in CDI SE environments.
      if (candidateTransactionManager == null) {
        candidateTransactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();
      }

      returnValue = candidateTransactionManager;

    } else {
      
      // We're not supposed to use JNDI.  Use the sole
      // TransactionManager CDI bean that was found as our delegate.
      assert beanManager != null;
      assert transactionManagerBean != null;
      assert !NarayanaTransactionManager.class.equals(transactionManagerBean.getBeanClass());
      returnValue = (TransactionManager)beanManager.getReference(transactionManagerBean, TransactionManager.class, beanManager.createCreationalContext(transactionManagerBean));
      
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

  private static final class AnyLiteral extends AnnotationLiteral<Any> implements Any {

    private static final long serialVersionUID = 1L;
    
    private static final Any INSTANCE = new AnyLiteral();

    private AnyLiteral() {
      super();
    }
    
  }

}