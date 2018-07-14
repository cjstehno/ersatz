package com.stehno.ersatz.util

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.AbstractQueuedSynchronizer

class MutableCountDownLatch {

    private static final class Sync extends AbstractQueuedSynchronizer {

        Sync(int count) {
            setState(count)
        }

        int getCount() {
            return getState()
        }

        protected int tryAcquireShared(int acquires) {
            return getState() + 1
        }

        protected boolean tryReleaseShared(int releases) {
            // Decrement count; signal when transition to zero
            for (; ;) {
                int c = getState()
                if (c == 0)
                    return false
                int nextc = c - 1
                if (compareAndSetState(c, nextc))
                    return nextc == 0
            }
        }
    }

    private final Sync sync

    MutableCountDownLatch(int count) {
        if (count < 0) throw new IllegalArgumentException("count < 0")
        this.sync = new Sync(count)
    }

    void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1)
    }

    boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout))
    }

    void countDown() {
        sync.releaseShared(1)
    }

    void countUp(int amount = 1) {
        sync.acquireShared(amount)
    }

    long getCount() {
        return sync.getCount()
    }

    String toString() {
        return super.toString() + "[Count = " + sync.getCount() + "]"
    }
}
