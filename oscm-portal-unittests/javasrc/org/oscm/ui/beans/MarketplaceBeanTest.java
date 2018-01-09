/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: weiser                                                       
 *                                                                              
 *  Creation Date: 07.03.2011                                                      
 *                                                                              
 *  Completion Time: <date>                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.application.FacesMessage.Severity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.types.exception.MarketplaceValidationException;
import org.oscm.internal.types.exception.UserRoleAssignmentException;
import org.oscm.test.stubs.MarketplaceServiceStub;
import org.oscm.ui.model.Marketplace;
import org.oscm.ui.model.Organization;
import org.oscm.ui.model.ServiceDetails;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAlreadyBannedException;
import org.oscm.internal.types.exception.OrganizationAlreadyExistsException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;

/**
 * @author weiser
 * 
 */
@SuppressWarnings("boxing")
public class MarketplaceBeanTest {

    private static final String GLOBAL_MP = "global";
    private static final String GLOBAL_OPEN_MP = "global_open";

    private static final String LOCAL_MP = "local";

    private MarketplaceBean bean;

    protected boolean serviceCalled = false;
    protected VOService passedService = null;
    protected List<VOCatalogEntry> passedMarketplaceEntries = null;
    protected String passedMarketplaceId;
    protected List<String> passedOrganizationIds;
    protected List<String> bannedOrganizationIds;

    protected List<VOCatalogEntry> returnedCatalogEntries = null;
    protected List<VOMarketplace> returnedMarketplaces = null;
    protected List<VOOrganization> returnedSuppliers = null;
    protected List<VOOrganization> bannedSuppliers = null;
    protected VOMarketplace passedVoMp = null;
    final String mpId = "marketplaceId";
    protected MarketplaceService marketplaceServiceSetUp = null;

    private VOMarketplace localMarketplace;
    private VOMarketplace globalMarketplace;
    private VOMarketplace localMarketplace2;
    private VOMarketplace globalMarketplace2;
    private Marketplace globalOpenMarketplace;

    private VOCatalogEntry localMarketplaceCE;
    private VOCatalogEntry globalMarketplaceCE;

    @Before
    public void setup() throws Exception {
        final MarketplaceService marketplaceService = new MarketplaceServiceStub() {

            @Override
            public VOServiceDetails publishService(VOService service,
                    List<VOCatalogEntry> marketplaceEntries) {
                serviceCalled = true;
                passedService = service;
                passedMarketplaceEntries = marketplaceEntries;
                return null;
            }

            @Override
            public List<VOCatalogEntry> getMarketplacesForService(
                    VOService service) {
                serviceCalled = true;
                passedService = service;
                return returnedCatalogEntries;
            }

            @Override
            public List<VOMarketplace> getMarketplacesForOrganization() {
                serviceCalled = true;
                return returnedMarketplaces;
            }

            @Override
            public List<VOMarketplace> getMarketplacesForOperator() {
                serviceCalled = true;
                return returnedMarketplaces;
            }

            @Override
            public List<VOMarketplace> getMarketplacesOwned() {
                serviceCalled = true;
                return returnedMarketplaces;
            }

            @Override
            public void addOrganizationsToMarketplace(
                    List<String> organizationIds, String marketplaceId)
                    throws ObjectNotFoundException,
                    OperationNotPermittedException,
                    OrganizationAuthorityException,
                    OrganizationAlreadyExistsException {
                serviceCalled = true;
                passedMarketplaceId = marketplaceId;
                passedOrganizationIds = organizationIds;
            }

            @Override
            public void banOrganizationsFromMarketplace(
                    List<String> organizationIds, String marketplaceId)
                    throws ObjectNotFoundException,
                    OperationNotPermittedException,
                    OrganizationAuthorityException,
                    OrganizationAlreadyBannedException {
                serviceCalled = true;
                passedMarketplaceId = marketplaceId;
                bannedOrganizationIds = organizationIds;
            }

            @Override
            public void removeOrganizationsFromMarketplace(
                    List<String> organizationIds, String marketplaceId)
                    throws ObjectNotFoundException,
                    OperationNotPermittedException {
                serviceCalled = true;
                passedMarketplaceId = marketplaceId;
                passedOrganizationIds = organizationIds;
            }

            @Override
            public void liftBanOrganizationsFromMarketplace(
                    List<String> organizationIds, String marketplaceId)
                    throws ObjectNotFoundException,
                    OperationNotPermittedException {
                serviceCalled = true;
                passedMarketplaceId = marketplaceId;
                bannedOrganizationIds = organizationIds;
            }

            @Override
            public List<VOOrganization> getOrganizationsForMarketplace(
                    String marketplaceId) throws ObjectNotFoundException,
                    OperationNotPermittedException {
                serviceCalled = true;
                passedMarketplaceId = marketplaceId;
                return returnedSuppliers;
            }

            @Override
            public List<VOOrganization> getBannedOrganizationsForMarketplace(
                    String marketplaceId) throws ObjectNotFoundException,
                    OperationNotPermittedException {
                serviceCalled = true;
                passedMarketplaceId = marketplaceId;
                return bannedSuppliers;
            }

            @Override
            public VOMarketplace createMarketplace(VOMarketplace marketplace)
                    throws OperationNotPermittedException, ObjectNotFoundException, ValidationException, UserRoleAssignmentException, MarketplaceValidationException {

                marketplace.setMarketplaceId(mpId);
                passedVoMp = marketplace;
                return marketplace;
            }

            @Override
            public void deleteMarketplace(String marketplaceId)
                    throws ObjectNotFoundException {
                passedMarketplaceId = marketplaceId;
            }

            @Override
            public VOMarketplace updateMarketplace(VOMarketplace marketplace)
                    throws ObjectNotFoundException,
                    OperationNotPermittedException,
                    ConcurrentModificationException, ValidationException {
                serviceCalled = true;
                passedVoMp = marketplace;
                return marketplace;
            }

            @Override
            public VOMarketplace getMarketplaceById(String marketplaceId)
                    throws ObjectNotFoundException {
                serviceCalled = true;
                passedMarketplaceId = marketplaceId;
                return passedVoMp;
            }

        };

        marketplaceServiceSetUp = marketplaceService;

        bean = new MarketplaceBean() {

            private static final long serialVersionUID = 3975287932811210059L;

            @Override
            protected MarketplaceService getMarketplaceService() {
                return marketplaceServiceSetUp;
            }

            @Override
            protected void addMessage(String clientId, Severity severity,
                    String key, String param) {
            }

            @Override
            protected void addMessage(String clientId, Severity severity,
                    String key) {
            }

        };
        bean.setToken(bean.getToken());

        MenuBean menuBean = new MenuBean();
        bean.setMenuBean(menuBean);
        bean.getMenuBean();

        localMarketplace = new VOMarketplace();
        localMarketplace.setMarketplaceId(LOCAL_MP);
        localMarketplaceCE = new VOCatalogEntry();
        localMarketplaceCE.setMarketplace(localMarketplace);
        localMarketplaceCE.setAnonymousVisible(true);

        globalMarketplace = new VOMarketplace();
        globalMarketplace.setMarketplaceId(GLOBAL_MP);
        globalMarketplaceCE = new VOCatalogEntry();
        globalMarketplaceCE.setMarketplace(globalMarketplace);
        globalMarketplaceCE.setAnonymousVisible(true);
        globalOpenMarketplace = new Marketplace();
        globalOpenMarketplace.setOpen(true);
        globalOpenMarketplace.setMarketplaceId(GLOBAL_OPEN_MP);

        localMarketplace2 = new VOMarketplace();
        localMarketplace2.setMarketplaceId(LOCAL_MP + "2");

        globalMarketplace2 = new VOMarketplace();
        globalMarketplace2.setMarketplaceId(GLOBAL_MP + "2");
    }

    @Test
    public void testGetMarketplacesForSupplier() throws Exception {
        returnedMarketplaces = new ArrayList<VOMarketplace>();
        returnedMarketplaces.add(localMarketplace);
        returnedMarketplaces.add(globalMarketplace);

        List<Marketplace> marketplaces = bean.getMarketplacesForSupplier();
        Assert.assertTrue(serviceCalled);
        assertMarketplaces(returnedMarketplaces, marketplaces);
    }

    // assume list have same order
    private void assertMarketplaces(List<VOMarketplace> expected,
            List<Marketplace> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getMarketplaceId(), actual.get(i)
                    .getMarketplaceId());
        }
    }

    @Test
    public void testGetMarketplacesForOperator() throws Exception {
        returnedMarketplaces = Arrays.asList(localMarketplace,
                localMarketplace2, globalMarketplace, globalMarketplace2);
        List<Marketplace> marketplaces = bean.getMarketplacesForOperator();
        Assert.assertTrue(serviceCalled);
        assertMarketplaces(returnedMarketplaces, marketplaces);

        serviceCalled = false;
        marketplaces = bean.getMarketplacesForOperator();
        Assert.assertFalse(serviceCalled);
        assertMarketplaces(returnedMarketplaces, marketplaces);
    }

    @Test
    public void testGetMarketplacesOwned() throws Exception {
        returnedMarketplaces = Arrays.asList(globalMarketplace2);
        List<Marketplace> marketplaces = bean.getMarketplacesOwned();
        Assert.assertTrue(serviceCalled);
        assertMarketplaces(returnedMarketplaces, marketplaces);

        serviceCalled = false;
        marketplaces = bean.getMarketplacesOwned();
        Assert.assertFalse(serviceCalled);
        assertMarketplaces(returnedMarketplaces, marketplaces);
    }

    @Test
    public void testSetAndGetMarketplaceId() throws Exception {
        String idToSet = "POIUZGTRFD";
        bean.setMarketplaceId(idToSet);
        Assert.assertEquals(idToSet, bean.getMarketplaceId());
    }

    @Test
    public void testPublishService() throws Exception {
        VOServiceDetails toPublish = new VOServiceDetails();
        List<VOCatalogEntry> expected = new ArrayList<VOCatalogEntry>();
        VOCatalogEntry voCE = new VOCatalogEntry();
        voCE.setMarketplace(globalMarketplace);
        voCE.setAnonymousVisible(true);
        expected.add(voCE);
        bean.setMarketplaceId(globalMarketplace.getMarketplaceId());
        ServiceDetails sd = new ServiceDetails(toPublish);
        sd.setPublicService(true);
        bean.publishService(sd, null);
        Assert.assertTrue(serviceCalled);
        Assert.assertEquals(toPublish, passedService);
        Assert.assertEquals(
                expected.get(0).getMarketplace().getMarketplaceId(),
                passedMarketplaceEntries.get(0).getMarketplace()
                        .getMarketplaceId());
        Assert.assertEquals(expected.get(0).isAnonymousVisible(),
                passedMarketplaceEntries.get(0).isAnonymousVisible());
    }

    @Test
    public void testPublishService_noMarketplace_publicService()
            throws Exception {
        VOServiceDetails toPublish = new VOServiceDetails();
        bean.setMarketplaceId(null);
        ServiceDetails sd = new ServiceDetails(toPublish);
        sd.setPublicService(false);
        bean.publishService(sd, null);
        Assert.assertTrue(serviceCalled);
        Assert.assertEquals(toPublish, passedService);
        Assert.assertEquals(null, passedMarketplaceEntries.get(0)
                .getMarketplace());
        Assert.assertEquals(false, passedMarketplaceEntries.get(0)
                .isAnonymousVisible());
    }

    @Test
    public void testPublishService_noMarketplace_nonpublicService()
            throws Exception {
        VOServiceDetails toPublish = new VOServiceDetails();
        bean.setMarketplaceId(null);
        ServiceDetails sd = new ServiceDetails(toPublish);
        sd.setPublicService(true);
        bean.publishService(sd, null);
        Assert.assertTrue(serviceCalled);
        Assert.assertEquals(toPublish, passedService);
        Assert.assertEquals(null, passedMarketplaceEntries.get(0)
                .getMarketplace());
        Assert.assertEquals(true, passedMarketplaceEntries.get(0)
                .isAnonymousVisible());
    }

    @Test
    public void testGetMarketplacesForService() throws Exception {
        returnedCatalogEntries = new ArrayList<VOCatalogEntry>();
        returnedCatalogEntries.add(localMarketplaceCE);
        returnedCatalogEntries.add(globalMarketplaceCE);
        VOService service = new VOService();

        List<VOCatalogEntry> catalogEntries = bean
                .getMarketplacesForService(service);

        Assert.assertEquals(service, passedService);
        Assert.assertTrue(serviceCalled);
        Assert.assertEquals(2, catalogEntries.size());
        Assert.assertEquals(localMarketplaceCE.getMarketplace()
                .getMarketplaceId(), catalogEntries.get(0).getMarketplace()
                .getMarketplaceId());
        Assert.assertEquals(globalMarketplaceCE.getMarketplace()
                .getMarketplaceId(), catalogEntries.get(1).getMarketplace()
                .getMarketplaceId());

        serviceCalled = true;
        returnedMarketplaces = new ArrayList<VOMarketplace>();
        bean.getMarketplacesForSupplier();
        Assert.assertTrue(serviceCalled);
    }

    @Test
    public void testGetSuppliersForMarketplace() throws Exception {
        bean.setMarketplaceId(GLOBAL_MP);
        returnedSuppliers = createOrgsToReturn();
        List<Organization> list = bean.getSupplierMarketplaceRelation();
        Assert.assertTrue(serviceCalled);
        Assert.assertEquals(GLOBAL_MP, passedMarketplaceId);
        Assert.assertEquals(returnedSuppliers.size(), list.size());
        for (int i = 0; i < list.size(); i++) {
            Assert.assertEquals(returnedSuppliers.get(i).getOrganizationId(),
                    list.get(i).getOrganizationId());
        }

        // calling the get method again must not cause a service call
        serviceCalled = false;
        bean.getSupplierMarketplaceRelation();
        Assert.assertFalse(serviceCalled);

        // changing the marketplace id must reset the supplier list causing a
        // service call
        bean.setMarketplaceId(LOCAL_MP);
        bean.marketplaceChangedForManageSeller();
        bean.getSupplierMarketplaceRelation();
        Assert.assertTrue(serviceCalled);
    }

    @Test
    public void testAddSuppliersToMarketplace() throws Exception {

        bean.setMarketplaceId(GLOBAL_MP);
        String supplierId = "supplier1";
        bean.setSupplierIdToAdd(supplierId);
        String token = bean.getToken();
        bean.setToken(token);
        bean.addSupplierMarketplaceRelation();
        Assert.assertTrue(serviceCalled);
        Assert.assertEquals(1, passedOrganizationIds.size());
        Assert.assertTrue(passedOrganizationIds.contains(supplierId));
        Assert.assertEquals(GLOBAL_MP, passedMarketplaceId);
        serviceCalled = false;
        bean.setToken(token);
        String outcome = bean.addSupplierMarketplaceRelation();
        // no execution with same token
        Assert.assertFalse(serviceCalled);
        Assert.assertEquals(BaseBean.OUTCOME_SUCCESS, outcome);
    }

    @Test
    public void testBanSuppliersFromMarketplace() throws Exception {
        bean.setMarketplaceId(GLOBAL_OPEN_MP);
        bean.marketplace = globalOpenMarketplace;
        String supplierId = "supplier1";
        bean.setSupplierIdToAdd(supplierId);
        String token = bean.getToken();
        bean.setToken(token);
        bean.addSupplierMarketplaceRelation();
        Assert.assertTrue(serviceCalled);
        Assert.assertNull(passedOrganizationIds);
        Assert.assertEquals(1, bannedOrganizationIds.size());
        Assert.assertTrue(bannedOrganizationIds.contains(supplierId));
        Assert.assertEquals(GLOBAL_OPEN_MP, passedMarketplaceId);
        serviceCalled = false;
        bean.setToken(token);
        String outcome = bean.addSupplierMarketplaceRelation();
        // no execution with same token
        Assert.assertFalse(serviceCalled);
        Assert.assertEquals(BaseBean.OUTCOME_SUCCESS, outcome);
    }

    @Test
    public void testAddSuppliersToMarketplace_NoMarketplaceIdSet()
            throws Exception {
        bean.setSupplierIdToAdd("supplier1");
        bean.addSupplierMarketplaceRelation();
        Assert.assertFalse(serviceCalled);
    }

    @Test
    public void testAddSuppliersToMarketplace_NoSupplierIdSet()
            throws Exception {
        bean.setMarketplaceId(GLOBAL_MP);
        bean.marketplaceChangedForManageSeller();
        bean.addSupplierMarketplaceRelation();
        Assert.assertTrue(serviceCalled);
    }

    @Test
    public void testRemoveSuppliersFromMarketplace() throws Exception {
        bean.setMarketplaceId(GLOBAL_MP);
        returnedSuppliers = createOrgsToReturn();
        List<Organization> list = bean.getSupplierMarketplaceRelation();
        for (Organization org : list) {
            org.setSelected(true);
        }
        // reset due to get suppliers call;
        serviceCalled = false;
        String token = bean.getToken();
        bean.setToken(token);

        bean.removeSupplierMarketplaceRelation();
        Assert.assertTrue(serviceCalled);
        Assert.assertEquals(GLOBAL_MP, passedMarketplaceId);
        Assert.assertEquals(returnedSuppliers.size(),
                passedOrganizationIds.size());
        for (int i = 0; i < list.size(); i++) {
            Assert.assertEquals(returnedSuppliers.get(i).getOrganizationId(),
                    list.get(i).getOrganizationId());
        }
        serviceCalled = false;
        bean.setToken(token);
        String outcome = bean.removeSupplierMarketplaceRelation();
        Assert.assertFalse(serviceCalled);
        Assert.assertEquals(BaseBean.OUTCOME_SUCCESS, outcome);
    }

    @Test
    public void testLiftBanForSuppliersOnMarketplace() throws Exception {
        bean.setMarketplaceId(GLOBAL_OPEN_MP);
        bean.marketplace = globalOpenMarketplace;
        bannedSuppliers = createOrgsToReturn();
        List<Organization> list = bean.getSupplierMarketplaceRelation();
        for (Organization org : list) {
            org.setSelected(true);
        }
        // reset due to get suppliers call;
        serviceCalled = false;
        String token = bean.getToken();
        bean.setToken(token);

        bean.removeSupplierMarketplaceRelation();
        Assert.assertTrue(serviceCalled);
        Assert.assertEquals(GLOBAL_OPEN_MP, passedMarketplaceId);
        Assert.assertNull(passedOrganizationIds);
        Assert.assertNull(returnedSuppliers);
        Assert.assertEquals(bannedSuppliers.size(),
                bannedOrganizationIds.size());
        for (int i = 0; i < list.size(); i++) {
            Assert.assertEquals(bannedSuppliers.get(i).getOrganizationId(),
                    list.get(i).getOrganizationId());
        }
        serviceCalled = false;
        bean.setToken(token);
        String outcome = bean.removeSupplierMarketplaceRelation();
        Assert.assertFalse(serviceCalled);
        Assert.assertEquals(BaseBean.OUTCOME_SUCCESS, outcome);
    }

    @Test
    public void testRemoveSuppliersFromMarketplace_NoSuppliersSelected()
            throws Exception {
        bean.setMarketplaceId(GLOBAL_MP);
        returnedSuppliers = createOrgsToReturn();
        bean.getSupplierMarketplaceRelation();

        // reset due to get suppliers call;
        serviceCalled = false;
        bean.removeSupplierMarketplaceRelation();
        Assert.assertFalse(serviceCalled);
    }

    @Test
    public void testRemoveSuppliersFromMarketplace_NoMarketplace()
            throws Exception {
        bean.setMarketplaceId(null);
        bean.removeSupplierMarketplaceRelation();
        assertFalse(serviceCalled);
    }

    @Test
    public void testSupplierIdToAdd() {

        bean.setSupplierIdToAdd(GLOBAL_MP);
        assertEquals(GLOBAL_MP, bean.getSupplierIdToAdd());
    }

    private static List<VOOrganization> createOrgsToReturn() {
        List<VOOrganization> result = new ArrayList<VOOrganization>();
        VOOrganization org = new VOOrganization();
        org.setOrganizationId("supplier1");
        result.add(org);
        org = new VOOrganization();
        org.setOrganizationId("supplier2");
        result.add(org);
        return result;
    }

    @Test
    public void testMarketplaceDelete() throws SaaSApplicationException {

        Marketplace voMp = bean.getMarketplace();
        voMp.setMarketplaceId(mpId);

        String result = bean.deleteMarketplace();
        Assert.assertEquals(null, result);
        Assert.assertEquals(mpId, passedMarketplaceId);
    }

    @Test
    public void testFindMarketplace() {

        // set MarketplaceBean:marketplaces
        returnedMarketplaces = new ArrayList<VOMarketplace>();
        returnedMarketplaces.add(localMarketplace);
        returnedMarketplaces.add(globalMarketplace);
        returnedMarketplaces.add(localMarketplace2);
        returnedMarketplaces.add(globalMarketplace2);
        bean.getMarketplacesForSupplier();

        // trigger call of findMarketplace
        bean.setMarketplaceId(GLOBAL_MP);
        bean.marketplaceChangedForManageSeller();
        Assert.assertEquals(globalMarketplace.getMarketplaceId(), bean
                .getMarketplace().getMarketplaceId());
    }

    @Test
    public void testDisabledFlag() {
        // no ID => no edit possible
        bean.setMarketplaceId(null);
        Assert.assertTrue(bean.isDisabledForEdit());
        bean.setMarketplaceId("0");
        bean.marketplaceChangedForManageSeller();
        Assert.assertNull(bean.getMarketplaceId());
    }
}
