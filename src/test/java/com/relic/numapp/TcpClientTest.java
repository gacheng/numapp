package com.relic.numapp;

import com.relic.numapp.client.TcpClient;
import com.relic.numapp.client.TcpClientAuto;
import com.relic.numapp.utils.Constants;
import org.junit.Test;


public class TcpClientTest {
    String hostname = "localhost";
    int port = 4000;

    @Test
    public void testClientAuto() {
        TcpClientAuto.run100();
    }

    @Test
    public void testClientManual() {
        //TcpClientManual.runIt();
    }

    @Test
    /**
     * Run this test only once when the server is fresh started
     */
    public void testFunctionality() {
        //start 2 clients
        TcpClient tcpClient1 = new TcpClient();
        tcpClient1.getUserInputs().add("923456789");
        tcpClient1.getUserInputs().add("933456789");
        tcpClient1.getUserInputs().add("923456788");
        tcpClient1.getUserInputs().add("009456789");
        tcpClient1.getUserInputs().add(Constants.stop.name());
        tcpClient1.start(hostname, port);

        TcpClient tcpClient2 = new TcpClient();
        tcpClient2.getUserInputs().add("923456789");
        tcpClient2.getUserInputs().add(Constants.stop.name());
        tcpClient2.start(hostname, port);
    }

    @Test
    public void testSendDuplicateNumber() {
        TcpClient tcpClient = new TcpClient();
        tcpClient.getUserInputs().add("623456789");
        tcpClient.getUserInputs().add("623456789");
        tcpClient.getUserInputs().add("523456789");
        tcpClient.getUserInputs().add(Constants.stop.name());
        tcpClient.start(hostname, port);
    }

    @Test
    public void testLeadingZeros() {
        TcpClient tcpClient = new TcpClient();
        tcpClient.getUserInputs().add("000006789");
        tcpClient.getUserInputs().add(Constants.stop.name());
        tcpClient.start(hostname, port);
    }

    @Test
    public void testSendLess9Digits() {
        TcpClient tcpClient = new TcpClient();
        tcpClient.getUserInputs().add("56789");
        tcpClient.start(hostname, port);
    }

    @Test
    public void testSendNonDigits() {
        TcpClient tcpClient = new TcpClient();
        tcpClient.getUserInputs().add("xyz56789");
        tcpClient.start(hostname, port);
    }

    @Test
    public void testTermination() {
        TcpClient tcpClient = new TcpClient();
        tcpClient.getUserInputs().add(Constants.terminate.name());
        tcpClient.start(hostname, port);
    }

    @Test
    //Comment out since it will hang other tests
    public void testLingeringThreadInServer() {
        //by not sending wrong or invalid value, thus socket is kept open
        TcpClient tcpClient = new TcpClient();
        tcpClient.getUserInputs().add("100006789");
        tcpClient.start(hostname, port);
    }

}
