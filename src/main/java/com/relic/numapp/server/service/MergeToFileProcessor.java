package com.relic.numapp.server.service;

import com.relic.numapp.server.TcpServer;
import com.relic.numapp.utils.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class MergeToFileProcessor {
    private final static Log logger = LogFactory.getLog(MergeToFileProcessor.class);

    private String outDirectory;
    private String outputFile;
    FileWriter targetFileWriter;
    BlockingQueue<String > writingQueue;
    boolean stopFlag;

    public MergeToFileProcessor(String outDirectory, String outputFile) {
        this.outDirectory = outDirectory;
        this.outputFile = outputFile;
        this.writingQueue = TcpServer.getReadingQueue();
        this.stopFlag = false;

        String fullFileName = outDirectory + "/" + outputFile;
        try {
            File file = new File(fullFileName);
            targetFileWriter = new FileWriter(file);
        } catch (IOException e) {
            logger.error("failed to create the output file: " + fullFileName);
        }
    }

    public void process() {
        try {
            //a separate thread to flush writing to disk
            new Thread(()-> {
                while ( !stopFlag ) {
                    try {
                        Thread.sleep(2000);
                        targetFileWriter.flush();
                        //logger.info("=========================== writing going... queue size: " + writingQueue.size());
                    } catch (InterruptedException | IOException e) {
                        logger.error("caught", e);
                    }
                }
            }).start();

            while ( !stopFlag ) {
                String value = writingQueue.take();
                if ( value != null ) {
                    targetFileWriter.write(value + System.lineSeparator());
                }
            }

        } catch (InterruptedException | IOException e) {
            logger.error("caught during write to the file: " + outputFile);
        }
    }

    public void flushWriting() {
        try {
            targetFileWriter.flush();
        } catch (IOException e) {
            logger.error("caught during file flush", e);
        }
    }
}
