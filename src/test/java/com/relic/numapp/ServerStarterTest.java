/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.relic.numapp;

import com.relic.numapp.server.ServerStarter;
import com.relic.numapp.utils.Constants;
import org.junit.Test;
import static org.junit.Assert.*;

public class ServerStarterTest {

    @Test
    public void testEnumValue1() {
        assertEquals("Acknowledge", Constants.ACK.toString());
    }

    @Test
    public void testEnumValue2() {
        assertEquals("ACK", Constants.ACK.name());
    }

    @Test
    public void testStart() {

    }
}