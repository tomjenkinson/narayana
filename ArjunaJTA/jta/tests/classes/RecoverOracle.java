import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.jta.utils.XAHelper;
import com.arjuna.ats.jta.xa.XidImple;

public class RecoverOracle {

	@Test
	public void recoverOracle() throws SQLException, XAException {
		String hostName = "tywin.buildnet.ncl.jboss.com";
		String userName = "dtf11";
		String password = "dtf11";
		String recoveryUserName = "dtf11";
		String recoveryPassword = "dtf11";
		String databaseName = "ORCL";

		System.out
				.println("Business logic being executed in JVM2 but in TX under control of JVM1");
		oracle.jdbc.xa.client.OracleXADataSource dataSource = new oracle.jdbc.xa.client.OracleXADataSource();
		dataSource.setDriverType("thin");
		dataSource.setPortNumber(1521);
		dataSource.setNetworkProtocol("tcp");
		dataSource.setUser(userName);
		dataSource.setPassword(password);
		dataSource.setServerName(hostName);
		dataSource.setDatabaseName(databaseName);

		XAConnection xaConnection1 = dataSource.getXAConnection();
		final XAResource xaResource1 = xaConnection1.getXAResource();
		assertTrue(xaResource1.setTransactionTimeout(2));
		final Xid xid1 = new XidImple(new Uid(), true, 1);
		xaResource1.start(xid1, XAResource.TMNOFLAGS);
		Connection connection1 = xaConnection1.getConnection();
		ResultSet executeQuery = connection1.createStatement().executeQuery(
				"select * from testentity for update");
		assertTrue(executeQuery.next());

		System.out.println("Any JVM");
		oracle.jdbc.xa.client.OracleXADataSource dataSource2 = new oracle.jdbc.xa.client.OracleXADataSource();
		dataSource2.setDriverType("thin");
		dataSource2.setPortNumber(1521);
		dataSource2.setNetworkProtocol("tcp");
		dataSource2.setUser(userName);
		dataSource2.setPassword(password);
		dataSource2.setServerName(hostName);
		dataSource2.setDatabaseName(databaseName);

		XAConnection xaConnection2 = dataSource2.getXAConnection();
		XAResource xaResource2 = xaConnection2.getXAResource();
		Xid xid2 = new XidImple(new Uid(), true, 1);
		assertTrue(xaResource2.setTransactionTimeout(2));
		xaResource2.start(xid2, XAResource.TMNOFLAGS);
		Connection connection2 = xaConnection2.getConnection();


		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.currentThread().sleep(10000);
					xaResource1.prepare(xid1);
					xaResource1.commit(xid1, false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}).start();

		System.out.println("Crash in JVM1");
		connection2.createStatement().execute(
				"UPDATE testentity set a = 2 where id = '1'");

		
		xaResource2.prepare(xid2);
		xaResource2.commit(xid2, false);
	}
}
