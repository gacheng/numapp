package com.relic.numapp.server;

import com.relic.numapp.server.service.ListeningThread;
import com.relic.numapp.server.service.MergeToFileProcessor;
import com.relic.numapp.utils.Constants;
import com.relic.numapp.utils.FileUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A backbone class that represents the server.  It listening for client connection request and then spin out a
 * separate thread for each client
 */
public class TcpServer {

    private final static Log logger = LogFactory.getLog(TcpServer.class);
    private final static int MAX_NUMBER = 9999999;

    private int runInterval = 10*1000;  //10 seconds

    public static int port = 4000; //default
    public static int maxClientThread = 5; //default

    //a count for the current threads in the application
    private static AtomicInteger clientCount = new AtomicInteger(0);
    private static AtomicInteger uniqueCount = new AtomicInteger(0);
    private static AtomicInteger duplicateCount = new AtomicInteger(0);
    private static AtomicInteger totalUniqueCount = new AtomicInteger(0);
    private static AtomicInteger totalReadCount = new AtomicInteger(0);

    String outDirectory;
    String outputFile;
    private static BlockingQueue<String> readingQueue;
    private static Map<String, String> allNumbers;
    private static boolean stopFlag;


    public TcpServer() {
        checkBackupServer();

        if (  FileUtil.appProperties.getProperty("listening_port") != null ) {
            port = Integer.parseInt(FileUtil.appProperties.getProperty("listening_port"));
        }
        if (  FileUtil.appProperties.getProperty("max_client_threads") != null ) {
            maxClientThread = Integer.parseInt(FileUtil.appProperties.getProperty("max_client_threads"));
        }

        outDirectory = FileUtil.appProperties.getProperty("output_directory");
        outputFile = FileUtil.appProperties.getProperty("output_file");
        FileUtil.cleanUpOutputDirectory(outDirectory, outputFile);

        stopFlag = false;
        readingQueue = new LinkedBlockingQueue<String>();
        allNumbers = Collections.synchronizedMap(new HashMap<String, String>());
    }

    public void start() {
        /***********************************************************************************************************
         * Start a thread for writing out report to console and append entries to the output file every 10 seconds
         ***********************************************************************************************************/
        new Thread(()->{
            while ( !stopFlag ) {
                try {
                    Thread.sleep(runInterval);
                    System.out.println("Received " + uniqueCount.get() + " unique numbers, " + duplicateCount.get() +
                            " duplicates. Unique Total: " +  allNumbers.size());
                    //logger.info("******* readingQueue: " + readingQueue.size() + ", thread count: "
                    // + clientCount + " Map Size: " + allNumbers.size() + ", Read Total: " + totalReadCount.get() + "******");
                    uniqueCount.set(0);
                    duplicateCount.set(0);
                } catch (InterruptedException e) {
                    logger.error("caught when merging entries:", e);
                }
            }
        }).start();

        /***********************************************************************************************************
         * Start a thread for writing out the numbers in the blockingqueue to the output file
         ***********************************************************************************************************/
        new Thread(()->{
            MergeToFileProcessor processor = new MergeToFileProcessor(outDirectory, outputFile);
            processor.process();
        }).start();

        /***********************************************************************************************************
         * Start the server listening socket and accept message from client, each client is in its own thread
         * these threads acting as producer fro BlockingQueue
         ***********************************************************************************************************/
        int clientSequence = 0;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("TcpServer is listening on port " + port);
            ExecutorService threadExecutor = Executors.newFixedThreadPool(maxClientThread);

            while (!stopFlag) {
                Socket socket = serverSocket.accept();
                clientSequence++;
                //logger.info("New client " + socket.getInetAddress() + " connected; Sequence: " + clientSequence);

                threadExecutor.execute(new ListeningThread("Thread-"+clientSequence, socket, readingQueue));
            }

        } catch (IOException ex) {
            logger.error("TcpServer exception: ",  ex);
        }

    }

    /**
     * This method is to shutdown the application gracefully by checking the server
     */
    public static void sutdown() {
        //waiting for blockqueue size to be zero (all remaining items in the queue are processed or nothing to process)
        stopFlag = true;
        while ( readingQueue.size() == 0 ) {
            System.exit(0);
        }
    }

    public static void checkBackupServer() {
        String backupHostname = null;
        boolean backupEnabled = false;
        int backupPort = 0;
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

        try {
            Socket bkupSocket = backupEnabled ? new Socket(backupHostname, backupPort) : null;
            PrintWriter backupWriter = backupEnabled ? new PrintWriter(bkupSocket.getOutputStream(), true) : null;
            backupWriter.println(Constants.PNG.name());
        }
        catch (Exception e) {
            logger.error("Backup Server not reachable or not started");
            System.exit(1);
        }
    }

    public static BlockingQueue<String> getReadingQueue() {
        return readingQueue;
    }

    public static Map<String, String> getAllNumbers() {
        return allNumbers;
    }

    public static AtomicInteger getClientCount() {
        return clientCount;
    }

    public static AtomicInteger getUniqueCount() {
        return uniqueCount;
    }

    public static AtomicInteger getDuplicateCount() {
        return duplicateCount;
    }

    public static AtomicInteger getTotalUniqueCount() {
        return totalUniqueCount;
    }

    public static AtomicInteger getTotalReadCount() {
        return totalReadCount;
    }
}
