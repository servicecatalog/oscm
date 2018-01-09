/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.concurrent.Callable;

import org.junit.Assert;

import org.junit.Test;

import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.exception.SaaSSystemException;

public class ConfigurationSettingIT extends DomainObjectTestBase {

    @Test
    public void testCreation() throws Exception {
        final ConfigurationSetting setting = runTX(new Callable<ConfigurationSetting>() {

            public ConfigurationSetting call() throws Exception {
                ConfigurationSetting cs = new ConfigurationSetting();
                ConfigurationSettingData data = new ConfigurationSettingData();
                data.setContextId("some_context");
                data.setInformationId(ConfigurationKey.BASE_URL);
                data.setValue("some value");
                cs.setDataContainer(data);
                mgr.persist(cs);
                return cs;
            }
        });
        runTX(new Callable<Void>() {

            public Void call() throws Exception {
                ConfigurationSettingData cs = mgr.getReference(
                        ConfigurationSetting.class, setting.getKey())
                        .getDataContainer();
                Assert.assertEquals(setting.getContextId(), cs.getContextId());
                Assert.assertEquals(setting.getInformationId(), cs
                        .getInformationId());
                Assert.assertEquals(setting.getValue(), cs.getValue());
                return null;
            }
        });
    }

    @Test
    public void testGetIntValue() throws Exception {
        ConfigurationSetting cs = new ConfigurationSetting();
        cs.setContextId("some_context");
        cs.setInformationId(ConfigurationKey.LOG_LEVEL);
        cs.setValue("12345");
        Assert.assertEquals(12345, cs.getIntValue());
    }

    @Test
    public void testGetIntValueNoValue() throws Exception {
        ConfigurationSetting cs = new ConfigurationSetting();
        cs.setContextId("some_context");
        cs.setInformationId(ConfigurationKey.LOG_LEVEL);
        Assert.assertEquals(0, cs.getIntValue());
    }

    @Test(expected = SaaSSystemException.class)
    public void testGetIntValueInvalidFormat() throws Exception {
        ConfigurationSetting cs = new ConfigurationSetting();
        cs.setContextId("some_context");
        cs.setInformationId(ConfigurationKey.LOG_LEVEL);
        cs.setValue("some value");
        cs.getIntValue();
    }

}
