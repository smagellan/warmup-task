package test.warmup.banktransfer;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by vladimir on 4/19/16.
 */
public class BankSecurityService {

    private static final BankSecurityService instance = createSecurityService();

    private final BlockingQueue<Transfer> transfersQueue;
    private final SecurityServiceWorker worker;

    private BankSecurityService(){
        this.transfersQueue  = new LinkedBlockingQueue<>();
        this.worker          = new SecurityServiceWorker(transfersQueue);
    }

    private static BankSecurityService  createSecurityService (){
        BankSecurityService result = new BankSecurityService();
        result.init();
        return result;
    }

    void init() {
        this.worker.start();
    }

    public void shutdown() throws InterruptedException{
        this.worker.interrupt();
        this.worker.join();
    }

    public static BankSecurityService securityService() {
        return instance;
    }

    public void scheduleForFraudCheck(String fromAccountNum, String toAccountNum, long amount) throws InterruptedException{
        transfersQueue.put(new Transfer(fromAccountNum, toAccountNum, amount));
    }

    static class SecurityServiceWorker extends Thread {
        private final Random rnd;
        private final BlockingQueue<Transfer> jobQueue;
        private SecurityServiceWorker(BlockingQueue<Transfer> jobQueue){
            this.rnd             = new Random();
            this.jobQueue        = jobQueue;
        }

        boolean isFraud(String fromAccountNum, String toAccountNum, long amount) throws InterruptedException {
            Thread.sleep(1000);
            return rnd.nextBoolean();
        }

        boolean isFraud(Transfer t) throws InterruptedException {
            return isFraud(t.fromAccountNum(), t.toAccountNum(), t.amount());
        }

        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Transfer currentTransfer = jobQueue.take();
                    if (isFraud(currentTransfer)) {
                        Bank.bank().fraudTransfer(currentTransfer);
                    }
                }
            } catch (InterruptedException ex) {
                System.err.println("SecurityServiceWorker was interrupted");
            }
            System.err.println("SecurityServiceWorker: exit");
        }
    }
}


