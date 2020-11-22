package com.relic.numapp.client;

import com.relic.numapp.utils.Constants;
import com.relic.numapp.utils.RandomNumber;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A simulator to run TcpClient automatically
 */
public class TcpClientAuto {
    final static Log logger = LogFactory.getLog(TcpClientAuto.class);
    final static int ONE_K = 1000;
    final static int ONE_MILLION = 1000000;
    static String hostname = "localhost";
    static int port = 4000;

    public static void main(String[] args) {
        int num = ONE_K;

        if ( args.length > 0 ) {
            String str = args[0];
            try {
                num = ONE_MILLION * Integer.parseInt(str);
            } catch (NumberFormatException fe) { }
        }
        testLoadTemplate(5, num);
    }

    public static void run100() {
        TcpClient tcpClient = new TcpClient();
        for(int i=0; i<101; i++) {
            String userInput = RandomNumber.getNextNumber();
            tcpClient.getUserInputs().add(userInput);
        }
        tcpClient.getUserInputs().add(Constants.stop.name());
        new Thread(()->{
            tcpClient.start(hostname, port);
        }).start();
    }

    static void testLoadTemplate(int threadNum, int totalNumbers) {
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

}
