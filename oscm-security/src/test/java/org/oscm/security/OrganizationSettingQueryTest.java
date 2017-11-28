/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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
import java.util.Properties;

import javax.naming.Context;
import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.internal.types.enumtypes.SettingType;

public class OrganizationSettingQueryTest {

    private OrganizationSettingQuery query;
    private Connection conn;
    @Captor
    private ArgumentCaptor<String> createdStatement;
    @Captor
    private ArgumentCaptor<Long> passedParameter;
    private PreparedStatement stmt;
    private ResultSet rs;
    private DataSource ds;
    private Long orgTkey;
    private List<ResultEntry> entries = new ArrayList<OrganizationSettingQueryTest.ResultEntry>();
    private Iterator<ResultEntry> entryIterator = entries.iterator();
    private ResultEntry currentEntry;

    @Before
    public void setup() throws Exception {
        initMocks();
        orgTkey = Long.valueOf(1234L);
        query = new OrganizationSettingQuery(ds, orgTkey);
    }

    @Test
    public void verifyQuery() throws Exception {
        query.execute();
        assertEquals(1, createdStatement.getAllValues().size());
        assertEquals(
                "SELECT os.settingtype, os.settingvalue, ps.settingvalue FROM organizationsetting os LEFT OUTER JOIN platformsetting ps ON os.settingtype = ps.settingtype WHERE organization_tkey = ?",
                createdStatement.getValue());
    }

    @Test
    public void verifyParam() throws Exception {
        query.execute();
        assertEquals(1, passedParameter.getAllValues().size());
        assertEquals(orgTkey, passedParameter.getValue());
    }

    @Test(expected = NullPointerException.class)
    public void verifyResultMapping_MissingMandatorySettings() throws Exception {
        query.execute();
        query.getProperties();
    }

    @Test
    public void verifyResultMapping_MandatorySettings() throws Exception {
        addEntry(SettingType.LDAP_URL, "url", "url");
        addEntry(SettingType.LDAP_ATTR_REFERRAL, "ignore", "ignore");
        query.execute();
        Properties properties = query.getProperties();
        assertEquals("url", properties.get(Context.PROVIDER_URL));
        assertEquals("com.sun.jndi.ldap.LdapCtxFactory",
                properties.get(Context.INITIAL_CONTEXT_FACTORY));
        assertFalse(properties.containsKey(Context.SECURITY_PRINCIPAL));
        assertEquals("ignore", properties.get(Context.REFERRAL));
    }

    @Test
    public void verifyResultMapping_CheckProperties() throws Exception {
        addEntry(SettingType.LDAP_URL, "url", "url");
        addEntry(SettingType.LDAP_PRINCIPAL, "principal", "principal");
        addEntry(SettingType.LDAP_CREDENTIALS, "credentials", "credentials");
        addEntry(SettingType.LDAP_ATTR_REFERRAL, "ignore", "ignore");
        query.execute();
        Properties properties = query.getProperties();
        assertEquals("url", properties.get(Context.PROVIDER_URL));
        assertEquals("com.sun.jndi.ldap.LdapCtxFactory",
                properties.get(Context.INITIAL_CONTEXT_FACTORY));
        assertEquals("principal", properties.get(Context.SECURITY_PRINCIPAL));
        assertEquals("credentials",
                properties.get(Context.SECURITY_CREDENTIALS));
        assertEquals("ignore", properties.get(Context.REFERRAL));
    }

    @Test
    public void verifyResultMapping_Evaluation_OrgSetting() throws Exception {
        addEntry(SettingType.LDAP_URL, "url", "url");
        addEntry(SettingType.LDAP_BASE_DN, "orgBaseDN", "platformBaseDN");
        query.execute();
        assertEquals("orgBaseDN", query.getBaseDN());
    }

    @Test
    public void verifyResultMapping_Evaluation_LinkedSetting() throws Exception {
        addEntry(SettingType.LDAP_URL, "url", "url");
        addEntry(SettingType.LDAP_ATTR_UID, "", "platformUid");
        query.execute();
        assertEquals("platformUid", query.getAttrUid());
    }

    @Test
    public void verifyResultMapping_Evaluation_UnsetBaseDN() throws Exception {
        addEntry(SettingType.LDAP_URL, "url", "url");
        addEntry(SettingType.LDAP_ATTR_UID, "", "platformUid");
        query.execute();
        assertNull(String.format(
                "attribute for uid should be null but has value '%s'",
                query.getBaseDN()), query.getBaseDN());
    }

    @Test
    public void verifyResultMapping_Evaluation_DefaultUid() throws Exception {
        addEntry(SettingType.LDAP_URL, "url", "url");
        addEntry(SettingType.LDAP_BASE_DN, "", "platformBaseDN");
        query.execute();
        assertEquals(SettingType.LDAP_ATTR_UID.getDefaultValue(),
                query.getAttrUid());
    }

    private void addEntry(SettingType type, String orgValue,
            String platformValue) {
        entries.add(new ResultEntry(type.name(), orgValue, platformValue));
        entryIterator = entries.iterator();
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
                switch (columnNo.intValue()) {
                case 1:
                    return currentEntry.orgSettingType;
                case 2:
                    return currentEntry.orgSettingValue;
                case 3:
                    return currentEntry.platformSettingValue;
                default:
                    throw new RuntimeException("invalid column number");
                }
            }
        }).when(rs).getString(anyInt());
    }

    private class ResultEntry {
        String orgSettingType;
        String orgSettingValue;
        String platformSettingValue;

        ResultEntry(String orgSettingType, String orgSettingValue,
                String platformSettingValue) {
            this.orgSettingType = orgSettingType;
            this.orgSettingValue = orgSettingValue;
            this.platformSettingValue = platformSettingValue;
        }
    }

}
