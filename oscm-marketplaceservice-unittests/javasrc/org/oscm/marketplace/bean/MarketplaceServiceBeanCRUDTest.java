/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Mar 30, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.bean;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PublicLandingpage;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.internal.intf.MarketplaceCacheService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.UserRoleAssignmentException;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.landingpageService.local.LandingpageType;
import org.oscm.test.data.UserRoles;
import org.oscm.types.enumtypes.EmailType;

/**
 * Unit testing of the CRUD operations of {@link MarketplaceServiceBean}.
 * 
 * @author barzu
 */
public class MarketplaceServiceBeanCRUDTest {

    private MarketplaceServiceBean mpSrv;

    private PlatformUser admin1;
    private PlatformUser admin2;

    @Before
    public void setup() throws Exception {
        mpSrv = spy(new MarketplaceServiceBean());
        mpSrv.landingpageService = mock(LandingpageServiceBeanLocal.class);
        mpSrv.marketplaceCache = mock(MarketplaceCacheService.class);
        doReturn(new PublicLandingpage()).when(mpSrv.landingpageService)
                .createDefaultLandingpage();
        mpSrv.dm = mock(DataService.class);
        MarketplaceServiceLocalBean mpSrvL = spy(new MarketplaceServiceLocalBean());
        mpSrvL.marketplaceCache = mock(MarketplaceCacheService.class);
        mpSrv.marketplaceServiceLocal = mpSrvL;
        DataService dml = mock(DataService.class);
        ((MarketplaceServiceLocalBean) mpSrv.marketplaceServiceLocal).ds = dml;

        admin1 = new PlatformUser();
        doReturn(admin1).when(mpSrv.dm).getCurrentUser();
        doReturn(admin1).when(dml).getCurrentUser();

        Organization org = new Organization();
        org.setOrganizationId("myOrg");
        admin1.setOrganization(org);
        admin1.setAssignedRoles(UserRoles.createRoleAssignments(admin1,
                UserRoleType.PLATFORM_OPERATOR));
        doReturn(org).when(dml).getReferenceByBusinessKey(
                any(Organization.class));
        admin2 = new PlatformUser();

        mpSrv.accountService = mock(AccountServiceLocal.class);
        doReturn(Arrays.asList(admin1, admin2)).when(mpSrv.accountService)
                .getOrganizationAdmins(anyLong());

        mpSrvL.accountService = mock(AccountServiceLocal.class);
        doReturn(org).when(mpSrvL.accountService).addOrganizationToRole(
                anyString(), any(OrganizationRoleType.class));
        doReturn(Arrays.asList(admin1, admin2)).when(mpSrvL.accountService)
                .getOrganizationAdmins(anyLong());

        mpSrv.localizer = mock(LocalizerServiceLocal.class);
        mpSrvL.localizer = mock(LocalizerServiceLocal.class);

        mpSrv.identityService = mock(IdentityServiceLocal.class);
        ((MarketplaceServiceLocalBean) mpSrv.marketplaceServiceLocal).identityService = mock(IdentityServiceLocal.class);

        mpSrvL.landingpageService = mock(LandingpageServiceBeanLocal.class);
        when(mpSrvL.landingpageService.loadLandingpageType(anyString()))
                .thenReturn(LandingpageType.PUBLIC);

        doNothing().when(mpSrv.marketplaceServiceLocal).sendNotification(
                any(EmailType.class), any(Marketplace.class),
                anyListOf(PlatformUser.class));

        doReturn(Boolean.FALSE).when(mpSrv).findMarketplaceKeyByMarketplaceId(
                anyString());
    }

    @Test
    public void createMarketplace_AutoMarketplaceOwner() throws Exception {
        mpSrv.createMarketplace(getMarketplace());

        // MARKETPLACE_OWNER role is automatically given to administrators
        verify(mpSrv.identityService, times(1)).grantUserRoles(eq(admin1),
                argThat(isRole(UserRoleType.MARKETPLACE_OWNER)));
        verify(mpSrv.identityService, times(1)).grantUserRoles(eq(admin2),
                argThat(isRole(UserRoleType.MARKETPLACE_OWNER)));
    }

    @Test(expected = UserRoleAssignmentException.class)
    public void createMarketplace_AutoMarketplaceOwner_NoMatchingOrgRoles()
            throws Exception {
        doThrow(new UserRoleAssignmentException()).when(mpSrv.identityService)
                .grantUserRoles(any(PlatformUser.class),
                        anyListOf(UserRoleType.class));

        mpSrv.createMarketplace(getMarketplace());
    }

    @Test
    public void createMarketplace_RevenueModelCreation() throws Exception {
        mpSrv.createMarketplace(getMarketplace());

        verify(mpSrv.marketplaceServiceLocal, times(1)).createRevenueModels(
                any(Marketplace.class), eq(BigDecimal.ZERO),
                eq(BigDecimal.ZERO), eq(BigDecimal.ZERO));
    }

    @Test
    public void updateMarketplace_AutoMarketplaceOwner() throws Exception {
        setupUpdateMarketplace();

        mpSrv.updateMarketplace(getMarketplace());

        // MARKETPLACE_OWNER role is automatically given to administrators
        verify(
                ((MarketplaceServiceLocalBean) mpSrv.marketplaceServiceLocal).identityService,
                times(1)).grantUserRoles(eq(admin1),
                argThat(isRole(UserRoleType.MARKETPLACE_OWNER)));
        verify(
                ((MarketplaceServiceLocalBean) mpSrv.marketplaceServiceLocal).identityService,
                times(1)).grantUserRoles(eq(admin2),
                argThat(isRole(UserRoleType.MARKETPLACE_OWNER)));
    }

    @Test(expected = UserRoleAssignmentException.class)
    public void updateMarketplace_AutoMarketplaceOwner_NoMatchingOrgRoles()
            throws Exception {
        setupUpdateMarketplace();
        doThrow(new UserRoleAssignmentException())
                .when(((MarketplaceServiceLocalBean) mpSrv.marketplaceServiceLocal).identityService)
                .grantUserRoles(any(PlatformUser.class),
                        anyListOf(UserRoleType.class));

        mpSrv.updateMarketplace(getMarketplace());
    }

    private void setupUpdateMarketplace() throws Exception {
        doReturn(Boolean.TRUE).when(mpSrv.marketplaceServiceLocal)
                .updateOwningOrganization(any(Marketplace.class), anyString(),
                        Matchers.anyBoolean());
        Marketplace mp = new Marketplace();
        mp.setOrganization(new Organization());
        doReturn(mp).when(mpSrv.marketplaceServiceLocal).getMarketplace(
                anyString());
        VOMarketplace voMp = getMarketplace();
        doReturn(voMp.getName())
                .when(((MarketplaceServiceLocalBean) mpSrv.marketplaceServiceLocal).localizer)
                .getLocalizedTextFromDatabase(anyString(), anyLong(),
                        any(LocalizedObjectTypes.class));
        ((MarketplaceServiceLocalBean) mpSrv.marketplaceServiceLocal).commService = mock(CommunicationServiceLocal.class);
    }

    private VOMarketplace getMarketplace() {
        VOMarketplace voMarketplace = new VOMarketplace();
        voMarketplace.setName("Super MP");
        voMarketplace.setMarketplaceId("FUJITSU");
        return voMarketplace;
    }

    private ArgumentMatcher<List<UserRoleType>> isRole(
            final UserRoleType expected) {
        return new ArgumentMatcher<List<UserRoleType>>() {
            @Override
            public boolean matches(Object argument) {
                UserRoleType actual = (UserRoleType) (((List<?>) argument)
                        .get(0));
                return actual == expected;
            }
        };
    }
}
