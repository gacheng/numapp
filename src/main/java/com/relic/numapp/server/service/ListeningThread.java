package com.relic.numapp.server.service;

import com.relic.numapp.server.TcpServer;
import com.relic.numapp.utils.Constants;
import com.relic.numapp.utils.FileUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A processing thread class that accepts message from client and persist it in the server
 */
public class ListeningThread implements Runnable {
    private final static Log logger = LogFactory.getLog(ListeningThread.class);

    private String name;
    private Socket socketClientToServer;
    private AtomicBoolean stopFlag;

    private static BlockingQueue<String> blockingQueue;
    private static Map<String, String> allNumbers;
    private static AtomicInteger uniqueCount;
    private static AtomicInteger duplicateCount;
    private static AtomicInteger totalUniqueCount;
    private static AtomicInteger totalReadCount;
    private static AtomicInteger clientCount;

    private static String backupHostname;
    private static int backupPort;
    private static boolean backupEnabled;

    public ListeningThread(String name, Socket socket, BlockingQueue<String> blockingQueue) {
        this.name = name;
        this.socketClientToServer = socket;
        this.blockingQueue = blockingQueue;
        this.stopFlag = new AtomicBoolean(false);

        allNumbers = TcpServer.getAllNumbers();
        uniqueCount = TcpServer.getUniqueCount();
        duplicateCount = TcpServer.getDuplicateCount();
        totalUniqueCount = TcpServer.getTotalUniqueCount();
        totalReadCount = TcpServer.getTotalReadCount();
        clientCount = TcpServer.getClientCount();
        clientCount.incrementAndGet();

        if (  backupHostname == null && FileUtil.appProperties.getProperty("enable_bkup") != null ) {
            backupEnabled = Boolean.parseBoolean(FileUtil.appProperties.getProperty("enable_bkup"));
        }
        if ( !backupEnabled )
            return;

        if (  backupHostname == null && FileUtil.appProperties.getProperty("hostname_bkup") != null ) {
            backupHostname = FileUtil.appProperties.getProperty("hostname_bkup");
        }
        if (  backupPort == 0 && FileUtil.appProperties.getProperty("listening_port_bkup") != null ) {
            backupPort = Integer.parseInt(FileUtil.appProperties.getProperty("listening_port_bkup"));
        }
    }

    @Override
    public void run() {
        try (
                //reader and writer for TcpClient
                BufferedReader reader = new BufferedReader(new InputStreamReader(socketClientToServer.getInputStream()));
                PrintWriter writer = new PrintWriter(socketClientToServer.getOutputStream(), true);

                //writer for backup server
                Socket bkupSocket = backupEnabled ? new Socket(backupHostname, backupPort) : null;
                PrintWriter backupWriter = backupEnabled ? new PrintWriter(bkupSocket.getOutputStream(), true) : null;
        ) {
            //start a new thread to check if the socket from client alive, if not, closing this socket and thread
            //this is necessary since this thread needs to be closed when client is not there anymore so we can maximize
            //the reuse of the usage of 5 threads
            new Thread(()->{
                while ( !stopFlag.get() ) {
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

            while ( !stopFlag.get() ) {
                String fromClient = reader.readLine();
                //logger.info("$$$$$$$$$$$ got from client(" + name + "): " + fromClient);

                if (fromClient == null) {
                    //client socket is closed
                    closeSocketAndThread(1);
                    return;
                } else if (fromClient.length() == 9 ) {
                    try {
                        if ( backupEnabled ) backupWriter.println(fromClient);
                        Integer.parseInt(fromClient);
                        processAndCounts(fromClient); //write to blockingqueue
                        writer.println(Constants.ACK.name() + ":" + fromClient);
                    }
                    catch (NumberFormatException fe) {
                        closeSocketAndThread(2);
                        if (fromClient.equals(Constants.terminate.name()))  {
                            TcpServer.sutdown();
                        }
                    }
                } else if (fromClient.equals(Constants.PNG.name())) {
                    //it's ping check, ignore
                    continue;
                }
                else {
                    closeSocketAndThread(3);
                    return;
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
    private  void closeSocketAndThread(int caller) {
        stopFlag.set(true);
        try {
            socketClientToServer.close();
        } catch (IOException e) {
            logger.error("during socket closing", e);
        }
        int cnt = clientCount.decrementAndGet();
        //logger.info(caller + ". " + this.name + " exiting...; Thread count in App: " + cnt);
    }

    private static void processAndCounts(String value) {
        String already = allNumbers.put(value, value);
        if ( already == null ) { //it is new one
            blockingQueue.add(value);
            uniqueCount.incrementAndGet();
            totalUniqueCount.incrementAndGet();
        } else {
            duplicateCount.incrementAndGet();
        }
        totalReadCount.incrementAndGet();
    }

}
