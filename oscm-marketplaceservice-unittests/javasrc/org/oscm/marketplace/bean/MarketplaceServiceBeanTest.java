/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 16-09-2016                                                  
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.bean;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.oscm.accountservice.assembler.OrganizationAssembler;
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
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.landingpageService.local.LandingpageType;
import org.oscm.marketplace.assembler.MarketplaceAssembler;
import org.oscm.marketplace.dao.MarketplaceAccessDao;
import org.oscm.test.data.UserRoles;
import org.oscm.types.enumtypes.EmailType;

/**
 * Unit testing of the operations of {@link MarketplaceServiceBean}.
 * 
 * @author stavreva
 */
public class MarketplaceServiceBeanTest {

    private MarketplaceServiceBean mpSrv;

    private PlatformUser admin1;
    private Organization org;

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
        mpSrvL.marketplaceAccessDao = mock(MarketplaceAccessDao.class);
        doNothing().when(mpSrvL.marketplaceAccessDao)
                .removeAccessForMarketplace(anyLong());
        mpSrv.marketplaceServiceLocal = mpSrvL;
        DataService dml = mock(DataService.class);
        ((MarketplaceServiceLocalBean) mpSrv.marketplaceServiceLocal).ds = dml;

        admin1 = new PlatformUser();
        doReturn(admin1).when(mpSrv.dm).getCurrentUser();
        doReturn(admin1).when(dml).getCurrentUser();

        org = new Organization();
        org.setOrganizationId("myOrg");
        admin1.setOrganization(org);
        admin1.setAssignedRoles(UserRoles.createRoleAssignments(admin1,
                UserRoleType.PLATFORM_OPERATOR));
        doReturn(org).when(dml).getReferenceByBusinessKey(
                any(Organization.class));

        mpSrv.accountService = mock(AccountServiceLocal.class);
        doReturn(Arrays.asList(admin1)).when(mpSrv.accountService)
                .getOrganizationAdmins(anyLong());

        mpSrvL.accountService = mock(AccountServiceLocal.class);
        doReturn(org).when(mpSrvL.accountService).addOrganizationToRole(
                anyString(), any(OrganizationRoleType.class));
        doReturn(Arrays.asList(admin1)).when(mpSrvL.accountService)
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

        doReturn(MarketplaceAssembler.toMarketplace(getMarketplace(true)))
                .when(mpSrvL.ds).getReferenceByBusinessKey(
                        any(Marketplace.class));
    }

    @Test
    public void openMarketplace_mpCache() throws Exception {
        // given
        VOMarketplace restrictedMp = getMarketplace(true);
        doReturn(getDomainMarketplace(true))
                .when(mpSrv.marketplaceServiceLocal).getMarketplaceForId(
                        restrictedMp.getMarketplaceId());

        // when
        mpSrv.openMarketplace(restrictedMp.getMarketplaceId());

        // then
        verify(mpSrv.marketplaceCache, atLeastOnce()).resetConfiguration(
                restrictedMp.getMarketplaceId());

    }

    @Test
    public void closeMarketplace_mpCache() throws Exception {
        // given
        VOMarketplace restrictedMp = getMarketplace(true);

        // when
        mpSrv.closeMarketplace(restrictedMp.getMarketplaceId(),
                Collections.<Long> emptySet(), Collections.<Long> emptySet());

        // then
        verify(mpSrv.marketplaceCache, atLeastOnce()).resetConfiguration(
                restrictedMp.getMarketplaceId());
    }

    @Test
    public void grantAccessToMarketplaceToOrganization_MpCache()
            throws Exception {
        // given
        VOMarketplace restrictedMp = setupUpdateMarketplace();

        // when
        mpSrv.grantAccessToMarketPlaceToOrganization(restrictedMp,
                OrganizationAssembler.toVOOrganization(org));

        // then
        verify(mpSrv.marketplaceCache, atLeastOnce()).resetConfiguration(
                restrictedMp.getMarketplaceId());
    }

    @Test
    public void updateMarketplace_mpCache() throws Exception {
        // given
        VOMarketplace restrictedMp = setupUpdateMarketplace();

        // when
        mpSrv.updateMarketplace(getMarketplace(true));

        // then
        verify(mpSrv.marketplaceCache, atLeastOnce()).resetConfiguration(
                restrictedMp.getMarketplaceId());
    }

    private VOMarketplace setupUpdateMarketplace() throws Exception {
        doReturn(Boolean.TRUE).when(mpSrv.marketplaceServiceLocal)
                .updateOwningOrganization(any(Marketplace.class), anyString(),
                        Matchers.anyBoolean());
        Marketplace mp = new Marketplace();
        mp.setOrganization(new Organization());
        doReturn(mp).when(mpSrv.marketplaceServiceLocal).getMarketplace(
                anyString());
        VOMarketplace voMp = getMarketplace(true);
        doReturn(voMp.getName())
                .when(((MarketplaceServiceLocalBean) mpSrv.marketplaceServiceLocal).localizer)
                .getLocalizedTextFromDatabase(anyString(), anyLong(),
                        any(LocalizedObjectTypes.class));
        ((MarketplaceServiceLocalBean) mpSrv.marketplaceServiceLocal).commService = mock(CommunicationServiceLocal.class);
        return voMp;
    }

    private VOMarketplace getMarketplace(boolean isRestricted) {
        VOMarketplace voMarketplace = new VOMarketplace();
        voMarketplace.setName("Super MP");
        voMarketplace.setMarketplaceId("FUJITSU");
        if (isRestricted) {
            voMarketplace.setRestricted(true);
        }
        return voMarketplace;
    }

    private Marketplace getDomainMarketplace(boolean isRestricted) {
        Marketplace mp = new Marketplace();
        mp.setMarketplaceId("FUJITSU");
        if (isRestricted) {
            mp.setRestricted(true);
        }
        return mp;
    }
}
