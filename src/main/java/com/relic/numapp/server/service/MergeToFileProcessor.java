package com.relic.numapp.server.service;

import com.relic.numapp.utils.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MergeToFileProcessor {
    private final static Log logger = LogFactory.getLog(MergeToFileProcessor.class);

    private String outDirectory;
    private String outputFile;
    FileWriter targetFileWriter;

    public MergeToFileProcessor(String outDirectory, String outputFile) {
        this.outDirectory = outDirectory;
        this.outputFile = outputFile;

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
            String processingDirName = outDirectory + "/" + Constants.processing.name();
            File processingDir = new File(processingDirName);
            File[] allFiles = processingDir.listFiles();
            if ( allFiles == null ) {
                logger.error("Direct " + processingDirName + " does not exist");
                return;
            }

            for (File oneFile : allFiles) {
                targetFileWriter.write(oneFile.getName() + System.lineSeparator());
                boolean success = oneFile.delete();
                if ( !success ) {
                    logger.error("Failed to delete file: " + oneFile.getName());
                }
            }
            targetFileWriter.flush();

        } catch (IOException e) {
            logger.error("caught during merge to the file: " + outputFile);
        }
    }
}
