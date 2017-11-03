/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 21, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.SessionContext;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.subscriptionservice.auditlog.SubscriptionAuditLogCollector;
import org.oscm.types.enumtypes.UdaTargetType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;

public class AccountServicePartnerOrgUdaTest {

    DataService ds;
    AccountServiceBean as;
    Organization organization;
    Organization otherOrganization;
    List<VOUda> udas = new ArrayList<VOUda>();

    @Before
    public void setup() throws Exception {
        ds = mock(DataService.class);
        as = new AccountServiceBean() {
            @Override
            public List<VOUda> getUdas(String targetType, long targetObjectKey,
                    boolean checkSeller)
                    throws OrganizationAuthoritiesException {
                throw (new OrganizationAuthoritiesException());
            }

        };
        as.sessionCtx = mock(SessionContext.class);
        as.subscriptionAuditLogCollector = mock(SubscriptionAuditLogCollector.class);
        as.dm = ds;

        organization = new Organization();
        organization.setKey(1L);
        PlatformUser user = new PlatformUser();
        user.setOrganization(organization);
        when(ds.getCurrentUser()).thenReturn(user);

        Subscription subscription = new Subscription();
        subscription.setKey(1000);
        when(ds.getReference(eq(Subscription.class), eq(1000))).thenReturn(
                subscription);

        otherOrganization = new Organization();
        otherOrganization.setKey(10L);
    }

    private void givenBrokerOrganization() {
        setRole(OrganizationRoleType.BROKER);
    }

    private void setRole(OrganizationRoleType roleType) {
        OrganizationToRole otr = new OrganizationToRole();
        otr.setOrganization(organization);
        OrganizationRole role = new OrganizationRole();
        role.setRoleName(roleType);
        otr.setOrganizationRole(role);

        OrganizationToRole ctr = new OrganizationToRole();
        ctr.setOrganization(organization);
        role = new OrganizationRole();
        role.setRoleName(OrganizationRoleType.CUSTOMER);
        ctr.setOrganizationRole(role);

        Set<OrganizationToRole> grantedRoles = new HashSet<OrganizationToRole>();
        grantedRoles.add(otr);
        grantedRoles.add(ctr);
        organization.setGrantedRoles(grantedRoles);
    }

    private void givenResellerOrganization() {
        setRole(OrganizationRoleType.RESELLER);
    }

    @Test(expected = OrganizationAuthoritiesException.class)
    public void saveUdaDefinitions_Broker() throws Exception {
        // given
        givenBrokerOrganization();
        List<VOUdaDefinition> definitionsToSave = givenUdaDefinition();
        List<VOUdaDefinition> definitionsToDelete = new ArrayList<VOUdaDefinition>();

        // when
        as.saveUdaDefinitions(definitionsToSave, definitionsToDelete);
    }

    private List<VOUdaDefinition> givenUdaDefinition() throws Exception {
        List<VOUdaDefinition> udaDefinitions = new ArrayList<VOUdaDefinition>();
        VOUdaDefinition definition = createVoUdaDefinition();
        createUdaDefinition(definition.getKey());
        udaDefinitions.add(definition);
        return udaDefinitions;
    }

    private VOUdaDefinition createVoUdaDefinition() throws Exception {
        long key = 0;
        VOUdaDefinition definition = new VOUdaDefinition();
        definition.setKey(key);
        definition.setTargetType(UdaTargetType.CUSTOMER.name());
        definition.setUdaId("udaId");
        definition.setConfigurationType(UdaConfigurationType.SUPPLIER);
        return definition;
    }

    private UdaDefinition createUdaDefinition(long key)
            throws ObjectNotFoundException {
        UdaDefinition def = new UdaDefinition();
        def.setKey(key);
        def.setConfigurationType(UdaConfigurationType.SUPPLIER);
        def.setTargetType(UdaTargetType.CUSTOMER);

        when(ds.getReference(UdaDefinition.class, key)).thenReturn(def);
        when(ds.find(UdaDefinition.class, key)).thenReturn(def);
        return def;
    }

    @Test(expected = OperationNotPermittedException.class)
    public void saveUdaDefinitions_toDelete_Broker() throws Exception {
        // given
        givenBrokerOrganization();
        List<VOUdaDefinition> definitionsToSave = new ArrayList<VOUdaDefinition>();
        List<VOUdaDefinition> definitionsToDelete = givenNotOwningUdaDefinition();

        // when
        as.saveUdaDefinitions(definitionsToSave, definitionsToDelete);
    }

    @Test(expected = OrganizationAuthoritiesException.class)
    public void saveUdaDefinitions_Reseller() throws Exception {
        // given
        givenResellerOrganization();
        List<VOUdaDefinition> definitionsToSave = givenUdaDefinition();
        List<VOUdaDefinition> definitionsToDelete = new ArrayList<VOUdaDefinition>();

        // when
        as.saveUdaDefinitions(definitionsToSave, definitionsToDelete);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void saveUdaDefinitions_toDelete_Reseller() throws Exception {
        // given
        givenResellerOrganization();
        List<VOUdaDefinition> definitionsToSave = new ArrayList<VOUdaDefinition>();
        List<VOUdaDefinition> definitionsToDelete = givenNotOwningUdaDefinition();

        // when
        as.saveUdaDefinitions(definitionsToSave, definitionsToDelete);
    }

    private List<VOUdaDefinition> givenNotOwningUdaDefinition()
            throws Exception {
        List<VOUdaDefinition> udaDefinitions = new ArrayList<VOUdaDefinition>();
        VOUdaDefinition definition = createVoUdaDefinition();
        UdaDefinition udaDefinition = createUdaDefinition(definition.getKey());
        udaDefinition.setOrganization(otherOrganization);
        udaDefinitions.add(definition);
        return udaDefinitions;
    }

    @Test(expected = OrganizationAuthoritiesException.class)
    public void saveUdas_Broker() throws Exception {
        // given
        givenBrokerOrganization();
        List<VOUda> udasToSave = givenUda();

        // when
        as.saveUdas(udasToSave);
        verify(as.subscriptionAuditLogCollector, never())
                .editSubscriptionAttributeByServiceManager(
                        any(DataService.class), any(Subscription.class),
                        anyString(), anyString());
    }

    private List<VOUda> givenUda() throws Exception {
        VOUdaDefinition voUdaDefinition = createVoUdaDefinition();
        createUdaDefinition(voUdaDefinition.getKey());

        List<VOUda> udasToSave = new ArrayList<VOUda>();
        VOUda uda = new VOUda();
        uda.setKey(0L);
        uda.setTargetObjectKey(1L);
        uda.setUdaValue("udaValue");
        uda.setUdaDefinition(voUdaDefinition);

        Organization org = new Organization();
        org.setKey(1L);

        when(ds.getReference(Organization.class, org.getKey())).thenReturn(org);

        udasToSave.add(uda);
        return udasToSave;
    }

    @Test(expected = OrganizationAuthoritiesException.class)
    public void saveUdas_Reseller() throws Exception {
        // given
        givenResellerOrganization();
        List<VOUda> udasToSave = givenUda();

        // when
        as.saveUdas(udasToSave);

        verify(as.subscriptionAuditLogCollector, never())
                .editSubscriptionAttributeByServiceManager(
                        any(DataService.class), any(Subscription.class),
                        anyString(), anyString());
    }

    @Test
    public void getUdaDefinitions_Broker() {
        // given
        givenBrokerOrganization();

        // when
        List<VOUdaDefinition> udaDefinitions = as.getUdaDefinitions();

        // then
        assertTrue(udaDefinitions.isEmpty());
    }

    @Test
    public void getUdaDefinitions_Reseller() {
        // given
        givenResellerOrganization();

        // when
        List<VOUdaDefinition> udaDefinitions = as.getUdaDefinitions();

        // then
        assertTrue(udaDefinitions.isEmpty());
    }

}
