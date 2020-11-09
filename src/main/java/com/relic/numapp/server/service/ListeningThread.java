package com.relic.numapp.server.service;

import com.relic.numapp.server.ServerStarter;
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
import java.util.concurrent.BlockingQueue;

/**
 * A processing thread class that accepts message from client and persist it in the server
 */
public class ListeningThread implements Runnable {
    private final static Log logger = LogFactory.getLog(ListeningThread.class);

    private String name;
    private Socket socketClientToServer;
    private BlockingQueue<String> blockingQueue;

    public ListeningThread(String name, Socket socket, BlockingQueue<String> blockingQueue) {
        this.name = name;
        this.socketClientToServer = socket;
        this.blockingQueue = blockingQueue;
    }

    @Override
    public void run() {
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(socketClientToServer.getInputStream()));
            PrintWriter writer = new PrintWriter(socketClientToServer.getOutputStream(), true);
        ) {
            String fromClient = null;
            while (!socketClientToServer.isClosed()) {
                fromClient = reader.readLine();
                if ( logger.isDebugEnabled() ) {
                    logger.info("got from client(" + name + "): " + fromClient);
                }

                if (fromClient == null) {
                    //client socket mostly is closed
                    socketClientToServer.close();
                    logger.info("Current thread " + this.name + " exits");
                    int cnt = TcpServer.getClientCount().decrementAndGet();
                    logger.info("Current thread count: " + cnt + "");
                    return;
                } else if (fromClient.equalsIgnoreCase(Constants.PNG.name())) {
                    //it's ping check, ignore
                    continue;
                } else if (fromClient.equalsIgnoreCase(Constants.terminate.name()))  {
                    //instructed to terminate the process
                    System.exit(0);
                }

                //write to blockingqueue
                blockingQueue.add(fromClient);
                writer.println(Constants.ACK.name() +":"+fromClient);
            }
        }
        catch (IOException ioe) {
            String msg = ioe.getMessage();
            if ( ioe instanceof SocketException && msg.equals("Connection reset")) {
                logger.info("Current thread " + this.name + " exits");
                int cnt = TcpServer.getClientCount().decrementAndGet();
                logger.info("Current thread count: " + cnt);
            }
            else {
                logger.error("caught", ioe);
            }
        }
    }

    public void closeSocket() throws IOException {
        this.socketClientToServer.close();
    }
}
