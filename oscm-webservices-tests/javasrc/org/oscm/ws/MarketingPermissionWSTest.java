/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 12.12.2011                                                      
 *                                                                              
 *  Completion Time: 14.12.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ws;

import static org.oscm.ws.base.WebserviceTestBase.DEFAULT_PASSWORD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.ws.soap.SOAPFaultException;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.TSXMLForWebService;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.intf.AccountService;
import org.oscm.intf.ServiceProvisioningService;
import org.oscm.types.enumtypes.OrganizationRoleType;
import org.oscm.types.exceptions.AddMarketingPermissionException;
import org.oscm.types.exceptions.MarketingPermissionNotFoundException;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.OrganizationAuthoritiesException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOMarketplace;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOService;
import org.oscm.vo.VOTechnicalService;

/**
 * Tests a marketing permission related scenario.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class MarketingPermissionWSTest {

    private VOOrganization tp1;
    private VOOrganization tp2;
    private long timeBase;
    private VOOrganization supp1;
    private VOOrganization supp2;
    private VOOrganization supp3;
    private String tp1_adminKey;
    private String tp2_adminKey;
    private String supp1_adminKey;
    private String supp2_adminKey;
    private String supp3_adminKey;
    private ServiceFactory serviceFactory;
    private VOTechnicalService tp1_ts1;
    private VOTechnicalService tp1_ts2;
    private VOTechnicalService tp2_ts3;
    private VOTechnicalService tp2_ts4;
    private VOTechnicalService tp2_ts5;

    @Before
    public void setUp() throws Exception {
        WebserviceTestBase.getMailReader().deleteMails();
        timeBase = System.currentTimeMillis();

        // create two technology providers
        tp1 = WebserviceTestBase.createOrganization("tp1_admin" + timeBase,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        tp1_adminKey = WebserviceTestBase.readLastMailAndSetCommonPassword();
        tp2 = WebserviceTestBase.createOrganization("tp2_admin" + timeBase,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        tp2_adminKey = WebserviceTestBase.readLastMailAndSetCommonPassword();

        // create three suppliers
        supp1 = WebserviceTestBase.createOrganization("supp1_admin" + timeBase,
                OrganizationRoleType.SUPPLIER);
        supp1_adminKey = WebserviceTestBase.readLastMailAndSetCommonPassword();
        supp2 = WebserviceTestBase.createOrganization("supp2_admin" + timeBase,
                OrganizationRoleType.SUPPLIER);
        supp2_adminKey = WebserviceTestBase.readLastMailAndSetCommonPassword();
        supp3 = WebserviceTestBase.createOrganization("supp3_admin" + timeBase,
                OrganizationRoleType.SUPPLIER);
        supp3_adminKey = WebserviceTestBase.readLastMailAndSetCommonPassword();

        // create two technical services for tp1
        serviceFactory = ServiceFactory.getDefault();
        tp1_ts1 = createTechnicalService("TS1", tp1_adminKey);
        tp1_ts2 = createTechnicalService("TS2", tp1_adminKey);

        // create three technical services for tp2
        tp2_ts3 = createTechnicalService("TS3", tp2_adminKey);
        tp2_ts4 = createTechnicalService("TS4", tp2_adminKey);
        tp2_ts5 = createTechnicalService("TS5", tp2_adminKey);
    }

    @Test
    public void handleTestScenario() throws Exception {
        unauthorizedAccess();
        useCase1_grantSupplierUsageOfTechnicalService();
        useCase2_revokeSupplierUsageOfTechnicalService();
        useCase3_retrieveSuppliersWithUsageOfTechnicalService();
        useCase4_retrieveUsableTechnicalServices();
        useCase5_deleteTechnicalService();
        useCase6_createMarketableService();
    }

    // helper methods

    private void useCase6_createMarketableService() throws Exception {
        VOService service = createMarketableService("marketableService1",
                tp1_ts1, supp1_adminKey, supp1.getOrganizationId());
        assertNotNull(service);
        assertEquals("marketableService1", service.getServiceId());
        assertTrue(service.getKey() > 0);

        // alteration 4a
        removeMarketingPermissions(tp1_ts1,
                Arrays.asList(supp3.getOrganizationId()), tp1_adminKey);
        try {
            createMarketableService("marketableService2", tp1_ts1,
                    supp3_adminKey, supp3.getOrganizationId());
            fail();
        } catch (OperationNotPermittedException e) {
            assertEquals("ex.OperationNotPermittedException", e.getMessageKey());
            assertNull(e.getMessageParams());
            assertTrue(e.getMessage().contains(supp3.getOrganizationId()));
        }
    }

    private VOService createMarketableService(String serviceId,
            VOTechnicalService ts, String userKey, String orgId)
            throws Exception {
        VOMarketplace mpl = WebserviceTestBase.getGlobalMarketplace();
        WebserviceTestBase.grantMarketplaceUsage(mpl.getMarketplaceId(), orgId,
                null, null);
        return WebserviceTestBase
                .createAndActivateMarketableServiceFreeOfCharge(serviceId, ts,
                        mpl, userKey, DEFAULT_PASSWORD);
    }

    private void useCase5_deleteTechnicalService() throws Exception {
        // remove TS5 and verify that the permissions are gone, too
        deleteTechnicalService(tp2_ts5, tp2_adminKey);

        try {
            verifyMarketingPermissions(tp2_ts5, null, tp2_adminKey);
            fail();
        } catch (ObjectNotFoundException e) {
            assertEquals("ex.ObjectNotFoundException.TECHNICAL_SERVICE",
                    e.getMessageKey());
            assertEquals(1, e.getMessageParams().length);
            String tsKey = String.valueOf(tp2_ts5.getKey());
            assertEquals(tsKey, e.getMessageParams()[0]);
            assertTrue(e.getMessage().contains(tsKey));
        }
    }

    private void deleteTechnicalService(VOTechnicalService ts, String userKey)
            throws Exception {
        ServiceProvisioningService sps = serviceFactory
                .getServiceProvisioningService(userKey, DEFAULT_PASSWORD);
        sps.deleteTechnicalService(ts);
    }

    private void useCase4_retrieveUsableTechnicalServices() throws Exception {
        // set marketing permissions for TS3 and TS4
        createMarketingPermissions(tp2_ts3,
                Arrays.asList(supp2.getOrganizationId()), tp2_adminKey);
        createMarketingPermissions(tp2_ts4,
                Arrays.asList(supp3.getOrganizationId()), tp2_adminKey);

        // now verify the availability of services
        verifyServiceAvailability(supp1_adminKey,
                Arrays.asList(tp1_ts1, tp1_ts2, tp2_ts5));
        verifyServiceAvailability(supp2_adminKey,
                Arrays.asList(tp1_ts2, tp2_ts3, tp2_ts5));
        verifyServiceAvailability(supp3_adminKey,
                Arrays.asList(tp1_ts1, tp2_ts4));
    }

    private void verifyServiceAvailability(String userKey,
            List<VOTechnicalService> expectedServices) throws Exception {
        ServiceProvisioningService sps = serviceFactory
                .getServiceProvisioningService(userKey, DEFAULT_PASSWORD);
        List<VOTechnicalService> availableServices = sps
                .getTechnicalServices(OrganizationRoleType.SUPPLIER);
        Set<Long> expectedKeys = new HashSet<Long>();
        for (VOTechnicalService expectedTS : expectedServices) {
            expectedKeys.add(Long.valueOf(expectedTS.getKey()));
        }
        for (VOTechnicalService service : availableServices) {
            expectedKeys.remove(Long.valueOf(service.getKey()));
        }
        assertTrue(expectedKeys.isEmpty());
    }

    private void useCase3_retrieveSuppliersWithUsageOfTechnicalService()
            throws Exception {
        List<String> orgIds = Arrays.asList(supp1.getOrganizationId(),
                supp2.getOrganizationId());
        createMarketingPermissions(tp2_ts5, orgIds, tp2_adminKey);
        verifyMarketingPermissions(tp2_ts5, orgIds, tp2_adminKey);

        // alteration 2a
        try {
            verifyMarketingPermissions(tp2_ts5, orgIds, tp1_adminKey);
            fail();
        } catch (OperationNotPermittedException e) {
            assertEquals("ex.OperationNotPermittedException", e.getMessageKey());
            assertNull(e.getMessageParams());
            assertTrue(e.getMessage().contains(tp1.getOrganizationId()));
        }
    }

    private void useCase2_revokeSupplierUsageOfTechnicalService()
            throws Exception {
        // good case
        List<String> orgIds = Arrays.asList(supp2.getOrganizationId());
        createMarketingPermissions(tp2_ts3, orgIds, tp2_adminKey);
        removeMarketingPermissions(tp2_ts3, orgIds, tp2_adminKey);
        verifyMarketingPermissions(tp2_ts3, null, tp2_adminKey);

        // foreign ts
        try {
            removeMarketingPermissions(tp1_ts1, orgIds, tp2_adminKey);
            fail();
        } catch (OperationNotPermittedException e) {
            assertEquals("ex.OperationNotPermittedException", e.getMessageKey());
            assertNull(e.getMessageParams());
            assertTrue(e.getMessage().contains(tp2.getOrganizationId()));
        }

        // alteration 4a:
        try {
            removeMarketingPermissions(tp2_ts3, orgIds, tp2_adminKey);
            fail();
        } catch (MarketingPermissionNotFoundException e) {
            assertEquals("ex.MarketingPermissionNotFoundException",
                    e.getMessageKey());
            assertEquals(1, e.getMessageParams().length);
            assertEquals(supp2.getOrganizationId(), e.getMessageParams()[0]);
            assertTrue(e.getMessage().contains(supp2.getOrganizationId()));
        }

    }

    private void useCase1_grantSupplierUsageOfTechnicalService()
            throws Exception {
        // good case, allow supplier 1 and supplier 3 for technical service TS1
        createMarketingPermissions(
                tp1_ts1,
                Arrays.asList(supp1.getOrganizationId(),
                        supp3.getOrganizationId()), tp1_adminKey);
        verifyMarketingPermissions(
                tp1_ts1,
                Arrays.asList(supp1.getOrganizationId(),
                        supp3.getOrganizationId()), tp1_adminKey);

        // for a foreign object
        try {
            createMarketingPermissions(tp1_ts1,
                    Arrays.asList(supp3.getOrganizationId()), tp2_adminKey);
            fail();
        } catch (OperationNotPermittedException e) {
            assertEquals("ex.OperationNotPermittedException", e.getMessageKey());
            assertNull(e.getMessageParams());
            assertTrue(e.getMessage().contains(tp2.getOrganizationId()));
        }

        // alteration 3a
        try {
            createMarketingPermissions(tp1_ts1,
                    Arrays.asList("nonExistingOrgId"), tp1_adminKey);
            fail();
        } catch (AddMarketingPermissionException e) {
            assertEquals("ex.AddMarketingPermissionException",
                    e.getMessageKey());
            assertEquals(1, e.getMessageParams().length);
            assertEquals("nonExistingOrgId", e.getMessageParams()[0]);
            assertTrue(e.getMessage().contains("nonExistingOrgId"));
        }

        // alteration 3b
        try {
            createMarketingPermissions(tp1_ts1,
                    Arrays.asList(tp1.getOrganizationId()), tp1_adminKey);
            fail();
        } catch (AddMarketingPermissionException e) {
            assertEquals("ex.AddMarketingPermissionException",
                    e.getMessageKey());
            assertEquals(1, e.getMessageParams().length);
            assertEquals(tp1.getOrganizationId(), e.getMessageParams()[0]);
            assertTrue(e.getMessage().contains(tp1.getOrganizationId()));
        }

        // alteration 3c: create permissions for TS2 to supp1 and supp2 twice,
        // what must succeed
        createMarketingPermissions(
                tp1_ts2,
                Arrays.asList(supp1.getOrganizationId(),
                        supp2.getOrganizationId()), tp1_adminKey);
        createMarketingPermissions(
                tp1_ts2,
                Arrays.asList(supp1.getOrganizationId(),
                        supp2.getOrganizationId()), tp1_adminKey);
        verifyMarketingPermissions(
                tp1_ts2,
                Arrays.asList(supp1.getOrganizationId(),
                        supp2.getOrganizationId()), tp1_adminKey);
    }

    /**
     * Ensures that the given organizations are permitted to use the technical
     * service.
     */
    private void verifyMarketingPermissions(VOTechnicalService tp,
            List<String> orgIds, String userKey) throws Exception {
        AccountService as = serviceFactory.getAccountService(userKey,
                DEFAULT_PASSWORD);
        List<VOOrganization> supps = as.getSuppliersForTechnicalService(tp);
        for (VOOrganization supp : supps) {
            assertTrue(orgIds.contains(supp.getOrganizationId()));
        }
        if (orgIds == null || orgIds.isEmpty()) {
            assertTrue(supps.isEmpty());
        }
    }

    /**
     * Ensures that a supplier cannot invoke the methods to create or remove
     * marketing permissions, nor to retrieve them.
     */
    private void unauthorizedAccess() throws Exception {
        // try to create marketing permission as non-technology provider, which
        // must fail
        try {
            createMarketingPermissions(tp1_ts1,
                    Arrays.asList(supp1.getOrganizationId()), supp1_adminKey);
            fail();
        } catch (SOAPFaultException e) {
            assertTrue(e.getMessage().contains("Unauthorized"));
        }
        // similar for removing the permissions
        try {
            removeMarketingPermissions(tp1_ts1,
                    Arrays.asList(supp1.getOrganizationId()), supp1_adminKey);
            fail();
        } catch (SOAPFaultException e) {
            assertTrue(e.getMessage().contains("Unauthorized"));
        }
        // and for the retrieval
        try {
            getPermittedSuppliers(tp1_ts1, supp1_adminKey);
            fail();
        } catch (SOAPFaultException e) {
            assertTrue(e.getMessage().contains("Unauthorized"));
        }
    }

    private void getPermittedSuppliers(VOTechnicalService tp, String userKey)
            throws Exception {
        AccountService as = serviceFactory.getAccountService(userKey,
                DEFAULT_PASSWORD);
        as.getSuppliersForTechnicalService(tp);
    }

    private void removeMarketingPermissions(VOTechnicalService tp,
            List<String> orgIds, String userKey) throws Exception {
        AccountService as = serviceFactory.getAccountService(userKey,
                DEFAULT_PASSWORD);
        as.removeSuppliersFromTechnicalService(tp, orgIds);
    }

    private void createMarketingPermissions(VOTechnicalService tp,
            List<String> orgIds, String userKey) throws Exception {
        AccountService as = serviceFactory.getAccountService(userKey,
                DEFAULT_PASSWORD);
        as.addSuppliersForTechnicalService(tp, orgIds);
    }

    private VOTechnicalService createTechnicalService(String id, String userKey)
            throws Exception, OrganizationAuthoritiesException,
            ValidationException, NonUniqueBusinessKeyException {
        ServiceProvisioningService sps = serviceFactory
                .getServiceProvisioningService(userKey, DEFAULT_PASSWORD);

        String xml = TSXMLForWebService.createTSXML(id);

        return WebserviceTestBase.createTechnicalService(xml, id, sps);

    }
}
