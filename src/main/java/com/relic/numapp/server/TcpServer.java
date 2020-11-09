package com.relic.numapp.server;

import com.relic.numapp.server.service.ListeningThread;
import com.relic.numapp.server.service.MergeToFileProcessor;
import com.relic.numapp.server.service.PersistThread;
import com.relic.numapp.utils.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A backbone class that represents the server.  It listening for client connection request and then spin out a
 * separate thread for each client
 */
public class TcpServer {

    private final static Log logger = LogFactory.getLog(TcpServer.class);
    private int runInterval = 10*1000;  //10 seconds
    private String outDirectory;
    private String outputFile;

    public static int port = 4000; //default
    public static int maxClientThread = 5; //default
    public static int maxPersistThread = 10; //default

    private static AtomicInteger clientCount = new AtomicInteger(0);      //a count for the current threads in the application
    private static AtomicInteger uniqueCount = new AtomicInteger(0);
    private static AtomicInteger duplicateCount = new AtomicInteger(0);
    private static AtomicInteger totalCount = new AtomicInteger(0);
    private static BlockingQueue<String> blockingQueue;

    public TcpServer() {
        outDirectory = ServerStarter.appProperties.getProperty("output_directory");
        outputFile = ServerStarter.appProperties.getProperty("output_file");
        if (  ServerStarter.appProperties.getProperty("listening_port") != null ) {
            port = Integer.parseInt(ServerStarter.appProperties.getProperty("listening_port"));
        }
        if (  ServerStarter.appProperties.getProperty("max_client_threads") != null ) {
            maxClientThread = Integer.parseInt(ServerStarter.appProperties.getProperty("max_client_threads"));
        }
        if (  ServerStarter.appProperties.getProperty("max_persist_threads") != null ) {
            maxClientThread = Integer.parseInt(ServerStarter.appProperties.getProperty("max_persist_threads"));
        }

        blockingQueue = new LinkedBlockingDeque<String>();
        cleanUpDirectories();
    }

    public void start() {
        /***********************************************************************************************************
         * Start a thread for writing out report to console and append entries to the output file every 10 seconds
         ***********************************************************************************************************/
        new Thread(()->{
            MergeToFileProcessor processor = new MergeToFileProcessor(outDirectory, outputFile);
            logger.info("merge thread running: " + Thread.currentThread().getName());
            while ( true ) {
                try {
                    Thread.sleep(runInterval);
                    processor.process();
                    System.out.println("Received " + uniqueCount.get() + " unique numbers, " + duplicateCount.get() +
                            " duplicates. Unique Total: " + totalCount.get());
                    uniqueCount.set(0);
                    duplicateCount.set(0);
                } catch (InterruptedException e) {
                    logger.error("caught when merging entries:", e);
                }
            }
        }).start();

        /***********************************************************************************************************
         * Start thread for persisting data to file directory, acting as consumer for BlockingQueue
         ***********************************************************************************************************/
        ExecutorService persistExecutor = Executors.newFixedThreadPool(maxPersistThread);
        for (int i=0; i<maxPersistThread; i++) {
            persistExecutor.execute(new PersistThread(blockingQueue, outDirectory));
        }

        /***********************************************************************************************************
         * Start the server listening socket and accept message from client, each client is in its own thread
         * these threads acting as producer fro BlockingQueue
         ***********************************************************************************************************/
        int clientSequence = 0;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("TcpServer is listening on port " + port);
            ExecutorService threadExecutor = Executors.newFixedThreadPool(maxClientThread);

            while (true) {
                Socket socket = serverSocket.accept();
                int cnt = clientCount.incrementAndGet();
                clientSequence++;
                logger.info("New client " + socket.getInetAddress() + " connected; Sequence: " + clientSequence +
                        "; Current thread count: " + cnt);

                threadExecutor.execute(new ListeningThread("Thread-"+clientSequence, socket, blockingQueue));
            }

        } catch (IOException ex) {
            logger.error("TcpServer exception: ",  ex);
        }
    }

    private void cleanUpDirectories() {
        String fullFileName = outDirectory + "/" + outputFile;
        try {
            //cleaning up complete and processing directory
            File file = new File(outDirectory + "/" + Constants.all.name());
            FileUtils.forceMkdir(file);
            FileUtils.cleanDirectory(file);

            file = new File(outDirectory + "/" + Constants.processing.name());
            FileUtils.forceMkdir(file);
            FileUtils.cleanDirectory(file);

            //delete if output collect.log exits
            file = new File(fullFileName);
            if ( file.exists() && file.isFile() ) {
                file.delete();
            }
        } catch (IOException e) {
            logger.error("failed to create the output file: " + fullFileName);
        }
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

    public static AtomicInteger getTotalCount() {
        return totalCount;
    }
}
