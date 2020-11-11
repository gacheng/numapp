package com.relic.numapp.client;

import com.relic.numapp.utils.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TcpClient {
    final static Log logger = LogFactory.getLog(TcpClient.class);
    private BlockingQueue<String> userInputs = new LinkedBlockingQueue<String>();
    private boolean stopFlag = false;

    public void start(String hostname, int port) {
        logger.info("TcpClient started");

        try (
            Socket socket = new Socket(hostname, port);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            //start a new thread to check if the socket to server is alive, if not, closing this client
            new Thread(()->{
                while ( !stopFlag ) {
                    try {
                        writer.println("PNG");
                        if ( writer.checkError() ) {
                            logger.info("Socket no longer reachable, possibly the server is shutdown");
                            System.exit(0);
                        }
                        Thread.sleep(30000);  //every minute
                        logger.info("BlockingQueue size: " + userInputs.size());
                    }
                    catch (InterruptedException ite) {
                        logger.error("caught", ite);
                    }
                }
            }).start();

            String userInput;
            while ( !stopFlag && (userInput = userInputs.take()) != null ) {
                //if ( logger.isDebugEnabled() ) {
                    logger.info("You input is: " + userInput);
                //}
                if ( userInput.equalsIgnoreCase(Constants.stop.name()) ) {
                    logger.info("TcpClient be stopped");
                    //exit gracefully
                    socket.close();
                    return;
                }
                if ( !validateUserInput(userInput) ) {
                    logger.error("Invalid input: [" + userInput + "]. Closing the socket");
                    socket.close();
                    System.exit(1);
                }

                if (socket.isClosed()) {
                    logger.error("Socket is closed. Exit client app");
                    socket.close();
                    System.exit(2);
                }

                writer.println(userInput);
                if ( userInput.equalsIgnoreCase(Constants.terminate.name()) ) {
                    logger.info("TcpClient closing...");
                    //exit gracefully
                    socket.close();
                    return;
                }

                String fromServer = reader.readLine();
                //if ( logger.isDebugEnabled() ) {
                    logger.info("from server: " + fromServer);
                //}
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (UnknownHostException uex) {
            logger.error("Unknown Host. Check name or network connection.", uex);
        } catch (IOException ioex) {
            logger.error("I/O error. Check if the server is started.", ioex);
        } catch (InterruptedException e) {
            logger.error("caught", e);
        }
    }

    /**
     * A helper method to validate user input string. This is the place that you put or grow the
     * validation rules
     *
     * @param userInput
     * @return
     */
    public static boolean validateUserInput(String userInput) {
        if ( userInput == null || userInput.length() != 9 )
            return false;

        try {
            if ( !userInput.equalsIgnoreCase(Constants.terminate.name()) ) {
                Long.parseLong(userInput);
            }
        }
        catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public void setStopFlag(boolean stopFlag) {
        this.stopFlag = stopFlag;
    }

    public BlockingQueue<String> getUserInputs() {
        return userInputs;
    }
}
