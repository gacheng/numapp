package com.relic.numapp.server.service;

import com.relic.numapp.server.TcpServer;
import com.relic.numapp.utils.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A processing thread class that accepts message from client and persist it in the server
 */
public class ListeningThread implements Runnable {
    private final static Log logger = LogFactory.getLog(ListeningThread.class);

    private String name;
    private Socket socketClientToServer;
    private BlockingQueue<Integer> blockingQueue;
    private boolean stopFlag;

    private static Integer[] allNumbers;
    //private static AtomicInteger uniqueCount;
    //private static AtomicInteger duplicateCount;
    //private static AtomicInteger totalUniqueCount;
    //private static AtomicInteger totalReadCount;


    public ListeningThread(String name, Socket socket, BlockingQueue<Integer> blockingQueue) {
        this.name = name;
        this.socketClientToServer = socket;
        this.blockingQueue = blockingQueue;
        this.stopFlag = false;

        allNumbers = TcpServer.allNumbers;
        //uniqueCount = TcpServer.getUniqueCount();
        //duplicateCount = TcpServer.getDuplicateCount();
        //totalUniqueCount = TcpServer.getTotalUniqueCount();
        //totalReadCount = TcpServer.getTotalReadCount();
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socketClientToServer.getInputStream()));
                PrintWriter writer = new PrintWriter(socketClientToServer.getOutputStream(), true);
        ) {
            //start a new thread to check if the socket from client alive, if not, closing this socket and thread
            new Thread(()->{
                while ( !stopFlag ) {
                    try {
                        writer.println(Constants.PNG.name());
                        if ( writer.checkError() ) {
                            closeSocketAndThread(0);
                        }
                        Thread.sleep(5000);  //every minute
                    }
                    catch (InterruptedException ie) {
                        logger.error("caught", ie);
                    }
                }
            }).start();

            while ( !stopFlag ) {
                String fromClient = reader.readLine();
                //logger.info("$$$$$$$$$$$ got from client(" + name + "): " + fromClient);

                if (fromClient == null) {
                    //client socket is closed
                    closeSocketAndThread(1);
                    return;
                } else if (fromClient.length() == 9 ) {
                    try {
                        //write to blockingqueue
                        int num = Integer.parseInt(fromClient);
                        processCounts(num);
                        writer.println(Constants.ACK.name() + ":" + fromClient);
                    }
                    catch (NumberFormatException fe) {
                        //if client send non-numeric value, close the socket
                        closeSocketAndThread(2);
                    }
                } else if (fromClient.equals(Constants.PNG.name())) {
                    //it's ping check, ignore
                    continue;
                } else if (fromClient.equals(Constants.terminate.name()))  {
                    closeSocketAndThread(3);
                    TcpServer.shutdownSever();
                }
                else {
                    closeSocketAndThread(4);
                }
            }
        }
        catch (IOException ioe) {
            String msg = ioe.getMessage();
            if ( ioe instanceof SocketException && (msg.equals("Connection reset") ||
                    msg.equals("socket closed")) ) {
                closeSocketAndThread(5);
            }
            else {
                logger.error("caught", ioe);
            }
        }
    }


    /**
     * Help method to close down socket and this thread
     */
    private void closeSocketAndThread(int caller) {
        stopFlag = true;
        logger.info(caller + ". " + this.name + " exiting...");
        try {
            this.socketClientToServer.close();
        } catch (IOException e) {
            logger.error("during socket closing", e);
        }
        int cnt = --TcpServer.clientCount;
        logger.info(caller + ". Thread count in App: " + cnt + ". " + this.name);
    }

    private void processCounts(int value) {
        if ( allNumbers[value] == 0 ) { //it is new one
            TcpServer.uniqueCount++;
            TcpServer.totalUniqueCount++;
            allNumbers[value] = 1;
            blockingQueue.add(value);
        } else {
            TcpServer.duplicateCount++;
        }
        TcpServer.totalReadCount++;
    }

    public void setStopFlag(boolean stopFlag) {
        this.stopFlag = stopFlag;
    }
}
