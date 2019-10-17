/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2015年3月30日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.security;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.SearchResult;
import javax.security.auth.login.LoginException;

import org.junit.Before;
import org.junit.Test;
import org.oscm.identity.ApiIdentityClient;
import org.oscm.identity.exception.IdentityClientException;
import org.oscm.identity.model.TokenType;

/**
 * @author qiu
 * 
 */
public class ADMRealmImplTest {
    private ADMRealmImpl realmImpl;
    private Properties ldapProps;

    @Before
    public void setup() {
        Logger logger = Logger.getLogger(ADMRealmImplTest.class.toString());
        logger.setLevel(Level.FINEST);
        realmImpl = new ADMRealmImpl(logger);
        ldapProps = new Properties();
    }

    @Test
    public void retrieveName_relative() {
        // given
        SearchResult searchResult = new SearchResult(null, null, null, true);
        searchResult.setName("cn=ldap01");

        // when
        String name = realmImpl.retrieveName(ldapProps, searchResult);

        // then
        assertEquals("cn=ldap01", name);
    }

    @Test
    public void retrieveName_notRelative() {
        // given
        SearchResult searchResult = new SearchResult(null, null, null, false);
        searchResult.setNameInNamespace("cn=ldap01");
        searchResult
                .setName("ldap://estdevmail1.dev.est.fujitsu.com:389/cn=ldap01");
        ldapProps.put(Context.PROVIDER_URL, "");
        // when
        String name = realmImpl.retrieveName(ldapProps, searchResult);

        // then
        assertEquals("cn=ldap01", name);
        assertEquals("ldap://estdevmail1.dev.est.fujitsu.com:389",
                ldapProps.getProperty(Context.PROVIDER_URL));
    }

    @Test
    public void retrieveName_notRelative_Empty() {
        // given
        SearchResult searchResult = new SearchResult(null, null, null, false);
        searchResult.setNameInNamespace("cn=ldap01");
        searchResult.setName("");
        ldapProps.put(Context.PROVIDER_URL, "a");
        // when
        String name = realmImpl.retrieveName(ldapProps, searchResult);

        // then
        assertEquals("cn=ldap01", name);
        assertEquals("a", ldapProps.getProperty(Context.PROVIDER_URL));
    }
    
    @Test
    public void handleOIDCLogin_UI_emptyPassword() throws Exception {
        // Given
    
        ADMRealmImpl realm = spy(realmImpl);
        ApiIdentityClient idc = mock(ApiIdentityClient.class);
        UserQuery uq = mockOidcUser(realm, idc);
        
        // When
        realmImpl.handleOIDCLogin("1000", "", uq);
        
        verify(idc, never()).validateToken(anyString(), any());
    }
    
    @Test
    public void handleOIDCLogin_WS() throws Exception {
        // Given
        ADMRealmImpl realm = spy(realmImpl);
        ApiIdentityClient idc = mock(ApiIdentityClient.class);
        UserQuery uq = mockOidcUser(realm, idc);
        doReturn("admin").when(idc).validateToken(anyString(), eq(TokenType.ID_TOKEN));
        // When
        realm.handleOIDCLogin("1000", "WSadmin123", uq);
        
        // Then
        verify(idc,times(1)).getIdToken(eq("admin"), eq("admin123"));
        verify(idc,times(1)).validateToken(anyString(), eq(TokenType.ID_TOKEN));
        
    }
    
    @Test(expected=LoginException.class)
    public void handleOIDCLogin_WS_tokenMismatch() throws Exception {
        // Given
        ADMRealmImpl realm = spy(realmImpl);
        ApiIdentityClient idc = mock(ApiIdentityClient.class);
        UserQuery uq = mockOidcUser(realm, idc);
        doReturn("supplier").when(idc).validateToken(anyString(), eq(TokenType.ID_TOKEN));
        
        // When
        realm.handleOIDCLogin("1000", "WSadmin123", uq);
    }
    
    @Test
    public void handleOIDCLogin_OC() throws Exception {
        // Given
        ADMRealmImpl realm = spy(realmImpl);
        ApiIdentityClient idc = mock(ApiIdentityClient.class);
        UserQuery uq = mockOidcUser(realm, idc);
        
        // When
        realm.handleOIDCLogin("1000", "admin123", uq);
        
        // Then
        verify(realm, times(1)).handleLoginAttempt(eq("1000"),eq("admin123"), any());
        verify(idc,never()).validateToken(anyString(), eq(TokenType.ID_TOKEN));
        
    }

   
    protected UserQuery mockOidcUser(ADMRealmImpl realm, ApiIdentityClient idc)
            throws SQLException, IdentityClientException, LoginException, NamingException {
        UserQuery uq = mock(UserQuery.class);
        doNothing().when(uq).execute();
        
        doReturn("default").when(uq).getTenantId();
        doReturn("admin").when(uq).getUserId();
       
        doReturn(idc).when(realm).getIdentityClient(anyString());
        doNothing().when(realm).handleLoginAttempt(eq("1000"), anyString(), any());
        doReturn("0123456789").when(idc).getIdToken(anyString(), anyString());
        return uq;
    }
}
