package com.relic.numapp.server.service;

import com.relic.numapp.server.TcpServer;
import com.relic.numapp.utils.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class PersistThread implements Runnable {
    private final static Log logger = LogFactory.getLog(PersistThread.class);

    private BlockingQueue<String> blockingQueue;
    private String outDirectory;

    public PersistThread(BlockingQueue<String> blockingQueue, String outDirectory) {
        this.blockingQueue = blockingQueue;
        this.outDirectory = outDirectory;

        try {
            File completeFile = new File(outDirectory + "/" + Constants.all.name());
            FileUtils.forceMkdir(completeFile);
            File processingFile = new File(outDirectory + "/" + Constants.processing.name());
            FileUtils.forceMkdir(processingFile);
        } catch ( IOException e) {
            logger.error("caught", e);
        }
    }

    @Override
    public void run() {
        try {
            while ( true ) {
                String value = blockingQueue.take();
                if (value != null) {
                    File completeFile = new File(outDirectory + "/" + Constants.all.name() + "/" + value);
                    File processingFile = new File(outDirectory + "/" + Constants.processing.name() + "/" + value);

                    if (completeFile.createNewFile()) {
                        processingFile.createNewFile(); //create the same one in the processing directory
                        int uniqueCnt = TcpServer.getUniqueCount().incrementAndGet();
                        int totalCnt = TcpServer.getTotalCount().incrementAndGet();
                        logger.debug("unique count: " + uniqueCnt + " total count: " + totalCnt);
                    } else {
                        int dup = TcpServer.getDuplicateCount().incrementAndGet();
                        logger.debug("duplicate count: " + dup);
                    }
                }
            }

        } catch (InterruptedException | IOException e) {
            logger.error("caught", e);
        }

    }

}
