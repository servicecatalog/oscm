/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.11.2013                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.internal.operatorservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.operatorservice.bean.OperatorServiceLocalBean;
import org.oscm.types.enumtypes.LocalizedDataType;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.PropertiesImportException;

/**
 * 
 * @author goebel
 * 
 */
public class LocalizedDataServiceBeanTest {

    private LocalizedDataServiceBean bean;
    private OperatorServiceLocalBean operatorService;
    private LocalizerServiceLocal localizer;

    @Before
    public void setUp() throws Exception {
        bean = new LocalizedDataServiceBean();
        operatorService = mock(OperatorServiceLocalBean.class);
        localizer = spy(new LocalizerServiceBean());
        bean.operatorService = operatorService;
        bean.localizer = localizer;
    }

    @Test(expected = IllegalArgumentException.class)
    public void importProperties_NullArgument() throws Exception,
            ObjectNotFoundException {
        bean.importProperties(null, null);
    }

    @Test
    public void importProperties_OK() throws OperationNotPermittedException,
            PropertiesImportException, ObjectNotFoundException {
        // given
        List<POLocalizedData> localizedData = new ArrayList<POLocalizedData>();
        POLocalizedData data = new POLocalizedData();
        data.setPropertiesMap(new HashMap<String, Properties>());
        data.setType(LocalizedDataType.MailProperties);
        localizedData.add(data);
        // when
        bean.importProperties(localizedData, "de");
        // then
        verify(operatorService, times(1)).saveProperties(
                eq(data.getPropertiesMap()), eq("de"),
                eq(LocalizedDataType.MailProperties));
    }

    @Test
    public void exportProperties_NullArgument() throws Exception {
        bean.exportProperties(null);
    }

    @Test
    public void exportProperties_OK() throws Exception {
        // given
        Map<String, Properties> map = new HashMap<String, Properties>();
        doReturn(map).when(operatorService).loadMessageProperties(eq("de"));
        doReturn(map).when(operatorService).loadMailProperties(eq("de"));
        doReturn(map).when(operatorService).loadPlatformObjects(eq("de"));
        // when
        List<POLocalizedData> result = bean.exportProperties("de");
        // then
        assertEquals(3, result.size());
        assertEquals(LocalizedDataType.MessageProperties, result.get(0)
                .getType());
        assertEquals(LocalizedDataType.MailProperties, result.get(1).getType());
        assertEquals(LocalizedDataType.PlatformObjects, result.get(2).getType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadMessageProperties_NullArgument() throws Exception {
        bean.loadMessageProperties(null);
    }

    @Test
    public void loadMessageProperties_OK() throws Exception {
        // given
        Properties prop = new Properties();
        doReturn(prop).when(operatorService).loadPropertiesFromDB(eq("de"));
        // when
        Properties result = bean.loadMessageProperties("de");
        // then
        verify(operatorService, times(1)).loadPropertiesFromDB(eq("de"));
        assertEquals(prop, result);
    }

    @Test
    public void loadMailPropertiesFromFile_OK() throws Exception {
        // given
        Properties props = new Properties();
        doReturn(props).when(localizer).loadLocalizedPropertiesFromFile(
                anyString(), eq("de"));
        // when
        Properties result = bean.loadMailPropertiesFromFile("de");
        // then
        verify(localizer, times(1)).loadLocalizedPropertiesFromFile(
                anyString(), eq("de"));
        assertEquals(props, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadMailPropertiesFromFile_null() throws Exception {
        // when
        try {
            bean.loadMailPropertiesFromFile(null);
            fail();
        } catch (IllegalArgumentException e) {
            // then
            verify(localizer, never()).loadLocalizedPropertiesFromFile(anyString(), anyString());
            throw e;
        }
    }

    @Test
    public void loadPlatformObjectPropertiesFromFile_OK() throws Exception {
        // given
        Properties props = new Properties();
        doReturn(props).when(operatorService).loadPlatformObjectsFromFile(
                eq("de"));
        // when
        Properties result = bean.loadPlatformObjectsFromFile("de");
        // then
        verify(operatorService, times(1)).loadPlatformObjectsFromFile(eq("de"));
        assertEquals(props, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadPlatformObjectPropertiesFromFile_null() throws Exception {
        // when
        try {
            bean.loadPlatformObjectsFromFile(null);
            fail();
        } catch (IllegalArgumentException e) {
            // then
            verify(operatorService, never()).loadPropertiesFromDB(anyString());
            throw e;
        }
    }

    @Test
    public void toPOLocalizedData_NullArgument() {
        assertNotNull(bean.toPOLocalizedData(null, null));
    }

    @Test
    public void toPOLocalizedData() {
        // given
        Map<String, Properties> propertiesMap = new HashMap<String, Properties>();
        LocalizedDataType type = LocalizedDataType.MessageProperties;
        // when
        POLocalizedData data = bean.toPOLocalizedData(propertiesMap, type);
        // then
        assertNotNull(data);
        assertEquals(propertiesMap, data.getPropertiesMap());
        assertEquals(type, data.getType());
    }
}
