/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 23.05.2016
 *
 *******************************************************************************/

package org.oscm.app.vmware.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.oscm.app.ui.SessionConstants;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.app.v2_0.intf.APPlatformController;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.model.VCenter;
import org.oscm.app.vmware.persistence.DataAccessService;
import org.oscm.app.vmware.remote.vmware.VMwareClient;
import org.oscm.test.ejb.TestNamingContextFactoryBuilder;

/**
 * @author kulle
 */
public class VMControllerTest {

	private final String CONTROLLER_ID = "ess.vmware";

	@Mock
	private APPlatformService platformService;
	@Mock
	private FacesContext facesContext;
	@Mock
	private ExternalContext externalContext;
	@Mock
	private HttpSession httpSession;
	@Mock
	private HttpServletRequest request;
	@Mock
	private Application application;
	@Mock
	private DataAccessService dataAccessService;
	@Mock
	private VMwareClient vmWareClient;

	@InjectMocks
	@Spy
	private VMController controller;
	private ProvisioningSettings settings;

	@Before
	public void before() {

		controller = new VMController();
		settings = new ProvisioningSettings(new HashMap<String, Setting>(), new HashMap<String, Setting>(), "en");

		MockitoAnnotations.initMocks(this);

		stubObjectsWithMocks();
	}

	private void stubObjectsWithMocks() {
		when(facesContext.getExternalContext()).thenReturn(externalContext);
		when(externalContext.getSession(Matchers.anyBoolean())).thenReturn(httpSession);
		when(externalContext.getRequest()).thenReturn(request);
		when(httpSession.getAttribute(Matchers.anyString())).thenReturn("mockUserId");
		when(facesContext.getApplication()).thenReturn(application);
		when(application.getDefaultLocale()).thenReturn(Locale.FRANCE);

		doReturn(httpSession).when(request).getSession();
		doReturn(facesContext).when(controller).getContext();

		doReturn(vmWareClient).when(controller).getClient();
	}

	@Test
	public void validateDataDiskMountPoints_noMountPointNoValidationPattern() throws Exception {
		// given
		VMPropertyHandler newParameters = new VMPropertyHandler(settings);

		// when
		controller.validateDataDiskMountPoints(newParameters);

		// then no exception is expected
	}

	@Test(expected = APPlatformException.class)
	public void validateDataDiskMountPoints_emptyMountPoint() throws Exception {
		// given
		VMPropertyHandler newParameters = new VMPropertyHandler(settings);
		settings.getParameters().put("DATA_DISK_TARGET_1", new Setting("DATA_DISK_TARGET_1", ""));
		settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_1",
				new Setting("DATA_DISK_TARGET_VALIDATION_1", "/opt/data"));

		// when
		controller.validateDataDiskMountPoints(newParameters);

		// then exception is expected
	}

	@Test
	public void validateDataDiskMountPoints_mointPointWithEmptyValidationPattern() throws Exception {
		// given
		VMPropertyHandler newParameters = new VMPropertyHandler(settings);
		settings.getParameters().put("DATA_DISK_TARGET_1", new Setting("DATA_DISK_TARGET_1", "/opt/data"));
		settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_1", new Setting("DATA_DISK_TARGET_VALIDATION_1", ""));

		// when
		controller.validateDataDiskMountPoints(newParameters);

		// then no exception is expected
	}

	@Test
	public void validateDataDiskMountPoints_mointPointWithMissingValidationPattern() throws Exception {
		// given
		VMPropertyHandler newParameters = new VMPropertyHandler(settings);
		settings.getParameters().put("DATA_DISK_TARGET_1", new Setting("DATA_DISK_TARGET_1", "/opt/data"));

		// when
		controller.validateDataDiskMountPoints(newParameters);

		// then no exception is expected
	}

	@Test
	public void validateDataDiskMountPoints_matches() throws Exception {
		// given
		VMPropertyHandler newParameters = new VMPropertyHandler(settings);
		settings.getParameters().put("DATA_DISK_TARGET_1", new Setting("DATA_DISK_TARGET_1", "/opt/data"));
		settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_1",
				new Setting("DATA_DISK_TARGET_VALIDATION_1", "/opt/data"));

		// when
		controller.validateDataDiskMountPoints(newParameters);

		// then no exception is expected
	}

	@Test(expected = APPlatformException.class)
	public void validateDataDiskMountPoints_noMatch() throws Exception {
		// given
		VMPropertyHandler newParameters = new VMPropertyHandler(settings);
		settings.getParameters().put("DATA_DISK_TARGET_1", new Setting("DATA_DISK_TARGET_1", "/opt/data"));
		settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_1",
				new Setting("DATA_DISK_TARGET_VALIDATION_1", "/opt/data2"));

		// when
		controller.validateDataDiskMountPoints(newParameters);

		// then exception expected
	}

	@Test(expected = APPlatformException.class)
	public void validateDataDiskMountPoints_validationPatternMissingMountPoint() throws Exception {

		// given
		VMPropertyHandler newParameters = new VMPropertyHandler(settings);
		settings.getParameters().put("DATA_DISK_TARGET_1", new Setting("DATA_DISK_TARGET_1", ""));
		settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_1",
				new Setting("DATA_DISK_TARGET_VALIDATION_1", "/opt/data2"));

		// when
		controller.validateDataDiskMountPoints(newParameters);

		// then exception expected
	}

	@Test
	public void validateDataDiskMountPoints_multipleDisks_matches() throws Exception {
		// given
		VMPropertyHandler newParameters = new VMPropertyHandler(settings);
		settings.getParameters().put("DATA_DISK_TARGET_1", new Setting("DATA_DISK_TARGET_1", "/opt/data"));
		settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_1",
				new Setting("DATA_DISK_TARGET_VALIDATION_1", "/opt/data"));
		settings.getParameters().put("DATA_DISK_TARGET_2", new Setting("DATA_DISK_TARGET_2", "/opt/data2"));
		settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_2",
				new Setting("DATA_DISK_TARGET_VALIDATION_2", "/opt/data2"));
		settings.getParameters().put("DATA_DISK_TARGET_4", new Setting("DATA_DISK_TARGET_4", "/opt/data4"));
		settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_4",
				new Setting("DATA_DISK_TARGET_VALIDATION_4", "/opt/data4"));

		// when
		controller.validateDataDiskMountPoints(newParameters);

		// then no exception is expected
	}

	@Test
	public void validateDataDiskMountPoints_multipleDisk_noMatch() throws Exception {
		// given
		VMPropertyHandler newParameters = new VMPropertyHandler(settings);
		settings.getParameters().put("DATA_DISK_TARGET_1", new Setting("DATA_DISK_TARGET_1", "/opt/data"));
		settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_1",
				new Setting("DATA_DISK_TARGET_VALIDATION_1", "/opt/data"));
		settings.getParameters().put("DATA_DISK_TARGET_2", new Setting("DATA_DISK_TARGET_2", "/opt/data"));
		settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_2",
				new Setting("DATA_DISK_TARGET_VALIDATION_2", "/opt/data2"));

		// when
		try {
			controller.validateDataDiskMountPoints(newParameters);
			fail();
		} catch (APPlatformException e) {
			// then
			assertTrue(e.getLocalizedMessage("en").contains("DATA_DISK_TARGET_2"));

		}

	}

	@SuppressWarnings("serial")
	@Test
	public void shouldNotValidateConfiguration_whenControllerVCenterParamsAreNull_givenCanPingRequest() {
		boolean result = false;
		Exception exception = null;
		setControllerContext();

		VCenter mockVCenter = new VCenter();
		mockVCenter.setUrl(null);
		mockVCenter.setUserid(null);
		mockVCenter.setPassword(null);

		try {
			when(platformService.getControllerSettings(anyString(), any())).thenReturn(new HashMap<String, Setting>() {
				{
					put("BSS_USER_KEY", new Setting("BSS_USER_KEY", "KEY"));
				}
			});
			doReturn(mockVCenter).when(controller).getVCenterList(any());

			result = controller.canPing();
		} catch (Exception e) {
			exception = e;
		}

		assertFalse("CanPing should not succeed", result);
		assertNotNull("Exception should be thrown", exception);
	}

	@Test
	public void canPing() throws ConfigurationException {
		setControllerContext();

		boolean result = controller.canPing();

		assertTrue("canPing should succeed", result);

	}

	@SuppressWarnings("serial")
	@Test
	public void shouldValidateConnection_whenServiceInstanceIsReachable_givenPingRequest() {
		boolean result = false;
		setControllerContext();

		VCenter mockVCenter = new VCenter();
		mockVCenter.setUrl("URL");
		mockVCenter.setUserid("USERID");
		mockVCenter.setPassword("PASSWORDK");

		try {
			when(platformService.getControllerSettings(anyString(), any())).thenReturn(new HashMap<String, Setting>() {
				{
					put("BSS_USER_KEY", new Setting("BSS_USER_KEY", "KEY"));
				}
			});
			doReturn(Arrays.asList(mockVCenter)).when(controller).getVCenterList(any());
			when(Boolean.valueOf(vmWareClient.isConnected())).thenReturn(Boolean.TRUE);

			controller.canPing();
			result = controller.ping(CONTROLLER_ID);

		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}

		assertTrue("CanPing should succeed", result);
	}

	private void setControllerContext() {
		doReturn(httpSession).when(request).getSession();
		doReturn("uid").when(httpSession).getAttribute(SessionConstants.SESSION_USER_ID);
		doReturn("en").when(httpSession).getAttribute(SessionConstants.SESSION_USER_LOCALE);

		try {
			if (!NamingManager.hasInitialContextFactoryBuilder()) {
				NamingManager.setInitialContextFactoryBuilder(new TestNamingContextFactoryBuilder());
			}
			InitialContext context = new InitialContext();
			context.bind(APPlatformController.JNDI_PREFIX + CONTROLLER_ID, controller);
			context.bind("BSSAppVMwareDS", dataAccessService);
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
}
