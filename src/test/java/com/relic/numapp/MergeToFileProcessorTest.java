package com.relic.numapp;

import com.relic.numapp.server.service.MergeToFileProcessor;
import org.junit.Test;

public class MergeToFileProcessorTest {
    @Test
    public void testProcess() {
        MergeToFileProcessor processor = new MergeToFileProcessor("./output", "collect-test.log");
        processor.process();
    }
}
