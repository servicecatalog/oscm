/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.string;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class StringsTest {

    /**
     * Constructor tested for coverage.
     */
    @Test
    public void testConstructor() {
        new Strings();
    }

    /**
     * Shorten a long text to 20 chars
     */
    @Test
    public void shortenText() {
        String longText = "hello world hello world hello world hello world hello world hello world hello world hello world hello world";
        String shortenedText = Strings.shortenText(longText, 20);
        assertTrue(shortenedText.length() == 20);
    }

    /**
     * Do not shorten short text
     */
    @Test
    public void shortenText_negativ() {
        String shortText = "hello";
        String shortenedText = Strings.shortenText(shortText, 20);
        assertTrue(shortenedText.length() == shortText.length());
    }

    /**
     * Assert no exception for null
     */
    @Test
    public void shortenText_null() {
        String nullText = null;
        String shortenedText = Strings.shortenText(nullText, 20);
        assertNull(shortenedText);
    }

    /**
     * Leading and tailing spaces are not taken into account
     */
    @Test
    public void shortenText_trimm() {
        String textWithLeadingSpaces = "    hello";
        String shortenedText = Strings.shortenText(textWithLeadingSpaces, 5);
        assertEquals("hello", shortenedText);
    }

    /**
     * The maximum string length matches the given string. No shorting
     */
    @Test
    public void shortenText_exactLength() {
        String shortenedText = Strings.shortenText("hello", 5);
        assertEquals("hello", shortenedText);
    }

    /**
     * Convert a string to bytes and back again to a string
     * 
     * @throws Exception
     */
    @Test
    public void testToStringAndToBytes() throws Exception {
        byte[] stringAsBytes = Strings.toBytes("someString");
        assertEquals("someString", Strings.toString(stringAsBytes));
    }

    /**
     * Exception is thrown if UTF-8 does not exists.
     */
    @Test(expected = IllegalArgumentException.class)
    public void doToString_utf8NotExisting() {
        Strings.doToString("asdf".getBytes(), "utf8-non-existing");
    }

    /**
     * Exception is thrown if UTF-8 does not exists.
     */
    @Test(expected = IllegalArgumentException.class)
    public void doToBytes_utf8NotExisting() {
        Strings.doToBytes("asdf", "utf8-non-existing");
    }

    @Test
    public void isEmpty_Null() {
        assertTrue(Strings.isEmpty(null));
    }

    @Test
    public void isEmpty_Empty() {
        assertTrue(Strings.isEmpty("   "));
    }

    @Test
    public void isEmpty_NonEmpty() {
        assertFalse(Strings.isEmpty(" abc "));
    }

    @Test
    public void join_NullCollection() {
        assertNull(Strings.join(null, null));
    }

    @Test
    public void join_NullSeparator() {
        assertEquals("ab", Strings.join(Arrays.asList("a", "b"), null));
    }

    @Test
    public void join() {
        assertEquals("a,b", Strings.join(Arrays.asList("a", "b"), ","));
    }

    @Test
    public void join_WithSpace() {
        assertEquals("a, b", Strings.join(Arrays.asList("a", "b"), ", "));
    }

    @Test
    public void areStringsEqual_BothNull() {
        assertTrue(Strings.areStringsEqual(null, null));
    }

    @Test
    public void areStringsEqual_OneNull() {
        assertFalse(Strings.areStringsEqual(null, ""));
    }

    @Test
    public void areStringsEqual_NotEqual() {
        assertFalse(Strings.areStringsEqual("a", ""));
    }

    @Test
    public void areStringsEqual_SameText() {
        assertTrue(Strings.areStringsEqual("abc", "abc"));
    }

    @Test
    public void firstCharToUppercase_null() {
        assertNull(Strings.firstCharToUppercase(null));
    }

    @Test
    public void firstCharToUppercase_empty() {
        assertEquals("", Strings.firstCharToUppercase(""));
    }

    @Test
    public void firstCharToUppercase_singleChar() {
        assertEquals("A", Strings.firstCharToUppercase("a"));
    }

    @Test
    public void firstCharToUppercase() {
        assertEquals("Word", Strings.firstCharToUppercase("word"));
    }

    @Test
    public void firstCharToUppercase_alreadyUppercase() {
        assertEquals("Word", Strings.firstCharToUppercase("Word"));
    }

    @Test
    public void firstCharToUppercase_nonLetter() {
        assertEquals("#word", Strings.firstCharToUppercase("#word"));
    }

    @Test
    public void nullToEmpty_null() {
        assertEquals("", Strings.nullToEmpty(null));
    }

    @Test
    public void nullToEmpty_notNull() {
        String str = "test";
        assertEquals(str, Strings.nullToEmpty(str));
    }

    @Test
    public void replaceSubstring_lowerBoundary() {
        String str = "test string";
        String result = Strings.replaceSubstring(-1, 1, str, "replacement");
        assertNull(result);
    }

    @Test
    public void replaceSubstring_upperBoundary() {
        String str = "test string";
        String result = Strings.replaceSubstring(0, str.length() + 1, str,
                "replacement");
        assertNull(result);
    }

    @Test
    public void replaceSubstring_fromGreaterTo() {
        String str = "test string";
        String result = Strings.replaceSubstring(4, 1, str, "replacement");
        assertNull(result);
    }

    @Test
    public void replaceSubstring_boundaries() {
        String str = "test string";
        String result = Strings.replaceSubstring(-1, str.length() + 1, str,
                "replacement");
        assertNull(result);
    }

    @Test
    public void replaceSubstring_nullString() {
        String str = null;
        String result = Strings.replaceSubstring(0, 0, str, "replacement");
        assertNull(result);
    }

    @Test
    public void replaceSubstring_emptyString() {
        String str = "";
        String result = Strings.replaceSubstring(0, 0, str, "replacement");
        assertNull(result);
    }

    @Test
    public void replaceSubstring_nullValue() {
        String str = "test string";
        String result = Strings.replaceSubstring(0, 3, str, null);
        assertNull(result);
    }

    @Test
    public void replaceSubstring_emptyValue() {
        String str = "test string";
        String result = Strings.replaceSubstring(0, 4, str, "");
        assertEquals("string", result);
    }

    @Test
    public void replaceSubstring_beginning() {
        String str = "test string";
        String result = Strings.replaceSubstring(0, 3, str, "replacement");
        assertEquals("replacement string", result);
    }

    @Test
    public void replaceSubstring_ending() {
        String str = "test string";
        String result = Strings.replaceSubstring(5, 10, str, "replacement");
        assertEquals("test replacement", result);
    }

    @Test
    public void replaceSubstring_replaceOneCharacter() {
        String str = "test string";
        String result = Strings.replaceSubstring(4, 4, str, "");
        assertEquals("teststring", result);
    }

    @Test
    public void replaceSubstring_oneCharacterString() {
        String str = "s";
        String result = Strings.replaceSubstring(0, 0, str, "string");
        assertEquals("string", result);
    }

}
