/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.j2ep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

import org.oscm.logging.LoggerFactory;
import org.oscm.ui.stubs.HttpServletRequestStub;

/**
 * Test class
 * 
 * @author pock
 */
public class AdmRuleTest {

    private AdmRule rule;

    private String url = "/img/logo.jpg";
    private String prefix = "/opt/1000";
    private String rewriteTo = "/example";

    @Before
    public void setup() {
        assertEquals("^/opt(/[^/]*)(/.*)?", AdmRule.getMatchPattern().pattern());

        rule = new AdmRule();
        rule.setRewriteTo(rewriteTo + "$2");
        rule.setRevertPattern(Pattern.compile("^" + rewriteTo + "(/.*)?"));

        LoggerFactory.activateRollingFileAppender("./logs", null, "DEBUG");
    }

    @Test
    public void testMatches() {
        HttpServletRequest request;
        request = new HttpServletRequestStub() {
            public String getRequestURI() {
                return prefix + url;
            }

            public String getContextPath() {
                return "";
            }
        };
        request.getRequestURI();
        assertTrue(rule.matches(request));

        request = new HttpServletRequestStub(null) {
            public String getRequestURI() {
                return "/index.jsp";
            }

            public String getContextPath() {
                return "";
            }
        };
        assertFalse(rule.matches(request));
    }

    @Test
    public void testProcess() {
        String expected = rewriteTo + url;
        assertEquals(expected, rule.process(prefix + url));
    }

    @Test
    public void testRevert() {
        testProcess();
        String expected = prefix + url;
        assertEquals(expected, rule.revert(rewriteTo + url));
    }
}
