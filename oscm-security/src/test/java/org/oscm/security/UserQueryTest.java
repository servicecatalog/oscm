/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
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

import org.oscm.internal.types.enumtypes.UserAccountStatus;

public class UserQueryTest {

    private UserQuery query;
    private Connection conn;
    @Captor
    private ArgumentCaptor<String> createdStatement;
    @Captor
    private ArgumentCaptor<Long> passedParameter;
    private PreparedStatement stmt;
    private ResultSet rs;
    private DataSource ds;
    private Long userTkey;
    private List<ResultEntry> entries = new ArrayList<UserQueryTest.ResultEntry>();
    private Iterator<ResultEntry> entryIterator = entries.iterator();
    private ResultEntry currentEntry;

    @Before
    public void setup() throws Exception {
        initMocks();
        userTkey = Long.valueOf(1235L);
        query = new UserQuery(ds, String.valueOf(userTkey));
    }

    @Test
    public void verifyQuery() throws Exception {
        query.execute();
        assertEquals(1, createdStatement.getAllValues().size());
        assertEquals(
                "SELECT u.userId, u.passwordsalt, u.passwordhash, o.tkey, o.remoteldapactive, u.realmuserid, u.status FROM PlatformUser u, Organization o WHERE u.organizationkey=o.tkey AND u.tkey=?",
                createdStatement.getValue());
    }

    @Test
    public void verifyParam() throws Exception {
        query.execute();
        assertEquals(1, passedParameter.getAllValues().size());
        assertEquals(userTkey, passedParameter.getValue());
    }

    @Test
    public void verifyEvaluation_UserId() throws Exception {
        addEntry("userId", "123", "hash", "12345", "false", "realmUserId",
                UserAccountStatus.ACTIVE.name());
        query.execute();
        assertEquals("userId", query.getUserId());
    }

    @Test
    public void verifyEvaluation_PwdSalt() throws Exception {
        addEntry("userId", "123", "hash", "12345", "false", "realmUserId",
                UserAccountStatus.ACTIVE.name());
        query.execute();
        assertEquals(123, query.getPasswordSalt());
    }

    @Test
    public void verifyEvaluation_PwdHash() throws Exception {
        addEntry("userId", "123", "hash", "12345", "false", "realmUserId",
                UserAccountStatus.ACTIVE.name());
        query.execute();
        assertEquals("hash", new String(query.getPasswordHash()));
    }

    @Test
    public void verifyEvaluation_orgKey() throws Exception {
        addEntry("userId", "123", "hash", "12349", "false", "realmUserId",
                UserAccountStatus.ACTIVE.name());
        query.execute();
        assertEquals(12349, query.getOrgKey().longValue());
    }

    @Test
    public void verifyEvaluation_remoteLdapActiveTrue() throws Exception {
        addEntry("userId", "123", "hash", "12349", "true", "realmUserId",
                UserAccountStatus.ACTIVE.name());
        query.execute();
        assertTrue(query.isRemoteLdapActive());
    }

    @Test
    public void verifyEvaluation_realmUserId() throws Exception {
        addEntry("userId", "123", "hash", "12349", "false", "realmUserId",
                UserAccountStatus.ACTIVE.name());
        query.execute();
        assertEquals("realmUserId", query.getRealmUserId());
    }

    @Test
    public void verifyEvaluation_userStatus() throws Exception {
        addEntry("userId", "123", "hash", "12349", "false", "realmUserId",
                UserAccountStatus.ACTIVE.name());
        query.execute();
        assertEquals("ACTIVE", query.getStatus());
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
        doNothing().when(stmt).setLong(anyInt(), passedParameter.capture());
        doAnswer(new Answer<Boolean>() {
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                Boolean result = Boolean.valueOf(entryIterator.hasNext());
                if (result) {
                    currentEntry = entryIterator.next();
                }
                return result;
            }
        }).when(rs).next();
        doAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocation) throws Throwable {
                Integer columnNo = (Integer) invocation.getArguments()[0];
                return getValueForPosition(columnNo.intValue());
            }
        }).when(rs).getString(anyInt());
        doAnswer(new Answer<Long>() {
            public Long answer(InvocationOnMock invocation) throws Throwable {
                Integer columnNo = (Integer) invocation.getArguments()[0];
                return Long.parseLong(getValueForPosition(columnNo.intValue()));
            }
        }).when(rs).getLong(anyInt());
        doAnswer(new Answer<byte[]>() {
            public byte[] answer(InvocationOnMock invocation) throws Throwable {
                Integer columnNo = (Integer) invocation.getArguments()[0];
                return getValueForPosition(columnNo.intValue()).getBytes();
            }
        }).when(rs).getBytes(anyInt());
        doAnswer(new Answer<Boolean>() {
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
            return currentEntry.userId;
        case 2:
            return currentEntry.passwordSalt;
        case 3:
            return currentEntry.passwordHash;
        case 4:
            return currentEntry.orgKey;
        case 5:
            return currentEntry.remoteLdapActive;
        case 6:
            return currentEntry.realmUserId;
        case 7:
            return currentEntry.userStatus;
        default:
            throw new RuntimeException("invalid column number");
        }
    }

    private void addEntry(String userId, String passwordSalt,
            String passwordHash, String orgKey, String remoteLdapActive,
            String realmUserId, String userStatus) {
        entries.add(new ResultEntry(userId, passwordSalt, passwordHash, orgKey,
                remoteLdapActive, realmUserId, userStatus));
        entryIterator = entries.iterator();
    }

    private class ResultEntry {
        String userId;
        String passwordSalt;
        String passwordHash;
        String orgKey;
        String remoteLdapActive;
        String realmUserId;
        String userStatus;

        ResultEntry(String userId, String passwordSalt, String passwordHash,
                String orgKey, String remoteLdapActive, String realmUserId,
                String userStatus) {
            this.userId = userId;
            this.passwordSalt = passwordSalt;
            this.passwordHash = passwordHash;
            this.orgKey = orgKey;
            this.remoteLdapActive = remoteLdapActive;
            this.realmUserId = realmUserId;
            this.userStatus = userStatus;
        }
    }

}
