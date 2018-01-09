/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 26.05.2014                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.common.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.faces.application.Application;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.InitialContext;
import javax.servlet.http.HttpSession;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.oscm.app.common.APPlatformServiceMockup;
import org.oscm.app.v2_0.data.ControllerConfigurationKey;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.oscm.app.v2_0.intf.ControllerAccess;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;

/**
 * Unit test of configuration bean
 */
public class ConfigurationBeanTest extends EJBTestBase {

    // Local mockups
    private static final List<String> TEST_KEYS = Arrays.asList(new String[] {
            "TEST1", "TEST2" });
    private APPlatformServiceMockup platformService;
    private FacesContext facesContext;
    private ExternalContext externalContext;
    private Application application;
    private HttpSession httpSession;
    private UIViewRoot viewRoot;
    private ControllerAccess controllerAccess;

    @Override
    protected void setup(TestContainer container) throws Exception {

        platformService = new APPlatformServiceMockup();
        enableJndiMock();
    }

    /**
     * Init and return testing bean
     */
    private ConfigurationBean getTestBean() throws Exception {
        InitialContext context = new InitialContext();
        context.bind(APPlatformService.JNDI_NAME, platformService);

        facesContext = Mockito.mock(FacesContext.class);
        externalContext = Mockito.mock(ExternalContext.class);
        httpSession = Mockito.mock(HttpSession.class);
        application = Mockito.mock(Application.class);
        controllerAccess = Mockito.mock(ControllerAccess.class);
        viewRoot = Mockito.mock(UIViewRoot.class);

        Mockito.when(facesContext.getExternalContext()).thenReturn(
                externalContext);
        Mockito.when(facesContext.getApplication()).thenReturn(application);
        Mockito.when(externalContext.getSession(Matchers.anyBoolean()))
                .thenReturn(httpSession);
        Mockito.when(httpSession.getAttribute(Matchers.anyString()))
                .thenReturn("aValue");
        Mockito.when(facesContext.getViewRoot()).thenReturn(viewRoot);
        Mockito.when(viewRoot.getLocale()).thenReturn(new Locale("en"));
        Mockito.when(controllerAccess.getControllerId()).thenReturn(
                "ess.common");
        Mockito.when(controllerAccess.getControllerParameterKeys()).thenReturn(
                TEST_KEYS);

        // Init testing bean
        ConfigurationBean bean = new ConfigurationBean() {
            private static final long serialVersionUID = -1300403486736808608L;

            @Override
            protected FacesContext getContext() {
                return facesContext;
            }
        };
        bean.init();
        bean.setControllerAccess(controllerAccess);
        bean.resetToken();
        bean.setToken(bean.getToken());
        return bean;
    }

    private ConfigurationItem findItem(List<ConfigurationItem> items, String key) {
        for (ConfigurationItem item : items) {
            if (key.equals(item.getKey())) {
                return item;
            }
        }
        return null;
    }

    @Test
    public void getInitialize_saved() throws Exception {
        // given
        ConfigurationBean bean = getTestBean();
        bean.setSaved(true);
        // when
        bean.getInitialize();
        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(bean.isSaved()));
    }

    @Test
    public void getInitialize_notSaved() throws Exception {
        // given
        ConfigurationBean bean = getTestBean();
        bean.setSaved(false);
        // when
        bean.getInitialize();
        // then
        assertNull(bean.getStatus());
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingBeanException() throws Exception {
        // Simulate missing EJB
        InitialContext context = new InitialContext();
        context.unbind(APPlatformService.JNDI_NAME);
        // And invoke internal EJB constructor
        new ConfigurationBean();
    }

    @Test
    public void testGetItems() throws Exception {
        // Get test bean
        ConfigurationBean bean = getTestBean();

        // Get default items
        List<ConfigurationItem> items = bean.getItems();
        assertNotNull(items);
        assertEquals(2, items.size());

        // Check one value
        ConfigurationItem item = findItem(items, "TEST1");
        assertNotNull(item);
        assertEquals("secret2", item.getValue());

        // Check again (now cached value will be used)
        items = bean.getItems();
        assertEquals(2, items.size());
        item = findItem(items, "TEST1");
        assertNotNull(item);
        assertEquals("secret2", item.getValue());
    }

    @Test
    public void testGetAccessItems() throws Exception {
        // Get test bean
        ConfigurationBean bean = getTestBean();

        // Get default items
        List<ConfigurationItem> items = bean.getAccessItems();
        assertNotNull(items);
        assertEquals(4, items.size());

        // Check one value
        ConfigurationItem item = findItem(items, "BSS_USER_ID");
        assertNotNull(item);
        assertEquals("userId", item.getValue());

        // Check again (now cached value will be used)
        items = bean.getAccessItems();
        assertEquals(4, items.size());
        item = findItem(items, "BSS_USER_ID");
        assertNotNull(item);
        assertEquals("userId", item.getValue());

    }

    @Test
    public void testGetItemsMissingValue() throws Exception {
        // Get test bean
        ConfigurationBean bean = getTestBean();

        // Remove one default value from controller settings
        HashMap<String, Setting> def_settings = platformService
                .getControllerSettings("ess.common",
                        new PasswordAuthentication("user", "password"));
        def_settings.remove(ControllerConfigurationKey.BSS_ORGANIZATION_ID
                .name());
        def_settings.remove("TEST1");
        platformService.storeControllerSettings("ess.common", def_settings,
                new PasswordAuthentication("user", "password"));

        // Get default items (missing value must be defined with an empty
        // string!)
        List<ConfigurationItem> items = bean.getAccessItems();
        assertNotNull(items);
        assertEquals(4, items.size());

        // Check the missing value
        ConfigurationItem item = findItem(items, "BSS_ORGANIZATION_ID");
        assertEquals("", item.getValue());
    }

    @Test
    public void testGetItemsException() throws Exception {
        // Get test bean
        ConfigurationBean bean = getTestBean();

        // Throw exception when settings are evaluated
        Mockito.when(httpSession.getAttribute(Matchers.anyString())).thenThrow(
                new RuntimeException("problem"));

        // Get items
        List<ConfigurationItem> items = bean.getItems();

        // Exception will be tramnsformed to a NULL
        assertNull(items);
    }

    @Test
    public void testSave() throws Exception {
        // Get test bean
        ConfigurationBean bean = getTestBean();

        assertFalse(bean.isDirty());

        // Get items
        List<ConfigurationItem> items = bean.getAccessItems();
        assertNotNull(items);
        assertEquals(4, items.size());
        assertFalse(bean.isDirty());

        // Change and save it
        ConfigurationItem item = changeItem(bean, items, "new_user  ");
        bean.save();
        assertFalse(bean.isDirty());
        assertEquals(Boolean.TRUE, Boolean.valueOf(bean.isSaved()));

        String status = bean.getStatus();
        assertTrue(status.contains("saved successfully"));
        assertEquals("statusInfo", bean.getStatusClass());

        // Create new bean
        List<ConfigurationItem> items_new = bean.getAccessItems();

        // And check new value
        item = findItem(items_new, "BSS_USER_KEY");
        assertEquals("new_user", item.getValue());
    }

    private ConfigurationItem changeItem(ConfigurationBean bean,
            List<ConfigurationItem> items, String newValue) {
        ConfigurationItem item = findItem(items, "BSS_USER_KEY");
        item.setValue(newValue);
        item.setDirty(true);
        bean.setChangedItemKey(item.getKey());
        bean.updateItems();
        bean.getInitialize();
        assertTrue(bean.isDirty());
        return item;
    }

    @Test
    public void testSaveWithException() throws Exception {
        // Get test bean
        ConfigurationBean bean = getTestBean();

        // Prepare some exception!
        platformService.exceptionOnStoreControllerSettings = new APPlatformException(
                "myFailure");

        // Invoke command which needs the context
        bean.save();
        String status = bean.getStatus();
        assertTrue(status.contains("*** myFailure"));
        assertEquals("statusError", bean.getStatusClass());
    }

    @Test
    public void testSaveWithNPE() throws Exception {
        // Get test bean
        ConfigurationBean bean = getTestBean();

        // Prepare NPE!
        facesContext = null;

        // Invoke command which needs the context
        bean.save();
        String status = bean.getStatus();
        assertTrue(status.contains("*** java.lang.NullPointerException"));
    }

    @Test
    public void testSaveWithTokensNotMatch() throws Exception {
        // given
        ConfigurationBean bean = getTestBean();
        bean.setToken("notValid");

        // when
        bean.save();
        // then
        assertNull(bean.getStatus());
    }

    @Test
    public void testUndo() throws Exception {
        // Get test bean
        ConfigurationBean bean = getTestBean();
        assertFalse(bean.isDirty());

        // Get items
        List<ConfigurationItem> items = bean.getAccessItems();
        assertNotNull(items);
        assertFalse(bean.isDirty());

        // Change and undo it
        ConfigurationItem item = changeItem(bean, items, "12345");

        bean.undo();
        assertFalse(bean.isDirty());

        items = bean.getAccessItems();
        item = findItem(items, "BSS_USER_KEY");
        assertNotNull(item);
        assertEquals("12345", item.getValue());
    }

    @Test
    public void testStatusEmpty() throws Exception {
        // Get test bean
        ConfigurationBean bean = getTestBean();
        // Get items
        assertNull(bean.getStatus());
        assertNull(bean.getStatusClass());
    }

    @Test
    public void testSwitchBrowserLocale() throws Exception {
        // Get test bean
        ConfigurationBean bean = getTestBean();

        Mockito.when(
                controllerAccess.getMessage(Matchers.eq("en"),
                        Matchers.anyString())).thenReturn("english");
        Mockito.when(
                controllerAccess.getMessage(Matchers.eq("de"),
                        Matchers.anyString())).thenReturn("german");

        // First try: "en"
        Mockito.when(viewRoot.getLocale()).thenReturn(new Locale("en"));
        ConfigurationItem itemEN = bean.getItems().get(0);
        String dnEN = itemEN.getDisplayName();
        String ttEN = itemEN.getTooltip();

        // Switch locale to "de"
        Mockito.when(viewRoot.getLocale()).thenReturn(new Locale("de"));

        // Compare locale specific texts:
        ConfigurationItem itemDE = bean.getItems().get(0);
        String dnDE = itemDE.getDisplayName();
        String ttDE = itemDE.getTooltip();

        assertFalse(dnEN.equals(dnDE));
        assertFalse(ttEN.equals(ttDE));
        assertEquals("english", dnEN);
        assertEquals("english", ttEN);
        assertEquals("german", dnDE);
        assertEquals("german", ttDE);
    }

    @Test
    public void testSwitchBrowserLocaleUnknown() throws Exception {
        // Get test bean
        ConfigurationBean bean = getTestBean();

        Mockito.when(
                controllerAccess.getMessage(Matchers.eq("en"),
                        Matchers.anyString())).thenReturn("english");
        Mockito.when(
                controllerAccess.getMessage(Matchers.eq("un"),
                        Matchers.anyString())).thenReturn("!unknown-locale!");

        // First try: "en"
        Mockito.when(viewRoot.getLocale()).thenReturn(new Locale("en"));
        ConfigurationItem itemEN = bean.getItems().get(0);
        String dnEN = itemEN.getDisplayName();
        String ttEN = itemEN.getTooltip();

        // Switch locale to "de"
        Mockito.when(viewRoot.getLocale()).thenReturn(new Locale("un"));

        // Compare locale specific texts:
        ConfigurationItem itemDE = bean.getItems().get(0);
        String dnDE = itemDE.getDisplayName();
        String ttDE = itemDE.getTooltip();

        assertFalse(dnEN.equals(dnDE));
        assertFalse(ttEN.equals(ttDE));
        assertEquals("english", dnEN);
        assertEquals("english", ttEN);
        assertEquals(itemDE.getKey(), dnDE);
        assertEquals("", ttDE);
    }

    @Test
    public void testApplyCurrentUser() throws Exception {
        // Get test bean
        ConfigurationItem item = null;
        ConfigurationBean bean = getTestBean();

        // Set current user
        Mockito.when(httpSession.getAttribute(Matchers.eq("loggedInUserId")))
                .thenReturn("a_user");
        Mockito.when(
                httpSession.getAttribute(Matchers.eq("loggedInUserPassword")))
                .thenReturn("a_password");
        Mockito.when(httpSession.getAttribute(Matchers.eq("loggedInUserKey")))
                .thenReturn("10000");

        // Check default values
        List<ConfigurationItem> items = bean.getAccessItems();
        item = findItem(items, "BSS_USER_KEY");
        assertEquals("12345", item.getValue());
        item = findItem(items, "BSS_USER_ID");
        assertEquals("userId", item.getValue());
        item = findItem(items, "BSS_USER_PWD");
        assertEquals("secret1", item.getValue());

        // Set current user
        bean.applyCurrentUser();

        // Recheck values
        items = bean.getAccessItems();
        item = findItem(items, "BSS_USER_KEY");
        assertEquals("10000", item.getValue());
        item = findItem(items, "BSS_USER_ID");
        assertEquals("a_user", item.getValue());
        item = findItem(items, "BSS_USER_PWD");
        assertEquals("a_password", item.getValue());
    }

    @Test
    public void testTitle() throws Exception {
        // Get test bean
        ConfigurationBean bean = getTestBean();

        Mockito.when(
                controllerAccess.getMessage(Matchers.anyString(),
                        Matchers.eq("config_ui_title"))).thenReturn("title");

        String title = bean.getConfigurationTitle();
        assertEquals("title", title);
    }

    @Test
    public void testTitleMissing() throws Exception {
        // Get test bean
        ConfigurationBean bean = getTestBean();

        Mockito.when(
                controllerAccess.getMessage(Matchers.anyString(),
                        Matchers.eq("config_ui_title"))).thenReturn(
                "!config_ui_title!");

        String title = bean.getConfigurationTitle();
        assertTrue(title.contains("ess.common"));
    }

}
