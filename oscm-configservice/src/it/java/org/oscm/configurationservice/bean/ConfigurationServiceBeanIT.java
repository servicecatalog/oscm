/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: jaeger                                         
 *                                                                              
 *  Creation Date: 22.01.2009                                                      
 *                                                                              
 *  Completion Time: 13.12.2011                                          
 *                                                                              
 *******************************************************************************/

package org.oscm.configurationservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.oscm.test.matchers.BesMatchers.hasAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;
import javax.ejb.Lock;
import javax.ejb.Schedule;
import javax.persistence.TypedQuery;

import org.junit.Test;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.vo.VOConfigurationSetting;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.types.constants.Configuration;

/**
 * @author jaeger
 */
public class ConfigurationServiceBeanIT extends EJBTestBase {

    private ConfigurationServiceBean confSvc;
    private ConfigurationServiceLocal confSvcLocal;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new ConfigurationServiceBean());
        confSvc = container.get(ConfigurationServiceBean.class);
        confSvcLocal = container.get(ConfigurationServiceLocal.class);
        confSvc.init();
    }

    @Test(expected = Exception.class)
    public void testGetConfigurationSetting_ForNullContext() throws Exception {
        final ConfigurationSetting setting = new ConfigurationSetting(
                ConfigurationKey.BASE_URL, Configuration.GLOBAL_CONTEXT, "bla");
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                confSvcLocal.setConfigurationSetting(setting);
                return null;
            }
        });

        confSvc.getVOConfigurationSetting(ConfigurationKey.BASE_URL, null);
    }

    @Test
    public void testGetConfigurationSetting_ForNonNullContext()
            throws Exception {
        final ConfigurationSetting initSetting = new ConfigurationSetting(
                ConfigurationKey.BASE_URL, "context2", "anotherValue");
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                confSvcLocal.setConfigurationSetting(initSetting);
                return null;
            }
        });
        VOConfigurationSetting setting = confSvc.getVOConfigurationSetting(
                ConfigurationKey.BASE_URL, "context2");
        assertNotNull(setting);
    }

    @Test
    public void testSetConfigurationSetting_NotExistingNonNullContext()
            throws Exception {
        final ConfigurationSetting initialSetting = new ConfigurationSetting(
                ConfigurationKey.BASE_URL, "context3", "testValueForSet");
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                confSvcLocal.setConfigurationSetting(initialSetting);
                return null;
            }
        });
        VOConfigurationSetting setting = confSvc.getVOConfigurationSetting(
                ConfigurationKey.BASE_URL, "context3");
        assertNotNull(setting);
    }

    @Test
    public void testSetConfigurationSetting_ExistingSettingNonNullContext()
            throws Exception {
        final ConfigurationSetting initialSetting = new ConfigurationSetting(
                ConfigurationKey.BASE_URL, "context", "testValueForSet");
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                confSvcLocal.setConfigurationSetting(initialSetting);
                return null;
            }
        });
        final ConfigurationSetting initialSetting1 = new ConfigurationSetting(
                ConfigurationKey.BASE_URL, "context", "testValueForSet2");
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                confSvcLocal.setConfigurationSetting(initialSetting1);
                return null;
            }
        });
        VOConfigurationSetting setting = confSvc.getVOConfigurationSetting(
                ConfigurationKey.BASE_URL, "context");
        assertNotNull(setting);
        assertEquals("testValueForSet2", setting.getValue());
    }

    @Test
    public void testSetConfigurationSetting_EmptyValueOptional()
            throws Exception {
        // must not be saved
        final ConfigurationSetting setting = createConfigurationSetting("   ",
                false);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                confSvcLocal.setConfigurationSetting(setting);
                return null;
            }
        });
        ConfigurationSetting read = runTX(new Callable<ConfigurationSetting>() {
            @Override
            public ConfigurationSetting call() {
                return confSvcLocal.getConfigurationSetting(
                        setting.getInformationId(), setting.getContextId());
            }
        });
        // must be a fresh object created with default value
        assertEquals(0, read.getKey());
        assertEquals(setting.getInformationId().getFallBackValue(),
                read.getValue());
    }

    @Test
    public void testSetConfigurationSetting_NullValueOptional()
            throws Exception {
        // must not be saved
        final ConfigurationSetting setting = createConfigurationSetting(null,
                false);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                confSvcLocal.setConfigurationSetting(setting);
                return null;
            }
        });
        ConfigurationSetting read = runTX(new Callable<ConfigurationSetting>() {
            @Override
            public ConfigurationSetting call() {
                return confSvcLocal.getConfigurationSetting(
                        setting.getInformationId(), setting.getContextId());
            }
        });
        // must be a fresh object created with default value
        assertEquals(0, read.getKey());
        assertEquals(setting.getInformationId().getFallBackValue(),
                read.getValue());
    }

    @Test
    public void testSetConfigurationSetting_DeleteOptional() throws Exception {
        // first save it
        final ConfigurationSetting setting = createConfigurationSetting("test",
                false);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                confSvcLocal.setConfigurationSetting(setting);
                return null;
            }
        });
        final ConfigurationSetting read = runTX(
                new Callable<ConfigurationSetting>() {
                    @Override
                    public ConfigurationSetting call() {
                        return confSvcLocal.getConfigurationSetting(
                                setting.getInformationId(),
                                setting.getContextId());
                    }
                });
        assertEquals(setting.getInformationId(), read.getInformationId());
        assertEquals(setting.getValue(), read.getValue());
        // and now delete it by setting value to empty string
        read.setValue("  ");
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                confSvcLocal.setConfigurationSetting(read);
                return null;
            }
        });
        // now check that the default value is used again
        ConfigurationSetting read1 = runTX(
                new Callable<ConfigurationSetting>() {
                    @Override
                    public ConfigurationSetting call() {
                        return confSvcLocal.getConfigurationSetting(
                                setting.getInformationId(),
                                setting.getContextId());
                    }
                });
        // must be a fresh object created with default value
        assertEquals(0, read1.getKey());
        assertEquals(setting.getInformationId().getFallBackValue(),
                read1.getValue());
    }

    @Test
    public void testGetConfigurationSettings_OneHit() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                confSvcLocal.setConfigurationSetting(
                        new ConfigurationSetting(ConfigurationKey.BASE_URL,
                                Configuration.GLOBAL_CONTEXT, "initialValue"));
                return null;
            }
        });
        List<ConfigurationSetting> result = runTX(
                new Callable<List<ConfigurationSetting>>() {
                    @Override
                    public List<ConfigurationSetting> call() {
                        return confSvcLocal.getAllConfigurationSettings();
                    }
                });
        assertNotNull(result);
        assertEquals(1, result.size());
        ConfigurationSetting entry = result.get(0);
        assertEquals(ConfigurationKey.BASE_URL, entry.getInformationId());
        assertEquals("initialValue", entry.getValue());
    }

    @Test
    public void testGetConfigurationSettings_NoHits() throws Exception {
        List<ConfigurationSetting> result = runTX(
                new Callable<List<ConfigurationSetting>>() {
                    @Override
                    public List<ConfigurationSetting> call() {
                        return confSvcLocal.getAllConfigurationSettings();
                    }
                });
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(expected = EJBException.class)
    public void testGetConfigurationSetting_NoHitsException() throws Exception {
        final ConfigurationSetting setting = createConfigurationSetting("test",
                true);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                confSvcLocal.setConfigurationSetting(setting);
                return null;
            }
        });

        // BASE_URL_HTTPS not set, but mandatory
        confSvcLocal.getConfigurationSetting(ConfigurationKey.BASE_URL_HTTPS,
                "");
    }

    @Test
    public void testGetConfigurationSetting_NoHitsUseGlobalContext()
            throws Exception {
        String value = "test";
        final ConfigurationSetting setting = createConfigurationSetting(value,
                false);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                confSvcLocal.setConfigurationSetting(setting);
                return null;
            }
        });

        // empty context id, global context will be used
        assertEquals(value,
                confSvcLocal
                        .getConfigurationSetting(ConfigurationKey.LOG_LEVEL, "")
                        .getValue());
    }

    @Test
    public void testGetNodeName() {
        assertEquals("SingleNode", confSvcLocal.getNodeName());
        System.setProperty("bss.nodename", "local");
        assertEquals("local", confSvcLocal.getNodeName());
    }

    @Test
    public void testGetConfigurationSettings_MultipleHits() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                confSvcLocal.setConfigurationSetting(
                        new ConfigurationSetting(ConfigurationKey.BASE_URL,
                                Configuration.GLOBAL_CONTEXT, "initialValue"));
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                confSvcLocal.setConfigurationSetting(new ConfigurationSetting(
                        ConfigurationKey.HIDDEN_UI_ELEMENTS,
                        Configuration.GLOBAL_CONTEXT, "initialValue2"));
                return null;
            }
        });
        List<ConfigurationSetting> result = runTX(
                new Callable<List<ConfigurationSetting>>() {
                    @Override
                    public List<ConfigurationSetting> call() {
                        return confSvcLocal.getAllConfigurationSettings();
                    }
                });
        assertNotNull(result);
        assertEquals(2, result.size());
        ConfigurationSetting entry = result.get(0);
        assertEquals(ConfigurationKey.BASE_URL, entry.getInformationId());
        assertEquals("initialValue", entry.getValue());
        entry = result.get(1);
        assertEquals(ConfigurationKey.HIDDEN_UI_ELEMENTS,
                entry.getInformationId());
        assertEquals("initialValue2", entry.getValue());
    }

    private static ConfigurationSetting createConfigurationSetting(String value,
            boolean mandatory) {
        ConfigurationKey key;
        if (mandatory) {
            key = ConfigurationKey.LOG_FILE_PATH;
        } else {
            key = ConfigurationKey.LOG_LEVEL;
        }
        return new ConfigurationSetting(key, Configuration.GLOBAL_CONTEXT,
                value);
    }

    @Test
    public void init() {
        // given
        ConfigurationServiceBean service = spy(new ConfigurationServiceBean());
        service.dm = mock(DataService.class);
        doReturn(mock(TypedQuery.class)).when(service.dm)
                .createNamedQuery(anyString(), eq(ConfigurationSetting.class));

        // when
        service.init();

        // then
        verify(service, times(1)).refreshCache();
    }

    @Test
    public void refreshCache() {
        // when
        ConfigurationServiceBean service = spy(new ConfigurationServiceBean());
        doReturn(givenConfigurationSettings()).when(service)
                .getAllConfigurationSettings();

        // given
        service.refreshCache();

        // then
        service.cache.containsKey(ConfigurationKey.BASE_URL);
    }

    private List<ConfigurationSetting> givenConfigurationSettings() {
        List<ConfigurationSetting> result = new ArrayList<>();
        result.add(new ConfigurationSetting(ConfigurationKey.BASE_URL,
                "aContext", "aValue"));
        return result;
    }

    /**
     * Verify if the cache is refreshed every 10 minutes and the method is
     * locked properly.
     */
    @Test
    public void refreshCache_checkAnnotations() throws Exception {
        // given
        Method method = ConfigurationServiceBean.class
                .getMethod("refreshCache");
        List<Annotation> annotations = givenRefreshCacheAnnotations();

        // when
        assertThat(method, hasAnnotation(annotations));

        // then no exception
    }

    private List<Annotation> givenRefreshCacheAnnotations() {
        List<Annotation> result = new ArrayList<>();
        Annotation schedule = mock(Annotation.class);
        doReturn(Schedule.class).when(schedule).annotationType();
        doReturn("minute = \"*/10\"").when(schedule).toString();
        result.add(schedule);

        result.add(createLockAnnotation("LockType.WRITE"));
        return result;
    }

    private Annotation createLockAnnotation(String type) {
        Annotation lock = mock(Annotation.class);
        doReturn(Lock.class).when(lock).annotationType();
        doReturn(type).when(lock).toString();
        return lock;
    }

    @Test
    public void setConfigurationSetting_refreshCacheCalled() throws Exception {
        // given
        ConfigurationServiceBean service = spy(new ConfigurationServiceBean());
        service.dm = mock(DataService.class);
        doReturn(mock(TypedQuery.class)).when(service.dm)
                .createNamedQuery(anyString(), eq(ConfigurationSetting.class));

        // when
        service.setConfigurationSetting(new ConfigurationSetting());

        // then
        verify(service, times(1)).refreshCache();
    }

    @Test
    public void setConfigurationSetting_writeLockSet() throws Exception {
        // given
        Method method = ConfigurationServiceBean.class.getMethod(
                "setConfigurationSetting", ConfigurationSetting.class);
        List<Annotation> annotations = givenSetConfigurationSettingAnnotations();

        // when
        assertThat(method, hasAnnotation(annotations));

        // then no exception
    }

    private List<Annotation> givenSetConfigurationSettingAnnotations() {
        return Arrays.asList(createLockAnnotation("LockType.WRITE"));
    }

    @Test
    public void ConfigurationServiceBean_readLockSet() {
        // given
        List<Annotation> annotations = Arrays
                .asList(createLockAnnotation("LockType.READ"));

        // when
        assertThat(ConfigurationServiceBean.class, hasAnnotation(annotations));

        // then no exception
    }

    @Test
    public void ConfigurationServiceBean_readLockSet1() {
        // given
        List<Annotation> annotations = Arrays
                .asList(createLockAnnotation("LockType.READ"));

        // when
        assertThat(ConfigurationServiceBean.class, hasAnnotation(annotations));

        // then no exception
    }

    @Test
    public void ConfigurationServiceBean_getBaseUrl_EmptyBaseUrl()
            throws Exception {
        // given
        final ConfigurationSetting settingHttp = new ConfigurationSetting(
                ConfigurationKey.BASE_URL, Configuration.GLOBAL_CONTEXT, "");
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                confSvcLocal.setConfigurationSetting(settingHttp);
                return null;
            }
        });
        final ConfigurationSetting settingHttps = new ConfigurationSetting(
                ConfigurationKey.BASE_URL_HTTPS, Configuration.GLOBAL_CONTEXT,
                "initialValue");
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                confSvcLocal.setConfigurationSetting(settingHttps);
                return null;
            }
        });
        // when
        String result = confSvc.getBaseURL();
        // then no exception
        assertEquals("initialValue", result);
    }

    @Test
    public void ConfigurationServiceBean_getBaseUrl_NullBaseUrl()
            throws Exception {
        // given
        final ConfigurationSetting settingHttp = new ConfigurationSetting(
                ConfigurationKey.BASE_URL, Configuration.GLOBAL_CONTEXT, null);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                confSvcLocal.setConfigurationSetting(settingHttp);
                return null;
            }
        });
        final ConfigurationSetting settingHttps = new ConfigurationSetting(
                ConfigurationKey.BASE_URL_HTTPS, Configuration.GLOBAL_CONTEXT,
                "initialValue");
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                confSvcLocal.setConfigurationSetting(settingHttps);
                return null;
            }
        });
        // when
        String result = confSvc.getBaseURL();
        // then no exception
        assertEquals("initialValue", result);
    }

}
