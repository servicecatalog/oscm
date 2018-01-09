/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 05.03.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.controller;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.app.iaas.PropertyHandler;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.intf.APPlatformService;

/**
 * @author iversen
 * 
 */
public class ProvisioningValidatorTest {

    private ProvisioningValidator provisioningValidator;
    private PropertyHandler paramHandler;
    private PropertyHandler oldParamsMock;
    private PropertyHandler oldParams;
    private PropertyHandler newParamsMock;
    private PropertyHandler newParams;
    private HashMap<String, Setting> parameters;
    private HashMap<String, Setting> newParameters;
    private APPlatformService platformService;
    private final String CONTROLLERID = "CONTROLLERID";
    private final String INSTANCENAME_PREFIX = "ess";

    private final String INSTANCENAME_CUSTOM = "demo";
    private final String INSTANCENAME_PATTERN = "ess([a-z0-9]){2,25}";

    @Before
    public void setUp() throws Exception {
        provisioningValidator = mock(ProvisioningValidator.class,
                Mockito.CALLS_REAL_METHODS);
        parameters = new HashMap<>();
        newParameters = new HashMap<>();
        HashMap<String, Setting> configSettings = new HashMap<>();
        ProvisioningSettings settings = new ProvisioningSettings(parameters,
                configSettings, "en");
        ProvisioningSettings newSettings = new ProvisioningSettings(
                newParameters, configSettings, "en");
        paramHandler = new PropertyHandler(settings);
        oldParams = new PropertyHandler(settings);
        newParams = new PropertyHandler(newSettings);
        oldParamsMock = mock(PropertyHandler.class);
        newParamsMock = mock(PropertyHandler.class);

        platformService = mock(APPlatformService.class);
    }

    @Test(expected = APPlatformException.class)
    public void validateExistingInstance_InstanceExists() throws Exception {
        // given
        parameters.put(PropertyHandler.INSTANCENAME_PREFIX, new Setting(
                PropertyHandler.INSTANCENAME_PREFIX, INSTANCENAME_PREFIX));
        parameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, INSTANCENAME_CUSTOM));
        doReturn(Boolean.TRUE).when(platformService).exists(CONTROLLERID,
                INSTANCENAME_PREFIX + INSTANCENAME_CUSTOM);

        // when
        provisioningValidator.validateExistingInstance(platformService,
                CONTROLLERID, paramHandler);

        // then an APPlatformException occurs
    }

    @Test
    public void validateExistingInstance() throws Exception {
        // given
        parameters.put(PropertyHandler.INSTANCENAME_PREFIX, new Setting(
                PropertyHandler.INSTANCENAME_PREFIX, INSTANCENAME_PREFIX));
        parameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, INSTANCENAME_CUSTOM));
        doReturn(Boolean.FALSE).when(platformService).exists(CONTROLLERID,
                INSTANCENAME_PREFIX + INSTANCENAME_CUSTOM);

        // when
        provisioningValidator.validateExistingInstance(platformService,
                CONTROLLERID, paramHandler);

        // then no exception occurs
    }

    @Test(expected = APPlatformException.class)
    public void validateInstanceName_EmptyInstanceName() throws Exception {
        // given
        parameters.put(PropertyHandler.INSTANCENAME_PREFIX, new Setting(
                PropertyHandler.INSTANCENAME_PREFIX, ""));
        parameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, ""));
        parameters.put(PropertyHandler.INSTANCENAME_PATTERN, new Setting(
                PropertyHandler.INSTANCENAME_PATTERN, INSTANCENAME_PATTERN));
        // when
        provisioningValidator.validateInstanceName(paramHandler);

        // then an APPlatformException occurs
    }

    @Test(expected = APPlatformException.class)
    public void validateInstanceName_PrefixNotMatching() throws Exception {
        // given
        parameters.put(PropertyHandler.INSTANCENAME_PREFIX, new Setting(
                PropertyHandler.INSTANCENAME_PREFIX, "nonMatchingPrefix"));
        parameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, INSTANCENAME_CUSTOM));
        parameters.put(PropertyHandler.INSTANCENAME_PATTERN, new Setting(
                PropertyHandler.INSTANCENAME_PATTERN, INSTANCENAME_PATTERN));
        // when
        provisioningValidator.validateInstanceName(paramHandler);

        // then an APPlatformException occurs
    }

    @Test(expected = APPlatformException.class)
    public void validateInstanceName_NameNotMatching() throws Exception {
        // given
        parameters.put(PropertyHandler.INSTANCENAME_PREFIX, new Setting(
                PropertyHandler.INSTANCENAME_PREFIX, INSTANCENAME_PREFIX));
        parameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, "nonMatchingName"));
        parameters.put(PropertyHandler.INSTANCENAME_PATTERN, new Setting(
                PropertyHandler.INSTANCENAME_PATTERN, INSTANCENAME_PATTERN));
        // when
        provisioningValidator.validateInstanceName(paramHandler);

        // then an APPlatformException occurs
    }

    @Test
    public void validateInstanceName() throws Exception {
        // given
        parameters.put(PropertyHandler.INSTANCENAME_PREFIX, new Setting(
                PropertyHandler.INSTANCENAME_PREFIX, INSTANCENAME_PREFIX));
        parameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, INSTANCENAME_CUSTOM));
        parameters.put(PropertyHandler.INSTANCENAME_PATTERN, new Setting(
                PropertyHandler.INSTANCENAME_PATTERN, INSTANCENAME_PATTERN));

        // when
        provisioningValidator.validateInstanceName(paramHandler);

        // then no exception occurs
    }

    @Test(expected = APPlatformException.class)
    public void validateParametersForVserverProvisioning_NullVSysId()
            throws Exception {
        // when
        provisioningValidator.validateParametersForVserverProvisioning(
                oldParamsMock, newParamsMock, false);

        // then an APPlatformException occurs
    }

    @Test(expected = APPlatformException.class)
    public void validateParametersForVserverProvisioning_EmptyVServerType()
            throws Exception {
        // given
        doReturn("vSysId").when(newParamsMock).getVsysId();
        doReturn("").when(newParamsMock).getVserverType();

        // when
        provisioningValidator.validateParametersForVserverProvisioning(
                oldParamsMock, newParamsMock, false);

        // then an APPlatformException occurs
    }

    @Test(expected = APPlatformException.class)
    public void validateParametersForVserverProvisioning_EmptyDiskImageId()
            throws Exception {
        // given
        doReturn("vSysId").when(newParamsMock).getVsysId();
        doReturn("vServerType").when(newParamsMock).getVserverType();
        doReturn("").when(newParamsMock).getDiskImageId();

        // when
        provisioningValidator.validateParametersForVserverProvisioning(
                oldParamsMock, newParamsMock, false);

        // then an APPlatformException occurs
    }

    @Test(expected = APPlatformException.class)
    public void validateParametersForVserverProvisioning_EmptyNetworkId()
            throws Exception {
        // given
        doReturn("vSysId").when(newParamsMock).getVsysId();
        doReturn("vServerType").when(newParamsMock).getVserverType();
        doReturn("diskImageId").when(newParamsMock).getDiskImageId();
        doReturn("").when(newParamsMock).getNetworkId();

        // when
        provisioningValidator.validateParametersForVserverProvisioning(
                oldParamsMock, newParamsMock, false);

        // then an APPlatformException occurs
    }

    @Test(expected = APPlatformException.class)
    public void validateParametersForVserverProvisioning_AdditionalDiskSelected_NoDiskName()
            throws Exception {

        boolean isAdditionalDiskSelected = true;
        doReturn("vSysId").when(newParamsMock).getVsysId();
        doReturn("vServerType").when(newParamsMock).getVserverType();
        doReturn("diskImageId").when(newParamsMock).getDiskImageId();
        doReturn("networkId").when(newParamsMock).getNetworkId();
        // given a null disk name for the new instance
        doReturn(null).when(newParamsMock).getVDiskNameCustom();

        // when
        provisioningValidator.validateParametersForVserverProvisioning(
                oldParamsMock, newParamsMock, isAdditionalDiskSelected);

        // then an APPlatformException occurs
    }

    @Test(expected = APPlatformException.class)
    public void validateParametersForVserverProvisioning_UnsupportedNameModification()
            throws Exception {
        doReturn("vSysId").when(newParamsMock).getVsysId();
        doReturn("vServerType").when(newParamsMock).getVserverType();
        doReturn("diskImageId").when(newParamsMock).getDiskImageId();
        doReturn("networkId").when(newParamsMock).getNetworkId();
        // given a modified instance name
        doReturn(INSTANCENAME_PREFIX + INSTANCENAME_CUSTOM).when(oldParamsMock)
                .getInstanceName();
        doReturn("newInstanceName").when(newParamsMock).getInstanceName();

        // when
        provisioningValidator.validateParametersForVserverProvisioning(
                oldParamsMock, newParamsMock, false);

        // then an APPlatformException occurs
    }

    @Test(expected = APPlatformException.class)
    public void validateParametersForVserverProvisioning_UnsupportedDiskImageModification()
            throws Exception {
        doReturn("vSysId").when(newParamsMock).getVsysId();
        doReturn("vServerType").when(newParamsMock).getVserverType();
        doReturn("networkId").when(newParamsMock).getNetworkId();

        doReturn(INSTANCENAME_PREFIX + INSTANCENAME_CUSTOM).when(oldParamsMock)
                .getInstanceName();
        doReturn(INSTANCENAME_PREFIX + INSTANCENAME_CUSTOM).when(newParamsMock)
                .getInstanceName();
        // given a modified disk image id
        doReturn("diskImageId").when(oldParamsMock).getDiskImageId();
        doReturn("newDiskImageId").when(newParamsMock).getDiskImageId();

        // when
        provisioningValidator.validateParametersForVserverProvisioning(
                oldParamsMock, newParamsMock, false);

        // then an APPlatformException occurs
    }

    @Test(expected = APPlatformException.class)
    public void validateParametersForVserverProvisioning_UnsupportedNetworkIdModification()
            throws Exception {
        doReturn("vSysId").when(newParamsMock).getVsysId();
        doReturn("vServerType").when(newParamsMock).getVserverType();

        doReturn(INSTANCENAME_PREFIX + INSTANCENAME_CUSTOM).when(oldParamsMock)
                .getInstanceName();
        doReturn("diskImageId").when(oldParamsMock).getDiskImageId();

        doReturn(INSTANCENAME_PREFIX + INSTANCENAME_CUSTOM).when(newParamsMock)
                .getInstanceName();
        doReturn("diskImageId").when(newParamsMock).getDiskImageId();

        // given a modified network id
        doReturn("networkId").when(oldParamsMock).getNetworkId();
        doReturn("newNetworkId").when(newParamsMock).getNetworkId();

        // when
        provisioningValidator.validateParametersForVserverProvisioning(
                oldParamsMock, newParamsMock, false);

        // then an APPlatformException occurs
    }

    @Test
    public void validateParametersForVserverProvisioning_NoAdditionalDiskSelected()
            throws Exception {
        // given
        boolean isAdditionalDiskSelected = false;
        doReturn("vSysId").when(newParamsMock).getVsysId();
        doReturn("vServerType").when(newParamsMock).getVserverType();

        doReturn(INSTANCENAME_PREFIX + INSTANCENAME_CUSTOM).when(oldParamsMock)
                .getInstanceName();
        doReturn("diskImageId").when(oldParamsMock).getDiskImageId();

        doReturn(INSTANCENAME_PREFIX + INSTANCENAME_CUSTOM).when(newParamsMock)
                .getInstanceName();
        doReturn("diskImageId").when(newParamsMock).getDiskImageId();

        doReturn("networkId").when(oldParamsMock).getNetworkId();
        doReturn("networkId").when(newParamsMock).getNetworkId();

        // when
        provisioningValidator.validateParametersForVserverProvisioning(
                oldParamsMock, newParamsMock, isAdditionalDiskSelected);

        // then no exception occurs
    }

    @Test(expected = APPlatformException.class)
    public void validateParametersForVserverProvisioning_UnsupportedDiskNameModification()
            throws Exception {
        boolean isAdditionalDiskSelected = true;
        doReturn("vSysId").when(newParamsMock).getVsysId();
        doReturn("vServerType").when(newParamsMock).getVserverType();

        doReturn(INSTANCENAME_PREFIX + INSTANCENAME_CUSTOM).when(oldParamsMock)
                .getInstanceName();
        doReturn("diskImageId").when(oldParamsMock).getDiskImageId();

        doReturn(INSTANCENAME_PREFIX + INSTANCENAME_CUSTOM).when(newParamsMock)
                .getInstanceName();
        doReturn("diskImageId").when(newParamsMock).getDiskImageId();

        doReturn("networkId").when(oldParamsMock).getNetworkId();
        doReturn("networkId").when(newParamsMock).getNetworkId();

        // given a modified VDisk name
        doReturn("vDiskName").when(oldParamsMock).getVDiskNameCustom();
        doReturn("newVDiskName").when(newParamsMock).getVDiskName();
        doReturn("newVDiskName").when(newParamsMock).getVDiskNameCustom();

        // when
        provisioningValidator.validateParametersForVserverProvisioning(
                oldParamsMock, newParamsMock, isAdditionalDiskSelected);

        // then an APPlatformException occurs
    }

    @Test(expected = APPlatformException.class)
    public void validateParametersForVserverProvisioning_UnsupportedDiskSizeModification()
            throws Exception {
        boolean isAdditionalDiskSelected = true;
        doReturn("vSysId").when(newParamsMock).getVsysId();
        doReturn("vServerType").when(newParamsMock).getVserverType();

        doReturn(INSTANCENAME_PREFIX + INSTANCENAME_CUSTOM).when(oldParamsMock)
                .getInstanceName();
        doReturn("diskImageId").when(oldParamsMock).getDiskImageId();

        doReturn(INSTANCENAME_PREFIX + INSTANCENAME_CUSTOM).when(newParamsMock)
                .getInstanceName();
        doReturn("diskImageId").when(newParamsMock).getDiskImageId();

        doReturn("networkId").when(oldParamsMock).getNetworkId();
        doReturn("networkId").when(newParamsMock).getNetworkId();

        doReturn("vDiskName").when(oldParamsMock).getVDiskNameCustom();
        doReturn("vDiskName").when(newParamsMock).getVDiskName();
        doReturn("vDiskName").when(newParamsMock).getVDiskNameCustom();

        // given
        doReturn("1").when(oldParamsMock).getVDiskSize();
        doReturn("2").when(newParamsMock).getVDiskSize();

        // when
        provisioningValidator.validateParametersForVserverProvisioning(
                oldParamsMock, newParamsMock, isAdditionalDiskSelected);

        // then an APPlatformException occurs
    }

    @Test
    public void validateParametersForVserverProvisioning_DiskSizeModification_ZeroDiskSize()
            throws Exception {
        boolean isAdditionalDiskSelected = true;
        doReturn("vSysId").when(newParamsMock).getVsysId();
        doReturn("vServerType").when(newParamsMock).getVserverType();

        doReturn(INSTANCENAME_PREFIX + INSTANCENAME_CUSTOM).when(oldParamsMock)
                .getInstanceName();
        doReturn("diskImageId").when(oldParamsMock).getDiskImageId();

        doReturn(INSTANCENAME_PREFIX + INSTANCENAME_CUSTOM).when(newParamsMock)
                .getInstanceName();
        doReturn("diskImageId").when(newParamsMock).getDiskImageId();

        doReturn("networkId").when(oldParamsMock).getNetworkId();
        doReturn("networkId").when(newParamsMock).getNetworkId();

        doReturn("vDiskName").when(oldParamsMock).getVDiskNameCustom();
        doReturn("vDiskName").when(newParamsMock).getVDiskName();
        doReturn("vDiskName").when(newParamsMock).getVDiskNameCustom();

        // given an initial disk size of 0
        doReturn("0").when(oldParamsMock).getVDiskSize();
        doReturn("2").when(newParamsMock).getVDiskSize();

        // when
        provisioningValidator.validateParametersForVserverProvisioning(
                oldParamsMock, newParamsMock, isAdditionalDiskSelected);

        // then no exception occurs
    }

    @Test
    public void validateParametersForVserverProvisioning_DiskSizeModification_ZeroNewDiskSize()
            throws Exception {
        boolean isAdditionalDiskSelected = true;
        doReturn("vSysId").when(newParamsMock).getVsysId();
        doReturn("vServerType").when(newParamsMock).getVserverType();

        doReturn(INSTANCENAME_PREFIX + INSTANCENAME_CUSTOM).when(oldParamsMock)
                .getInstanceName();
        doReturn("diskImageId").when(oldParamsMock).getDiskImageId();

        doReturn(INSTANCENAME_PREFIX + INSTANCENAME_CUSTOM).when(newParamsMock)
                .getInstanceName();
        doReturn("diskImageId").when(newParamsMock).getDiskImageId();

        doReturn("networkId").when(oldParamsMock).getNetworkId();
        doReturn("networkId").when(newParamsMock).getNetworkId();

        doReturn("vDiskName").when(oldParamsMock).getVDiskNameCustom();
        doReturn("vDiskName").when(newParamsMock).getVDiskName();
        doReturn("vDiskName").when(newParamsMock).getVDiskNameCustom();

        // given a new disk size of 0
        doReturn("2").when(oldParamsMock).getVDiskSize();
        doReturn("0").when(newParamsMock).getVDiskSize();

        // when
        provisioningValidator.validateParametersForVserverProvisioning(
                oldParamsMock, newParamsMock, isAdditionalDiskSelected);

        // then no exception occurs
    }

    @Test
    public void validateParametersForVserverProvisioning() throws Exception {
        // given everything correct
        boolean isAdditionalDiskSelected = true;
        doReturn("vSysId").when(newParamsMock).getVsysId();
        doReturn("vServerType").when(newParamsMock).getVserverType();

        doReturn(INSTANCENAME_PREFIX + INSTANCENAME_CUSTOM).when(oldParamsMock)
                .getInstanceName();
        doReturn("diskImageId").when(oldParamsMock).getDiskImageId();

        doReturn(INSTANCENAME_PREFIX + INSTANCENAME_CUSTOM).when(newParamsMock)
                .getInstanceName();
        doReturn("diskImageId").when(newParamsMock).getDiskImageId();

        doReturn("networkId").when(oldParamsMock).getNetworkId();
        doReturn("networkId").when(newParamsMock).getNetworkId();

        doReturn("vDiskName").when(oldParamsMock).getVDiskNameCustom();
        doReturn("vDiskName").when(newParamsMock).getVDiskName();
        doReturn("vDiskName").when(newParamsMock).getVDiskNameCustom();

        doReturn("2").when(oldParamsMock).getVDiskSize();
        doReturn("2").when(newParamsMock).getVDiskSize();

        // when
        provisioningValidator.validateParametersForVserverProvisioning(
                oldParamsMock, newParamsMock, isAdditionalDiskSelected);

        // then no exception occurs
    }

    @Test(expected = APPlatformException.class)
    public void validateParametersForVsysProvisiong_NoTemplate()
            throws Exception {
        // when
        provisioningValidator.validateParametersForVsysProvisioning(oldParams,
                newParams);

        // then throw APPlatformexception

    }

    @Test(expected = APPlatformException.class)
    public void validateParametersForVsysProvisiong_ClusterDefined_NonNumericClusterSize()
            throws Exception {
        // given
        newParameters.put(PropertyHandler.SYSTEM_TEMPLATE_ID, new Setting(
                PropertyHandler.SYSTEM_TEMPLATE_ID, "SYSTEM_TEMPLATE_ID"));
        newParameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, "CLUSTER_SIZE"));
        newParameters.put(PropertyHandler.MASTER_TEMPLATE_ID, new Setting(
                PropertyHandler.MASTER_TEMPLATE_ID, "MASTER_TEMPLATE_ID"));
        newParameters.put(PropertyHandler.SLAVE_TEMPLATE_ID, new Setting(
                PropertyHandler.SLAVE_TEMPLATE_ID, "SLAVE_TEMPLATE_ID"));

        // when
        provisioningValidator.validateParametersForVsysProvisioning(oldParams,
                newParams);
    }

    @Test(expected = APPlatformException.class)
    public void validateParametersForVsysProvisiong_UnsupportedNameModification()
            throws Exception {
        // given
        newParameters.put(PropertyHandler.SYSTEM_TEMPLATE_ID, new Setting(
                PropertyHandler.SYSTEM_TEMPLATE_ID, "SYSTEM_TEMPLATE_ID"));
        newParameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, "15"));
        newParameters.put(PropertyHandler.MASTER_TEMPLATE_ID, new Setting(
                PropertyHandler.MASTER_TEMPLATE_ID, "MASTER_TEMPLATE_ID"));
        newParameters.put(PropertyHandler.SLAVE_TEMPLATE_ID, new Setting(
                PropertyHandler.SLAVE_TEMPLATE_ID, "SLAVE_TEMPLATE_ID"));
        newParameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, "newInstance"));
        parameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, "instance"));

        // when
        provisioningValidator.validateParametersForVsysProvisioning(oldParams,
                newParams);
    }

    @Test(expected = APPlatformException.class)
    public void validateParametersForVsysProvisiong_InvalidClusterchange()
            throws Exception {
        // given
        newParameters.put(PropertyHandler.SYSTEM_TEMPLATE_ID, new Setting(
                PropertyHandler.SYSTEM_TEMPLATE_ID, "SYSTEM_TEMPLATE_ID"));
        newParameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, "instance"));

        parameters.put(PropertyHandler.SYSTEM_TEMPLATE_ID, new Setting(
                PropertyHandler.SYSTEM_TEMPLATE_ID, "SYSTEM_TEMPLATE_ID"));
        parameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, "15"));
        parameters.put(PropertyHandler.MASTER_TEMPLATE_ID, new Setting(
                PropertyHandler.MASTER_TEMPLATE_ID, "MASTER_TEMPLATE_ID"));
        parameters.put(PropertyHandler.SLAVE_TEMPLATE_ID, new Setting(
                PropertyHandler.SLAVE_TEMPLATE_ID, "SLAVE_TEMPLATE_ID"));
        parameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, "instance"));

        // when
        provisioningValidator.validateParametersForVsysProvisioning(oldParams,
                newParams);
    }

    @Test(expected = APPlatformException.class)
    public void validateParametersForVsysProvisiong_InvalidMasterDiskImageRename()
            throws Exception {
        // given
        newParameters.put(PropertyHandler.SYSTEM_TEMPLATE_ID, new Setting(
                PropertyHandler.SYSTEM_TEMPLATE_ID, "SYSTEM_TEMPLATE_ID"));
        newParameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, "15"));
        newParameters.put(PropertyHandler.MASTER_TEMPLATE_ID, new Setting(
                PropertyHandler.MASTER_TEMPLATE_ID, "MASTER_TEMPLATE_ID_1"));
        newParameters.put(PropertyHandler.SLAVE_TEMPLATE_ID, new Setting(
                PropertyHandler.SLAVE_TEMPLATE_ID, "SLAVE_TEMPLATE_ID"));

        newParameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, "instance"));

        parameters.put(PropertyHandler.SYSTEM_TEMPLATE_ID, new Setting(
                PropertyHandler.SYSTEM_TEMPLATE_ID, "SYSTEM_TEMPLATE_ID"));
        parameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, "15"));
        parameters.put(PropertyHandler.MASTER_TEMPLATE_ID, new Setting(
                PropertyHandler.MASTER_TEMPLATE_ID, "MASTER_TEMPLATE_ID_2"));
        parameters.put(PropertyHandler.SLAVE_TEMPLATE_ID, new Setting(
                PropertyHandler.SLAVE_TEMPLATE_ID, "SLAVE_TEMPLATE_ID"));
        parameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, "instance"));

        // when
        provisioningValidator.validateParametersForVsysProvisioning(oldParams,
                newParams);
    }

    @Test(expected = APPlatformException.class)
    public void validateParametersForVsysProvisiong_InvalidSlaveDiskImageRename()
            throws Exception {
        // given
        newParameters.put(PropertyHandler.SYSTEM_TEMPLATE_ID, new Setting(
                PropertyHandler.SYSTEM_TEMPLATE_ID, "SYSTEM_TEMPLATE_ID"));
        newParameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, "15"));
        newParameters.put(PropertyHandler.MASTER_TEMPLATE_ID, new Setting(
                PropertyHandler.MASTER_TEMPLATE_ID, "MASTER_TEMPLATE_ID"));
        newParameters.put(PropertyHandler.SLAVE_TEMPLATE_ID, new Setting(
                PropertyHandler.SLAVE_TEMPLATE_ID, "SLAVE_TEMPLATE_ID_1"));

        newParameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, "instance"));

        parameters.put(PropertyHandler.SYSTEM_TEMPLATE_ID, new Setting(
                PropertyHandler.SYSTEM_TEMPLATE_ID, "SYSTEM_TEMPLATE_ID"));
        parameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, "15"));
        parameters.put(PropertyHandler.MASTER_TEMPLATE_ID, new Setting(
                PropertyHandler.MASTER_TEMPLATE_ID, "MASTER_TEMPLATE_ID"));
        parameters.put(PropertyHandler.SLAVE_TEMPLATE_ID, new Setting(
                PropertyHandler.SLAVE_TEMPLATE_ID, "SLAVE_TEMPLATE_ID_2"));
        parameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, "instance"));

        // when
        provisioningValidator.validateParametersForVsysProvisioning(oldParams,
                newParams);
    }

    @Test
    public void validateParametersForVsysProvisiong_EqualSlaveTemplates()
            throws Exception {
        // given
        newParameters.put(PropertyHandler.SYSTEM_TEMPLATE_ID, new Setting(
                PropertyHandler.SYSTEM_TEMPLATE_ID, "SYSTEM_TEMPLATE_ID"));
        newParameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, "15"));
        newParameters.put(PropertyHandler.MASTER_TEMPLATE_ID, new Setting(
                PropertyHandler.MASTER_TEMPLATE_ID, "MASTER_TEMPLATE_ID"));
        newParameters.put(PropertyHandler.SLAVE_TEMPLATE_ID, new Setting(
                PropertyHandler.SLAVE_TEMPLATE_ID, "SLAVE_TEMPLATE_ID_1"));

        newParameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, "instance"));

        parameters.put(PropertyHandler.SYSTEM_TEMPLATE_ID, new Setting(
                PropertyHandler.SYSTEM_TEMPLATE_ID, "SYSTEM_TEMPLATE_ID"));
        parameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, "15"));
        parameters.put(PropertyHandler.MASTER_TEMPLATE_ID, new Setting(
                PropertyHandler.MASTER_TEMPLATE_ID, "MASTER_TEMPLATE_ID"));
        parameters.put(PropertyHandler.SLAVE_TEMPLATE_ID, new Setting(
                PropertyHandler.SLAVE_TEMPLATE_ID, "SLAVE_TEMPLATE_ID_1"));
        parameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, "instance"));

        // when
        provisioningValidator.validateParametersForVsysProvisioning(oldParams,
                newParams);
    }

    @Test
    public void validateParametersForVsysProvisiong_NoOldParams()
            throws Exception {
        // given
        newParameters.put(PropertyHandler.SYSTEM_TEMPLATE_ID, new Setting(
                PropertyHandler.SYSTEM_TEMPLATE_ID, "SYSTEM_TEMPLATE_ID"));
        newParameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, "15"));
        newParameters.put(PropertyHandler.MASTER_TEMPLATE_ID, new Setting(
                PropertyHandler.MASTER_TEMPLATE_ID, "MASTER_TEMPLATE_ID"));
        newParameters.put(PropertyHandler.SLAVE_TEMPLATE_ID, new Setting(
                PropertyHandler.SLAVE_TEMPLATE_ID, "SLAVE_TEMPLATE_ID_1"));

        newParameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, "instance"));

        // when
        provisioningValidator.validateParametersForVsysProvisioning(null,
                newParams);
    }

    @Test
    public void validateParametersForVsysProvisiong_NoClusterDefined()
            throws Exception {
        // given
        newParameters.put(PropertyHandler.SYSTEM_TEMPLATE_ID, new Setting(
                PropertyHandler.SYSTEM_TEMPLATE_ID, "SYSTEM_TEMPLATE_ID"));

        newParameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, "instance"));

        parameters.put(PropertyHandler.SYSTEM_TEMPLATE_ID, new Setting(
                PropertyHandler.SYSTEM_TEMPLATE_ID, "SYSTEM_TEMPLATE_ID"));

        parameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, "instance"));

        // when
        provisioningValidator.validateParametersForVsysProvisioning(oldParams,
                newParams);
    }
}
