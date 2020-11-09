package com.relic.numapp.server;

import com.relic.numapp.server.service.ListeningThread;
import com.relic.numapp.server.service.MergeToFileProcessor;
import com.relic.numapp.server.service.PersistThread;
import com.relic.numapp.utils.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A backbone class that represents the server.  It listening for client connection request and then spin out a
 * separate thread for each client
 */
public class BackupServer {

    private final static Log logger = LogFactory.getLog(BackupServer.class);
    private int runInterval = 10*1000;  //10 seconds
    private String outDirectory;
    private String outputFile;

    public static int port = 4001; //default
    public static int maxPersistThread = 2;
    private BlockingQueue<String> blockingQueue = new LinkedBlockingDeque<String>();

    public BackupServer() {
        outDirectory = ServerStarter.appProperties.getProperty("output_directory");
        outputFile = ServerStarter.appProperties.getProperty("output_file_bkup");
        if (  ServerStarter.appProperties.getProperty("listening_port_bkup") != null ) {
            port = Integer.parseInt(ServerStarter.appProperties.getProperty("listening_port_bkup"));
        }
    }

    public void start() {
        /***********************************************************************************************************
         * Start thread for persisting data to file directory, read from BlockingQueue
         ***********************************************************************************************************/
        ExecutorService persistExecutor = Executors.newFixedThreadPool(maxPersistThread);
        for (int i=0; i<maxPersistThread; i++) {
            persistExecutor.execute(new PersistThread(blockingQueue, outDirectory));
        }

        /***********************************************************************************************************
         * Start the server listening socket and accept message from client, each client is in its own thread
         * these threads acting as producer fro BlockingQueue
         ***********************************************************************************************************/
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("BackupServer is listening on port " + port);
            ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
            Socket socket = serverSocket.accept();
            threadExecutor.execute(new ListeningThread("Thread-Backup", socket, blockingQueue));
        } catch (IOException ex) {
            logger.error("BackupServer exception: ",  ex);
        }
    }

}
