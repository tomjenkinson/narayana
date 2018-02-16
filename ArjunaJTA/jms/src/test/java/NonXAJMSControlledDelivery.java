import com.arjuna.ats.arjuna.coordinator.TxControl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.mockito.Mockito.when;


public class NonXAJMSControlledDelivery {
    @Mock
    private javax.jms.Connection connection;

    @Mock
    private javax.jms.Session session;

    @Mock
    private MessageConsumer messageConsumer;

    @Mock
    private Message message;


    @Before
    public void before() throws JMSException {
        TxControl.setXANodeName("1");
        MockitoAnnotations.initMocks(this);
        when(connection.createSession(false, Session.CLIENT_ACKNOWLEDGE)).thenReturn(session);
        when(session.createConsumer(null)).thenReturn(messageConsumer);
        when(messageConsumer.receive()).thenReturn(message);
    }

    @Test
    public void test() throws Exception {
        // User has to do this - as you can see it is a normal dequeue - it could also be any non-XA operation
        File tmp = new File("next-to-process.txt");
        tmp.createNewFile();
        File file = new File("next-to-process.txt");
        FileOutputStream out = new FileOutputStream(file);
        java.nio.channels.FileLock lock = out.getChannel().lock();
        // Read the content from the file
        // Now begin processing on content
        javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
        tm.begin();
        javax.transaction.Transaction theTransaction = tm.getTransaction();
        theTransaction.enlistResource(new DummyXAResource("first")); // Would be done by normal enlist, e.g. application server
        theTransaction.enlistResource(new DummyXAResource("second")); // Would be done by normal enlist, e.g. application server
        theTransaction.registerSynchronization(new Synchronization() {
            @Override
            public void beforeCompletion() {
                // NO-OP
            }

            @Override
            public void afterCompletion(int status) {
                if (status == Status.STATUS_COMMITTED) {
                    file.setWritable(false);
                    try {
                        lock.release();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Deleted: "+ file.delete());
                    System.out.println("Non-XA message acknowledged");
                } else {
                    try {
                        lock.release(); // Let someone else have a go?
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }); // This allows the user to control the acknowledgement of the message
        tm.commit();
    }

    // This would be provided by AS as normal
    private class DummyXAResource implements XAResource {
        private final String name;

        public DummyXAResource(String name) {
            this.name = name;
        }

        @Override
        public void commit(Xid xid, boolean b) throws XAException {
            System.out.println(name + " was committed");
        }

        @Override
        public void end(Xid xid, int i) throws XAException {

        }

        @Override
        public void forget(Xid xid) throws XAException {

        }

        @Override
        public int getTransactionTimeout() throws XAException {
            return 0;
        }

        @Override
        public boolean isSameRM(XAResource xaResource) throws XAException {
            return false;
        }

        @Override
        public int prepare(Xid xid) throws XAException {
            System.out.println(name + " was prepared");
            return 0;
        }

        @Override
        public Xid[] recover(int i) throws XAException {
            return new Xid[0];
        }

        @Override
        public void rollback(Xid xid) throws XAException {

        }

        @Override
        public boolean setTransactionTimeout(int i) throws XAException {
            return false;
        }

        @Override
        public void start(Xid xid, int i) throws XAException {

        }
    }
}