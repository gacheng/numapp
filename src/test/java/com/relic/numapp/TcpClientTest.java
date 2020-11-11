package com.relic.numapp;

import com.relic.numapp.client.TcpClient;
import com.relic.numapp.client.TcpClientAuto;
import com.relic.numapp.client.TcpClientManual;
import com.relic.numapp.utils.Constants;
import org.junit.Test;

public class TcpClientTest {

    @Test
    public void testClientAuto() {
        TcpClientAuto.runIt();
    }

    @Test
    public void testClientManual() {
        //TcpClientManual.runIt();
    }

    @Test
    public void testFunctionality() {
        //start 2 clients
        TcpClient tcpClient1 = new TcpClient();
        tcpClient1.getUserInputs().add("923456789");
        tcpClient1.getUserInputs().add("933456789");
        tcpClient1.getUserInputs().add("923456788");
        tcpClient1.getUserInputs().add("009456789");
        tcpClient1.getUserInputs().add(Constants.stop.name());
        tcpClient1.start("localhost", 4000);

        TcpClient tcpClient2 = new TcpClient();
        tcpClient2.getUserInputs().add("923456789");
        tcpClient2.getUserInputs().add(Constants.stop.name());
        tcpClient2.start("localhost", 4000);
    }

    @Test
    public void testSendDuplicateNumber() {

    }

    @Test
    public void testSendUniqueNumber() {

    }

    @Test
    public void testTermination() {

    }

    @Test
    public void testLoad() {

    }
}
