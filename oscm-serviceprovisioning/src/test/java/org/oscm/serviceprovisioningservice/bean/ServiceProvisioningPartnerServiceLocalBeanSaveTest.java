/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.SessionContext;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ServiceOperationException;
import org.oscm.internal.types.exception.ValidationException;

public class ServiceProvisioningPartnerServiceLocalBeanSaveTest {

    ServiceProvisioningPartnerServiceLocalBean bean;

    @Before
    public void setup() {
        bean = spy(new ServiceProvisioningPartnerServiceLocalBean());
        bean.dm = mock(DataService.class);
        bean.sessionCtx = mock(SessionContext.class);
    }

    @Test(expected = ValidationException.class)
    public void updateRevenueShare_RevenueShareOverMax() throws Exception {
        // given
        RevenueShareModel invalidRevenueShare = createRevenueShareModel(
                BigDecimal.valueOf(100.01),
                RevenueShareModelType.BROKER_REVENUE_SHARE);

        RevenueShareModel revenueShare = new RevenueShareModel();

        // when
        bean.updateRevenueShare(invalidRevenueShare, revenueShare, 0);

        // then ValidationException thrown
    }

    @Test(expected = ValidationException.class)
    public void updateRevenueShare_RevenueShareUnderMin() throws Exception {
        // given
        RevenueShareModel invalidRevenueShare = createRevenueShareModel(
                BigDecimal.valueOf(-0.01),
                RevenueShareModelType.BROKER_REVENUE_SHARE);

        RevenueShareModel revenueShare = new RevenueShareModel();
        // when
        bean.updateRevenueShare(invalidRevenueShare, revenueShare, 0);

        // then ValidationException thrown
    }

    @Test
    public void updateRevenueShare_NewRevenueShare() throws Exception {
        // given
        RevenueShareModel revenueShareModelNew = createRevenueShareModel(
                BigDecimal.TEN, RevenueShareModelType.BROKER_REVENUE_SHARE);
        revenueShareModelNew.setKey(0);

        RevenueShareModel revenueShare = createRevenueShareModel(
                BigDecimal.ZERO, RevenueShareModelType.BROKER_REVENUE_SHARE);
        revenueShare.setKey(10000);

        // when
        bean.updateRevenueShare(revenueShare, revenueShareModelNew, 1);

        // then
        assertEquals(BigDecimal.ZERO, revenueShareModelNew.getRevenueShare());
        assertEquals(0, revenueShareModelNew.getKey());
        assertEquals(0, revenueShareModelNew.getVersion());
    }

    @Test(expected = SaaSSystemException.class)
    public void updateRevenueShare_NonMatchingKeys() throws Exception {
        // given
        RevenueShareModel revenueShareModelNew = createRevenueShareModel(
                BigDecimal.ZERO, RevenueShareModelType.BROKER_REVENUE_SHARE);
        revenueShareModelNew.setKey(3);

        RevenueShareModel revenueShare = new RevenueShareModel();
        revenueShare.setKey(2);

        // when
        bean.updateRevenueShare(revenueShare, revenueShareModelNew, 0);

        // then SaaSSystemException thrown
    }

    @Test(expected = ConcurrentModificationException.class)
    public void updateRevenueShare_ConcurrentCreation() throws Exception {
        // given
        RevenueShareModel revenueShareModelNew = createRevenueShareModel(
                BigDecimal.ZERO, RevenueShareModelType.BROKER_REVENUE_SHARE);
        revenueShareModelNew.setKey(2);

        RevenueShareModel revenueShare = new RevenueShareModel();
        revenueShare.setKey(0);

        // when
        bean.updateRevenueShare(revenueShare, revenueShareModelNew, 0);

        // then ConcurrentModificationException thrown
    }

    @Test(expected = ConcurrentModificationException.class)
    public void updateRevenueShare_versionMismatch() throws Exception {
        // given
        RevenueShareModel revenueShareModelNew = createRevenueShareModel(
                BigDecimal.ZERO, RevenueShareModelType.BROKER_REVENUE_SHARE);
        revenueShareModelNew.setKey(1);

        RevenueShareModel revenueShare = new RevenueShareModel();
        revenueShareModelNew.setKey(1);

        // when
        bean.updateRevenueShare(revenueShare, revenueShareModelNew, -1);

        // then ConcurrentModificationException thrown
    }

    @Test
    public void updateRevenueShare() throws Exception {
        // given
        RevenueShareModel revenueShareNew = createRevenueShareModel(
                BigDecimal.ZERO, RevenueShareModelType.BROKER_REVENUE_SHARE);
        revenueShareNew.setKey(2);

        RevenueShareModel revenueShare = new RevenueShareModel();
        revenueShare.setKey(2);

        // when
        RevenueShareModel updatedRevenueShare = bean.updateRevenueShare(
                revenueShare, revenueShareNew, 0);

        // then
        assertEquals(updatedRevenueShare.getRevenueShare(),
                revenueShareNew.getRevenueShare());
    }

    @Test
    public void updatePartnerRevenueShareForCatalogEntry_CreateNewRevenueShare()
            throws Exception {

        // given a catalog entry with no revenue shares
        RevenueShareModel brokerRevenueShareNew = createRevenueShareModel(
                BigDecimal.ONE, RevenueShareModelType.BROKER_REVENUE_SHARE);
        RevenueShareModel resellerRevenueShareNew = createRevenueShareModel(
                BigDecimal.TEN, RevenueShareModelType.RESELLER_REVENUE_SHARE);

        CatalogEntry ce = new CatalogEntry();

        CatalogEntry updatedCatalogEntry = bean
                .updatePartnerRevenueShareForCatalogEntry(ce,
                        brokerRevenueShareNew, resellerRevenueShareNew, 0, 0);

        // then verify that the revenue shares are created
        assertEquals(BigDecimal.ONE, updatedCatalogEntry.getBrokerPriceModel()
                .getRevenueShare());
        assertEquals(RevenueShareModelType.BROKER_REVENUE_SHARE,
                updatedCatalogEntry.getBrokerPriceModel()
                        .getRevenueShareModelType());

        assertEquals(BigDecimal.TEN, updatedCatalogEntry
                .getResellerPriceModel().getRevenueShare());
        assertEquals(RevenueShareModelType.RESELLER_REVENUE_SHARE,
                updatedCatalogEntry.getResellerPriceModel()
                        .getRevenueShareModelType());
    }

    @Test
    public void updatePartnerRevenueShareForCatalogEntry_Rollback_ValidationException()
            throws Exception {
        // given
        doThrow(new ValidationException()).when(bean)
                .updateRevenueShareForCatalogEntry(any(CatalogEntry.class),
                        any(RevenueShareModel.class),
                        any(RevenueShareModel.class),
                        any(RevenueShareModelType.class), anyInt());
        // when
        try {
            bean.updatePartnerRevenueShareForCatalogEntry(new CatalogEntry(),
                    new RevenueShareModel(), new RevenueShareModel(), 0, 0);
            fail();
        } catch (ValidationException e) {
            // then
            verify(bean.sessionCtx).setRollbackOnly();
        }
    }

    @Test
    public void updatePartnerRevenueShareForCatalogEntry_Rollback_NonUniqueBusinessKeyException()
            throws Exception {
        // given
        doThrow(new NonUniqueBusinessKeyException()).when(bean)
                .updateRevenueShareForCatalogEntry(any(CatalogEntry.class),
                        any(RevenueShareModel.class),
                        any(RevenueShareModel.class),
                        any(RevenueShareModelType.class), anyInt());
        // when
        try {
            bean.updatePartnerRevenueShareForCatalogEntry(new CatalogEntry(),
                    new RevenueShareModel(), new RevenueShareModel(), 0, 0);
            fail();
        } catch (NonUniqueBusinessKeyException e) {
            // then
            verify(bean.sessionCtx).setRollbackOnly();
        }
    }

    @Test
    public void updatePartnerRevenueShareForCatalogEntry_UpdateRevenueShareForBroker()
            throws Exception {

        // given a catalog entry with existing broker revenue shares
        RevenueShareModel brokerRevenueShareNew = createRevenueShareModel(
                BigDecimal.ONE, RevenueShareModelType.BROKER_REVENUE_SHARE);

        CatalogEntry ce = new CatalogEntry();
        ce.setBrokerPriceModel(createRevenueShareModel(BigDecimal.ZERO,
                RevenueShareModelType.BROKER_REVENUE_SHARE));
        ce.setResellerPriceModel(createRevenueShareModel(BigDecimal.ZERO,
                RevenueShareModelType.RESELLER_REVENUE_SHARE));

        // when
        CatalogEntry updatedCatalogEntry = bean
                .updatePartnerRevenueShareForCatalogEntry(ce,
                        brokerRevenueShareNew, null, 0, 0);

        // then verify that the revenue shares are updated
        assertEquals(BigDecimal.ONE, updatedCatalogEntry.getBrokerPriceModel()
                .getRevenueShare());
        assertEquals(BigDecimal.ZERO, updatedCatalogEntry
                .getResellerPriceModel().getRevenueShare());
    }

    @Test
    public void updatePartnerRevenueShareForCatalogEntry_UpdateRevenueShareForReseller()
            throws Exception {

        // given a catalog entry with existing reseller revenue share
        RevenueShareModel resellerRevenueShareNew = createRevenueShareModel(
                BigDecimal.TEN, RevenueShareModelType.RESELLER_REVENUE_SHARE);

        CatalogEntry ce = new CatalogEntry();
        ce.setBrokerPriceModel(createRevenueShareModel(BigDecimal.ZERO,
                RevenueShareModelType.BROKER_REVENUE_SHARE));
        ce.setResellerPriceModel(createRevenueShareModel(BigDecimal.ZERO,
                RevenueShareModelType.RESELLER_REVENUE_SHARE));

        // when
        CatalogEntry updatedCatalogEntry = bean
                .updatePartnerRevenueShareForCatalogEntry(ce, null,
                        resellerRevenueShareNew, 0, 0);

        // then verify that the revenue shares are updated
        assertEquals(BigDecimal.ZERO, updatedCatalogEntry.getBrokerPriceModel()
                .getRevenueShare());
        assertEquals(BigDecimal.TEN, updatedCatalogEntry
                .getResellerPriceModel().getRevenueShare());
    }

    @Test
    public void updatePartnerRevenueShareForCatalogEntry_UpdateRevenueShareForBoth()
            throws Exception {

        // given a catalog entry with existing revenue shares
        RevenueShareModel brokerRevenueShareNew = createRevenueShareModel(
                BigDecimal.ONE, RevenueShareModelType.BROKER_REVENUE_SHARE);
        RevenueShareModel resellerRevenueShareNew = createRevenueShareModel(
                BigDecimal.TEN, RevenueShareModelType.RESELLER_REVENUE_SHARE);

        CatalogEntry ce = new CatalogEntry();
        ce.setBrokerPriceModel(createRevenueShareModel(BigDecimal.ZERO,
                RevenueShareModelType.BROKER_REVENUE_SHARE));
        ce.setResellerPriceModel(createRevenueShareModel(BigDecimal.ZERO,
                RevenueShareModelType.RESELLER_REVENUE_SHARE));

        // when
        CatalogEntry updatedCatalogEntry = bean
                .updatePartnerRevenueShareForCatalogEntry(ce,
                        brokerRevenueShareNew, resellerRevenueShareNew, 0, 0);

        // then verify that the revenue shares are updated
        assertEquals(BigDecimal.ONE, updatedCatalogEntry.getBrokerPriceModel()
                .getRevenueShare());
        assertEquals(BigDecimal.TEN, updatedCatalogEntry
                .getResellerPriceModel().getRevenueShare());
    }

    @Test
    public void saveRevenueShareModelsForProduct() throws Exception {
        // given a product whose catalog entry has revenue share models
        Product prod = createProductWithRevenueShares(true);

        doReturn(prod).when(bean.dm).getReference(Product.class, prod.getKey());
        doNothing().when(bean).verifyOwningPermission(prod);

        // when
        RevenueShareModel brokerRevenueShareNew = createRevenueShareModel(
                BigDecimal.ONE, RevenueShareModelType.BROKER_REVENUE_SHARE);
        RevenueShareModel resellerRevenueShareNew = createRevenueShareModel(
                BigDecimal.TEN, RevenueShareModelType.RESELLER_REVENUE_SHARE);

        Map<RevenueShareModelType, RevenueShareModel> revenueShareModels = bean
                .saveRevenueShareModelsForProduct(prod.getKey(),
                        brokerRevenueShareNew, resellerRevenueShareNew, 0, 0);
        // then
        assertRevenueSharesExist(revenueShareModels);

    }

    @Test
    public void saveRevenueShareModelsForProduct_NoPriceModelCatalogEntry()
            throws Exception {
        // given a product whose catalog entry has revenue share models
        Product prod = createProductWithRevenueShares(false);
        doReturn(prod).when(bean.dm).getReference(Product.class, prod.getKey());
        doNothing().when(bean).verifyOwningPermission(prod);

        // when
        RevenueShareModel brokerRevenueShareNew = createRevenueShareModel(
                BigDecimal.ONE, RevenueShareModelType.BROKER_REVENUE_SHARE);
        RevenueShareModel resellerRevenueShareNew = createRevenueShareModel(
                BigDecimal.TEN, RevenueShareModelType.RESELLER_REVENUE_SHARE);

        Map<RevenueShareModelType, RevenueShareModel> revenueShareModels = bean
                .saveRevenueShareModelsForProduct(prod.getKey(),
                        brokerRevenueShareNew, resellerRevenueShareNew, 0, 0);
        // then
        assertRevenueSharesExist(revenueShareModels);

    }

    @Test(expected = ObjectNotFoundException.class)
    public void saveRevenueShareModelsForProduct_ServiceNotExist()
            throws Exception {
        // given
        when(bean.dm.getReference(eq(Product.class), anyLong())).thenThrow(
                new ObjectNotFoundException());

        // when
        bean.saveRevenueShareModelsForProduct(0, null, null, 0, 0);
    }

    @Test(expected = ServiceOperationException.class)
    public void savePartnerRevenueSharesForServices_SubscriptionSpecificCopy()
            throws Exception {
        // given
        Product product = new Product();
        product.setStatus(ServiceStatus.ACTIVE);
        product.setOwningSubscription(new Subscription());
        when(bean.dm.getReference(eq(Product.class), anyLong())).thenReturn(
                product);

        // when
        bean.saveRevenueShareModelsForProduct(0, null, null, 0, 0);
    }

    @Test(expected = ServiceOperationException.class)
    public void savePartnerRevenueSharesForServices_CustomerSpecificCopy()
            throws Exception {
        // given
        Product product = new Product();
        product.setStatus(ServiceStatus.ACTIVE);
        product.setTargetCustomer(new Organization());
        when(bean.dm.getReference(eq(Product.class), anyLong())).thenReturn(
                product);

        // when
        bean.saveRevenueShareModelsForProduct(0, null, null, 0, 0);
    }

    @Test(expected = ValidationException.class)
    public void savePartnerRevenueSharesForServices_NullBothRevenueShareParameter()
            throws Exception {
        // given
        Product product = new Product();
        product.setStatus(ServiceStatus.ACTIVE);

        when(bean.dm.getReference(eq(Product.class), anyLong())).thenReturn(
                product);

        // when
        bean.saveRevenueShareModelsForProduct(0, null, null, 0, 0);
    }

    @Test(expected = SaaSSystemException.class)
    public void savePartnerRevenueSharesForServices_CopyAndNoPartnerRevenueShare()
            throws Exception {
        // given
        Product product = new Product();
        product.setStatus(ServiceStatus.ACTIVE);
        product.setTemplate(new Product());
        List<CatalogEntry> catalogEntries = new ArrayList<CatalogEntry>();
        catalogEntries.add(new CatalogEntry());
        product.setCatalogEntries(catalogEntries);

        when(bean.dm.getReference(eq(Product.class), anyLong())).thenReturn(
                product);

        // when
        bean.saveRevenueShareModelsForProduct(0, new RevenueShareModel(),
                new RevenueShareModel(), 0, 0);
    }

    @Test(expected = NullPointerException.class)
    public void savePartnerRevenueSharesForServices_TemplateAndNoMarketplace()
            throws Exception {
        // given
        Product product = new Product();
        product.setStatus(ServiceStatus.ACTIVE);
        List<CatalogEntry> catalogEntries = new ArrayList<CatalogEntry>();
        catalogEntries.add(new CatalogEntry());
        product.setCatalogEntries(catalogEntries);

        when(bean.dm.getReference(eq(Product.class), anyLong())).thenReturn(
                product);

        // when
        bean.saveRevenueShareModelsForProduct(0, new RevenueShareModel(),
                new RevenueShareModel(), 0, 0);
    }

    private void assertRevenueSharesExist(
            Map<RevenueShareModelType, RevenueShareModel> revenueShareModels) {
        RevenueShareModel brokerPriceModel = revenueShareModels
                .get(RevenueShareModelType.BROKER_REVENUE_SHARE);
        RevenueShareModel resellerPriceModel = revenueShareModels
                .get(RevenueShareModelType.RESELLER_REVENUE_SHARE);

        assertNotNull(revenueShareModels);
        assertNotNull(brokerPriceModel);
        assertNotNull(resellerPriceModel);

        assertEquals(BigDecimal.ONE, brokerPriceModel.getRevenueShare());
        assertEquals(RevenueShareModelType.BROKER_REVENUE_SHARE,
                brokerPriceModel.getRevenueShareModelType());

        assertEquals(BigDecimal.TEN, resellerPriceModel.getRevenueShare());
        assertEquals(RevenueShareModelType.RESELLER_REVENUE_SHARE,
                resellerPriceModel.getRevenueShareModelType());
    }

    private Product createProductWithRevenueShares(boolean createRevenueShares) {
        Product prod = new Product();
        prod.setStatus(ServiceStatus.ACTIVE);
        List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
        CatalogEntry ce = new CatalogEntry();
        if (createRevenueShares) {
            ce.setBrokerPriceModel(createRevenueShareModel(BigDecimal.ZERO,
                    RevenueShareModelType.BROKER_REVENUE_SHARE));
            ce.setResellerPriceModel(createRevenueShareModel(BigDecimal.ZERO,
                    RevenueShareModelType.RESELLER_REVENUE_SHARE));
        }

        Marketplace mp = new Marketplace("mId");
        mp.setBrokerPriceModel(createRevenueShareModel(BigDecimal.ZERO,
                RevenueShareModelType.BROKER_REVENUE_SHARE));
        mp.setResellerPriceModel(createRevenueShareModel(BigDecimal.ZERO,
                RevenueShareModelType.RESELLER_REVENUE_SHARE));

        ce.setMarketplace(mp);
        ce.setProduct(prod);

        entries.add(ce);
        prod.setCatalogEntries(entries);
        prod.setTemplate(prod);
        return prod;
    }

    private RevenueShareModel createRevenueShareModel(BigDecimal value,
            RevenueShareModelType type) {
        RevenueShareModel model = new RevenueShareModel();
        model.setRevenueShare(value);
        model.setRevenueShareModelType(type);
        return model;
    }

}
