package com.relic.numapp.server.service;

import com.relic.numapp.server.BackupServer;
import com.relic.numapp.utils.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A processing thread class that accepts message to backup all received numbers
 */
public class BackupThread implements Runnable {
    private final static Log logger = LogFactory.getLog(BackupThread.class);

    private String name;
    private Socket backupSocket;
    private FileWriter backupFileWriter;
    private AtomicBoolean stopFlag;


    public BackupThread(String name, Socket socket, FileWriter backupFileWriter) {
        this.name = name;
        this.backupSocket = socket;
        this.backupFileWriter = backupFileWriter;
        this.stopFlag = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(backupSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(backupSocket.getOutputStream(), true);
        ) {

            new Thread(()->{
                while ( !stopFlag.get() ) {
                    try {
                        writer.println(Constants.PNG.name());
                        if ( writer.checkError() ) {
                            closeSocketAndThread(0);
                        }
                        Thread.sleep(10000);  //every minute
                    }
                    catch (InterruptedException ie) {
                        logger.error("caught", ie);
                    }
                }
            }).start();

            while ( !stopFlag.get() ) {
                String fromClient = reader.readLine();
                //logger.info("BACKUP got from client(" + name + "): " + fromClient);

                if (fromClient == null) {
                    //client socket is closed
                    closeSocketAndThread(1);
                    return;
                } else if (fromClient.equals(Constants.terminate.name()))  {
                    closeSocketAndThread(2);
                    BackupServer.shutdown();
                } else if (fromClient.equals(Constants.PNG.name()))  {
                    continue;
                } else {
                    backupFileWriter.write(fromClient+System.lineSeparator());
                }
            }
        }
        catch (IOException ioe) {
            String msg = ioe.getMessage();
            if ( ioe instanceof SocketException && (msg.equals("Connection reset") ||
                    msg.equals("socket closed")) ) {
                closeSocketAndThread(3);
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
        stopFlag.set(true);
        //logger.info(caller + ". Backup " + this.name + " exiting...");
        try {
            this.backupSocket.close();
        } catch (IOException e) {
            logger.error("during socket closing", e);
        }
    }

}
