package com.varteq.catslovers;

import com.varteq.catslovers.utils.TimeUtils;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    private String TAG = ExampleUnitTest.class.getSimpleName();

    @Test
    public void testTimeUtils() throws Exception {
        Date date = new Date(System.currentTimeMillis() / 1000L * 1000L);
        int sec = TimeUtils.getUtcFromLocal(date.getTime());
        System.out.print(sec);
        Date date1 = TimeUtils.getLocalDateFromUtc(sec);
        assertEquals(date.getTime(), date1.getTime());
    }
}