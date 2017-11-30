/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class EscapeUtilsTest {

    @Test
    public void escapeJSON_null() {
        // when
        String escaped = EscapeUtils.escapeJSON(null);

        // then
        assertNull(escaped);
    }

    @Test
    public void unescapeJSON_null() {
        // when
        String unescaped = EscapeUtils.unescapeJSON(null);

        // then
        assertNull(unescaped);
    }

    @Test
    public void escapeJSON_noControlChars() {
        // given
        String input = "abcdefghijklmnopqrstuvwxyz1234567890üöä.,?!%&$§()=+*#':;@€<>|-_~ß{}[]^";

        // when
        String escaped = EscapeUtils.escapeJSON(input);

        // then
        assertEquals(input, escaped);
    }

    @Test
    public void unescapeJSON_noControlChars() {
        // given
        String input = "abcdefghijklmnopqrstuvwxyz1234567890üöä.,?!%&$§()=+*#':;@€<>|-_~ß{}[]^";

        // when
        String unescaped = EscapeUtils.escapeJSON(input);

        // then
        assertEquals(input, unescaped);
    }

    @Test
    public void escapeJSON_1() {
        // given
        String input = "\"";

        // when
        String escaped = EscapeUtils.escapeJSON(input);

        // then
        assertEquals("\\\"", escaped);
    }

    @Test
    public void unescapeJSON_1() {
        // given
        String input = "\\\"";

        // when
        String unescaped = EscapeUtils.unescapeJSON(input);

        // then
        assertEquals("\"", unescaped);
    }

    @Test
    public void escapeJSON_2() {
        // given
        String input = "\\";

        // when
        String escaped = EscapeUtils.escapeJSON(input);

        // then
        assertEquals("\\\\", escaped);
    }

    @Test
    public void unescapeJSON_2() {
        // given
        String input = "\\\\";

        // when
        String unescaped = EscapeUtils.unescapeJSON(input);

        // then
        assertEquals("\\", unescaped);
    }

    @Test
    public void escapeJSON_3() {
        // given
        String input = "/";

        // when
        String escaped = EscapeUtils.escapeJSON(input);

        // then
        assertEquals("\\/", escaped);
    }

    @Test
    public void unescapeJSON_3() {
        // given
        String input = "\\/";

        // when
        String unescaped = EscapeUtils.unescapeJSON(input);

        // then
        assertEquals("/", unescaped);
    }

    @Test
    public void escapeJSON_n() {
        // given
        String input = "\n";

        // when
        String escaped = EscapeUtils.escapeJSON(input);

        // then
        assertEquals("\\n", escaped);
    }

    @Test
    public void unescapeJSON_n() {
        // given
        String input = "\\n";

        // when
        String unescaped = EscapeUtils.unescapeJSON(input);

        // then
        assertEquals("\n", unescaped);
    }

    @Test
    public void escapeJSON_t() {
        // given
        String input = "\t";

        // when
        String escaped = EscapeUtils.escapeJSON(input);

        // then
        assertEquals("\\t", escaped);
    }

    @Test
    public void unescapeJSON_t() {
        // given
        String input = "\\t";

        // when
        String unescaped = EscapeUtils.unescapeJSON(input);

        // then
        assertEquals("\t", unescaped);
    }

    @Test
    public void escapeJSON_b() {
        // given
        String input = "\b";

        // when
        String escaped = EscapeUtils.escapeJSON(input);

        // then
        assertEquals("\\b", escaped);
    }

    @Test
    public void unescapeJSON_b() {
        // given
        String input = "\\b";

        // when
        String unescaped = EscapeUtils.unescapeJSON(input);

        // then
        assertEquals("\b", unescaped);
    }

    @Test
    public void escapeJSON_f() {
        // given
        String input = "\f";

        // when
        String escaped = EscapeUtils.escapeJSON(input);

        // then
        assertEquals("\\f", escaped);
    }

    @Test
    public void unescapeJSON_f() {
        // given
        String input = "\\f";

        // when
        String unescaped = EscapeUtils.unescapeJSON(input);

        // then
        assertEquals("\f", unescaped);
    }

    @Test
    public void escapeJSON_r() {
        // given
        String input = "\r";

        // when
        String escaped = EscapeUtils.escapeJSON(input);

        // then
        assertEquals("\\r", escaped);
    }

    @Test
    public void unescapeJSON_r() {
        // given
        String input = "\\r";

        // when
        String unescaped = EscapeUtils.unescapeJSON(input);

        // then
        assertEquals("\r", unescaped);
    }

    @Test
    public void unescapeJSON_all() {
        // given
        String input = "\\n\\r\\\\\\\"\\t\\b\\/\f";

        // when
        String unescaped = EscapeUtils.unescapeJSON(input);

        // then
        assertEquals("\n\r\\\"\t\b/\f", unescaped);
    }
}
