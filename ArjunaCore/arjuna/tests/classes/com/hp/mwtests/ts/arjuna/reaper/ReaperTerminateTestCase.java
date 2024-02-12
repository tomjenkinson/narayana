package com.hp.mwtests.ts.arjuna.reaper;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReaperTerminateTestCase extends ReaperTestCaseControl {

    @Test
    public void testReaperWait() throws Exception
    {
        runTest(true, 5);
    }

    @Test
    public void testReaperForce() throws Exception
    {
        runTest(false, 5);
    }

    private void runTest(boolean waitForTransactions, int timeoutInSeconds) throws Exception
    {
        TransactionReaper reaper = TransactionReaper.transactionReaper();

        // give the reaper worker time to start too

        Thread.sleep(1000);

        // create test reapable some of which will not respond immediately to cancel requests

        Uid uid0 = new Uid();

        // reapable0 will return CANCELLED from cancel and will rendezvous inside the cancel call
        // so we can delay it. prevent_commit should not get called so we don't care about the arguments
        ReaperTestCaseControl.TestReapable reapable0 = new ReaperTestCaseControl.TestReapable(uid0, true, false, false, false);
        // reapable0 will return CANCELLED from cancel and will rendezvous inside the cancel call
        // so we can delay it. prevent_commit should not get called so we don't care about the arguments
        ReaperTestCaseControl.TestReapable reapable1 = new ReaperTestCaseControl.TestReapable(uid0, true, false, false, false);

        reaper.insert(reapable0, timeoutInSeconds);
        reaper.insert(reapable1, 2 * timeoutInSeconds);

        assertEquals(2, reaper.numberOfTransactions());
        assertEquals(2, reaper.numberOfTimeouts());

        // ensure the first reapable is ready

        Thread.sleep(1000);

        long beforeTerminate = System.currentTimeMillis();

        TransactionReaper.terminate(waitForTransactions);

        long afterTerminate = System.currentTimeMillis();

        long duration = afterTerminate - beforeTerminate;

        System.out.println("The duration of TransactionReaper.terminate is " + duration);

        if (waitForTransactions)
            assertTrue(duration > ((2 * timeoutInSeconds  * 1000L) - 1000L));
        else
            assertTrue(duration < ((timeoutInSeconds * 1000L)));

        assertEquals(0, reaper.numberOfTransactions());

        assertTrue(reapable0.getCancelTried());
    }
}