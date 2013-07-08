import static org.junit.Assert.assertTrue;

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

        {
            oracle.jdbc.xa.client.OracleXADataSource dataSource = new oracle.jdbc.xa.client.OracleXADataSource();
            dataSource.setDriverType("thin");
            dataSource.setPortNumber(1521);
            dataSource.setNetworkProtocol("tcp");
            dataSource.setUser(recoveryUserName);
            dataSource.setPassword(recoveryPassword);
            dataSource.setServerName(hostName);
            dataSource.setDatabaseName(databaseName);

            XAResource xaResource = dataSource.getXAConnection().getXAResource();
            Xid[] recover = xaResource.recover(XAResource.TMSTARTRSCAN);
            for (int i = 0; i < recover.length; i++) {
                try {
                    System.out.println("Rolling back: " + new XidImple(recover[i]));
                    xaResource.rollback(recover[i]);
                    System.out.println("Rolled back");
                } catch (XAException e) {
                    e.printStackTrace();
                    System.out.println(XAHelper.printXAErrorCode(e));
                }
            }
            xaResource.recover(XAResource.TMENDRSCAN);
        }

        {
            oracle.jdbc.xa.client.OracleXADataSource dataSource = new oracle.jdbc.xa.client.OracleXADataSource();
            dataSource.setDriverType("thin");
            dataSource.setPortNumber(1521);
            dataSource.setNetworkProtocol("tcp");
            dataSource.setUser(userName);
            dataSource.setPassword(password);
            dataSource.setServerName(hostName);
            dataSource.setDatabaseName(databaseName);

            XAConnection xaConnection = dataSource.getXAConnection();
            XAResource xaResource2 = xaConnection.getXAResource();
            XidImple xid = new XidImple(new Uid(), true, 1);
            xaResource2.start(xid, XAResource.TMNOFLAGS);
            System.out.println("Preparing: " + xid);
            xaConnection.getConnection().createStatement().execute("INSERT INTO testentity (id, a) VALUES (1, 1)");
            System.out.println("Prepared");
            xaResource2.prepare(xid);
        }

        {
            oracle.jdbc.xa.client.OracleXADataSource dataSource = new oracle.jdbc.xa.client.OracleXADataSource();
            dataSource.setDriverType("thin");
            dataSource.setPortNumber(1521);
            dataSource.setNetworkProtocol("tcp");
            dataSource.setUser(recoveryUserName);
            dataSource.setPassword(recoveryPassword);
            dataSource.setServerName(hostName);
            dataSource.setDatabaseName(databaseName);

            XAResource xaResource = dataSource.getXAConnection().getXAResource();
            Xid[] recover = xaResource.recover(XAResource.TMSTARTRSCAN);
            int completed = 0;
            int failed = 0;
            for (int i = 0; i < recover.length; i++) {
                try {
                    System.out.println("Commiting: " + new XidImple(recover[i]));
                    xaResource.commit(recover[i], false);
                    System.out.println("Committed");
                    completed++;
                } catch (XAException e) {
                    e.printStackTrace();
                    System.out.println(XAHelper.printXAErrorCode(e));
                    failed++;
                }
            }
            xaResource.recover(XAResource.TMENDRSCAN);
            assertTrue("Completed: " + completed + " Expected: " + 1, completed == 1);
            assertTrue("Failed: " + failed + " Expected: " + 0, failed == 0);
        }
    }
}
