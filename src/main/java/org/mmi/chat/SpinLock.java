package org.mmi.chat;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class SpinLock {

    private final static ConcurrentHashMap<String, SpinLock> locks = new ConcurrentHashMap<>();

    private final Lock lock = new ReentrantLock();

    public void acquire() {
        while (!lock.tryLock()) {
            try {
                Thread.sleep(10l);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);// just for MVP
            }
        }
    }

    public void release() {
        lock.unlock();
    }


    public static SpinLock forString(String s) {
        return locks.computeIfAbsent(s, k -> new SpinLock());
    }
}
