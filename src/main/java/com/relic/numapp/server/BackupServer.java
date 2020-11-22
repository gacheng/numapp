package com.relic.numapp.server;

import com.relic.numapp.server.service.BackupThread;
import com.relic.numapp.utils.FileUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * A server/process to backup all data accepted by the front server
 */
public class BackupServer {

    private final static Log logger = LogFactory.getLog(BackupServer.class);
    private int runInterval = 10*1000;  //10 seconds
    private String outDirectory;
    private String outputFile;

    public static int port = 4001; //default
    public static int maxClientThread = 5; //default
    private static FileWriter bkupFileWriter;
    private static boolean stopFlag;

    public BackupServer() {
        FileUtil.loadProperties();
        if (  FileUtil.appProperties.getProperty("listening_port_bkup") != null ) {
            port = Integer.parseInt(FileUtil.appProperties.getProperty("listening_port_bkup"));
        }
        if (  FileUtil.appProperties.getProperty("max_client_threads") != null ) {
            maxClientThread = Integer.parseInt(FileUtil.appProperties.getProperty("max_client_threads"));
        }

        outDirectory = FileUtil.appProperties.getProperty("output_directory");
        outputFile = FileUtil.appProperties.getProperty("output_file_bkup");
        FileUtil.createOutputDirectory(outDirectory);
        String fullFileName = outDirectory + "/" + outputFile;
        try {
            File file = new File(fullFileName);
            bkupFileWriter = new FileWriter(file, true);
        } catch (IOException e) {
            logger.error("failed to create the output file for backup: " + fullFileName);
        }
    }

    public void start() {
        /***********************************************************************************************************
         * Start a thread for flush out to the backup file every 10 seconds
         ***********************************************************************************************************/
        new Thread(()->{
            while ( !stopFlag ) {
                try {
                    Thread.sleep(runInterval);
                    bkupFileWriter.flush();
                    System.out.println("BackServer running...");
                } catch (InterruptedException | IOException e) {
                    logger.error("caught when flushing to backup file:", e);
                }
            }
        }).start();

        /***********************************************************************************************************
         * Start the server listening socket and accept message from client, each client is in its own thread
         * these threads acting as producer fro BlockingQueue
         ***********************************************************************************************************/
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("BackupServer is listening on port " + port);
            ExecutorService threadExecutor = Executors.newFixedThreadPool((int) (maxClientThread*1.5));

            int sequence = 0;
            while (!stopFlag) {
                Socket socket = serverSocket.accept();
                sequence++;
                threadExecutor.execute(new BackupThread("Thread-Backup-"+sequence, socket, bkupFileWriter));
            }

        } catch (IOException ex) {
            logger.error("BackupServer exception: ",  ex);
        }
    }

    /**
     * This method is to shutdown the application gracefully by checking the server
     */
    public static void shutdown() {
        stopFlag = true;
        try {
            bkupFileWriter.close();
            System.exit(0);
        } catch (IOException e) {
            logger.error("caught", e);
        }
    }

    /**
     * For start up backserver in separately in different machine
     * @param args
     */
    public static void  main(String... args) {
        BackupServer backupServer = new BackupServer();
        backupServer.start();
    }
}
