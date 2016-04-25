package test.warmup.banktransfer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

/**
 * Created by vladimir on 4/19/16.
 */
public class Bank {
    public static final long TRANSFER_THRESHOLD = 50000L;

    private static final Bank instance = createBankInstance();

    private final Map<String, Account> accounts;

    private Bank(){
        accounts            = new ConcurrentHashMap<>();
    }

    private static Bank createBankInstance() {
        Bank result = new Bank();
        return result;
    }

    public static Bank bank() {
        return instance;
    }

    public void shutdown() throws InterruptedException{
        BankSecurityService.securityService().shutdown();
    }

    public void fraudTransfer(Transfer t) {
        Account acc = accounts.get(t.fromAccountNum());
        acc.suspend();
        acc = accounts.get(t.toAccountNum());
        acc.suspend();
    }

    public void transfer(String fromAccountNum, String toAccountNum, long amount) {
        if ( !(fromAccountNum == null || toAccountNum == null || fromAccountNum.equals(toAccountNum))  &&
                accounts.containsKey(fromAccountNum) && accounts.containsKey(toAccountNum) ) {
            doTransferTransaction(fromAccountNum, toAccountNum, amount);
            if (amount > TRANSFER_THRESHOLD) {
                try {
                    scheduleForFraudCheck(fromAccountNum, toAccountNum, amount);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } else {
            throw new IllegalArgumentException("both 'fromNum' and 'toNum' account's identifiers must be unique and non-null");
        }
    }

    public void scheduleForFraudCheck(String fromAccountNum, String toAccountNum, long amount) throws InterruptedException{
        BankSecurityService.securityService().scheduleForFraudCheck(fromAccountNum, toAccountNum, amount);
    }

    private void doTransferTransaction(String fromAccountNum, String toAccountNum, long amount) {
        final int compResult          = fromAccountNum.compareTo(toAccountNum);
        final String firstAccountNum  = compResult < 0 ? fromAccountNum : toAccountNum;
        final String secondAccountNum = compResult < 0 ? toAccountNum   : fromAccountNum;
        final Account firstAccount    = accounts.get(firstAccountNum);
        final Account secondAccount   = accounts.get(secondAccountNum);
        final Lock firstLock          = firstAccount.getLock().writeLock();
        final Lock secondtLock        = secondAccount.getLock().writeLock();
        final Account fromAccount     = compResult < 0 ? firstAccount : secondAccount;
        final Account toAccount       = compResult < 0 ? secondAccount : firstAccount;
        try {
            firstLock.lock();
            secondtLock.lock();
            if (fromAccount.money() - amount > 0) {
                if (fromAccount.isSane() && toAccount.isSane()) {
                    fromAccount.add(-amount);
                    toAccount.add(amount);
                } else {
                    throw new IllegalStateException("both accounts must be sane");
                }
            } else {
                throw new IllegalStateException("fromAccount has no enough money");
            }
        } finally {
            secondtLock.unlock();
            firstLock.unlock();
        }
    }

    public long getBalance(String accountNum) {
        long result;
        if (accountNum != null && accounts.containsKey(accountNum)) {
            final Account account = accounts.get(accountNum);
            result = account.money();
        } else {
            throw new IllegalArgumentException("unknown account");
        }
        return result;
    }
}
