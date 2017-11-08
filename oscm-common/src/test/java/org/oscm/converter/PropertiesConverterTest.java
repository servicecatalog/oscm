/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-11-12                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.converter;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Test;

/**
 * Unit tests for {@link PropertiesConverter}.
 * 
 * @author Gao
 */
public class PropertiesConverterTest {

    @Test
    public void propertiesToString() throws Exception {
        // given
        Properties properties = givenProperties();

        // when
        String result = PropertiesConverter.propertiesToString(properties);

        // then
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(result.contains("key2=value2")));
    }

    @Test
    public void propertiesToStringWithoutEmptyKeys() throws Exception {
        // given
        Properties properties = givenProperties();

        // when
        String result = PropertiesConverter
                .propertiesToStringIgnoreEmptyKeys(properties);

        // then
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(result.contains("key1=value1")));
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(result.contains("key2=value2")));
        assertEquals(Boolean.FALSE, Boolean.valueOf(result.contains("key3")));
    }

    @Test
    public void removeEmptyValue_Null() throws Exception {
        // given
        Properties properties = null;

        // when
        Properties resultProps = PropertiesConverter
                .removeEmptyValue(properties);

        // then
        assertEquals(null, resultProps);
    }

    @Test
    public void removeEmptyValue_NotNull() throws Exception {
        // given
        Properties properties = givenProperties();
        // when
        Properties resultProps = PropertiesConverter
                .removeEmptyValue(properties);
        // then
        assertEquals("value1", resultProps.get("key1"));
        assertEquals("value2", resultProps.get("key2"));
        assertEquals(null, resultProps.get("key3"));
    }

    @Test
    public void countNonEmptyValue_Null() throws Exception {
        // given
        Properties properties = null;

        // when
        int result = PropertiesConverter.countNonEmptyValue(properties);

        // then
        assertEquals(0, result);
    }

    @Test
    public void countNonEmptyValue_NotNull() throws Exception {
        // given
        Properties properties = givenProperties();

        // when
        int result = PropertiesConverter.countNonEmptyValue(properties);

        // then
        assertEquals(2, result);
    }

    private Properties givenProperties() {
        Properties properties = new Properties();
        properties.put("key1", "value1");
        properties.put("key2", "value2");
        properties.put("key3", "");
        return properties;
    }

}
