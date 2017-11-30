/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.04.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.faces.context.ExternalContext;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import sun.util.locale.BaseLocale;

import org.oscm.internal.intf.BrandService;
import org.oscm.internal.operatorservice.LocalizedDataService;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.ServiceAccess;
import org.oscm.ui.stubs.ExternalContextStub;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.HttpServletRequestStub;
import org.oscm.ui.stubs.HttpSessionStub;

/**
 * @author weiser
 * 
 */
public class DbMessagesTest {

    private static final long TIME = System.currentTimeMillis();

    private DbMessages dbm;

    private LocalizedDataService lsMock;
    private BrandService brMock;

    @Before
    public void setup() throws Exception {
        dbm = spy(new DbMessages(Locale.ENGLISH) {
            @Override
            public Locale getLocale() {
                return Locale.ENGLISH;
            }
        });
        new FacesContextStub(Locale.ENGLISH) {
            @Override
            public ExternalContext getExternalContext() {
                ExternalContext exContext = spy(new ExternalContextStub(
                        Locale.ENGLISH));
                HttpServletRequestStub request = new HttpServletRequestStub() {
                    @Override
                    public HttpSession getSession() {
                        return new HttpSessionStub(Locale.ENGLISH) {
                            Map<String, Object> attributes = new HashMap<String, Object>();

                            @Override
                            public Object getAttribute(String key) {
                                if (Constants.REQ_PARAM_MARKETPLACE_ID
                                        .equals(key)) {
                                    return "marketplace_id";
                                }
                                return attributes.get(key);
                            }

                            @Override
                            public void setAttribute(String key, Object val) {
                                attributes.put(key, val);
                            }
                        };
                    }
                };

                doReturn(request).when(exContext).getRequest();
                return exContext;
            };

        };

        dbm.refreshPropertyTime = TIME;
        lsMock = mock(LocalizedDataService.class);
        brMock = mock(BrandService.class);
    }

    @Test
    public void isResetRequired() {
        assertFalse(dbm.isResetRequired(TIME + DbMessages.RESET_INTERVAL - 1));
    }

    @Test
    public void isResetRequired_Positive() {
        assertTrue(dbm.isResetRequired(TIME + DbMessages.RESET_INTERVAL + 1));
    }

    @Test
    public void handleGetObject() {
        // given
        doReturn(lsMock).when(dbm).getLocalizedDataService(
                any(ServiceAccess.class));
        Properties properties = new Properties();
        properties.put("key", "value");
        doReturn(properties).when(lsMock).loadMessageProperties(anyString());
        doReturn(properties).when(brMock).loadMessagePropertiesFromDB(
                anyString(), anyString());
        doReturn(brMock).when(dbm).getBrandManagementService(
                any(ServiceAccess.class));
        // when
        String value = (String) dbm.handleGetObject("key");
        // then
        assertEquals("value", value);
    }

    @Test
    public void handleGetObject_Cache() {
        // given
        doReturn(brMock).when(dbm).getBrandManagementService(
                any(ServiceAccess.class));
        doReturn(lsMock).when(dbm).getLocalizedDataService(
                any(ServiceAccess.class));
        Properties properties = new Properties();
        properties.put("key", "value");
        doReturn(properties).when(lsMock).loadMessageProperties(anyString());
        doReturn(properties).when(brMock).loadMessagePropertiesFromDB(
                anyString(), anyString());
        dbm.handleGetObject("key");
        // when
        String value = (String) dbm.handleGetObject("key");
        // then
        assertEquals("value", value);
        verify(brMock, times(1)).loadMessagePropertiesFromDB(anyString(),
                anyString());
    }

    @Test
    public void dbMessagesResourcesTest() throws Exception{
        //given
        final List<String> strings = Arrays.asList("az", "be", "bg", "bn", "br", "bs", "ca", "ch", "cs", "cy", "da", "de", "el",
                "en", "es", "et", "fi", "fr", "gl", "gu", "hi", "hr", "hu", "ia", "ii", "in", "is", "it", "ja", "ko", "lt", "lv",
                "mk", "ml", "mn", "ms", "nb", "nl", "nn", "no", "pl", "pt", "ro", "ru", "sc", "se", "si", "sk", "sl", "sq", "sr",
                "sv", "ta", "te", "th", "tr", "tt", "tw", "uk", "vi", "zh");
        //when
        for (String s : strings) {
            final Class<?> aClass = Class.forName("org.oscm.ui.resources.DbMessages_" + s);
            aClass.getConstructor().newInstance();
        }
        //then no exceptions
    }

}
