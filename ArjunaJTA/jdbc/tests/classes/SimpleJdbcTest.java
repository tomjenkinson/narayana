import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

import oracle.jdbc.xa.client.OracleXADataSource;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class SimpleJdbcTest {
	private static final String DB_USER1 = "DBALLO01";
	private static final String DB_USER2 = "DBALLO02";

	public static void main(String[] args) throws Exception {

		final DataSource dataSource1 = getDataSource(DB_USER1, "oracle: "
				+ DB_USER1);
		final DataSource dataSource2 = getDataSource(DB_USER2, "oracle: "
				+ DB_USER2);

		prepare(dataSource1);
		prepare(dataSource2);

		final UserTransaction userTransaction = com.arjuna.ats.jta.UserTransaction
				.userTransaction();
		userTransaction.begin();
		try {
			final Connection connection1 = dataSource1.getConnection();
			final Connection connection2 = dataSource2.getConnection();

			insert(connection1);
			insert(connection2);

			userTransaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
			userTransaction.rollback();
		}
	}

	private static void insert(Connection connection) throws SQLException {
		final PreparedStatement preparedStatement = connection
				.prepareStatement("INSERT INTO jta_test (some_string) VALUES ('test')");
		preparedStatement.execute();
	}

	private static void prepare(DataSource dataSource) throws SQLException {
		final Connection connection = dataSource.getConnection();
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM user_tables WHERE table_name = 'JTA_TEST'");
		try {
			final ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				connection.prepareStatement("DROP TABLE jta_test").execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		connection.prepareStatement(
				"CREATE TABLE jta_test (some_string VARCHAR(10))").execute();
	}

	private static DataSource getDataSource(String user, String resourceName)
			throws NamingException, SQLException, ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		InitialContext initialContext = prepareInitialContext();

		final OracleXADataSource xaDataSource = new OracleXADataSource();
		((OracleXADataSource) xaDataSource).setNetworkProtocol("tcp");
		((OracleXADataSource) xaDataSource)
				.setServerName("db04.mw.lab.eng.bos.redhat.com");
		((OracleXADataSource) xaDataSource).setPortNumber(1521);
		((OracleXADataSource) xaDataSource).setDatabaseName("qaora11");
		((OracleXADataSource) xaDataSource).setDriverType("thin");
		((OracleXADataSource) xaDataSource).setUser(user);
		((OracleXADataSource) xaDataSource).setPassword(user);

		// Class clazz = Class.forName("com.sybase.jdbc3.jdbc.SybXADataSource");
		// XADataSource xaDataSource = (XADataSource) clazz.newInstance();
		// clazz.getMethod("setServerName", new Class[] { String.class
		// }).invoke(
		// xaDataSource, new Object[] { DB_HOST });
		// clazz.getMethod("setDatabaseName", new Class[] { String.class })
		// .invoke(xaDataSource, new Object[] { "dballo02" });
		// clazz.getMethod("setUser", new Class[] { String.class }).invoke(
		// xaDataSource, new Object[] { user });
		// clazz.getMethod("setPassword", new Class[] { String.class }).invoke(
		// xaDataSource, new Object[] { user });
		// clazz.getMethod("setPortNumber", new Class[] { int.class }).invoke(
		// xaDataSource, new Object[] { 5000 });

		// Class clazz = Class
		// .forName("com.microsoft.sqlserver.jdbc.SQLServerXADataSource");
		// XADataSource xaDataSource = (XADataSource) clazz.newInstance();
		// clazz.getMethod("setServerName", new Class[] { String.class })
		// .invoke(xaDataSource,
		// new Object[] { "db06.mw.lab.eng.bos.redhat.com" });
		// clazz.getMethod("setDatabaseName", new Class[] { String.class })
		// .invoke(xaDataSource, new Object[] { user });
		// clazz.getMethod("setUser", new Class[] { String.class }).invoke(
		// xaDataSource, new Object[] { user });
		// clazz.getMethod("setPassword", new Class[] { String.class }).invoke(
		// xaDataSource, new Object[] { user });
		// clazz.getMethod("setPortNumber", new Class[] { int.class }).invoke(
		// xaDataSource, new Object[] { 1433 });

		// Class clazz = Class.forName("com.ibm.db2.jcc.DB2XADataSource");
		// XADataSource xaDataSource = (XADataSource) clazz.newInstance();
		// clazz.getMethod("setServerName", new Class[] { String.class })
		// .invoke(xaDataSource,
		// new Object[] { "vmg06.mw.lab.eng.bos.redhat.com" });
		// clazz.getMethod("setDatabaseName", new Class[] { String.class })
		// .invoke(xaDataSource, new Object[] { "dballo" });
		// clazz.getMethod("setUser", new Class[] { String.class }).invoke(
		// xaDataSource, new Object[] { user });
		// clazz.getMethod("setPassword", new Class[] { String.class }).invoke(
		// xaDataSource, new Object[] { user });
		// clazz.getMethod("setDriverType", new Class[] { int.class }).invoke(
		// xaDataSource, new Object[] { 4 });
		// clazz.getMethod("setPortNumber", new Class[] { int.class }).invoke(
		// xaDataSource, new Object[] { 50000 });

		// XADataSource xaDataSource = new MysqlXADataSource();
		// ((MysqlXADataSource) xaDataSource)
		// .setServerName("tywin.buildnet.ncl.jboss.com");
		// ((MysqlXADataSource) xaDataSource).setPortNumber(3306);
		// ((MysqlXADataSource) xaDataSource).setDatabaseName("jbossts");
		// ((MysqlXADataSource) xaDataSource).setUser(user);
		// ((MysqlXADataSource) xaDataSource).setPassword(user);

		// XADataSource xaDataSource = new PGXADataSource();
		// ((PGXADataSource) xaDataSource).setPortNumber(5432);
		// ((PGXADataSource) xaDataSource).setUser(user);
		// ((PGXADataSource) xaDataSource).setPassword(user);
		// ((PGXADataSource) xaDataSource)
		// .setServerName("tywin.buildnet.ncl.jboss.com");
		// ((PGXADataSource) xaDataSource).setDatabaseName("jbossts");

		final String name = "java:/comp/env/jdbc/" + user;
		initialContext.bind(name, xaDataSource);

		DriverManagerDataSource dataSource = new DriverManagerDataSource(
				"jdbc:arjuna:" + name);
		dataSource
				.setDriverClassName("com.arjuna.ats.jdbc.TransactionalDriver");

		return dataSource;
	}

	private static InitialContext prepareInitialContext()
			throws NamingException {
		final InitialContext initialContext = new InitialContext();

		try {
			initialContext.lookup("java:/comp/env/jdbc");
		} catch (NamingException ne) {
			initialContext.createSubcontext("java:");
			initialContext.createSubcontext("java:/comp");
			initialContext.createSubcontext("java:/comp/env");
			initialContext.createSubcontext("java:/comp/env/jdbc");
		}

		return initialContext;
	}
}
