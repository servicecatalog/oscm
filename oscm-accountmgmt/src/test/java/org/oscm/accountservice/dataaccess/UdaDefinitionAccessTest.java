/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 28.06.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.dataaccess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import javax.ejb.SessionContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.types.enumtypes.UdaTargetType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.vo.VOUdaDefinition;

/**
 * @author weiser
 * 
 */
public class UdaDefinitionAccessTest {

    private UdaDefinitionAccess uda;

    private DataService ds;
    private SessionContext ctx;

    private Organization supplier;
    private List<UdaDefinition> definitions;
    private UdaDefinition defSupplier;

    private VOUdaDefinition voDef;

    @Before
    public void setup() throws Exception {
        ds = mock(DataService.class);
        ctx = mock(SessionContext.class);

        supplier = new Organization();
        OrganizationToRole role = new OrganizationToRole();
        role.setOrganization(supplier);
        role.setOrganizationRole(new OrganizationRole(
                OrganizationRoleType.SUPPLIER));
        supplier.getGrantedRoles().add(role);

        defSupplier = new UdaDefinition();
        defSupplier.setTargetType(UdaTargetType.CUSTOMER);
        defSupplier.setConfigurationType(UdaConfigurationType.SUPPLIER);
        defSupplier.setKey(1234);
        defSupplier.setOrganization(supplier);

        definitions = new ArrayList<UdaDefinition>(Arrays.asList(defSupplier));

        supplier.setUdaDefinitions(definitions);

        voDef = new VOUdaDefinition();
        voDef.setConfigurationType(UdaConfigurationType.SUPPLIER);
        voDef.setTargetType(UdaTargetType.CUSTOMER.name());
        voDef.setUdaId("some id");
        voDef.setDefaultValue("defaultValue");

        when(ds.getReference(UdaDefinition.class, defSupplier.getKey()))
                .thenReturn(defSupplier);
        when(ds.find(UdaDefinition.class, defSupplier.getKey())).thenReturn(
                defSupplier);

        uda = new UdaDefinitionAccess(ds, ctx);
    }

    @Test
    public void getOwnUdaDefinitions() {
        List<UdaDefinition> result = uda.getOwnUdaDefinitions(supplier);

        assertSame(definitions, result);
    }

    @Test
    public void getReadableUdaDefinitionsFromSupplier_SUPPLIER() {
        List<UdaDefinition> list = uda.getReadableUdaDefinitionsFromSupplier(
                supplier, OrganizationRoleType.SUPPLIER);

        assertEquals(1, list.size());
        assertSame(defSupplier, list.get(0));
    }

    @Test
    public void getReadableUdaDefinitionsFromSupplier_CUSTOMER() {
        // given: additional types that have to be returned
        List<UdaDefinition> defs = getUdaDefinitions(
                UdaConfigurationType.USER_OPTION_MANDATORY,
                UdaConfigurationType.USER_OPTION_OPTIONAL);
        definitions.addAll(defs);

        // when
        List<UdaDefinition> list = uda.getReadableUdaDefinitionsFromSupplier(
                supplier, OrganizationRoleType.CUSTOMER);

        // then
        assertEquals(2, list.size());
        assertTrue(list.contains(defs.get(0)));
        assertTrue(list.contains(defs.get(1)));
    }

    @Test
    public void getReadableUdaDefinitionsFromSupplier_CUSTOMER_Empty() {
        List<UdaDefinition> list = uda.getReadableUdaDefinitionsFromSupplier(
                supplier, OrganizationRoleType.CUSTOMER);

        assertTrue(list.isEmpty());
    }

    @Test
    public void createDefinition() throws Exception {
        uda.createDefinition(defSupplier);

        verify(ds, times(1)).persist(eq(defSupplier));
        verifyZeroInteractions(ctx);
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void createDefinition_NonUniqueBK() throws Exception {
        doThrow(
                new NonUniqueBusinessKeyException(ClassEnum.UDA_DEFINITION,
                        "test")).when(ds).persist(defSupplier);

        try {
            uda.createDefinition(defSupplier);
        } finally {
            verify(ds, times(1)).persist(eq(defSupplier));
            verify(ctx, times(1)).setRollbackOnly();
        }
    }

    @Test
    public void updateDefinition() throws Exception {
        // given: definition will be found and default value updated
        voDef.setKey(defSupplier.getKey());

        // when
        uda.updateDefinition(voDef, supplier);

        // then
        verifyZeroInteractions(ctx);
        verify(ds, times(1)).validateBusinessKeyUniqueness(
                any(UdaDefinition.class));
        assertEquals(voDef.getDefaultValue(), defSupplier.getDefaultValue());
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void updateDefinition_NonUniqueBK() throws Exception {
        // given: definition will be found but update causes non unique business
        // key
        voDef.setKey(defSupplier.getKey());
        doThrow(
                new NonUniqueBusinessKeyException(ClassEnum.UDA_DEFINITION,
                        "test")).when(ds).validateBusinessKeyUniqueness(
                any(UdaDefinition.class));

        try {
            // when
            uda.updateDefinition(voDef, supplier);
        } finally {
            // then
            verify(ctx, times(1)).setRollbackOnly();
            assertNull(defSupplier.getDefaultValue());
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void updateDefinition_NotFound() throws Exception {
        // given: definition will be found and default value updated
        voDef.setKey(1111);
        when(ds.getReference(UdaDefinition.class, voDef.getKey()))
                .thenThrow(
                        new ObjectNotFoundException(ClassEnum.UDA_DEFINITION,
                                "udadef"));

        try {
            // when
            uda.updateDefinition(voDef, supplier);
        } finally {
            // then
            verifyZeroInteractions(ctx);
            verify(ds, times(1)).getReference(eq(UdaDefinition.class),
                    eq(voDef.getKey()));
            verifyNoMoreInteractions(ds);
        }
    }

    @Test(expected = ValidationException.class)
    public void saveUdaDefinitions_ValidationError() throws Exception {
        voDef.setTargetType("invalidType");

        try {
            uda.saveUdaDefinitions(Arrays.asList(voDef), supplier);
        } finally {
            verify(ctx, times(1)).setRollbackOnly();
            verifyZeroInteractions(ds);
        }
    }

    @Test(expected = OrganizationAuthoritiesException.class)
    public void saveUdaDefinitions_NotAuthorized() throws Exception {
        supplier.getGrantedRoles().clear();

        try {
            uda.saveUdaDefinitions(Arrays.asList(voDef), supplier);
        } finally {
            verify(ctx, times(1)).setRollbackOnly();
            verifyZeroInteractions(ds);
        }

    }

    @Test
    public void saveUdaDefinitions_Update() throws Exception {
        // given: definition will be found and default value updated
        voDef.setKey(defSupplier.getKey());

        // when
        uda.saveUdaDefinitions(Arrays.asList(voDef), supplier);

        // then
        verifyZeroInteractions(ctx);
        verify(ds, times(1)).validateBusinessKeyUniqueness(
                any(UdaDefinition.class));
        assertEquals(voDef.getDefaultValue(), defSupplier.getDefaultValue());
    }

    @Test
    public void saveUdaDefinitions_Create() throws Exception {
        ArgumentCaptor<UdaDefinition> ac = ArgumentCaptor
                .forClass(UdaDefinition.class);
        // when
        uda.saveUdaDefinitions(Arrays.asList(voDef), supplier);

        // then
        verifyZeroInteractions(ctx);
        verify(ds, times(1)).persist(ac.capture());

        UdaDefinition persisted = ac.getValue();
        assertEquals(voDef.getConfigurationType(),
                persisted.getConfigurationType());
        assertEquals(voDef.getDefaultValue(), persisted.getDefaultValue());
        assertEquals(voDef.getTargetType(), persisted.getTargetType().name());
        assertEquals(voDef.getUdaId(), persisted.getUdaId());
    }

    @Test
    public void rolesToString() {
        assertEquals("SUPPLIER",
                uda.rolesToString(EnumSet.of(OrganizationRoleType.SUPPLIER)));
        assertEquals("SUPPLIER, CUSTOMER", uda.rolesToString(EnumSet.of(
                OrganizationRoleType.SUPPLIER, OrganizationRoleType.CUSTOMER)));
    }

    @Test
    public void deleteUdaDefinitions_AlreadyDeleted() throws Exception {
        // given: no key set, will not be found

        // when
        uda.deleteUdaDefinitions(Arrays.asList(voDef), supplier);

        // then
        verify(ds, times(1)).find(eq(UdaDefinition.class), eq(voDef.getKey()));
        verify(ds, never()).remove(any(UdaDefinition.class));
    }

    @Test
    public void deleteUdaDefinitions() throws Exception {
        // given: definition will be found
        voDef.setKey(defSupplier.getKey());

        // when
        uda.deleteUdaDefinitions(Arrays.asList(voDef), supplier);

        // then
        verify(ds, times(1)).find(eq(UdaDefinition.class), eq(voDef.getKey()));
        verify(ds, times(1)).remove(eq(defSupplier));
    }

    private List<UdaDefinition> getUdaDefinitions(UdaConfigurationType... types) {
        ArrayList<UdaDefinition> list = new ArrayList<UdaDefinition>();
        for (UdaConfigurationType type : types) {
            UdaDefinition def = new UdaDefinition();
            def.setConfigurationType(type);
            def.setTargetType(UdaTargetType.CUSTOMER);
            list.add(def);
        }
        return list;
    }

}
