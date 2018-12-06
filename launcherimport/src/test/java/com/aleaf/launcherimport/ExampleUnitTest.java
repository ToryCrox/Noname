package com.aleaf.launcherimport;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
        String s = ":string/";
        String ss = "com.app.aaa:string/tv_sss";
        System.out.println("string[]="+ Arrays.toString(ss.split(s)));
    }
}