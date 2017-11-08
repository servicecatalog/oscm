/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 30.11.2011                                                      
 *                                                                              
 *  Completion Time: 30.11.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.ldap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.junit.Before;
import org.junit.Test;

import org.oscm.identityservice.local.ILdapResultMapper;
import org.oscm.identityservice.local.LdapVOUserDetailsMapper;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.vo.VOUserDetails;

/**
 * @author weiser
 * 
 */
public class LdapAccessServiceBeanTest {

    private LdapAccessServiceBean bean;
    private DirContext dcMock;
    private NamingEnumeration<SearchResult> neMock;
    private SearchResult srMock;
    private Attributes aMock;

    private static final String[] SEARCH_ATTRIBUTES = new String[] { "a1",
            "a2", "a3" };

    @SuppressWarnings("unchecked")
    @Before
    public void setup() throws Exception {
        bean = spy(new LdapAccessServiceBean());
        dcMock = mock(DirContext.class);
        neMock = mock(NamingEnumeration.class);
        srMock = mock(SearchResult.class);
        aMock = mock(Attributes.class);

        doReturn(new Integer(5)).when(bean).getSearchLimit();
        doReturn(dcMock).when(bean).getDirContext(any(Properties.class));

        when(dcMock.search(anyString(), anyString(), any(SearchControls.class)))
                .thenReturn(neMock);

        when(Boolean.valueOf(neMock.hasMore())).thenReturn(Boolean.TRUE,
                Boolean.FALSE);
        when(neMock.next()).thenReturn(srMock);
        when(srMock.getAttributes()).thenReturn(aMock);
    }

    private void initAttribute(String value, Attributes atts)
            throws NamingException {
        Attribute a = mock(Attribute.class);
        when(a.get()).thenReturn(value);
        when(atts.get(eq(value))).thenReturn(a);
    }

    @Test
    public void search() throws Exception {
        initAttribute(SEARCH_ATTRIBUTES[0], aMock);
        initAttribute(SEARCH_ATTRIBUTES[1], aMock);
        initAttribute(SEARCH_ATTRIBUTES[2], aMock);

        Map<SettingType, String> map = new HashMap<SettingType, String>();
        map.put(SettingType.LDAP_ATTR_UID, SEARCH_ATTRIBUTES[0]);
        map.put(SettingType.LDAP_ATTR_FIRST_NAME, SEARCH_ATTRIBUTES[1]);
        map.put(SettingType.LDAP_ATTR_EMAIL, SEARCH_ATTRIBUTES[2]);

        ILdapResultMapper<VOUserDetails> mapper = new LdapVOUserDetailsMapper(
                null, map);
        List<VOUserDetails> list = bean.search(new Properties(), "baseDN",
                "filter", mapper, false);

        assertNotNull(list);
        assertEquals(1, list.size());

        VOUserDetails det = list.get(0);
        assertEquals(SEARCH_ATTRIBUTES[0], det.getRealmUserId());
        assertEquals(SEARCH_ATTRIBUTES[1], det.getFirstName());
        assertEquals(SEARCH_ATTRIBUTES[2], det.getEMail());
    }

    @Test
    public void search_AttributeValueNull() throws Exception {
        initAttribute(SEARCH_ATTRIBUTES[0], aMock);
        initAttribute(SEARCH_ATTRIBUTES[1], aMock);
        initAttribute(null, aMock);

        Map<SettingType, String> map = new HashMap<SettingType, String>();
        map.put(SettingType.LDAP_ATTR_UID, SEARCH_ATTRIBUTES[0]);
        map.put(SettingType.LDAP_ATTR_FIRST_NAME, SEARCH_ATTRIBUTES[1]);
        map.put(SettingType.LDAP_ATTR_EMAIL, SEARCH_ATTRIBUTES[2]);

        ILdapResultMapper<VOUserDetails> mapper = new LdapVOUserDetailsMapper(
                null, map);
        List<VOUserDetails> list = bean.search(new Properties(), "baseDN",
                "filter", mapper, false);

        assertNotNull(list);
        assertEquals(1, list.size());

        VOUserDetails det = list.get(0);
        assertEquals(SEARCH_ATTRIBUTES[0], det.getRealmUserId());
        assertEquals(SEARCH_ATTRIBUTES[1], det.getFirstName());
        assertEquals(null, det.getEMail());
    }

    @Test
    public void search_AttributeNull() throws Exception {
        initAttribute(SEARCH_ATTRIBUTES[0], aMock);
        initAttribute(null, aMock);
        when(aMock.get(eq(SEARCH_ATTRIBUTES[2]))).thenReturn(null);

        Map<SettingType, String> map = new HashMap<SettingType, String>();
        map.put(SettingType.LDAP_ATTR_UID, SEARCH_ATTRIBUTES[0]);
        map.put(SettingType.LDAP_ATTR_FIRST_NAME, SEARCH_ATTRIBUTES[1]);
        map.put(SettingType.LDAP_ATTR_EMAIL, SEARCH_ATTRIBUTES[2]);

        ILdapResultMapper<VOUserDetails> mapper = new LdapVOUserDetailsMapper(
                null, map);
        List<VOUserDetails> list = bean.search(new Properties(), "baseDN",
                "filter", mapper, false);

        assertNotNull(list);
        assertEquals(1, list.size());

        VOUserDetails det = list.get(0);
        assertEquals(SEARCH_ATTRIBUTES[0], det.getRealmUserId());
        assertEquals(null, det.getFirstName());
        assertEquals(null, det.getEMail());
    }

    @Test(expected = NamingException.class)
    public void search_AttributeNull_CheckTrue() throws Exception {
        when(aMock.get(eq(SEARCH_ATTRIBUTES[2]))).thenReturn(null);

        Map<SettingType, String> map = new HashMap<SettingType, String>();
        map.put(SettingType.LDAP_ATTR_EMAIL, SEARCH_ATTRIBUTES[2]);

        ILdapResultMapper<VOUserDetails> mapper = new LdapVOUserDetailsMapper(
                null, map);
        bean.search(new Properties(), "baseDN", "filter", mapper, true);
    }

    @Test
    public void search_filterAndDNEscape() throws Exception {
        // given
        initAttribute(SEARCH_ATTRIBUTES[0], aMock);
        initAttribute(SEARCH_ATTRIBUTES[1], aMock);
        initAttribute(SEARCH_ATTRIBUTES[2] + ")(tes,t=t)", aMock);

        Map<SettingType, String> map = new HashMap<SettingType, String>();
        map.put(SettingType.LDAP_ATTR_UID, SEARCH_ATTRIBUTES[0]);
        map.put(SettingType.LDAP_ATTR_FIRST_NAME, SEARCH_ATTRIBUTES[1]);
        map.put(SettingType.LDAP_ATTR_EMAIL, SEARCH_ATTRIBUTES[2]);

        ILdapResultMapper<VOUserDetails> mapper = new LdapVOUserDetailsMapper(
                null, map);

        // when
        bean.search(new Properties(), "baseDN", "filter();*,\u0000", mapper,
                false);

        // then
        verify(dcMock, times(1)).search(eq("baseDN"),
                eq("filter\\28\\29;*,\\00"), any(SearchControls.class));

    }

    @Test
    public void searchOverLimit_True() throws Exception {
        doReturn(new Integer(1)).when(bean).getSearchLimit();
        initAttribute(SEARCH_ATTRIBUTES[0], aMock);
        initAttribute(SEARCH_ATTRIBUTES[1], aMock);
        initAttribute(SEARCH_ATTRIBUTES[2], aMock);

        when(Boolean.valueOf(neMock.hasMore())).thenReturn(Boolean.TRUE,
                Boolean.TRUE, Boolean.FALSE);

        Map<SettingType, String> map = new HashMap<SettingType, String>();
        map.put(SettingType.LDAP_ATTR_UID, SEARCH_ATTRIBUTES[0]);
        map.put(SettingType.LDAP_ATTR_FIRST_NAME, SEARCH_ATTRIBUTES[1]);
        map.put(SettingType.LDAP_ATTR_EMAIL, SEARCH_ATTRIBUTES[2]);

        ILdapResultMapper<VOUserDetails> mapper = new LdapVOUserDetailsMapper(
                null, map);
        boolean flag = bean.searchOverLimit(new Properties(), "baseDN",
                "filter", mapper, false);

        assertTrue(flag);
    }

    @Test
    public void searchOverLimit_False() throws Exception {
        initAttribute(SEARCH_ATTRIBUTES[0], aMock);
        initAttribute(SEARCH_ATTRIBUTES[1], aMock);
        initAttribute(SEARCH_ATTRIBUTES[2], aMock);

        Map<SettingType, String> map = new HashMap<SettingType, String>();
        map.put(SettingType.LDAP_ATTR_UID, SEARCH_ATTRIBUTES[0]);
        map.put(SettingType.LDAP_ATTR_FIRST_NAME, SEARCH_ATTRIBUTES[1]);
        map.put(SettingType.LDAP_ATTR_EMAIL, SEARCH_ATTRIBUTES[2]);

        ILdapResultMapper<VOUserDetails> mapper = new LdapVOUserDetailsMapper(
                null, map);
        boolean flag = bean.searchOverLimit(new Properties(), "baseDN",
                "filter", mapper, false);

        assertFalse(flag);
    }

    @Test
    public void search_withPartialResultException() throws Exception {
        // given
        initAttribute(SEARCH_ATTRIBUTES[0], aMock);
        initAttribute(SEARCH_ATTRIBUTES[1], aMock);
        initAttribute(SEARCH_ATTRIBUTES[2], aMock);

        when(Boolean.valueOf(neMock.hasMore())).thenReturn(Boolean.TRUE)
                .thenThrow(new PartialResultException());

        Map<SettingType, String> map = new HashMap<SettingType, String>();
        map.put(SettingType.LDAP_ATTR_UID, SEARCH_ATTRIBUTES[0]);
        map.put(SettingType.LDAP_ATTR_FIRST_NAME, SEARCH_ATTRIBUTES[1]);
        map.put(SettingType.LDAP_ATTR_EMAIL, SEARCH_ATTRIBUTES[2]);

        ILdapResultMapper<VOUserDetails> mapper = new LdapVOUserDetailsMapper(
                null, map);

        List<VOUserDetails> list = new ArrayList<VOUserDetails>();

        // when
        list = bean.search(new Properties(), "baseDN", "filter", mapper, false);

        // then
        assertNotNull(list);
        assertEquals(1, list.size());
        VOUserDetails det = list.get(0);
        assertEquals(SEARCH_ATTRIBUTES[0], det.getRealmUserId());
        assertEquals(SEARCH_ATTRIBUTES[1], det.getFirstName());
        assertEquals(SEARCH_ATTRIBUTES[2], det.getEMail());
    }

}
