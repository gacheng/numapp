/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.relic.numapp.server;

import com.relic.numapp.utils.FileUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A wrapper class to start the server.  It first loads the properties and then start the server
 */
public class ServerStarter {

    public static void main(String[] args) {
        System.out.println("ServerStarter Started");
        FileUtil.loadProperties();
        TcpServer tcpServer = new TcpServer();
        tcpServer.start();
    }


}
