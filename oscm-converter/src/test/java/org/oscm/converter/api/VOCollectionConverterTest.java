/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.02.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.converter.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import org.oscm.internal.types.enumtypes.OperationParameterType;
import org.oscm.internal.vo.VOServiceOperationParameterValues;

/**
 * @author kulle
 * 
 */
public class VOCollectionConverterTest {

    VOCollectionConverter conv;

    class VOUnknown {

    }

    @Test
    public void before() {
        conv = new VOCollectionConverter();
    }

    @Test
    public void convertList_null() {
        assertNull(VOCollectionConverter.convertList(null,
                org.oscm.internal.vo.VOUser.class));
    }

    @SuppressWarnings("cast")
    @Test
    public void convertList() {
        // given
        org.oscm.vo.VOUser user = new org.oscm.vo.VOUser();

        // when
        List<org.oscm.internal.vo.VOUser> result = VOCollectionConverter
                .convertList(Collections.singletonList(user),
                        org.oscm.internal.vo.VOUser.class);

        // then
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof org.oscm.internal.vo.VOUser);
    }

    @Test
    public void convertSet_null() {
        assertNull(VOCollectionConverter.convertSet(null,
                org.oscm.internal.vo.VOUser.class));
    }

    @SuppressWarnings("cast")
    @Test
    public void convertSet() {
        // given
        org.oscm.vo.VOUser user = new org.oscm.vo.VOUser();
        Set<org.oscm.vo.VOUser> toBeConverted = new HashSet<org.oscm.vo.VOUser>();
        toBeConverted.add(user);

        // when
        Set<org.oscm.internal.vo.VOUser> result = VOCollectionConverter
                .convertSet(toBeConverted,
                        org.oscm.internal.vo.VOUser.class);

        // then
        assertEquals(1, result.size());
        assertTrue(result.iterator().next() instanceof org.oscm.internal.vo.VOUser);
    }

    @Test
    public void convertList_WithEnums() {
        VOServiceOperationParameterValues p = new VOServiceOperationParameterValues();
        p.setKey(123);
        p.setMandatory(true);
        p.setParameterId("parameterId");
        p.setParameterName("parameterName");
        p.setParameterValue("parameterValue");
        p.setType(OperationParameterType.REQUEST_SELECT);
        p.setValues(Arrays.asList("1", "2"));
        p.setVersion(7);

        List<org.oscm.vo.VOServiceOperationParameterValues> list = VOCollectionConverter
                .convertList(
                        Arrays.asList(p),
                        org.oscm.vo.VOServiceOperationParameterValues.class);

        assertEquals(1, list.size());
        org.oscm.vo.VOServiceOperationParameterValues conv = list.get(0);
        assertEquals(p.getKey(), conv.getKey());
        assertTrue(conv.isMandatory());
        assertEquals(p.getParameterId(), conv.getParameterId());
        assertEquals(p.getParameterName(), conv.getParameterName());
        assertEquals(p.getParameterValue(), conv.getParameterValue());
        assertEquals(p.getValues(), conv.getValues());
        assertEquals(p.getVersion(), conv.getVersion());
        assertEquals(
                org.oscm.types.enumtypes.OperationParameterType.REQUEST_SELECT,
                conv.getType());
    }
}
