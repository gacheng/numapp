package com.relic.numapp.client;

public class ShutdownHookup implements Runnable {

    public static void main(String[] args) throws InterruptedException {
        final ShutdownHookup test = new ShutdownHookup();
        Runtime.getRuntime().addShutdownHook(new Thread(){public void run(){
            test.shutdown();
        }});
        Thread t = new Thread(test);
        t.start();
    }

    public void run() {
        synchronized(this) {
            try {
                System.err.println("running");
                wait();
            } catch (InterruptedException e) {}
        }
    }

    public void shutdown() {
        System.err.println("shutdown");
    }
}