/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 12.08.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.marketplace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.pricing.POMarketplacePriceModel;
import org.oscm.internal.pricing.POPartnerPriceModel;
import org.oscm.internal.pricing.PORevenueShare;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.marketplace.bean.MarketplaceServiceBean;
import org.oscm.marketplace.bean.MarketplaceServiceLocalBean;
import org.oscm.types.enumtypes.EmailType;

/**
 * @author barzu
 */
public class MarketplaceServiceManagePartnerBeanTest {

    private MarketplaceServiceManagePartnerBean bean = new MarketplaceServiceManagePartnerBean();
    private Marketplace marketplace;

    @Before
    public void setup() throws Exception {
        bean.mpServiceLocal = mock(MarketplaceServiceLocalBean.class);
        marketplace = givenMarketplace();
        doReturn(marketplace).when(bean.mpServiceLocal).getMarketplace(
                anyString());
        PlatformUser user = new PlatformUser();
        user.setLocale("en");
        bean.dm = mock(DataService.class);
        doReturn(user).when(bean.dm).getCurrentUser();
        bean.localizer = mock(LocalizerServiceLocal.class);

        // mock saving of revenue shares
        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                Marketplace mp = ((Marketplace) args[0]);
                Marketplace newMp = ((Marketplace) args[1]);
                mp.getPriceModel().setRevenueShare(
                        newMp.getPriceModel().getRevenueShare());
                mp.getResellerPriceModel().setRevenueShare(
                        newMp.getResellerPriceModel().getRevenueShare());
                mp.getBrokerPriceModel().setRevenueShare(
                        newMp.getBrokerPriceModel().getRevenueShare());
                return Boolean.FALSE;
            }
        }).when(bean.mpServiceLocal).updateMarketplace(eq(marketplace),
                any(Marketplace.class), anyString(), anyString(), anyInt(),
                anyInt(), anyInt());
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMarketplace_nullMarketplace() throws Exception {
        bean.updateMarketplace(null, new POMarketplacePriceModel(),
                new POPartnerPriceModel());
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMarketplace_nullMarketplacePM() throws Exception {
        bean.updateMarketplace(new VOMarketplace(), null,
                new POPartnerPriceModel());
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMarketplace_nullPartnerPM() throws Exception {
        bean.updateMarketplace(new VOMarketplace(),
                new POMarketplacePriceModel(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMarketplace_nullMarketplaceShare() throws Exception {
        bean.updateMarketplace(givenVOMarketplace(),
                new POMarketplacePriceModel(), new POPartnerPriceModel());
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMarketplace_nullResellerShare() throws Exception {
        POPartnerPriceModel pPM = givenPOPartnerPriceModel();
        pPM.setRevenueShareResellerModel(null);

        bean.updateMarketplace(givenVOMarketplace(),
                givenPOMarketplacePriceModel(), pPM);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMarketplace_nullBrokerShare() throws Exception {
        POPartnerPriceModel pPM = givenPOPartnerPriceModel();
        pPM.setRevenueShareBrokerModel(null);

        bean.updateMarketplace(givenVOMarketplace(),
                givenPOMarketplacePriceModel(), pPM);
    }

    @Test
    public void updateMarketplace() throws Exception {
        // given
        POMarketplacePriceModel mPM = givenPOMarketplacePriceModel();
        POPartnerPriceModel pPM = givenPOPartnerPriceModel();

        // when
        Response response = bean.updateMarketplace(givenVOMarketplace(), mPM,
                pPM);

        // then
        verify(bean.mpServiceLocal, times(0)).sendNotification(
                eq(EmailType.MARKETPLACE_OWNER_ASSIGNED),
                any(Marketplace.class), anyLong());

        POMarketplacePriceModel rMPM = response
                .getResult(POMarketplacePriceModel.class);
        assertNotNull(rMPM);
        assertNotNull(rMPM.getRevenueShare());
        assertEquals(mPM.getRevenueShare().getRevenueShare(), rMPM
                .getRevenueShare().getRevenueShare());

        POPartnerPriceModel rPPM = response
                .getResult(POPartnerPriceModel.class);
        assertNotNull(rPPM);
        assertNotNull(rPPM.getRevenueShareResellerModel());
        assertEquals(pPM.getRevenueShareResellerModel().getRevenueShare(), rPPM
                .getRevenueShareResellerModel().getRevenueShare());
        assertNotNull(rPPM.getRevenueShareBrokerModel());
        assertEquals(pPM.getRevenueShareBrokerModel().getRevenueShare(), rPPM
                .getRevenueShareBrokerModel().getRevenueShare());
    }

    @Test
    public void updateMarketplace_OwnerChangedEmail() throws Exception {
        // given
        doReturn(Boolean.TRUE).when(bean.mpServiceLocal).updateMarketplace(
                eq(marketplace), any(Marketplace.class), anyString(),
                anyString(), anyInt(), anyInt(), anyInt());
        doNothing().when(bean.mpServiceLocal).sendNotification(
                eq(EmailType.MARKETPLACE_OWNER_ASSIGNED),
                any(Marketplace.class), anyLong());

        // when
        bean.updateMarketplace(givenVOMarketplace(),
                givenPOMarketplacePriceModel(), givenPOPartnerPriceModel());

        // then
        verify(bean.mpServiceLocal, times(1)).sendNotification(
                eq(EmailType.MARKETPLACE_OWNER_ASSIGNED),
                any(Marketplace.class), anyLong());
    }

    @Test(expected = SaaSApplicationException.class)
    public void updateMarketplace_MarketplaceNotFound() throws Exception {
        // given
        doThrow(new ObjectNotFoundException(ClassEnum.MARKETPLACE, "mId"))
                .when(bean.mpServiceLocal).updateMarketplace(eq(marketplace),
                        any(Marketplace.class), anyString(), anyString(),
                        anyInt(), anyInt(), anyInt());
        // when
        bean.updateMarketplace(givenVOMarketplace(),
                givenPOMarketplacePriceModel(), givenPOPartnerPriceModel());
    }

    static VOMarketplace givenVOMarketplace() {
        VOMarketplace mp = new VOMarketplace();
        mp.setMarketplaceId("mId");
        return mp;
    }

    static POMarketplacePriceModel givenPOMarketplacePriceModel() {
        POMarketplacePriceModel po = new POMarketplacePriceModel();
        po.setRevenueShare(new PORevenueShare());
        po.getRevenueShare().setRevenueShare(BigDecimal.TEN);
        return po;
    }

    static POPartnerPriceModel givenPOPartnerPriceModel() {
        POPartnerPriceModel po = new POPartnerPriceModel();
        po.setRevenueShareResellerModel(new PORevenueShare());
        po.getRevenueShareResellerModel().setRevenueShare(BigDecimal.TEN);
        po.setRevenueShareBrokerModel(new PORevenueShare());
        po.getRevenueShareBrokerModel().setRevenueShare(BigDecimal.TEN);
        return po;
    }

    static Marketplace givenMarketplace() {
        Marketplace mp = new Marketplace();
        mp.setMarketplaceId("mId");
        mp.setOrganization(new Organization());
        mp.setPriceModel(new RevenueShareModel());
        mp.getPriceModel().setRevenueShare(BigDecimal.ZERO);
        mp.setResellerPriceModel(new RevenueShareModel());
        mp.getResellerPriceModel().setRevenueShare(BigDecimal.ZERO);
        mp.setBrokerPriceModel(new RevenueShareModel());
        mp.getBrokerPriceModel().setRevenueShare(BigDecimal.ZERO);
        return mp;
    }

}
