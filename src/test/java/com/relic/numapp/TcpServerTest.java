package com.relic.numapp;

import com.relic.numapp.client.TcpClient;
import com.relic.numapp.utils.Constants;
import com.relic.numapp.utils.RandomNumber;
import org.junit.Assert;
import org.junit.Test;

public class TcpServerTest {
    String hostname = "localhost";
    int port = 4000;

    @Test
    public void testCleanUpDirectories() {

    }

    @Test
    public void testLoadTenThousand() {
        testLoadTemplate(5, 10000);
    }

    @Test
    public void testLoadTwoMillion() {
        //testLoadTemplate(5, 2000000);
    }

    @Test
    public void testLoadTenMillion() {
        //testLoadTemplate(5, 10000000);
    }

    @Test
    public void testLoadHundredMillion() {
        //testLoadTemplate(5, 100000000);
    }

    private void testLoadTemplate(int threadNum, int totalNumbers) {
        TcpClient[] clients = new TcpClient[threadNum];
        long beg = System.currentTimeMillis();
        for ( int i=0; i<threadNum; i++) {
            clients[i] = new TcpClient();
            for (int j = 0; j < totalNumbers/threadNum; j++) {
                String userInput = RandomNumber.getNextNumber();
                clients[i].getUserInputs().add(userInput);
            }
            clients[i].getUserInputs().add(Constants.stop.name());
        }
        for ( int i=0; i<threadNum; i++) {
            final int ii = i;
            Thread t = new Thread(() -> {
                clients[ii].start(hostname, port);
            });
            t.start();
        }
        long end = System.currentTimeMillis();
        System.out.println("time spent for " + totalNumbers + " numbers: " + (end - beg) / 1000.0);
    }


    @Test
    public void testGetUniqueCount() {

    }

    @Test
    public void testGetDuplicateCount() {

    }

    @Test
    public void testGetTotalCount() {

    }

    @Test
    public void testMoreThan5Clients() {

    }

}
