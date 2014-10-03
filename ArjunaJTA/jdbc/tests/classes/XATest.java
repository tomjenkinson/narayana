import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashMap;

import javax.jms.XAConnectionFactory;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import oracle.jdbc.xa.client.OracleXADataSource;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.config.impl.FileConfiguration;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.server.impl.HornetQServerImpl;
import org.hornetq.jms.server.impl.JMSServerManagerImpl;
import org.hornetq.spi.core.security.HornetQSecurityManagerImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.xa.PGXADataSource;

import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.jta.utils.XAHelper;
import com.mysql.jdbc.jdbc2.optional.MysqlXADataSource;

//import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * 
 // // // // // // XAResource xaResource = // // //
 * dataSource1.getXAConnection().getXAResource(); // // // Xid[] recover =
 * xaResource.recover(XAResource.TMSTARTRSCAN); // // // for (int i = 0 ; i <
 * recover.length; i++) { // // // xaResource.rollback(recover[i]); // // // }
 * // // // xaResource.recover(XAResource.TMENDRSCAN); // // // // // // // //
 * // TransactionManager userTransaction = // TransactionManagerServices // //
 * // // .getTransactionManager(); // // // // // // // // //
 * System.setProperty("com.atomikos.icatch.service", // // // //
 * "com.atomikos.icatch.standalone.UserTransactionServiceFactory"); // // //
 * System.setProperty( // // //
 * "ccom.atomikos.icatch.automatic_resource_registration", // "true"); // // //
 * TransactionManager userTransaction = new // // //
 * com.atomikos.icatch.jta.UserTransactionManager(); // // // // // // //
 * userTransaction.getTransaction().enlistResource( // // // new
 * IsSameRMOverride(xaConnection1.getXAResource())); // // //
 * userTransaction.getTransaction().enlistResource( // // // new
 * IsSameRMOverride(xaConnection2.getXAResource())); // // //
 * 
 * @author tom
 *
 */
public class XATest {

	@BeforeClass
	public static void startup() {
		TxControl.setXANodeName("tom");
	}

	@Test
	public void testHQ() throws Exception {
		FileConfiguration configuration = new FileConfiguration();
		File file = new File("target/classes/hornetq/server0" + "/"
				+ "hornetq-configuration.xml");
		configuration
				.setConfigurationUrl(file.toURI().toURL().toExternalForm());
		configuration.start();
		HornetQSecurityManagerImpl hornetQSecurityManagerImpl = new HornetQSecurityManagerImpl();
		hornetQSecurityManagerImpl.addUser("user1", "user1");
		hornetQSecurityManagerImpl.addUser("user2", "user2");
		HornetQServerImpl server = new HornetQServerImpl(configuration,
				ManagementFactory.getPlatformMBeanServer(),
				hornetQSecurityManagerImpl);

		JMSServerManagerImpl manager = new JMSServerManagerImpl(server);
		manager.start();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("host", "localhost");
		params.put("port", 5445);
		TransportConfiguration transportConfiguration = new TransportConfiguration(
				NettyConnectorFactory.class.getName(), params);
		XAConnectionFactory xacf1 = (XAConnectionFactory) HornetQJMSClient
				.createConnectionFactoryWithoutHA(JMSFactoryType.QUEUE_XA_CF,
						transportConfiguration);
		XAConnectionFactory xacf2 = (XAConnectionFactory) HornetQJMSClient
				.createConnectionFactoryWithoutHA(JMSFactoryType.QUEUE_XA_CF,
						transportConfiguration);
		javax.jms.XAConnection connection1 = xacf1.createXAConnection("user1",
				"user1");
		XAResource xaResource1 = connection1.createXASession().getXAResource();
		javax.jms.XAConnection connection2 = xacf1.createXAConnection("user2",
				"user2");
		XAResource xaResource2 = connection2.createXASession().getXAResource();
		
		performXATest(xaResource1, xaResource2);

		connection2.close();
		connection1.close();
		manager.stop();
		configuration.stop();
	}

	@Test
	public void testSybase() throws Exception {

		Class clazz = Class.forName("com.sybase.jdbc3.jdbc.SybXADataSource");
		XADataSource dataSource1 = (XADataSource) clazz.newInstance();
		clazz.getMethod("setServerName", new Class[] { String.class }).invoke(
				dataSource1, new Object[] { "db05.mw.lab.eng.bos.redhat.com" });
		clazz.getMethod("setDatabaseName", new Class[] { String.class })
				.invoke(dataSource1, new Object[] { "dballo02" });
		clazz.getMethod("setUser", new Class[] { String.class }).invoke(
				dataSource1, new Object[] { "dballo02" });
		clazz.getMethod("setPassword", new Class[] { String.class }).invoke(
				dataSource1, new Object[] { "dballo02" });
		clazz.getMethod("setPortNumber", new Class[] { int.class }).invoke(
				dataSource1, new Object[] { 5000 });
		XADataSource dataSource2 = (XADataSource) clazz.newInstance();
		clazz.getMethod("setServerName", new Class[] { String.class }).invoke(
				dataSource2, new Object[] { "db05.mw.lab.eng.bos.redhat.com" });
		clazz.getMethod("setDatabaseName", new Class[] { String.class })
				.invoke(dataSource2, new Object[] { "dballo01" });
		clazz.getMethod("setUser", new Class[] { String.class }).invoke(
				dataSource2, new Object[] { "dballo01" });
		clazz.getMethod("setPassword", new Class[] { String.class }).invoke(
				dataSource2, new Object[] { "dballo01" });
		clazz.getMethod("setPortNumber", new Class[] { int.class }).invoke(
				dataSource2, new Object[] { 5000 });

//		performXATest(dataSource1.getXAConnection().getXAResource(),
//				dataSource2.getXAConnection().getXAResource());
		performTMTest(dataSource1, dataSource2);
	}

	@Test
	public void testSQLServer() throws SQLException, XAException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException,
			SecurityException, ClassNotFoundException, InstantiationException,
			IllegalStateException, NotSupportedException, SystemException,
			RollbackException, HeuristicMixedException,
			HeuristicRollbackException {
		Class clazz = Class
				.forName("com.microsoft.sqlserver.jdbc.SQLServerXADataSource");
		XADataSource dataSource1 = (XADataSource) clazz.newInstance();
		clazz.getMethod("setServerName", new Class[] { String.class }).invoke(
				dataSource1, new Object[] { "db06.mw.lab.eng.bos.redhat.com" });
		clazz.getMethod("setDatabaseName", new Class[] { String.class })
				.invoke(dataSource1, new Object[] { "dballo00" });
		clazz.getMethod("setUser", new Class[] { String.class }).invoke(
				dataSource1, new Object[] { "dballo00" });
		clazz.getMethod("setPassword", new Class[] { String.class }).invoke(
				dataSource1, new Object[] { "dballo00" });
		clazz.getMethod("setPortNumber", new Class[] { int.class }).invoke(
				dataSource1, new Object[] { 1433 });
		XADataSource dataSource2 = (XADataSource) clazz.newInstance();
		clazz.getMethod("setServerName", new Class[] { String.class }).invoke(
				dataSource2, new Object[] { "db06.mw.lab.eng.bos.redhat.com" });
		clazz.getMethod("setDatabaseName", new Class[] { String.class })
				.invoke(dataSource2, new Object[] { "dballo01" });
		clazz.getMethod("setUser", new Class[] { String.class }).invoke(
				dataSource2, new Object[] { "dballo01" });
		clazz.getMethod("setPassword", new Class[] { String.class }).invoke(
				dataSource2, new Object[] { "dballo01" });
		clazz.getMethod("setPortNumber", new Class[] { int.class }).invoke(
				dataSource2, new Object[] { 1433 });

		// performXATest(dataSource1.getXAConnection().getXAResource(),
		// dataSource2.getXAConnection().getXAResource());
		performTMTest(dataSource1, dataSource2);
	}

	@Test
	public void testDB2() throws SQLException, XAException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException,
			SecurityException, IllegalStateException, NotSupportedException,
			SystemException, RollbackException, HeuristicMixedException,
			HeuristicRollbackException {
		Class clazz = Class.forName("com.ibm.db2.jcc.DB2XADataSource");
		XADataSource dataSource1 = (XADataSource) clazz.newInstance();
		clazz.getMethod("setServerName", new Class[] { String.class })
				.invoke(dataSource1,
						new Object[] { "vmg06.mw.lab.eng.bos.redhat.com" });
		clazz.getMethod("setDatabaseName", new Class[] { String.class })
				.invoke(dataSource1, new Object[] { "dballo" });
		clazz.getMethod("setUser", new Class[] { String.class }).invoke(
				dataSource1, new Object[] { "dballo01" });
		clazz.getMethod("setPassword", new Class[] { String.class }).invoke(
				dataSource1, new Object[] { "dballo01" });
		clazz.getMethod("setDriverType", new Class[] { int.class }).invoke(
				dataSource1, new Object[] { 4 });
		clazz.getMethod("setPortNumber", new Class[] { int.class }).invoke(
				dataSource1, new Object[] { 50000 });
		XADataSource dataSource2 = (XADataSource) clazz.newInstance();
		clazz.getMethod("setServerName", new Class[] { String.class })
				.invoke(dataSource2,
						new Object[] { "vmg06.mw.lab.eng.bos.redhat.com" });
		clazz.getMethod("setDatabaseName", new Class[] { String.class })
				.invoke(dataSource2, new Object[] { "dballo" });
		clazz.getMethod("setUser", new Class[] { String.class }).invoke(
				dataSource2, new Object[] { "dballo02" });
		clazz.getMethod("setPassword", new Class[] { String.class }).invoke(
				dataSource2, new Object[] { "dballo02" });
		clazz.getMethod("setDriverType", new Class[] { int.class }).invoke(
				dataSource2, new Object[] { 4 });
		clazz.getMethod("setPortNumber", new Class[] { int.class }).invoke(
				dataSource2, new Object[] { 50000 });

		// performXATest(dataSource1.getXAConnection().getXAResource(),
		// dataSource2.getXAConnection().getXAResource());
		performTMTest(dataSource1, dataSource2);
	}

	@Test
	public void testMYSQL() throws SQLException, XAException,
			IllegalStateException, SecurityException, NotSupportedException,
			SystemException, RollbackException, HeuristicMixedException,
			HeuristicRollbackException {
		XADataSource dataSource1 = new MysqlXADataSource();
		((MysqlXADataSource) dataSource1)
				.setServerName("tywin.buildnet.ncl.jboss.com");
		((MysqlXADataSource) dataSource1).setPortNumber(3306);
		((MysqlXADataSource) dataSource1).setDatabaseName("jbossts");
		((MysqlXADataSource) dataSource1).setUser("dtf11");
		((MysqlXADataSource) dataSource1).setPassword("dtf11");
		XADataSource dataSource2 = new MysqlXADataSource();
		((MysqlXADataSource) dataSource2)
				.setServerName("tywin.buildnet.ncl.jboss.com");
		((MysqlXADataSource) dataSource2).setPortNumber(3306);
		((MysqlXADataSource) dataSource2).setDatabaseName("jbossts");
		((MysqlXADataSource) dataSource2).setUser("dtf12");
		((MysqlXADataSource) dataSource2).setPassword("dtf12");

		// performXATest(dataSource1.getXAConnection().getXAResource(),
		// dataSource2.getXAConnection().getXAResource());
		performTMTest(dataSource1, dataSource2);
	}

	@Test
	public void testOracle() throws SQLException, XAException,
			IllegalStateException, SecurityException, NotSupportedException,
			SystemException, RollbackException, HeuristicMixedException,
			HeuristicRollbackException {

		final String DB_USER1 = "DBALLO02";
		final String DB_USER2 = "DBALLO01";
		final String DB_SID = "qaora11";

		XADataSource dataSource1 = new OracleXADataSource();
		((OracleXADataSource) dataSource1).setNetworkProtocol("tcp");
		((OracleXADataSource) dataSource1)
				.setServerName("db04.mw.lab.eng.bos.redhat.com");
		((OracleXADataSource) dataSource1).setPortNumber(1521);
		((OracleXADataSource) dataSource1).setDatabaseName(DB_SID);
		((OracleXADataSource) dataSource1).setDriverType("thin");
		((OracleXADataSource) dataSource1).setUser(DB_USER1);
		((OracleXADataSource) dataSource1).setPassword(DB_USER1);
		XADataSource dataSource2 = new OracleXADataSource();
		((OracleXADataSource) dataSource2).setNetworkProtocol("tcp");
		((OracleXADataSource) dataSource2)
				.setServerName("db04.mw.lab.eng.bos.redhat.com");
		((OracleXADataSource) dataSource2).setPortNumber(1521);
		((OracleXADataSource) dataSource2).setDatabaseName(DB_SID);
		((OracleXADataSource) dataSource2).setDriverType("thin");
		((OracleXADataSource) dataSource2).setUser(DB_USER2);
		((OracleXADataSource) dataSource2).setPassword(DB_USER2);

		// performXATest(dataSource1.getXAConnection().getXAResource(),
		// dataSource2.getXAConnection().getXAResource());
		performTMTest(dataSource1, dataSource2);
	}

	@Test
	public void testPostgres() throws SQLException, XAException,
			IllegalStateException, SecurityException, NotSupportedException,
			SystemException, RollbackException, HeuristicMixedException,
			HeuristicRollbackException {
		XADataSource dataSource1 = new PGXADataSource();
		((PGXADataSource) dataSource1).setPortNumber(5432);
		((PGXADataSource) dataSource1).setUser("dtf11");
		((PGXADataSource) dataSource1).setPassword("dtf11");
		((PGXADataSource) dataSource1)
				.setServerName("tywin.buildnet.ncl.jboss.com");
		((PGXADataSource) dataSource1).setDatabaseName("jbossts");
		XADataSource dataSource2 = new PGXADataSource();
		((PGXADataSource) dataSource2).setPortNumber(5432);
		((PGXADataSource) dataSource2).setUser("dtf12");
		((PGXADataSource) dataSource2).setPassword("dtf12");
		((PGXADataSource) dataSource2)
				.setServerName("tywin.buildnet.ncl.jboss.com");
		((PGXADataSource) dataSource2).setDatabaseName("jbossts");

		// performXATest(dataSource1.getXAConnection().getXAResource(),
		// dataSource2.getXAConnection().getXAResource());
		performTMTest(dataSource1, dataSource2);
	}

	public void performTMTest(XADataSource dataSource1, XADataSource dataSource2)
			throws NotSupportedException, SystemException, SQLException,
			IllegalStateException, RollbackException, SecurityException,
			HeuristicMixedException, HeuristicRollbackException {
		TransactionManager transactionManager = com.arjuna.ats.jta.TransactionManager
				.transactionManager();
		transactionManager.setTransactionTimeout(0);
		transactionManager.begin();
		try {
			XAConnection xaConnection1 = dataSource1.getXAConnection();
			XAConnection xaConnection2 = dataSource2.getXAConnection();

			XAResource xaResource1 = xaConnection1.getXAResource();
			XAResource xaResource2 = xaConnection2.getXAResource();

			assertTrue(transactionManager.getTransaction().enlistResource(
					xaResource1));
			// assertTrue(userTransaction.getTransaction().delistResource(
			// xaResource1, XAResource.TMSUCCESS));

			assertTrue(transactionManager.getTransaction().enlistResource(
					xaResource2));
			// assertTrue(userTransaction.getTransaction().delistResource(
			// xaResource2, XAResource.TMSUCCESS));
		} finally {
			transactionManager.rollback();
		}
	}

	private void performXATest(XAResource xaResource1, XAResource xaResource2)
			throws SQLException, XAException {
		System.out.println(xaResource1.isSameRM(xaResource2));

		com.arjuna.ats.jta.xa.XidImple xid = new com.arjuna.ats.jta.xa.XidImple(
				new com.arjuna.ats.arjuna.common.Uid(), true, 1);
		xaResource1.start(xid, XAResource.TMNOFLAGS);
		// xaResource1.end(xid, XAResource.TMSUCCESS);
		try {
			try {
				xaResource2.start(xid, XAResource.TMJOIN);
			} catch (XAException e) {
				fail("Got error code: " + XAHelper.printXAErrorCode(e));
			}

			try {
				xaResource2.end(xid, XAResource.TMSUCCESS);
			} catch (XAException e) {
				fail("Got error code: " + XAHelper.printXAErrorCode(e));
			}
		} finally {
			xaResource1.end(xid, XAResource.TMSUCCESS);
			xaResource1.rollback(xid);
		}
		System.out.println("Done");
	}
}
