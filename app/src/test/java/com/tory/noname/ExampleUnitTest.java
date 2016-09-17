package com.tory.noname;

import org.junit.Test;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void formatNumber() throws Exception {
        System.out.println(String.format("%.1f万",1.254f));
    }
}