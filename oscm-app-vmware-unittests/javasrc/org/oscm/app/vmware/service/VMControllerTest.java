/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 23.05.2016
 *
 *******************************************************************************/

package org.oscm.app.vmware.service;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.vmware.business.VMPropertyHandler;

import static org.junit.Assert.*;

/**
 * @author kulle
 *
 */
public class VMControllerTest {

    @InjectMocks
    private VMController controller;
    private ProvisioningSettings settings;

    @Before
    public void before() {
        controller = new VMController();
        settings = new ProvisioningSettings(new HashMap<String, Setting>(),
                new HashMap<String, Setting>(), "en");
    }

    @Test
    public void validateDataDiskMountPoints_noMountPointNoValidationPattern()
            throws Exception {
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
        settings.getParameters().put("DATA_DISK_TARGET_1",
                new Setting("DATA_DISK_TARGET_1", ""));
        settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_1",
                new Setting("DATA_DISK_TARGET_VALIDATION_1", "/opt/data"));

        // when
        controller.validateDataDiskMountPoints(newParameters);

        // then exception is expected
    }

    @Test
    public void validateDataDiskMountPoints_mointPointWithEmptyValidationPattern()
            throws Exception {
        // given
        VMPropertyHandler newParameters = new VMPropertyHandler(settings);
        settings.getParameters().put("DATA_DISK_TARGET_1",
                new Setting("DATA_DISK_TARGET_1", "/opt/data"));
        settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_1",
                new Setting("DATA_DISK_TARGET_VALIDATION_1", ""));

        // when
        controller.validateDataDiskMountPoints(newParameters);

        // then no exception is expected
    }

    @Test
    public void validateDataDiskMountPoints_mointPointWithMissingValidationPattern()
            throws Exception {
        // given
        VMPropertyHandler newParameters = new VMPropertyHandler(settings);
        settings.getParameters().put("DATA_DISK_TARGET_1",
                new Setting("DATA_DISK_TARGET_1", "/opt/data"));

        // when
        controller.validateDataDiskMountPoints(newParameters);

        // then no exception is expected
    }

    @Test
    public void validateDataDiskMountPoints_matches() throws Exception {
        // given
        VMPropertyHandler newParameters = new VMPropertyHandler(settings);
        settings.getParameters().put("DATA_DISK_TARGET_1",
                new Setting("DATA_DISK_TARGET_1", "/opt/data"));
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
        settings.getParameters().put("DATA_DISK_TARGET_1",
                new Setting("DATA_DISK_TARGET_1", "/opt/data"));
        settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_1",
                new Setting("DATA_DISK_TARGET_VALIDATION_1", "/opt/data2"));

        // when
        controller.validateDataDiskMountPoints(newParameters);

        // then exception expected
    }

    @Test(expected = APPlatformException.class)
    public void validateDataDiskMountPoints_validationPatternMissingMountPoint()
            throws Exception {

        // given
        VMPropertyHandler newParameters = new VMPropertyHandler(settings);
        settings.getParameters().put("DATA_DISK_TARGET_1",
                new Setting("DATA_DISK_TARGET_1", ""));
        settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_1",
                new Setting("DATA_DISK_TARGET_VALIDATION_1", "/opt/data2"));

        // when
        controller.validateDataDiskMountPoints(newParameters);

        // then exception expected
    }

    @Test
    public void validateDataDiskMountPoints_multipleDisks_matches()
            throws Exception {
        // given
        VMPropertyHandler newParameters = new VMPropertyHandler(settings);
        settings.getParameters().put("DATA_DISK_TARGET_1",
                new Setting("DATA_DISK_TARGET_1", "/opt/data"));
        settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_1",
                new Setting("DATA_DISK_TARGET_VALIDATION_1", "/opt/data"));
        settings.getParameters().put("DATA_DISK_TARGET_2",
                new Setting("DATA_DISK_TARGET_2", "/opt/data2"));
        settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_2",
                new Setting("DATA_DISK_TARGET_VALIDATION_2", "/opt/data2"));
        settings.getParameters().put("DATA_DISK_TARGET_4",
                new Setting("DATA_DISK_TARGET_4", "/opt/data4"));
        settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_4",
                new Setting("DATA_DISK_TARGET_VALIDATION_4", "/opt/data4"));

        // when
        controller.validateDataDiskMountPoints(newParameters);

        // then no exception is expected
    }

    @Test
    public void validateDataDiskMountPoints_multipleDisk_noMatch()
            throws Exception {
        // given
        VMPropertyHandler newParameters = new VMPropertyHandler(settings);
        settings.getParameters().put("DATA_DISK_TARGET_1",
                new Setting("DATA_DISK_TARGET_1", "/opt/data"));
        settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_1",
                new Setting("DATA_DISK_TARGET_VALIDATION_1", "/opt/data"));
        settings.getParameters().put("DATA_DISK_TARGET_2",
                new Setting("DATA_DISK_TARGET_2", "/opt/data"));
        settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_2",
                new Setting("DATA_DISK_TARGET_VALIDATION_2", "/opt/data2"));

        // when
        try {
            controller.validateDataDiskMountPoints(newParameters);
            fail();
        } catch (APPlatformException e) {
            // then
            assertTrue(
                    e.getLocalizedMessage("en").contains("DATA_DISK_TARGET_2"));

        }

    }

    @Test
    @Ignore("Not implemented")
    public void shouldValidateConfiguration_whenControllerConfigIsValid_givenCanPingRequest() {
        //TODO: Prep step - mockito?

        boolean result = controller.canPing();

        assertTrue("Controller configuration should be valid", result);
    }

    @Test
    @Ignore("Not implemented")
    public void shouldNotValidateConfiguration_whenControllerConfigIsInvalid_givenCanPingRequest() {
        //TODO: Prep step - mockito?

        boolean result = controller.canPing();

        assertFalse("Controller configuration should be invalid", result);
    }

    @Test
    @Ignore("Not implemented")
    public void shouldValidateConnection_whenServiceInstanceIsReachable_givenPingRequest() {
        //TODO: Prep step - mockito?
        boolean result = false;
        Exception exception = null;

        try {
            result = controller.ping("vmwareControllerId");
        } catch (APPlatformException ex) {
            exception = ex;
        }

        assertTrue("Instance should be reachable", result);
        assertNull("There should be no exception thrown", exception);
    }

    @Test
    @Ignore("Not implemented")
    public void shouldNotValidateConnection_whenServiceInstanceIsUnreachable_givenPingRequest() {
        //TODO: Prep step - mockito?
        boolean result = true;
        Exception exception = null;

        try {
            result = controller.ping("vmwareControllerId");
        } catch (APPlatformException ex) {
            exception = ex;
        }

        assertFalse("Instance should not be reachable", result);
        assertNull("There should be no exception thrown", exception);
    }
}
