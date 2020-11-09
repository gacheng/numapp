package com.relic.numapp.client;

import com.relic.numapp.utils.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Random;
import java.util.Scanner;

/**
 * A simulator to run TcpClient manually by taking input from console
 */
public class TcpClientManual {
    final static Log logger = LogFactory.getLog(TcpClientManual.class);

    public static void main(String[] args) {
        runIt();
    }

    public static void runIt() {
        String hostname = "localhost";
        int port = 4000;
        Scanner scanner = new Scanner(System.in);

        TcpClient tcpClient = new TcpClient();
        new Thread(()-> {
            tcpClient.start(hostname, port);
        }).start();

        String userInput;
        while ( (userInput = scanner.next()) != null) {
            tcpClient.getUserInputs().add(userInput);
        }
    }

}
