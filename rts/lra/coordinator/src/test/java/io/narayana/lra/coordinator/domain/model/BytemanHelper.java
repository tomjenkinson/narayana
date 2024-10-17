package io.narayana.lra.coordinator.domain.model;

import java.util.concurrent.atomic.AtomicBoolean;

public class BytemanHelper {
    static AtomicBoolean businessCalled = new AtomicBoolean(false);
    public void rendezvousBusinessAndCompensateBusiness() throws InterruptedException {
        synchronized (businessCalled) {
            businessCalled.set(true);
            businessCalled.notifyAll();
            businessCalled.wait();
        }
    }
    public void rendezvousBusinessAndCompensateCompensate() throws InterruptedException {
        synchronized (businessCalled) {
            while (!businessCalled.get()) {
                businessCalled.wait();
            }
            businessCalled.notify();
        }
    }
}
