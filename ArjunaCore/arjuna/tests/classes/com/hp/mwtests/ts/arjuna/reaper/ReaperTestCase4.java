package com.hp.mwtests.ts.arjuna.reaper;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.Reapable;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitConfig;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.fail;

@RunWith(BMUnitRunner.class)
@BMScript("reaper")
@BMUnitConfig(debug = true)
public class ReaperTestCase4 extends ReaperTestCaseControl {

    private static int _timeout = 5;

    private Reapable reapable1 = new SimpleReapable();
    // private Reapable reapable2 = new SimpleReapable();

    @Test
    public void test() throws InterruptedException {
        enableRendezvous("controlledshutdown", false);
        TransactionReaper transactionReaper = TransactionReaper.transactionReaper();
        transactionReaper.insert(reapable1, _timeout);
        // This second Reapable must not be removed as we should check if TransactionReaper waits for it
        // even when waitForTransaction is false
        // transactionReaper.insert(reapable2, _timeout);

        AtomicBoolean terminated = new AtomicBoolean(false);
        new Thread(() -> {
            // The shutdown is from another thread
            transactionReaper.terminate(false);
            synchronized (terminated) {
                terminated.set(true);
                terminated.notify();
            }
        }).start();
        // This needs to be after the thread above has started so that the other thread can start a shutdown
        transactionReaper.remove(reapable1);

        synchronized (terminated) {
            if (!terminated.get()) {
                try {
                    // Give some time for the terminate to finish
                    long beforeWait = System.currentTimeMillis();
                    terminated.wait();
                    long afterWait = System.currentTimeMillis();
                    long duration = afterWait - beforeWait;

                    // The duration (_timeout * 1000) + 1000 is worked out by adding 1s to _timeout. This is needed
                    // as ReaperThread needs to wait the first timeout and then process the other transactions.
                    Assert.assertTrue(String.format("The timeout duration was %s!", duration), duration < ((_timeout * 1000) + 1000));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (!terminated.get()) {
            fail("TreansactionReaper terminate blocked");
        }
    }

    private class SimpleReapable implements Reapable {
        private Uid uid = new Uid();

        @Override
        public boolean running() {
            return false;
        }

        @Override
        public boolean preventCommit() {
            return false;
        }

        @Override
        public int cancel() {
            return 0;
        }

        @Override
        public Uid get_uid() {
            return uid;
        }
    }
}