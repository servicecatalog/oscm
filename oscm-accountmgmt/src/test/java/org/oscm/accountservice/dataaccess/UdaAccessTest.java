/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 29.06.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.dataaccess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ejb.SessionContext;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.oscm.accountservice.dataaccess.validator.MandatoryUdaValidator;
import org.oscm.accountservice.dataaccess.validator.UdaAccessValidator;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.MandatoryUdaMissingException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;
import org.oscm.types.enumtypes.UdaTargetType;

/**
 * @author weiser
 * 
 */
public class UdaAccessTest {

    private UdaAccess ua;

    private DataService ds;
    private SessionContext ctx;

    private Organization supplier;
    private UdaDefinition defSupplier;
    private Uda uda;
    private Subscription sub;
    private VOUdaDefinition voDef;
    private VOUda voUda;

    @Before
    public void setup() throws Exception {
        ds = mock(DataService.class);
        ctx = mock(SessionContext.class);

        supplier = new Organization();
        supplier.setKey(8765);

        defSupplier = new UdaDefinition();
        defSupplier.setTargetType(UdaTargetType.CUSTOMER);
        defSupplier.setConfigurationType(UdaConfigurationType.SUPPLIER);
        defSupplier.setKey(1234);
        defSupplier.setOrganization(supplier);

        uda = new Uda();
        uda.setTargetObjectKey(supplier.getKey());
        uda.setUdaDefinition(defSupplier);
        uda.setUdaValue("some value");
        uda.setKey(5678);

        supplier.setUdaDefinitions(new ArrayList<>(Arrays.asList(defSupplier)));

        sub = new Subscription();
        sub.setKey(9876);
        sub.setOrganization(new Organization());

        voDef = new VOUdaDefinition();
        voDef.setConfigurationType(UdaConfigurationType.SUPPLIER);
        voDef.setTargetType(UdaTargetType.CUSTOMER.name());
        voDef.setUdaId("some id");
        voDef.setDefaultValue("defaultValue");
        voDef.setKey(defSupplier.getKey());

        voUda = new VOUda();
        voUda.setUdaDefinition(voDef);
        voUda.setTargetObjectKey(supplier.getKey());
        voUda.setUdaValue("udaValue");
        voUda.setKey(uda.getKey());

        ua = new UdaAccess(ds, ctx);
        // validators are tested separately so we mock them
        ua.mandatoryUdaValidator = mock(MandatoryUdaValidator.class);
        ua.udaAccessValidator = mock(UdaAccessValidator.class);

        when(ds.find(eq(Uda.class), eq(uda.getKey()))).thenReturn(uda);
        when(ds.getReference(eq(Uda.class), eq(uda.getKey()))).thenReturn(uda);
        when(ds.getReference(eq(UdaDefinition.class), eq(defSupplier.getKey())))
                .thenReturn(defSupplier);

        Query query = mock(Query.class);
        when(query.getResultList()).thenReturn(Arrays.asList(uda));
        when(ds.createNamedQuery(anyString())).thenReturn(query);
    }

    @Test
    public void saveUdas_Create() throws Exception {
        voUda.setKey(0);

        ua.saveUdas(Arrays.asList(voUda), supplier);

        verify(ds, times(1)).persist(any(Uda.class));
    }

    @Test
    public void saveUdas_Update() throws Exception {
        ua.saveUdas(Arrays.asList(voUda), supplier);

        assertEquals(voUda.getUdaValue(), uda.getUdaValue());
    }

    @Test
    public void saveUdas_Delete() throws Exception {
        voUda.setUdaValue(null);

        ua.saveUdas(Arrays.asList(voUda), supplier);

        verify(ds, times(1)).remove(eq(uda));
    }

    @Test(expected = ValidationException.class)
    public void saveUdas_ValidationError_NoDefinition() throws Exception {
        String udaValue="";
        for (int i = 0; i < 30; i++) {
            udaValue += "1234567890";
        }
        voUda.setUdaValue(udaValue);
        ua.saveUdas(Arrays.asList(voUda), supplier);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void saveUdas_ConcurrentChangeOfDefinition() throws Exception {
        // given: definition has been updated concurrently - domain object has
        // version 0 and cannot be increased
        voDef.setVersion(-1);

        try {
            // when
            ua.saveUdas(Arrays.asList(voUda), supplier);
        } finally {
            // then
            assertFalse(uda.getUdaValue().equals(voUda.getUdaValue()));
        }
    }

    @Test(expected = ConcurrentModificationException.class)
    public void saveUdas_DefinitionNotFound() throws Exception {

        // given
        final ObjectNotFoundException definitionNotFound = new ObjectNotFoundException(
                ClassEnum.UDA_DEFINITION, "1");

        when(ds.getReference(eq(Uda.class), eq(uda.getKey()))).thenThrow(
                definitionNotFound);

        try {
            // when
            ua.saveUdas(Arrays.asList(voUda), supplier);
        } finally {
            // then
            assertFalse(uda.getUdaValue().equals(voUda.getUdaValue()));
        }
    }

    @Test
    public void deleteUda_NotFound() throws Exception {
        // given: uda will not be found
        when(ds.find(eq(Uda.class), anyLong())).thenReturn(null);

        // when
        ua.deleteUda(voUda, supplier);

        // then
        verifyZeroInteractions(ua.mandatoryUdaValidator);
        verifyZeroInteractions(ua.udaAccessValidator);
        verifyZeroInteractions(ctx);
        verify(ds, never()).remove(any(Uda.class));
    }

    @Test(expected = MandatoryUdaMissingException.class)
    public void deleteUda_Mandatory() throws Exception {
        // given: mandatory uda
        doThrow(new MandatoryUdaMissingException()).when(
                ua.mandatoryUdaValidator).checkMandatory(
                any(UdaDefinition.class));

        try {
            // when
            ua.deleteUda(voUda, supplier);
        } finally {
            // then
            verify(ds, never()).remove(any(Uda.class));
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void deleteUda_NotPermitted() throws Exception {
        // given: mandatory uda
        doThrow(new OperationNotPermittedException()).when(
                ua.udaAccessValidator).canDeleteUda(any(Uda.class),
                any(Organization.class));

        try {
            // when
            ua.deleteUda(voUda, supplier);
        } finally {
            // then
            verify(ds, never()).remove(any(Uda.class));
        }
    }

    @Test
    public void createUda() throws Exception {
        UdaDefinition anotherDef = new UdaDefinition();

        ua.createUda(anotherDef, uda);

        ArgumentCaptor<Uda> ac = ArgumentCaptor.forClass(Uda.class);
        verify(ds, times(1)).persist(ac.capture());
        Uda persisted = ac.getValue();
        assertSame(uda, persisted);
        assertSame(anotherDef, persisted.getUdaDefinition());
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void createUda_NonUniqueBK() throws Exception {
        doThrow(new NonUniqueBusinessKeyException()).when(ds).persist(
                any(Uda.class));

        try {
            ua.createUda(defSupplier, uda);
        } finally {
            verify(ctx, times(1)).setRollbackOnly();
        }
    }

    @Test
    public void updateUda() throws Exception {
        ua.updateUda(voUda, defSupplier, supplier);

        assertEquals(voUda.getUdaValue(), uda.getUdaValue());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void updateUda_NotFound() throws Exception {
        doThrow(new ObjectNotFoundException()).when(ds).getReference(
                eq(Uda.class), anyLong());

        try {
            ua.updateUda(voUda, defSupplier, supplier);
        } finally {
            verify(ctx, times(1)).setRollbackOnly();
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void updateUda_DifferentDefinitions() throws Exception {
        // given: the uda entity belongs to a different definition than the one
        // passed in the value object
        uda.setUdaDefinition(new UdaDefinition());

        try {
            ua.updateUda(voUda, defSupplier, supplier);
        } finally {
            verify(ctx, times(1)).setRollbackOnly();
            assertFalse(voUda.getUdaValue().equals(uda.getUdaValue()));
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void updateUda_DifferentTargets() throws Exception {
        // given: the uda entity belongs to a different target than the one
        // passed in the value object
        voUda.setTargetObjectKey(99999);

        try {
            ua.updateUda(voUda, defSupplier, supplier);
        } finally {
            verify(ctx, times(1)).setRollbackOnly();
            assertFalse(voUda.getUdaValue().equals(uda.getUdaValue()));
        }
    }

    @Test
    public void saveUdasForSubscription_CUSTOMER() throws Exception {
        ua = spy(ua);
        doNothing().when(ua).saveUdas(anyListOf(VOUda.class),
                any(Organization.class));
        List<VOUda> udas = Arrays.asList(voUda);

        ua.saveUdasForSubscription(udas, supplier, sub);

        verify(ua.mandatoryUdaValidator, times(1)).checkAllRequiredUdasPassed(
                anyListOf(UdaDefinition.class), anyListOf(Uda.class), eq(udas));
        verify(ua, times(1)).saveUdas(anyListOf(VOUda.class),
                eq(sub.getOrganization()));
    }

    @Test
    public void saveUdasForSubscription_CUSTOMER_SUBSCRIPTION()
            throws Exception {
        ua = spy(ua);
        doNothing().when(ua).saveUdas(anyListOf(VOUda.class),
                any(Organization.class));
        voDef.setTargetType(UdaTargetType.CUSTOMER_SUBSCRIPTION.name());
        voUda.setTargetObjectKey(sub.getKey());
        List<VOUda> udas = Arrays.asList(voUda);

        ua.saveUdasForSubscription(udas, supplier, sub);

        verify(ua.mandatoryUdaValidator, times(1)).checkAllRequiredUdasPassed(
                anyListOf(UdaDefinition.class), anyListOf(Uda.class), eq(udas));
        verify(ua, times(1)).saveUdas(anyListOf(VOUda.class),
                eq(sub.getOrganization()));
    }

    @Test
    public void getUdasForTypeAndTarget() throws Exception {
        List<Uda> list = ua.getUdasForTypeAndTarget(123L,
                UdaTargetType.CUSTOMER, supplier, false);

        assertEquals(1, list.size());
        assertSame(uda, list.get(0));
    }

}
