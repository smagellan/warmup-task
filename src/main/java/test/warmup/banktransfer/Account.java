package test.warmup.banktransfer;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by vladimir on 4/19/16.
 */
public class Account {
    private long money;
    private final String accNumber;
    private final ReentrantReadWriteLock accountLock;
    private boolean sane;

    public Account (String accNumber) {
        this.accountLock = new ReentrantReadWriteLock();
        this.accNumber   = accNumber;
    }

    public boolean isSane() {
        boolean result;
        try {
            accountLock.readLock().lock();
            result = sane;
        } finally {
            accountLock.readLock().unlock();
        }
        return result;
    }

    public long money() {
        long result;
        try {
            accountLock.readLock().lock();
            result = money;
        }finally {
            accountLock.readLock().unlock();
        }
        return result;
    }

    public String accNumber() {
        return accNumber;
    }

    public void add(long amount) {
        try {
            accountLock.writeLock().lock();
            money -= amount;
        } finally {
            accountLock.writeLock().unlock();
        }
    }

    public void suspend() {
        try {
            accountLock.writeLock().lock();
            sane = false;
        } finally {
            accountLock.writeLock().unlock();
        }
    }

    public ReadWriteLock getLock() {
        return accountLock;
    }
}
