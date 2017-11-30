/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Goebel                                                      
 *                                                                              
 *  Creation Date: 2014-05-13                                                      
                                                                                                                        
 *******************************************************************************/
package org.oscm.ui.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.logging.Log4jLogger;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.filter.RequestWithCleanParameters.NameValuePair;

/**
 * 
 * @author goebel
 * 
 */
public class RequestWithCleanParametersTest {

    private HttpServletRequest requestMock;
    private HashMap<String, String[]> map;

    final char[] badChar = new char[] { 0, 4, 8, 13 };
    final String REMOTE_HOST = "www.hackerhost.com";
    private String queryString = "param1=\"1\"";
    private static Log4jLogger loggerMock;

    @Before
    public void setUp() throws Exception {
        loggerMock = mock(Log4jLogger.class);

        requestMock = mock(HttpServletRequest.class);
        map = new HashMap<String, String[]>();

        when(requestMock.getParameterMap()).thenReturn(map);
        when(requestMock.getRemoteAddr()).thenReturn(REMOTE_HOST);

        doAnswer(new Answer<String[]>() {
            @Override
            public String[] answer(InvocationOnMock invocation)
                    throws Throwable {
                return map.get(invocation.getArguments()[0]);
            }
        }).when(requestMock).getParameterValues(anyString());

        when(requestMock.getQueryString()).thenReturn(queryString);

        RequestWithCleanParameters.logger = loggerMock;
    }

    private Map<String, String[]> givenRequestParameters() {
        return map;
    }

    @Test
    public void escapeAll_LineBreaksFromValues() {
        // given
        givenRequestParameters().put("param1",
                new String[] { "not\n\rok", "a\nd", "fujitsu" });

        RequestWithCleanParameters request = new RequestWithCleanParameters(
                requestMock, new Pattern[0]);

        final Map<String, String> escapePatterns = givenLineBreakEscapePatters();

        // when
        request.escapeAll(escapePatterns);

        // then
        assertNoLineBreaks(request);
        assertIllegalParameterValueWarning(3);
    }

    @Test
    public void escapeAll_ControlCharsFromValues() {

        // given
        givenRequestParameters().put("param1", givenValuesWith5CtrlChars());

        RequestWithCleanParameters request = new RequestWithCleanParameters(
                requestMock, new Pattern[0]);

        final Map<String, String> escapePatterns = givenControlCharEscapePatters();

        // when
        request.escapeAll(escapePatterns);

        // then
        assertNoCtrlChars(request);
        assertIllegalParameterValueWarning(5);
    }

    @Test
    public void escapeAll_LineBreaksFromNames() {
        givenRequestParameters().put("use\rs",
                new String[] { "user1", "user2" });
        givenRequestParameters().put("accou\nts",
                new String[] { "account1", "account2" });

        // given
        RequestWithCleanParameters request = new RequestWithCleanParameters(
                requestMock, new Pattern[0]);

        final Map<String, String> escapePatterns = givenLineBreakEscapePatters();

        // when
        request.escapeAll(escapePatterns);

        // then
        assertNoLineBreaks(request);

        assertIllegalParameterNameWarning(2);
    }

    @Test
    public void escapeAll_ControlCharsFromValues_Ignore() {

        // given
        final String[] valuesWithCtrlChars = givenValuesWith5CtrlChars();
        givenRequestParameters().put("SAMLrequest", valuesWithCtrlChars);
        givenRequestParameters().put("token", valuesWithCtrlChars);

        Pattern[] ignores = new Pattern[] { Pattern.compile("^SAML.*"),
                Pattern.compile("^token.*") };

        RequestWithCleanParameters request = new RequestWithCleanParameters(
                requestMock, ignores);

        final Map<String, String> escapePatterns = givenControlCharEscapePatters();

        // when
        request.escapeAll(escapePatterns);

        // then
        assertHasCtrlChars(request);

        verifyZeroInteractions(loggerMock);
    }

    @Test
    public void getParameter_NotExisting() {

        // given
        RequestWithCleanParameters request = new RequestWithCleanParameters(
                requestMock, null);

        // then
        assertNull(request.getParameter("name"));
    }

    @Test
    public void getParameterNames() {

        // given
        givenRequestParameters().put("param1", new String[] { "1" });
        RequestWithCleanParameters request = new RequestWithCleanParameters(
                requestMock, null);

        // when
        @SuppressWarnings("rawtypes")
        Enumeration enm = request.getParameterNames();

        // then
        assertTrue(enm.hasMoreElements());
        assertNotNull(request.getParameter("param1"));
    }

    @Test
    public void parseParamsFromQueryString_valid() {

        // given
        RequestWithCleanParameters request = new RequestWithCleanParameters(
                requestMock, null);

        // when
        request.parseParamsFromQueryString("param1=\"123\"&param2=\"1234\"&param3=\"1234\"");

        // then
        verifyZeroInteractions(loggerMock);
    }

    @Test
    public void parseParamsFromQueryString_invalidNames() {

        // given
        RequestWithCleanParameters request = new RequestWithCleanParameters(
                requestMock, null);
        // when
        NameValuePair[] result = request.parseParamsFromQueryString("param1=\"123\"&par%am2=\"1234\"&param3=\"1234\"");

        // then
        assertNotNull(result);
        assertIllegalParameterNameWarning(1);
    }

    @Test
    public void parseParamsFromQueryString_invalidValues() {

        // given
        RequestWithCleanParameters request = new RequestWithCleanParameters(
                requestMock, null);

        // when
        NameValuePair[] result =  request.parseParamsFromQueryString("param1=\"A\ts\"&param2=\"12%4\"&param3=\"&\r&\"");

        // then
        assertNotNull(result);
        assertIllegalParameterValueWarning(1);

    }

    @Test
    public void parseParamsFromQueryString_DelimiterValue() {
        // given
        RequestWithCleanParameters request = new RequestWithCleanParameters(
                requestMock, null);

        // when
        request.parseParamsFromQueryString("param1=\"A&B\"&param2=\"12=4\"&param3=\"&&&\"");

        // then
        verifyZeroInteractions(loggerMock);
    }
    

    @Test
    public void initParameter_OnlyInvalidNames() {
        // given
        givenQueryString("o%id=\"val1\"&o%id2=\"val2\"&o%id3=\"val3\"");

        // when
        RequestWithCleanParameters request = new RequestWithCleanParameters(
                requestMock, null);

        // then
        assertTrue(request.getParameterMap().isEmpty());
        assertEquals("", request.getQueryString());

        assertIllegalParameterNameWarning(3);
    }

    @Test
    public void initParameter_InvalidValue() {
        // given
        givenQueryString("oid=\"ab%%&abc&cde\"");

        // when
        RequestWithCleanParameters request = new RequestWithCleanParameters(
                requestMock, null);

        // then
        assertEquals(1, request.getParameterMap().size());
        assertEquals("", request.getParameter("oid"));

    }

    @Test
    public void initParameter_NoValue() {
        // given
        givenQueryString("oid=");

        // when
        RequestWithCleanParameters request = new RequestWithCleanParameters(
                requestMock, null);

        // then
        assertEquals(1, request.getParameterMap().size());
        assertEquals("", request.getParameter("oid"));

    }

    @Test
    public void initParameter_NotAssignment() {
        // given
        givenQueryString("nonsense");

        // when
        RequestWithCleanParameters request = new RequestWithCleanParameters(
                requestMock, null);

        // then
        assertEquals(1, request.getParameterMap().size());
        assertEquals("", request.getParameter("nonsense"));
    }

    @Test
    public void unquoteVistor_visit_replaceQuotedAmpersant() {
        String res = new RequestWithCleanParameters.QuotedDelimitors()
                .escapeDelimitorChars("mId=\"a&b\"&oid=\"&long&param&value&\"");
        assertEquals("mId=\"a%26b\"&oid=\"%26long%26param%26value%26\"", res);
    }

    @Test
    public void unquoteVistor_visit_replaceQuotedAssign() {
        String res = new RequestWithCleanParameters.QuotedDelimitors()
                .escapeDelimitorChars("mId=\"H=M\"&oid=\"=long=param=value=\"");
        assertEquals("mId=\"H%3DM\"&oid=\"%3Dlong%3Dparam%3Dvalue%3D\"", res);
    }

    @Test
    public void unquoteVistor_visit_replaceQuotedComplex() {
        String res = new RequestWithCleanParameters.QuotedDelimitors()
                .escapeDelimitorChars("mId=\"H&M\"&oid=\"=long=param=value=\"&userId=k%261&secId=\"&\"");
        assertEquals(
                "mId=\"H%26M\"&oid=\"%3Dlong%3Dparam%3Dvalue%3D\"&userId=k%261&secId=\"%26\"",
                res);
    }

    @Test
    public void unquoteVistor_visit_empty() {
        String res = new RequestWithCleanParameters.QuotedDelimitors()
                .escapeDelimitorChars("");
        assertEquals("", res);
    }

    private String[] givenValuesWith5CtrlChars() {
        return new String[] {
                "This is bad" + (char) 0,
                "My" + (char) 4 + " parameter" + (char) 8 + " is" + (char) 13
                        + " contaminated", (char) 0 + "fujitsu" };
    }

    private Map<String, String> givenLineBreakEscapePatters() {
        HashMap<String, String> escapes = new LinkedHashMap<>();
        escapes.put("\n", "");
        escapes.put("\r", "");
        return escapes;
    }

    private Map<String, String> givenControlCharEscapePatters() {
        HashMap<String, String> escapes = new LinkedHashMap<>();
        final char[] c = new char[] { 0, 4, 8, 13 };

        escapes.put(String.valueOf(c[0]), "");
        escapes.put(String.valueOf(c[1]), "");
        escapes.put(String.valueOf(c[2]), "");
        escapes.put(String.valueOf(c[3]), "");
        return escapes;
    }

    private void assertNoLineBreaks(RequestWithCleanParameters request) {

        for (Iterator<String> iter = request.getParameterMap().keySet()
                .iterator(); iter.hasNext();) {
            String parameter = iter.next();
            assertFalse(parameter.contains("\r"));
            assertFalse(parameter.contains("\n"));
            String[] values = request.getParameterValues(parameter);
            for (String value : values) {
                assertNotNull(value);
                assertFalse(value.contains("\r"));
                assertFalse(value.contains("\n"));
            }
        }
    }

    private void assertHasCtrlChars(RequestWithCleanParameters request) {
        boolean hasCtrlChar = false;
        for (Iterator<String> iter = request.getParameterMap().keySet()
                .iterator(); iter.hasNext();) {

            String[] values = request.getParameterValues(iter.next());
            assertTrue(values.length > 0);

            for (String value : values) {
                assertNotNull(value);
                hasCtrlChar |= (0 < value.indexOf((char) 0));
                hasCtrlChar |= (0 < value.indexOf((char) 4));
                hasCtrlChar |= (0 < value.indexOf((char) 8));
                hasCtrlChar |= (0 < value.indexOf((char) 12));
            }

        }
        assertTrue(hasCtrlChar);
    }

    private void assertNoCtrlChars(RequestWithCleanParameters request) {
        for (Iterator<String> iter = request.getParameterMap().keySet()
                .iterator(); iter.hasNext();) {
            String parameter = iter.next();
            String[] values = request.getParameterValues(parameter);
            assertTrue(values.length > 0);
            for (String value : values) {
                assertNotNull(values);
                assertEquals(-1, value.indexOf((char) 0));
                assertEquals(-1, value.indexOf((char) 4));
                assertEquals(-1, value.indexOf((char) 12));
                assertEquals(-1, value.indexOf((char) 8));
            }
        }
    }

    private void givenQueryString(String value) {
        queryString = value;
        when(requestMock.getQueryString()).thenReturn(queryString);
    }

    private void assertIllegalParameterNameWarning(int numberOfWarnings) {
        verify(loggerMock, times(numberOfWarnings)).logWarn(
                eq(Log4jLogger.ACCESS_LOG),
                eq(LogMessageIdentifier.WARN_ILLEGAL_REQUEST_PARAMETER_NAME),
                eq(REMOTE_HOST), anyString());
    }

    private void assertIllegalParameterValueWarning(int numberOfWarnings) {
        verify(loggerMock, times(numberOfWarnings)).logWarn(
                eq(Log4jLogger.ACCESS_LOG),
                eq(LogMessageIdentifier.WARN_ILLEGAL_REQUEST_PARAMETER_VALUE),
                eq(REMOTE_HOST), anyString());
    }
}
