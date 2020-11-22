package com.relic.numapp.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FileUtil {
    private final static Log logger = LogFactory.getLog(FileUtil.class);

    private final static String PROP_FILE_NAME = "app.properties";
    public static Properties appProperties = new Properties();

    public static void loadProperties() {
        try (InputStream input = FileUtil.class.getClassLoader().getResourceAsStream(PROP_FILE_NAME)) {
            if (input == null) {
                logger.error("Sorry, unable to find " + PROP_FILE_NAME);
                return;
            }
            // load a properties file
            appProperties.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Help method to clean up output directory
     */
    public static void cleanUpOutputDirectory(String outDirectory, String outputFile) {

        String fullFileName = outDirectory + "/" + outputFile;
        try {
            //cleaning up complete and processing directory
            File file = new File(outDirectory);
            FileUtils.forceMkdir(file);

            //delete if output numbers.log exits
            file = new File(fullFileName);
            if ( file.exists() && file.isFile() ) {
                file.delete();
            }
        } catch (IOException e) {
            logger.error("failed to create the output file: " + fullFileName);
        }
    }

    /**
     * make sure output directory is created if it is not
     * @param outDirectory
     */
    public static void createOutputDirectory(String outDirectory) {
        String fullFileName = outDirectory;
        try {
            File file = new File(outDirectory);
            if ( !file.exists() ) {
                FileUtils.forceMkdir(file);
            }

        } catch (IOException e) {
            logger.error("failed to create the output file: " + fullFileName);
        }
    }

}
