/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Mar 22, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Test;
import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.i18nservice.bean.ImageResourceServiceBean;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.internal.intf.MarketplaceCacheService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.landingpageService.local.LandingpageServiceLocal;
import org.oscm.marketplace.auditlog.MarketplaceAuditLogCollector;
import org.oscm.marketplace.dao.MarketplaceAccessDao;
import org.oscm.serviceprovisioningservice.auditlog.ServiceAuditLogCollector;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningPartnerServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.AccountServiceStub;
import org.oscm.test.stubs.CategorizationServiceStub;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.ServiceProvisioningServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;

/**
 * Tests the branding of the marketplace
 * 
 * @author barzu
 */
public class MarketplaceServiceBeanBrandingIT extends EJBTestBase {

    private static final String BRANDING_URL = "http://www.fujitsu.com";

    private VOMarketplace voMarketplace;
    private long supplierUserKey;
    private MarketplaceService mpService;

    @Override
    protected void setup(TestContainer container) throws Exception {
        voMarketplace = new VOMarketplace();
        voMarketplace.setMarketplaceId("1");
    }

    private void setupWithContainer() throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(mock(MarketplaceAccessDao.class));
        container.addBean(new LocalizerServiceBean());
        container.addBean(new CommunicationServiceStub());
        container.addBean(new TriggerQueueServiceStub());
        container.addBean(new ServiceProvisioningServiceStub());
        container.addBean(new AccountServiceStub());
        container.addBean(new CategorizationServiceStub());
        container.addBean(mock(IdentityServiceLocal.class));
        container.addBean(mock(LandingpageServiceLocal.class));
        container.addBean(mock(ApplicationServiceLocal.class));
        container.addBean(new MarketplaceAuditLogCollector());
        container.addBean(new ImageResourceServiceBean());
        container.addBean(new ServiceAuditLogCollector());
        container.addBean(mock(ServiceProvisioningPartnerServiceLocal.class));
        container.addBean(mock(MarketplaceCacheService.class));
        container.addBean(new MarketplaceServiceLocalBean());
        container.addBean(new MarketplaceServiceBean());
        mpService = container.get(MarketplaceService.class);
        final DataService mgr = container.get(DataService.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                Organization supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);
                String mId = Marketplaces.ensureMarketplace(supplier, null, mgr)
                        .getMarketplaceId();
                voMarketplace.setMarketplaceId(mId);

                PlatformUser createUserForOrg = Organizations
                        .createUserForOrg(mgr, supplier, true, "admin");
                PlatformUsers.grantRoles(mgr, createUserForOrg,
                        UserRoleType.SERVICE_MANAGER);
                supplierUserKey = createUserForOrg.getKey();

                mgr.flush();
                return null;
            }
        });

        container.login(supplierUserKey, new String[] { ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_MARKETPLACE_OWNER });

    }

    private MarketplaceServiceBean setupWithMock(Marketplace mp)
            throws Exception {
        Organization org = new Organization();
        DataService mockDs = mock(DataService.class);
        mp.setOrganization(org);
        doReturn(mp).when(mockDs)
                .getReferenceByBusinessKey(any(Marketplace.class));
        PlatformUser user = new PlatformUser();
        user.setOrganization(org);
        doReturn(user).when(mockDs).getCurrentUser();

        MarketplaceServiceBean mpService = spy(new MarketplaceServiceBean());
        mpService.dm = mockDs;

        mpService.identityService = mock(IdentityServiceLocal.class);

        return mpService;
    }

    @Test
    public void saveBrandingUrl() throws Exception {
        Marketplace mp = new Marketplace();
        MarketplaceServiceBean brandMgmtBean = setupWithMock(mp);

        brandMgmtBean.saveBrandingUrl(voMarketplace, BRANDING_URL);
        assertEquals(BRANDING_URL, mp.getBrandingUrl());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void saveBrandingUrl_Concurrency_OutdatedVersion() throws Exception {
        Marketplace mp = new Marketplace();
        MarketplaceServiceBean brandMgmtBean = setupWithMock(mp);
        voMarketplace.setVersion(mp.getVersion() - 1);

        brandMgmtBean.saveBrandingUrl(voMarketplace, BRANDING_URL);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void saveBrandingUrl_Concurrency_NullKeyVO() throws Exception {
        Marketplace mp = new Marketplace();
        MarketplaceServiceBean brandMgmtBean = setupWithMock(mp);
        // the VO has key=0
        mp.setKey(1L);

        brandMgmtBean.saveBrandingUrl(voMarketplace, BRANDING_URL);
    }

    @Test
    public void saveBrandingUrl_delete() throws Exception {
        Marketplace mp = new Marketplace();
        mp.setBrandingUrl(BRANDING_URL);
        MarketplaceServiceBean brandMgmtBean = setupWithMock(mp);

        brandMgmtBean.saveBrandingUrl(voMarketplace, null);
        assertEquals(null, mp.getBrandingUrl());
    }

    @Test
    public void saveBrandingUrl_deleteBlank() throws Exception {
        Marketplace mp = new Marketplace();
        mp.setBrandingUrl(BRANDING_URL);
        MarketplaceServiceBean brandMgmtBean = setupWithMock(mp);

        brandMgmtBean.saveBrandingUrl(voMarketplace, " ");
        assertEquals(null, mp.getBrandingUrl());
    }

    @Test(expected = EJBException.class)
    public void saveBrandingUrl_NoMpOwner() throws Exception {
        setupWithContainer();
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        mpService.saveBrandingUrl(voMarketplace, BRANDING_URL);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void saveBrandingUrl_MarketplaceNotFound() throws Exception {
        setupWithContainer();
        voMarketplace.setMarketplaceId("-1");
        mpService.saveBrandingUrl(voMarketplace, BRANDING_URL);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void saveBrandingUrl_NullMarketplaceId() throws Exception {
        setupWithContainer();
        voMarketplace.setMarketplaceId(null);
        mpService.saveBrandingUrl(voMarketplace, BRANDING_URL);
    }

    @Test(expected = org.oscm.internal.types.exception.IllegalArgumentException.class)
    public void saveBrandingUrl_NullMpId() throws Exception {
        new MarketplaceServiceBean().saveBrandingUrl(null, BRANDING_URL);
    }

    @Test
    public void saveBrandingUrl_InvalidUrl() throws Exception {
        try {
            new MarketplaceServiceBean().saveBrandingUrl(voMarketplace,
                    "fujitsu");
            fail("ValidationException expected");
        } catch (ValidationException e) {
            assertEquals(ValidationException.ReasonEnum.URL, e.getReason());
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void saveBrandingUrl_NotOwnerOfThisMP() throws Exception {
        Marketplace mp = new Marketplace();
        MarketplaceServiceBean brandMgmtBean = setupWithMock(mp);
        mp.setOrganization(new Organization());

        brandMgmtBean.saveBrandingUrl(voMarketplace, BRANDING_URL);
    }

    @Test
    public void getBrandingUrl() throws Exception {
        setupWithContainer();
        container.login(supplierUserKey);
        mpService.getBrandingUrl(voMarketplace.getMarketplaceId());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getBrandingUrl_MarketplaceNotFound() throws Exception {
        setupWithContainer();
        mpService.getBrandingUrl("-1");
    }

    @Test(expected = org.oscm.internal.types.exception.IllegalArgumentException.class)
    public void getBrandingUrl_NullMpId() throws Exception {
        new MarketplaceServiceBean().getBrandingUrl(null);
    }

    @Test
    public void getBrandingUrl_NotOwnerOfThisMP() throws Exception {
        Marketplace mp = new Marketplace();
        mp.setBrandingUrl(BRANDING_URL);
        MarketplaceServiceBean brandMgmtBean = setupWithMock(mp);
        mp.setOrganization(new Organization());

        assertEquals(BRANDING_URL, brandMgmtBean.getBrandingUrl("1"));
    }

}
