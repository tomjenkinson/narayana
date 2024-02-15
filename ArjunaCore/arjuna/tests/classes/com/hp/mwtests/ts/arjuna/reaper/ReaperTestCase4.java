package com.hp.mwtests.ts.arjuna.reaper;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.Reapable;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.hp.mwtests.ts.arjuna.resources.BasicObject;
import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.fail;

@RunWith(BMUnitRunner.class)
@BMScript("reaper")
public class ReaperTestCase4 extends ReaperTestCaseControl {

    private Reapable reapable = new Reapable() {
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
    };

    @Test
    public void test() throws InterruptedException {
        enableRendezvous("controlledshutdown", false);
        TransactionReaper transactionReaper = TransactionReaper.transactionReaper();
        transactionReaper.insert(reapable, 5);

        AtomicBoolean terminated = new AtomicBoolean(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // The shutdown is from another thread
                transactionReaper.terminate(false);
                synchronized (terminated) {
                    terminated.set(true);
                    terminated.notify();
                }
            }
        }).start();
        // This needs to be after the thread above has started so that the other thread can start a shutdown
        transactionReaper.remove(reapable);

        synchronized (terminated) {
            if (!terminated.get()) {
                try {
                    // Give some time for the terminate to finish
                    terminated.wait(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (!terminated.get()) {
            fail("TreansactionReaper terminate blocked");
        }
    }
}
