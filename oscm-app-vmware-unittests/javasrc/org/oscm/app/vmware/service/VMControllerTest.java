/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2016                                           
 *                                                                                                                                 
 *  Creation Date: 23.05.2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.vmware.service;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.oscm.app.v1_0.data.ProvisioningSettings;
import org.oscm.app.v1_0.exceptions.APPlatformException;
import org.oscm.app.vmware.business.VMPropertyHandler;

/**
 * @author kulle
 *
 */
public class VMControllerTest {

    private VMController controller;
    private ProvisioningSettings settings;

    @Before
    public void before() {
        controller = new VMController();
        settings = new ProvisioningSettings(new HashMap<String, String>(),
                new HashMap<String, String>(), "en");
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
        settings.getParameters().put("DATA_DISK_TARGET_1", "");
        settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_1",
                "/opt/data");

        // when
        controller.validateDataDiskMountPoints(newParameters);

        // then exception is expected
    }

    @Test
    public void validateDataDiskMountPoints_mointPointWithEmptyValidationPattern()
            throws Exception {
        // given
        VMPropertyHandler newParameters = new VMPropertyHandler(settings);
        settings.getParameters().put("DATA_DISK_TARGET_1", "/opt/data");
        settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_1", "");

        // when
        controller.validateDataDiskMountPoints(newParameters);

        // then no exception is expected
    }

    @Test
    public void validateDataDiskMountPoints_mointPointWithMissingValidationPattern()
            throws Exception {
        // given
        VMPropertyHandler newParameters = new VMPropertyHandler(settings);
        settings.getParameters().put("DATA_DISK_TARGET_1", "/opt/data");

        // when
        controller.validateDataDiskMountPoints(newParameters);

        // then no exception is expected
    }

    @Test
    public void validateDataDiskMountPoints_matches() throws Exception {
        // given
        VMPropertyHandler newParameters = new VMPropertyHandler(settings);
        settings.getParameters().put("DATA_DISK_TARGET_1", "/opt/data");
        settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_1",
                "/opt/data");

        // when
        controller.validateDataDiskMountPoints(newParameters);

        // then no exception is expected
    }

    @Test(expected = APPlatformException.class)
    public void validateDataDiskMountPoints_noMatch() throws Exception {
        // given
        VMPropertyHandler newParameters = new VMPropertyHandler(settings);
        settings.getParameters().put("DATA_DISK_TARGET_1", "/opt/data");
        settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_1",
                "/opt/data2");

        // when
        controller.validateDataDiskMountPoints(newParameters);

        // then exception expected
    }

    @Test(expected = APPlatformException.class)
    public void validateDataDiskMountPoints_validationPatternMissingMountPoint()
            throws Exception {

        // given
        VMPropertyHandler newParameters = new VMPropertyHandler(settings);
        settings.getParameters().put("DATA_DISK_TARGET_1", "");
        settings.getParameters().put("DATA_DISK_TARGET_VALIDATION_1",
                "/opt/data2");

        // when
        controller.validateDataDiskMountPoints(newParameters);

        // then exception expected
    }

}
