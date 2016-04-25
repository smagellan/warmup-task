package test.warmup.banktransfer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

/**
 * Created by vladimir on 4/20/16.
 */
public class BankTest {

    @org.junit.Test
    public void transfer() throws Exception {
        ExecutorService svc = Executors.newFixedThreadPool(4);
        //TODO: implement logic
    }

    @org.junit.AfterClass
    public void shutdownBank() throws Exception {
        Bank.bank().shutdown();
    }
}