/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.security;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class AuthenticationModeQueryTest {

    private AuthenticationModeQuery query;
    private Connection conn;
    @Captor
    private ArgumentCaptor<String> createdStatement;
    @Captor
    private ArgumentCaptor<Long> passedParameter;
    private PreparedStatement stmt;
    private ResultSet rs;
    private DataSource ds;
    private final List<ResultEntry> entries = new ArrayList<AuthenticationModeQueryTest.ResultEntry>();
    private Iterator<ResultEntry> entryIterator = entries.iterator();
    private ResultEntry currentEntry;

    private final String AUTH_MODE = "SAML_SP";
    private final String BASE_URL_NO_SLASH = "http://bes.de/oscm-portal";
    private final String BASE_URL_WITH_SLASH = BASE_URL_NO_SLASH + "/";
    private final String BASE_URL_HTTPS_NO_SLASH = "http://bes.de/oscm-portal";
    private final String BASE_URL_HTTPS_WITH_SLASH = BASE_URL_HTTPS_NO_SLASH
            + "/";
    private final String SSO_IDP_TRUSTSTORE = "/truststore.jks";
    private final String SSO_IDP_TRUSTSTORE_PASSWORD = "changeit";

    @Before
    public void setup() throws Exception {
        initMocks();
        query = new AuthenticationModeQuery(ds);
    }

    @Test
    public void verifyEvaluation_AuthenticationMode() throws Exception {
        addEntry("AUTH_MODE", AUTH_MODE);
        query.execute();
        assertEquals(AUTH_MODE, query.getAuthenticationMode());
    }

    @Test
    public void verifyEvaluation_RecipientWithoutSlash() throws Exception {
        addEntry("BASE_URL", BASE_URL_NO_SLASH);
        query.execute();
        assertEquals(BASE_URL_WITH_SLASH, query.getRecipient());
    }

    @Test
    public void verifyEvaluation_RecipientWithSlash() throws Exception {
        addEntry("BASE_URL", BASE_URL_WITH_SLASH);
        query.execute();
        assertEquals(BASE_URL_WITH_SLASH, query.getRecipient());
    }

    @Test
    public void verifyEvaluation_recipientHttpsWithoutSlash() throws Exception {
        // given
        addEntry("BASE_URL_HTTPS", BASE_URL_HTTPS_NO_SLASH);

        // when
        query.execute();

        // then
        assertEquals(BASE_URL_HTTPS_WITH_SLASH, query.getRecipientHttps());
    }

    @Test
    public void verifyEvaluation_recipientHttpsWithSlash() throws Exception {
        // given
        addEntry("BASE_URL_HTTPS", BASE_URL_HTTPS_WITH_SLASH);

        // when
        query.execute();

        // then
        assertEquals(BASE_URL_HTTPS_WITH_SLASH, query.getRecipientHttps());
    }

    @Test
    public void verifyEvaluation_Truststore() throws Exception {
        addEntry("SSO_IDP_TRUSTSTORE", SSO_IDP_TRUSTSTORE);
        query.execute();
        assertEquals(SSO_IDP_TRUSTSTORE, query.getIDPTruststore());
    }

    @Test
    public void verifyEvaluation_TruststorePassword() throws Exception {
        addEntry("SSO_IDP_TRUSTSTORE_PASSWORD", SSO_IDP_TRUSTSTORE_PASSWORD);
        query.execute();
        assertEquals(SSO_IDP_TRUSTSTORE_PASSWORD,
                query.getIDPTruststorePassword());
    }

    @Test
    public void verifyEvaluation_BASE_URL_Null() throws Exception {
        // given
        addEntry("BASE_URL", null);

        // when
        query.execute();

        // then
        assertEquals("/", query.getRecipient());
    }

    @Test
    public void verifyEvaluation_SSO_IDP_TRUSTSTORE_Null() throws Exception {
        // given
        addEntry("SSO_IDP_TRUSTSTORE", null);

        // when
        query.execute();

        // then
        assertEquals("", query.getIDPTruststore());
    }

    @Test
    public void verifyEvaluation_SSO_IDP_TRUSTSTORE_PASSWORD_Null()
            throws Exception {
        // given
        addEntry("SSO_IDP_TRUSTSTORE_PASSWORD", null);

        // when
        query.execute();

        // then
        assertEquals("", query.getIDPTruststorePassword());
    }

    @Test
    public void AuthenticationModeQuery_initValue() {
        // when
        AuthenticationModeQuery amQuery = new AuthenticationModeQuery(ds);
        // then
        assertEquals("", amQuery.getIDPTruststore());
        assertEquals("", amQuery.getIDPTruststorePassword());
        assertEquals("", amQuery.getRecipient());
        assertEquals("", amQuery.getRecipientHttps());
        assertEquals("", amQuery.getAuthenticationMode());
    }

    @SuppressWarnings("boxing")
    private void initMocks() throws Exception {
        MockitoAnnotations.initMocks(this);
        ds = mock(DataSource.class);
        conn = mock(Connection.class);
        stmt = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);

        doReturn(conn).when(ds).getConnection();
        doReturn(stmt).when(conn).prepareStatement(createdStatement.capture());
        doReturn(rs).when(stmt).executeQuery();
        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                Boolean result = Boolean.valueOf(entryIterator.hasNext());
                if (result) {
                    currentEntry = entryIterator.next();
                }
                return result;
            }
        }).when(rs).next();
        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Integer columnNo = (Integer) invocation.getArguments()[0];
                return getValueForPosition(columnNo.intValue());
            }
        }).when(rs).getString(anyInt());
        doAnswer(new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocation) throws Throwable {
                Integer columnNo = (Integer) invocation.getArguments()[0];
                return Long.parseLong(getValueForPosition(columnNo.intValue()));
            }
        }).when(rs).getLong(anyInt());
        doAnswer(new Answer<byte[]>() {
            @Override
            public byte[] answer(InvocationOnMock invocation) throws Throwable {
                Integer columnNo = (Integer) invocation.getArguments()[0];
                return getValueForPosition(columnNo.intValue()).getBytes();
            }
        }).when(rs).getBytes(anyInt());
        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                Integer columnNo = (Integer) invocation.getArguments()[0];
                return Boolean.parseBoolean(getValueForPosition(columnNo
                        .intValue()));
            }
        }).when(rs).getBoolean(anyInt());
    }

    private String getValueForPosition(int position) {
        switch (position) {
        case 1:
            return currentEntry.information_id;
        case 2:
            return currentEntry.env_value;
        default:
            throw new RuntimeException("invalid column number");
        }
    }

    private void addEntry(String information_id, String env_value) {
        entries.add(new ResultEntry(information_id, env_value));
        entryIterator = entries.iterator();
    }

    private class ResultEntry {
        String information_id;
        String env_value;

        ResultEntry(String information_id, String env_value) {
            this.information_id = information_id;
            this.env_value = env_value;
        }
    }

}
