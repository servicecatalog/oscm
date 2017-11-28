/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorsvc.client;

import static org.junit.Assert.assertEquals;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

public class OperatorClientTest {

    private static final String TEST_PROPERTIES = "./resources/test.properties";
    private CharArrayWriter err;
    private OperatorClient client;

    @Before
    public void setup() throws Exception {
        err = new CharArrayWriter();
        client = new OperatorClient(null, null, new PrintWriter(err, true));
    }

    @Test
    public void testReadArgumentsFromPropertyFile() throws IOException {
        final Map<String, String> result = new HashMap<String, String>();
        OperatorClient.readArgumentsFromPropertyFile(result, TEST_PROPERTIES);

        final Map<String, String> expected = new HashMap<String, String>();
        expected.put("key1", "aaa");
        expected.put("key2", "bbb");
        expected.put("key3", "ccc");
        assertEquals(expected, result);
    }

    @Test
    public void testReadArgumentsEmpty() throws IOException {
        final List<String> empty = Collections.emptyList();
        final Map<String, String> result = OperatorClient.readArguments(empty
                .iterator());
        assertEquals(Collections.emptyMap(), result);
    }

    @Test
    public void testReadArguments() throws IOException {
        final List<String> args = Arrays.asList("a=123", "b=", "c=2*2=4");
        final Map<String, String> result = OperatorClient.readArguments(args
                .iterator());

        final Map<String, String> expected = new HashMap<String, String>();
        expected.put("a", "123");
        expected.put("b", "");
        expected.put("c", "2*2=4");
        assertEquals(expected, result);
    }

    @Test
    public void testReadArgumentsWithFile() throws IOException {
        final List<String> args = Arrays.asList("key1=AAA", "-f",
                TEST_PROPERTIES, "key2=BBB");
        final Map<String, String> result = OperatorClient.readArguments(args
                .iterator());

        final Map<String, String> expected = new HashMap<String, String>();
        expected.put("key1", "aaa");
        expected.put("key2", "BBB");
        expected.put("key3", "ccc");
        assertEquals(expected, result);
    }

    @Test(expected = NoSuchElementException.class)
    public void testReadArgumentsNegative() throws IOException {
        final List<String> args = Arrays.asList("noequals");
        OperatorClient.readArguments(args.iterator());
    }

    @Test
    public void testHandleException() {
        final CharArrayWriter err = new CharArrayWriter();
        final OperatorClient client = new OperatorClient(null, null,
                new PrintWriter(err, true));

        final Exception cause1 = new Exception("Cause 1");
        final Exception cause2 = new Exception("Cause 2", cause1);
        final Exception exception = new Exception("Bumm", cause2);
        client.handleException(exception);

        final String expected = "Bumm\ncaused by: Cause 2\ncaused by: Cause 1";
        assertEquals(String.format(expected), err.toString());
    }

    @Test
    public void testGetMessage1() {
        Exception exception = new Exception("Message");
        client.handleException(exception);
        assertEquals("Message", err.toString());
    }

    @Test
    public void testGetMessage2() {
        Exception exception = new Exception();
        client.handleException(exception);
        assertEquals("java.lang.Exception", err.toString());
    }

    @Test
    public void testGetMessage3() {
        Exception exception = new Exception("   ");
        client.handleException(exception);
        assertEquals("java.lang.Exception:    ", err.toString());
    }

}
