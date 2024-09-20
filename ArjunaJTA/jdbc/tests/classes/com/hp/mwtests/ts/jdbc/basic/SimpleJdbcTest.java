/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.hp.mwtests.ts.jdbc.basic;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.jnp.server.NamingBeanImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.arjuna.ats.arjuna.common.arjPropertyManager;

import static org.junit.Assert.fail;

@RunWith(BMUnitRunner.class)
public class SimpleJdbcTest {
	private static final String DB_USER1 = "user1";
	private static final String DB_USER2 = "user2";

	private NamingBeanImpl namingBeanImpl = null;

	@Before
	public void setup() throws Exception {
		namingBeanImpl = new NamingBeanImpl();
		namingBeanImpl.start();
	}

	@After
	public void tearDown() {
		namingBeanImpl.stop();
	}

	@Test
	public void test() throws Exception {
		arjPropertyManager.getCoreEnvironmentBean().setNodeIdentifier("1");
		final DataSource dataSource1 = getDataSource(DB_USER1, "resource: "
				+ DB_USER1);
		final DataSource dataSource2 = getDataSource(DB_USER2, "resource: "
				+ DB_USER2);

		prepare(dataSource1);
		prepare(dataSource2);

		final UserTransaction userTransaction = com.arjuna.ats.jta.UserTransaction
				.userTransaction();
		userTransaction.begin();
		
		final Connection connection1 = dataSource1.getConnection();
		final Connection connection2 = dataSource2.getConnection();

		insert(connection1);
		insert(connection2);

		userTransaction.commit();
	}

	@Test
	@BMRule(
			name = "XA_RBINTEGRITY in prepare",
			targetClass = "com.arjuna.ats.internal.jdbc.IsSameRMOverrideXAResource",
			targetMethod = "prepare",
			// Action should also be wrappedXAResource.rollback(arg0);
			action = "throw new XAException(XAException.XA_RBINTEGRITY)",
			targetLocation = "AT ENTRY"
	)
	public void testRollbackXA_RBINTEGRITY() throws SQLException, NamingException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException, SystemException, NotSupportedException, RollbackException, HeuristicRollbackException, HeuristicMixedException {

		final DataSource dataSource1 = getDataSource(DB_USER1, "resource: "
				+ DB_USER1);
		final DataSource dataSource2 = getDataSource(DB_USER2, "resource: "
				+ DB_USER2);

		prepare(dataSource1);
		prepare(dataSource2);


		jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
				.transactionManager();

		tm.begin();


		final Connection connection1 = dataSource1.getConnection();
		final Connection connection2 = dataSource2.getConnection();

		insert(connection1);
		insert(connection2);

		try {
			tm.commit();
			fail("Should not have committed");
		} catch (RollbackException e) {
			// Expected
		} finally {
			// Without a connection close the underlying connection will not close
			connection1.close();
			connection2.close();
		}
	}

	private static void insert(Connection connection) throws SQLException {
		final PreparedStatement preparedStatement = connection
				.prepareStatement("INSERT INTO jta_test (some_string) VALUES ('test')");
		preparedStatement.execute();
	}

	private static void prepare(DataSource dataSource) throws SQLException {
		final Connection connection = dataSource.getConnection();
		try {
			connection.prepareStatement("DROP TABLE jta_test").execute();
		} catch (SQLException e) {
			// Ignore
		}
		connection.prepareStatement(
				"CREATE TABLE jta_test (some_string VARCHAR2(10))").execute();
	}

	private static DataSource getDataSource(String user, String resourceName)
			throws NamingException, SQLException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException,
			SecurityException, ClassNotFoundException {
		InitialContext initialContext = prepareInitialContext();

		Class clazz = Class.forName("org.h2.jdbcx.JdbcDataSource");
		XADataSource xaDataSource = (XADataSource) clazz.newInstance();
		clazz.getMethod("setURL", new Class[] { String.class }).invoke(
				xaDataSource, new Object[] { "jdbc:h2:mem:JBTMDB;DB_CLOSE_DELAY=-1" });

		final String name = user;
		initialContext.rebind(name, xaDataSource);

		DriverManagerDataSource dataSource = new DriverManagerDataSource(
				"jdbc:arjuna:" + name);
		dataSource
				.setDriverClassName("com.arjuna.ats.jdbc.TransactionalDriver");

		return dataSource;
	}

	private static InitialContext prepareInitialContext()
			throws NamingException {
		final InitialContext initialContext = new InitialContext();

//		try {
//			initialContext.lookup("java:/comp/env/jdbc");
//		} catch (NamingException ne) {
////			initialContext.createSubcontext("java:");
//			initialContext.createSubcontext("java:/comp");
//			initialContext.createSubcontext("java:/comp/env");
//			initialContext.createSubcontext("java:/comp/env/jdbc");
//		}

		return initialContext;
	}
}
