package com.relic.numapp.client;

import com.relic.numapp.utils.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Random;
import java.util.Scanner;

/**
 * A simulator to run TcpClient automatically
 */
public class TcpClientAuto {
    final static Log logger = LogFactory.getLog(TcpClientAuto.class);

    public static void main(String[] args) {
       runIt();
    }

    public static void runIt() {
        String hostname = "localhost";
        int port = 4000;
        String type = Constants.automatic.name();
        Scanner scanner = new Scanner(System.in);

        TcpClient tcpClient = new TcpClient();
        for(int i=0; i<100; i++) {
            String userInput = getNextNumber();
            tcpClient.getUserInputs().add(userInput);
        }
        tcpClient.getUserInputs().add(Constants.stop.name());
        tcpClient.start(hostname, port);
    }


    /**
     * Helper method to get next number, if it is automatic type, it is generated from random number,
     * otherwse, it is input from system console
     *
     * @return
     */
    private static String getNextNumber() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999999);
        return String.format("%09d", number);
    }


}
