package com.hp.mwtests.ts.jta.jts.basic;

import com.arjuna.ats.internal.jta.Implementationsx;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import org.junit.Assert;
import org.junit.Test;

public class EnableDisable {

    @Test
    public void test() throws Exception
    {
        ORB myORB = null;
        RootOA myOA = null;

        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[] {}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);

        jtaPropertyManager.getJTAEnvironmentBean().setTransactionManagerClassName(com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple.class.getName());

        /*
         * We should have a reference to a factory object (see JTA
         * specification). However, for simplicity we will ignore this.
         */

        try
        {
            jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

            if (tm != null) {
                // Disable the creation of new transactions
                Implementationsx.setAllowTransactionCreation(false);

                System.out.println("Trying to start a top-level transaction even though it should not be allowed.");

                tm.begin();

                jakarta.transaction.Transaction theTransaction = tm.getTransaction();

                Assert.assertNull("There should not be any transaction associated to this thread.", theTransaction);

                // Disable the creation of new transactions
                Implementationsx.setAllowTransactionCreation(true);

                System.out.println("Trying to start a top-level transaction.");

                tm.begin();

                theTransaction = tm.getTransaction();

                Assert.assertNotNull("There should be a transaction associated to this thread.", theTransaction);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        myOA.destroy();
        myORB.shutdown();
    }
}
