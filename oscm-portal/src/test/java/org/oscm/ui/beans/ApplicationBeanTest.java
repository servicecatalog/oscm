/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich                                                      
 *                                                                              
 *  Creation Date: 22.10.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
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
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.common.Constants;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.stubs.ApplicationStub;
import org.oscm.ui.stubs.ExternalContextStub;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.HttpServletRequestStub;
import org.oscm.ui.stubs.HttpSessionStub;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.operatorservice.ManageLanguageService;
import org.oscm.internal.operatorservice.POSupportedLanguage;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.vo.VOConfigurationSetting;
import org.oscm.internal.vo.VOUserDetails;

@SuppressWarnings("boxing")
public class ApplicationBeanTest {

    private static final String GMT = "GMT";
    private static final String CET = "CET";
    private ApplicationBean bean;
    private IdentityService idService;
    private ConfigurationService mock;
    private ManageLanguageService manageLanguageService;
    private ExternalContext externalContext;
    private FacesContext facesContext;
    private HttpSession session;
    private HttpServletRequest request;
    private VOUserDetails voUser;
    private String message;
    List<String> localeStrList;
    private UiDelegate ui;

    @Before
    public void setUp() {

        voUser = new VOUserDetails();
        voUser.setLocale(Locale.GERMAN.toString());

        session = new HttpSessionStub(Locale.ENGLISH) {
            @Override
            public int getMaxInactiveInterval() {
                return 300;
            }
        };
        session.setAttribute(Constants.REQ_PARAM_MARKETPLACE_ID, "aaaaaaaa");
        request = new HttpServletRequestStub(Locale.ENGLISH) {

            Map<String, Object> attributes = new HashMap<String, Object>();

            @Override
            public HttpSession getSession(boolean arg0) {
                return session;
            }

            @Override
            public Object getAttribute(String arg0) {

                Object obj = attributes.get(arg0);
                if (obj != null) {
                    return obj;
                }
                if (Constants.REQ_PARAM_MARKETPLACE_ID.equals(arg0)) {
                    return obj;
                }
                return null;
            }

            @Override
            public void setAttribute(String arg0, Object arg1) {
                attributes.put(arg0, arg1);
            }

        };

        externalContext = new ExternalContextStub(Locale.ENGLISH) {
            @Override
            public Object getRequest() {
                return request;
            }

            @Override
            public Object getSession(boolean arg0) {
                return session;
            }

            @Override
            public String getRequestContextPath() {
                return "/oscm-portal";
            }

        };

        facesContext = spy(new FacesContextStub(Locale.ENGLISH) {
            @Override
            public Application getApplication() {
                Application applicationStub = new ApplicationStub();
                List<Locale> locales = new ArrayList<Locale>();
                locales.add(Locale.ENGLISH);
                locales.add(Locale.GERMAN);
                locales.add(Locale.JAPANESE);
                locales.add(new Locale("in"));
                applicationStub.setSupportedLocales(locales);
                return applicationStub;
            }

            @Override
            public ExternalContext getExternalContext() {
                return externalContext;
            }

        });
        bean = spy(new ApplicationBean() {
            private static final long serialVersionUID = 2541342225602086901L;

            @Override
            protected FacesContext getFacesContext() {
                return facesContext;
            }

            @Override
            protected void addMessage(String clientId,
                    FacesMessage.Severity severity, String key, String param) {
                message = key;
            }

        });

        idService = mock(IdentityService.class);
        bean.identityService = idService;

        mock = mock(ConfigurationService.class);
        when(
                mock.getVOConfigurationSetting(
                        eq(ConfigurationKey.HIDDEN_UI_ELEMENTS), anyString()))
                .thenReturn(
                        createSetting(ConfigurationKey.HIDDEN_UI_ELEMENTS,
                                "subscription.mylist, organization.edit, organization.addCustomer"));
        when(
                mock.getVOConfigurationSetting(
                        eq(ConfigurationKey.REPORT_ENGINEURL), anyString()))
                .thenReturn(
                        createSetting(ConfigurationKey.REPORT_ENGINEURL,
                                "someUrl"));

        when(
                mock.getVOConfigurationSetting(
                        eq(ConfigurationKey.TIME_ZONE_ID), anyString()))
                .thenReturn(createSetting(ConfigurationKey.TIME_ZONE_ID, CET));
        bean.setConfigurationService(mock);

        manageLanguageService = mock(ManageLanguageService.class);
        bean.setManageLanguageService(manageLanguageService);

        List<Locale> localeList = new ArrayList<Locale>();
        localeList.add(Locale.ENGLISH);
        localeList.add(Locale.GERMAN);
        localeList.add(Locale.JAPANESE);

        localeStrList = new ArrayList<String>();
        localeStrList.add("en");
        localeStrList.add("de");

        Application application = mock(Application.class);
        when(facesContext.getApplication()).thenReturn(application);
        when(application.getSupportedLocales()).thenReturn(
                localeList.iterator());

        ui = mock(UiDelegate.class);
        doReturn(Locale.ENGLISH).when(ui).getViewLocale();
        bean.ui = ui;
    }

    @Test
    public void isUIElementHidden() {
        assertTrue(bean.isUIElementHidden("subscription.mylist"));
        assertTrue(bean.isUIElementHidden("organization.edit"));
        assertTrue(bean.isUIElementHidden("organization.addCustomer"));
        assertFalse(bean.isUIElementHidden("notExistedKey"));
    }

    @Test
    public void getTimeZoneId() {
        assertEquals(CET, bean.getTimeZoneId());
    }

    @Test
    public void getTimeZoneId_Null() {
        when(
                mock.getVOConfigurationSetting(
                        eq(ConfigurationKey.TIME_ZONE_ID), anyString()))
                .thenReturn(null);
        assertEquals(GMT, bean.getTimeZoneId());
    }

    @Test
    public void getTimeZoneId_NullValue() {
        when(
                mock.getVOConfigurationSetting(
                        eq(ConfigurationKey.TIME_ZONE_ID), anyString()))
                .thenReturn(createSetting(ConfigurationKey.TIME_ZONE_ID, null));
        assertEquals(GMT, bean.getTimeZoneId());
    }

    @Test
    public void getTimeZoneId_Invalid() {
        when(
                mock.getVOConfigurationSetting(
                        eq(ConfigurationKey.TIME_ZONE_ID), anyString()))
                .thenReturn(
                        createSetting(ConfigurationKey.TIME_ZONE_ID,
                                "some invalid time zone id"));
        assertEquals(GMT, bean.getTimeZoneId());
    }

    @Test
    public void isReportingAvailable() {
        assertEquals(true, bean.isReportingAvailable());
    }

    @Test
    public void isReportingAvailable_Null() {
        when(
                mock.getVOConfigurationSetting(
                        eq(ConfigurationKey.REPORT_ENGINEURL), anyString()))
                .thenReturn(null);
        assertEquals(false, bean.isReportingAvailable());
    }

    @Test
    public void isReportingAvailable_NullValue() {
        when(
                mock.getVOConfigurationSetting(
                        eq(ConfigurationKey.REPORT_ENGINEURL), anyString()))
                .thenReturn(
                        createSetting(ConfigurationKey.REPORT_ENGINEURL, null));
        assertEquals(false, bean.isReportingAvailable());
    }

    @Test
    public void isReportingAvailable_EmptyValue() {
        when(
                mock.getVOConfigurationSetting(
                        eq(ConfigurationKey.REPORT_ENGINEURL), anyString()))
                .thenReturn(
                        createSetting(ConfigurationKey.REPORT_ENGINEURL, "   "));
        assertEquals(false, bean.isReportingAvailable());
    }

    @Test
    public void getServerBaseUrl_WithoutSlash() {
        String value = "someurl";
        when(
                mock.getVOConfigurationSetting(eq(ConfigurationKey.BASE_URL),
                        anyString())).thenReturn(
                createSetting(ConfigurationKey.BASE_URL, value));
        assertEquals(value, bean.getServerBaseUrl());
    }

    @Test
    public void getServerBaseUrl_WithSlash() {
        String value = "someurl";
        when(
                mock.getVOConfigurationSetting(eq(ConfigurationKey.BASE_URL),
                        anyString())).thenReturn(
                createSetting(ConfigurationKey.BASE_URL, value + "/"));
        assertEquals(value, bean.getServerBaseUrl());
    }

    @Test
    public void getServerBaseUrl_Null() {
        when(
                mock.getVOConfigurationSetting(eq(ConfigurationKey.BASE_URL),
                        anyString())).thenReturn(
                createSetting(ConfigurationKey.BASE_URL, null));
        assertEquals(null, bean.getServerBaseUrl());
    }

    @Test
    public void getServerBaseUrlHttps_WithoutSlash() {
        String value = "https://thisisaurl";
        when(
                mock.getVOConfigurationSetting(
                        eq(ConfigurationKey.BASE_URL_HTTPS), anyString()))
                .thenReturn(
                        createSetting(ConfigurationKey.BASE_URL_HTTPS, value));
        assertEquals(value, bean.getServerBaseUrlHttps());
    }

    @Test
    public void getServerBaseUrlHttps_WithSlash() {
        String value = "https://thisisaurl";
        when(
                mock.getVOConfigurationSetting(
                        eq(ConfigurationKey.BASE_URL_HTTPS), anyString()))
                .thenReturn(
                        createSetting(ConfigurationKey.BASE_URL_HTTPS, value
                                + "/"));
        assertEquals(value, bean.getServerBaseUrlHttps());
    }

    @Test
    public void getServerBaseUrlHttps_Null() {
        when(
                mock.getVOConfigurationSetting(
                        eq(ConfigurationKey.BASE_URL_HTTPS), anyString()))
                .thenReturn(
                        createSetting(ConfigurationKey.BASE_URL_HTTPS, null));
        assertEquals(null, bean.getServerBaseUrlHttps());
    }

    @Test
    public void isInternalAuthMode() {
        // given
        when(
                mock.getVOConfigurationSetting(eq(ConfigurationKey.AUTH_MODE),
                        anyString())).thenReturn(
                createSetting(ConfigurationKey.AUTH_MODE, "INTERNAL"));
        // when
        boolean isInternalAuthMode = bean.isInternalAuthMode();
        // then
        assertTrue(isInternalAuthMode);
    }

    @Test
    public void isInternalAuthMode_Not() {
        // given
        when(
                mock.getVOConfigurationSetting(eq(ConfigurationKey.AUTH_MODE),
                        anyString())).thenReturn(
                createSetting(ConfigurationKey.AUTH_MODE, "SAML_SP"));
        // when
        boolean isInternalAuthMode = bean.isInternalAuthMode();
        // then
        assertFalse(isInternalAuthMode);
    }

    @Test
    public void isInternalAuthMode_NotNull() {
        // given
        Boolean internalAuthMode = Boolean.valueOf(false);
        bean.setInternalAuthMode(internalAuthMode);
        // when
        boolean isInternalAuthMode = bean.isInternalAuthMode();
        // then
        assertFalse(isInternalAuthMode);
    }

    @Test
    public void isSpSamlAuthMode() {
        // given
        when(
            mock.getVOConfigurationSetting(eq(ConfigurationKey.AUTH_MODE),
                anyString())).thenReturn(
            createSetting(ConfigurationKey.AUTH_MODE, "SAML_SP"));
        // when
        boolean isSamlSpAuthMode = bean.isSamlSpAuthMode();
        // then
        assertTrue(isSamlSpAuthMode);
    }

    @Test
    public void isSpSamlAuthMode_not() {
        // given
        when(
            mock.getVOConfigurationSetting(eq(ConfigurationKey.AUTH_MODE),
                anyString())).thenReturn(
            createSetting(ConfigurationKey.AUTH_MODE, "INTERNAL"));
        // when
        boolean isSamlSpAuthMode = bean.isSamlSpAuthMode();
        // then
        assertFalse(isSamlSpAuthMode);
    }

    @Test
    public void getInterval() {
        // when
        Long interval = bean.getInterval();
        // then
        assertEquals(240000L, interval.longValue());
    }

    @Test
    public void getSupportedLocales() {
        // given
        List<POSupportedLanguage> poSupportedLanguages = new ArrayList<POSupportedLanguage>();
        poSupportedLanguages.add(getPOSupportedLanguage(1, "en"));
        poSupportedLanguages.add(getPOSupportedLanguage(2, "de"));
        poSupportedLanguages.add(getPOSupportedLanguage(3, "ja"));

        doReturn(poSupportedLanguages).when(manageLanguageService)
                .getLanguages(true);
        // when
        List<String> locales = bean.getActiveLocales();

        // then
        assertEquals(3, locales.size());
        assertEquals("en", locales.get(0));
        assertEquals("de", locales.get(1));
        assertEquals("ja", locales.get(2));

    }

    @Test
    public void getSupportedLocales_activeLessThanSupportedLocales() {
        // given
        List<POSupportedLanguage> poSupportedLanguages = new ArrayList<POSupportedLanguage>();
        poSupportedLanguages.add(getPOSupportedLanguage(1, "en"));

        doReturn(poSupportedLanguages).when(manageLanguageService)
                .getLanguages(true);

        // when
        List<String> locales = bean.getActiveLocales();

        // then
        assertEquals(1, locales.size());
        assertEquals("en", locales.get(0));
    }

    @Test
    public void getSupportedLocales_activeMoreThanSupportedLocales() {
        // given
        List<POSupportedLanguage> poSupportedLanguages = new ArrayList<POSupportedLanguage>();
        poSupportedLanguages.add(getPOSupportedLanguage(1, "en"));
        poSupportedLanguages.add(getPOSupportedLanguage(2, "de"));
        poSupportedLanguages.add(getPOSupportedLanguage(3, "ja"));
        poSupportedLanguages.add(getPOSupportedLanguage(4, "zh"));

        doReturn(poSupportedLanguages).when(manageLanguageService)
                .getLanguages(true);

        // when
        List<String> locales = bean.getActiveLocales();

        // then
        assertEquals(3, locales.size());
        assertEquals("en", locales.get(0));
        assertEquals("de", locales.get(1));
        assertEquals("ja", locales.get(2));
    }

    @Test
    public void getSupportedLocales_NullActiveLanguage() {
        // given
        doReturn(null).when(manageLanguageService).getLanguages(true);

        // when
        List<String> locales = bean.getActiveLocales();

        // then
        assertNotNull(locales);
        assertEquals(0, locales.size());
    }

    @Test
    public void getSupportedLocales_NoneActiveLanguage() {
        // given
        doReturn(new ArrayList<POSupportedLanguage>()).when(
                manageLanguageService).getLanguages(true);

        // when
        List<String> locales = bean.getActiveLocales();

        // then
        assertNotNull(locales);
        assertEquals(0, locales.size());
    }

    @Test
    public void getSupportedLocalesIterator() {
        // given
        List<POSupportedLanguage> poSupportedLanguages = new ArrayList<POSupportedLanguage>();
        poSupportedLanguages.add(getPOSupportedLanguage(1, "en"));
        poSupportedLanguages.add(getPOSupportedLanguage(2, "de"));
        poSupportedLanguages.add(getPOSupportedLanguage(3, "ja"));

        doReturn(poSupportedLanguages).when(manageLanguageService)
                .getLanguages(true);
        // when
        Iterator<Locale> it = bean.getSupportedLocalesIterator();

        // then
        assertEquals("en", it.next().getLanguage());
        assertEquals("de", it.next().getLanguage());
        assertEquals("ja", it.next().getLanguage());
        assertFalse(it.hasNext());
    }

    @Test
    public void getSupportedLocalesIterator_activeLessThanSupportedLocales() {
        // given
        List<POSupportedLanguage> poSupportedLanguages = new ArrayList<POSupportedLanguage>();
        poSupportedLanguages.add(getPOSupportedLanguage(1, "en"));

        doReturn(poSupportedLanguages).when(manageLanguageService)
                .getLanguages(true);

        // when
        Iterator<Locale> it = bean.getSupportedLocalesIterator();

        // then
        assertEquals("en", it.next().getLanguage());
        assertFalse(it.hasNext());
    }

    @Test
    public void getSupportedLocalesIterator_activeMoreThanSupportedLocales() {
        // given
        List<POSupportedLanguage> poSupportedLanguages = new ArrayList<POSupportedLanguage>();
        poSupportedLanguages.add(getPOSupportedLanguage(1, "en"));
        poSupportedLanguages.add(getPOSupportedLanguage(2, "de"));
        poSupportedLanguages.add(getPOSupportedLanguage(3, "ja"));
        poSupportedLanguages.add(getPOSupportedLanguage(4, "zh"));

        doReturn(poSupportedLanguages).when(manageLanguageService)
                .getLanguages(true);

        // when
        Iterator<Locale> it = bean.getSupportedLocalesIterator();

        // then
        assertEquals("en", it.next().getLanguage());
        assertEquals("de", it.next().getLanguage());
        assertEquals("ja", it.next().getLanguage());
        assertFalse(it.hasNext());
    }

    @Test
    public void getSupportedLocalesIterator_NullActiveLanguage() {
        // given
        doReturn(null).when(manageLanguageService).getLanguages(true);

        // when
        Iterator<Locale> it = bean.getSupportedLocalesIterator();

        // then
        assertNotNull(it);
        assertFalse(it.hasNext());
    }

    @Test
    public void getSupportedLocalesIterator_NoneActiveLanguage() {
        // given
        doReturn(new ArrayList<POSupportedLanguage>()).when(
                manageLanguageService).getLanguages(true);

        // when
        Iterator<Locale> it = bean.getSupportedLocalesIterator();

        // then
        assertNotNull(it);
        assertFalse(it.hasNext());
    }

    @Test
    public void getSupportedLocaleList() {
        // given
        List<POSupportedLanguage> poSupportedLanguages = new ArrayList<POSupportedLanguage>();
        poSupportedLanguages.add(getPOSupportedLanguage(1, "en"));
        poSupportedLanguages.add(getPOSupportedLanguage(2, "de"));
        poSupportedLanguages.add(getPOSupportedLanguage(3, "ja"));

        doReturn(poSupportedLanguages).when(manageLanguageService)
                .getLanguages(true);
        // when
        List<Locale> it = bean.getSupportedLocaleList();

        // then
        assertEquals("en", it.get(0).getLanguage());
        assertEquals("de", it.get(1).getLanguage());
        assertEquals("ja", it.get(2).getLanguage());
        assertEquals(3, it.size());
    }

    @Test
    public void getSupportedLocaleList_activeLessThanSupportedLocales() {
        // given
        List<POSupportedLanguage> poSupportedLanguages = new ArrayList<POSupportedLanguage>();
        poSupportedLanguages.add(getPOSupportedLanguage(1, "en"));

        doReturn(poSupportedLanguages).when(manageLanguageService)
                .getLanguages(true);

        // when
        List<Locale> it = bean.getSupportedLocaleList();

        // then
        assertEquals("en", it.get(0).getLanguage());
        assertEquals(1, it.size());
    }

    @Test
    public void getSupportedLocaleList_activeMoreThanSupportedLocales() {
        // given
        List<POSupportedLanguage> poSupportedLanguages = new ArrayList<POSupportedLanguage>();
        poSupportedLanguages.add(getPOSupportedLanguage(1, "en"));
        poSupportedLanguages.add(getPOSupportedLanguage(2, "de"));
        poSupportedLanguages.add(getPOSupportedLanguage(3, "ja"));
        poSupportedLanguages.add(getPOSupportedLanguage(4, "zh"));

        doReturn(poSupportedLanguages).when(manageLanguageService)
                .getLanguages(true);

        // when
        List<Locale> it = bean.getSupportedLocaleList();

        // then
        assertEquals("en", it.get(0).getLanguage());
        assertEquals("de", it.get(1).getLanguage());
        assertEquals("ja", it.get(2).getLanguage());
        assertEquals(3, it.size());
    }

    @Test
    public void getSupportedLocaleList_NullActiveLanguage() {
        // given
        doReturn(null).when(manageLanguageService).getLanguages(true);

        // when
        List<Locale> it = bean.getSupportedLocaleList();

        // then
        assertNotNull(it);
        assertTrue(it.isEmpty());
    }

    @Test
    public void getSupportedLocaleList_NoneActiveLanguage() {
        // given
        doReturn(new ArrayList<POSupportedLanguage>()).when(
                manageLanguageService).getLanguages(true);

        // when
        List<Locale> it = bean.getSupportedLocaleList();

        // then
        assertNotNull(it);
        assertTrue(it.isEmpty());
    }

    @Test
    public void checkLocaleValidation_validLocale() {
        // given
        doReturn(localeStrList).when(bean).getActiveLocales();

        // when
        bean.checkLocaleValidation("en");

        // then
        verify(bean, times(1)).getActiveLocales();
        verify(bean, never()).addMessage(anyString(),
                eq(FacesMessage.SEVERITY_WARN),
                eq(BaseBean.WARNING_SUPPORTEDLANGUAGE_LOCALE_INVALID),
                anyString());
    }

    @Test
    public void checkLocaleValidation_invalidLocale() {
        // given
        doReturn(localeStrList).when(bean).getActiveLocales();

        // when
        bean.checkLocaleValidation("ja");

        // then
        verify(bean, times(1)).getActiveLocales();
        verify(bean, times(1)).addMessage(anyString(),
                eq(FacesMessage.SEVERITY_WARN),
                eq(BaseBean.WARNING_SUPPORTEDLANGUAGE_LOCALE_INVALID),
                anyString());
        assertEquals(BaseBean.WARNING_SUPPORTEDLANGUAGE_LOCALE_INVALID, message);
    }

    @Test
    public void checkLocaleValidation_locale_null() {
        // given
        doReturn(localeStrList).when(bean).getActiveLocales();

        // when
        bean.checkLocaleValidation(null);

        // then
        verify(bean, times(1)).getActiveLocales();
        verify(bean, never()).addMessage(anyString(),
                eq(FacesMessage.SEVERITY_WARN),
                eq(BaseBean.WARNING_SUPPORTEDLANGUAGE_LOCALE_INVALID),
                anyString());
    }

    @Test
    public void checkLocaleValidation_locales_null() {
        // given
        doReturn(null).when(bean).getActiveLocales();

        // when
        bean.checkLocaleValidation(null);

        // then
        verify(bean, times(1)).getActiveLocales();
    }

    @Test
    public void getUserLocaleUpdated_updated() {
        // given
        doReturn(localeStrList).when(bean).getActiveLocales();
        voUser.setLocale("ja");
        doReturn(voUser).when(bean).getUserFromSessionWithoutException();
        doReturn(voUser).when(idService).getCurrentUserDetailsIfPresent();
        doNothing().when(bean).setUserInSession(any(VOUserDetails.class));

        // when
        bean.getUserLocaleUpdated();

        // then
        verify(bean, times(1)).getActiveLocales();
        assertTrue(bean.getErrorPanelForLocaleShow());
        verify(bean, times(1)).setUserInSession(any(VOUserDetails.class));
        assertEquals(bean.getOldUserLocale(),
                Locale.JAPANESE.getDisplayLanguage(Locale.ENGLISH));
    }

    @Test
    public void getUserLocaleUpdated_noUpdated() {
        // given
        doReturn(localeStrList).when(bean).getActiveLocales();
        voUser.setLocale("en");
        doReturn(voUser).when(bean).getUserFromSessionWithoutException();
        doReturn(voUser).when(idService).getCurrentUserDetailsIfPresent();
        // when
        bean.getUserLocaleUpdated();

        // then
        verify(bean, times(1)).getActiveLocales();
        assertFalse(bean.getErrorPanelForLocaleShow());
        assertEquals(bean.getOldUserLocale(),
                Locale.ENGLISH.getDisplayLanguage(Locale.ENGLISH));
    }

    @Test
    public void getUserLocaleUpdated_noUpdated_languageActive() {
        // given
        doReturn(localeStrList).when(bean).getActiveLocales();
        voUser.setLocale(Locale.ENGLISH.toString());
        doReturn(voUser).when(bean).getUserFromSessionWithoutException();

        VOUserDetails voUserFromDB = new VOUserDetails();
        voUserFromDB.setLocale(Locale.GERMAN.toString());
        doReturn(voUserFromDB).when(idService).getCurrentUserDetailsIfPresent();
        doNothing().when(bean).setUserInSession(any(VOUserDetails.class));

        // when
        bean.getUserLocaleUpdated();

        // then
        verify(bean, times(1)).getActiveLocales();
        verify(bean, times(1)).setUserInSession(eq(voUserFromDB));
        assertFalse(bean.getErrorPanelForLocaleShow());
        assertEquals(bean.getOldUserLocale(),
                Locale.GERMAN.getDisplayLanguage(Locale.ENGLISH));
    }

    @Test
    public void getUserLocaleUpdated_Locales_null() {
        // given
        doReturn(null).when(bean).getActiveLocales();
        doReturn(voUser).when(bean).getUserFromSessionWithoutException();
        doReturn(voUser).when(idService).getCurrentUserDetailsIfPresent();
        // when
        bean.getUserLocaleUpdated();

        // then
        verify(bean, times(1)).getActiveLocales();
        assertFalse(bean.getErrorPanelForLocaleShow());

        // when
        String result = bean.getOldUserLocale();

        // then
        assertEquals(result, "");
    }

    @Test
    public void getUserLocaleUpdated_idService_user_null() {
        // given
        doReturn(voUser).when(bean).getUserFromSessionWithoutException();
        doReturn(null).when(idService).getCurrentUserDetailsIfPresent();

        // when
        bean.getUserLocaleUpdated();

        // then
        verify(bean, never()).getActiveLocales();
        assertFalse(bean.getErrorPanelForLocaleShow());
    }

    @Test
    public void getUserLocaleUpdated_session_user_null() {
        // given
        doReturn(null).when(bean).getUserFromSessionWithoutException();
        doReturn(voUser).when(idService).getCurrentUserDetailsIfPresent();

        // when
        bean.getUserLocaleUpdated();

        // then
        assertFalse(bean.getErrorPanelForLocaleShow());
        verify(bean, never()).getActiveLocales();
    }

    @Test
    public void getMarketplaceId_marketplaceInRequest_null() {
        // given
        request.setAttribute(Constants.REQ_PARAM_MARKETPLACE_ID, null);
        // when
        String s = bean.getMarketplaceId();
        // then
        assertEquals("aaaaaaaa", s);
    }

    @Test
    public void getMarketplaceId_marketplaceInRequest_notNull() {
        // given
        request.setAttribute(Constants.REQ_PARAM_MARKETPLACE_ID, "bbbbbbbb");
        // when
        String s = bean.getMarketplaceId();
        // then
        assertEquals("bbbbbbbb", s);
    }

    @Test
    public void getUserLocaleUpdated_user_null() {
        // given
        doReturn(null).when(bean).getUserFromSessionWithoutException();
        doReturn(null).when(idService).getCurrentUserDetailsIfPresent();

        // when
        bean.getUserLocaleUpdated();

        // then
        assertFalse(bean.getErrorPanelForLocaleShow());
        verify(bean, never()).getActiveLocales();
    }

    @Test
    public void getAvailableLanguageItems() {
        // given locales
        List<String> locales = bean.getActiveLocales();

        // when
        List<SelectItem> selectItems = bean.getAvailableLanguageItems();

        // then
        assertAvailableLanguageItems(locales, selectItems);
    }

    private void assertAvailableLanguageItems(List<String> locales,
            List<SelectItem> selectItems) {
        assertNotNull(selectItems);
        assertEquals(locales.size(), selectItems.size());
        for (int i = 0; i < locales.size(); i++) {
            assertNotNull(selectItems.get(i).getLabel());
            assertEquals(locales.get(i), (selectItems.get(i).getValue()));
        }

    }

    private VOConfigurationSetting createSetting(ConfigurationKey key,
            String value) {
        return new VOConfigurationSetting(key, "global", value);
    }

    private POSupportedLanguage getPOSupportedLanguage(long key,
            String languageISOCode) {
        POSupportedLanguage poSupportedLanguage = new POSupportedLanguage();
        poSupportedLanguage.setKey(key);
        poSupportedLanguage.setLanguageISOCode(languageISOCode);
        return poSupportedLanguage;
    }

    /**
     * The branding of the portal can be changed by deploying an extra branding
     * war file. The URL is automatically adapted if this war file has been
     * deployed.
     * 
     */
    @Test
    public void getBrandingURL() throws Exception {
        assertEquals("/oscm-portal", bean.getBrandingURL());
    }

    /**
     * The branding of the portal can be changed by deploying an extra branding
     * war file. The URL is automatically adapted if this war file has been
     * deployed.
     * 
     */
    @Test
    public void getBrandingURL_FCIP_DEPLOYED() throws Exception {
        // given
        givenBrandingPackageDeployed();

        // when
        String result = bean.getBrandingURL();

        // than
        assertEquals(ApplicationBean.FCIP_CONTEXT_PATH_ROOT, result);
    }

    /**
     * check if the branding folder is available
     */
    @Test
    public void isFCIPBrandingPackageAvailable() throws Exception {
        // given
        givenBrandingPackageDeployed();

        // than
        assertTrue(bean.isFCIPBrandingPackageAvailable());
    }

    /**
     * check if the branding folder is available
     */
    @Test
    public void isFCIPBrandingPackageAvailable_NothingDeployed()
            throws Exception {
        assertFalse(bean.isFCIPBrandingPackageAvailable());
    }

    private void givenBrandingPackageDeployed() throws Exception {
        given(ui.getSystemProperty("catalina.base")).willReturn(
                "glassfish_root");
        given(
                bean.fileExists("glassfish_root" + File.separator
                        + ApplicationBean.APPLICATIONS_ROOT_FOLDER
                        + File.separator
                        + ApplicationBean.FCIP_BRANDING_PACKAGE)).willReturn(
                true);
    }

    @Test
    public void getJSMessageByKey_UnsavedChangesMsg() {
        // given
        String oldText = "You have unsaved changes\" that will be lost\" if you continue.\nClick OK to confirm.";
        String expectedText = "You have unsaved changes\\\" that will be lost\\\" if you continue.\nClick OK to confirm.";
        when(ui.getText(any(String.class), any(Object[].class))).thenReturn(
                oldText);

        // when
        String newText = bean.getJSMessageByKey("confirm.unsavedChanges.lost");

        // then
        assertEquals(expectedText, newText);
    }

    @Test
    public void getJSMessageByKey_ErrorUploadMsg() {
        // given
        String oldText = "You have unsaved changes\" that will be lost\" if you continue.\nClick OK to confirm.";
        String expectedText = "You have unsaved changes\\\" that will be lost\\\" if you continue.\nClick OK to confirm.";
        when(ui.getText(any(String.class), any(Object[].class))).thenReturn(
                oldText);

        // when
        String newText = bean.getJSMessageByKey("error.upload.filename");

        // then
        assertEquals(expectedText, newText);
    }

    @Test
    public void getJSMessageByKey_LanguageDeactiveMsg() {
        // given
        String oldText = "You have unsaved changes\" that will be lost\" if you continue.\nClick OK to confirm.";
        String expectedText = "You have unsaved changes\\\" that will be lost\\\" if you continue.\nClick OK to confirm.";
        when(ui.getText(any(String.class), any(Object[].class))).thenReturn(
                oldText);

        // when
        String newText = bean.getJSMessageByKey("confirm.language.deactive");

        // then
        assertEquals(expectedText, newText);
    }
}
