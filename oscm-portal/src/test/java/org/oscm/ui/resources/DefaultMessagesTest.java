/* 
 *  Copyright FUJITSU LIMITED 2017
 **
 * 
 */
package org.oscm.ui.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Locale;

import org.junit.Test;

import org.oscm.stream.Streams;
import org.oscm.string.Strings;

/**
 * Test cases for DefaultMessages class
 * 
 * @author cheld
 * 
 */
public class DefaultMessagesTest {

    /**
     * Load properties for UI
     */
    @Test
    public void openUiMessages() throws Exception {
        InputStream stream = DefaultMessages.openUiMessages(Locale.ENGLISH);
        String result = Strings.toString(Streams.readFrom(stream));
        assertFalse(result.isEmpty());
    }

    /**
     * Load default locale if no translation exists for the requested locale
     */
    @Test
    public void openUiMessages_fallback() throws Exception {
        InputStream stream = DefaultMessages.openUiMessages(Locale.CHINA);
        String result = Strings.toString(Streams.readFrom(stream));
        assertFalse(result.isEmpty());
    }

    @Test
    public void openResource() throws Exception {
        InputStream stream = DefaultMessages
                .openResource("ExceptionMessages_en.properties");
        String result = Strings.toString(Streams.readFrom(stream));
        assertFalse(result.isEmpty());
    }

    @Test
    public void openResource_Empty() throws Exception {
        InputStream stream = DefaultMessages
                .openResource("ExceptionMessages_es.properties");
        assertNull(stream);
    }

    /**
     * Load properties for exceptions
     */
    @Test
    public void openExceptionMessages() throws Exception {
        InputStream stream = DefaultMessages
                .openExceptionMessages(Locale.ENGLISH);
        String result = Strings.toString(Streams.readFrom(stream));
        assertFalse(result.isEmpty());
    }

    /**
     * Load default locale if no translation exists for the requested locale
     */
    @Test
    public void openExceptionMessages_fallback() throws Exception {
        InputStream stream = DefaultMessages
                .openExceptionMessages(Locale.CHINA);
        String result = Strings.toString(Streams.readFrom(stream));
        assertFalse(result.isEmpty());
    }

    /**
     * Combine the keys of the properties for the UI and the properties for the
     * exceptions
     */
    @Test
    public void combinedKeys() throws Exception {
        DefaultMessages combinedMessages = new DefaultMessages(Locale.ENGLISH);
        assertTrue(combinedMessages.containsKey("button.abort"));
        assertTrue(combinedMessages
                .containsKey("ex.OperationNotPermittedException"));
    }

    /**
     * Combine the messages of the properties for the UI and the properties for
     * the exceptions
     */
    @Test
    public void combinedMessages() throws Exception {
        DefaultMessages combinedMessages = new DefaultMessages(Locale.ENGLISH);
        assertEquals("Abort", combinedMessages.getString("button.abort"));
        assertEquals("You are not allowed to perform this operation.",
                combinedMessages.getString("ex.OperationNotPermittedException"));
    }

    @Test
    public void dbMessagesResourcesTest() throws Exception{
        //given
        DefaultMessages defaultMessagesEN = new DefaultMessages_en();
        DefaultMessages defaultMessagesDE = new DefaultMessages_de();
        DefaultMessages defaultMessagesJA = new DefaultMessages_ja();
        //when
        final Enumeration<String> keysEN = defaultMessagesEN.getKeys();
        final Enumeration<String> keysDE = defaultMessagesDE.getKeys();
        final Enumeration<String> keysJA = defaultMessagesJA.getKeys();
        //then
        assertTrue(keysEN.nextElement() != null);
        assertTrue(keysDE.nextElement() != null);
        assertTrue(keysJA.nextElement() != null);
    }

}
