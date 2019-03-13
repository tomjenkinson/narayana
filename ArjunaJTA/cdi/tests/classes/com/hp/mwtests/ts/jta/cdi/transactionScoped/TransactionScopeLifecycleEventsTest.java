/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2019, Red Hat, Inc., and individual contributors
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

package com.hp.mwtests.ts.jta.cdi.transactionScoped;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;

import javax.enterprise.context.spi.Context;

import javax.enterprise.event.Observes;

import javax.enterprise.inject.spi.BeanManager;

import javax.inject.Inject;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.Transactional;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionScoped;

import com.arjuna.ats.jta.cdi.TransactionContext;
import com.arjuna.ats.jta.cdi.TransactionExtension;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
@ApplicationScoped
public class TransactionScopeLifecycleEventsTest {

    private static boolean initializedObserved;

    private static boolean destroyedObserved;

    private AutoCloseable container;

    @Inject
    private TransactionManager transactionManager;
    
    @Before
    public void setUp() throws Exception {
        this.tearDown();
        final Weld weld = new Weld()
            .addExtension(new TransactionExtension())
            .addBeanClass(this.getClass());
        this.container = weld.initialize();
    }
    
    @After
    public void tearDown() throws Exception {
        if (this.container != null) {
            this.container.close();
            this.container = null;
        }
        initializedObserved = false;
        destroyedObserved = false;
    }

    private static void onStartup(@Observes @Initialized(ApplicationScoped.class) final Object event,
                                  final TransactionScopeLifecycleEventsTest self) {
        self.doSomethingTransactional();
    }
    
    @Transactional
    void doSomethingTransactional() {

    }

    void transactionScopeActivated(@Observes @Initialized(TransactionScoped.class) final Object event,
                                   final BeanManager beanManager)
        throws SystemException {
        assertNotNull(event);
        assertNotNull(beanManager);
        assertNotNull(this.transactionManager);
        final Transaction transaction = this.transactionManager.getTransaction();
        assertNotNull(transaction);
        assertEquals(Status.STATUS_ACTIVE, transaction.getStatus());
        final Context transactionContext = beanManager.getContext(TransactionScoped.class);
        assertNotNull(transactionContext);
        assertTrue(transactionContext.isActive());
        initializedObserved = true;
    }

    void transactionScopeDectivated(@Observes @Destroyed(TransactionScoped.class) final Object event,
                                    final BeanManager beanManager) throws SystemException {
        assertNotNull(event);
        assertNotNull(beanManager);
        assertNotNull(this.transactionManager);
        assertNull(this.transactionManager.getTransaction());
        try {
            beanManager.getContext(TransactionScoped.class);
            fail();
        } catch (final ContextNotActiveException expected) {

        }
        destroyedObserved = true;
    }
    
    @Test
    public void testIt() throws Exception {
        assertTrue(initializedObserved);
        assertTrue(destroyedObserved);
    }

}
