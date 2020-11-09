package com.relic.numapp;

import com.relic.numapp.server.service.PersistThread;
import org.junit.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PersistThreadTest {
    @Test
    public void testRun() {
        BlockingQueue<String> blockingQueue = new LinkedBlockingQueue<>();
        blockingQueue.add("123456789");
        PersistThread runnable = new PersistThread(blockingQueue, "./output");
        new Thread(runnable).start();
    }
}
