/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-05-22                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.SessionContext;

import org.junit.Before;
import org.junit.Test;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingContact;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.ModifiedEntity;
import org.oscm.domobjects.ModifiedUda;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.domobjects.enums.ModifiedEntityType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.subscriptionservice.dao.ModifiedEntityDao;
import org.oscm.types.enumtypes.UdaTargetType;

/**
 * @author Gao Wenxin
 */
public class ModifyAndUpgradeSubscriptionBeanTest
        extends SubscriptionServiceMockBase {

    private ModifyAndUpgradeSubscriptionBean bean;
    private ModifiedEntityDao modifiedEntityDao;
    private PlatformUser user;

    private Subscription subscription;
    private PaymentInfo paymentInfo;
    private BillingContact billingContact;
    private static final String LOCALE = "en";
    private DataService ds;

    @Before
    public void setup() throws Exception {
        bean = initMocksAndSpys();
        subscription = new Subscription();
        subscription.setKey(1l);
        subscription.setSubscriptionId("oldId");
        paymentInfo = new PaymentInfo();
        billingContact = new BillingContact();
        user = givenCurrentUser();
        ds = mock(DataService.class);
        bean.dataManager = ds;
        when(bean.dataManager.getCurrentUser()).thenReturn(user);
        modifiedEntityDao = mock(ModifiedEntityDao.class);
        doReturn(modifiedEntityDao).when(bean).getModifiedEntityDao();
        doReturn(givenModifiedEntities()).when(modifiedEntityDao)
                .retrieveModifiedEntities(any(Subscription.class));
        doReturn(givenUdas()).when(bean).getExistingUdas(eq(subscription));
        doReturn(new ModifiedUda()).when(bean.dataManager)
                .getReferenceByBusinessKey(any(ModifiedUda.class));
    }

    @Test
    public void updateSubscriptionAttributesForAsyncUpgrade_WithModifiedEntities()
            throws Exception {
        // given
        doReturn(paymentInfo).when(bean.dataManager)
                .getReference(eq(PaymentInfo.class), anyLong());
        doReturn(billingContact).when(bean.dataManager)
                .getReference(eq(BillingContact.class), anyLong());
        // when
        Subscription result = bean
                .updateSubscriptionAttributesForAsyncUpgrade(subscription);
        // then
        assertEquals(paymentInfo, result.getPaymentInfo());
        assertEquals(billingContact, result.getBillingContact());
        verify(bean.dataManager, times(6)).remove(any(DomainObject.class));
    }

    @Test
    public void updateSubscriptionAttributesForAsyncUpgrade_WithoutModifiedEntities()
            throws Exception {
        // given
        doReturn(new ArrayList<ModifiedEntity>()).when(modifiedEntityDao)
                .retrieveModifiedEntities(any(Subscription.class));
        doReturn(new ArrayList<Uda>()).when(bean)
                .getExistingUdas(eq(subscription));
        // when
        Subscription result = bean
                .updateSubscriptionAttributesForAsyncUpgrade(subscription);
        // then
        assertEquals(null, result.getPaymentInfo());
        assertEquals(null, result.getBillingContact());
        verify(bean.dataManager, never()).remove(any(DomainObject.class));
    }

    @Test
    public void updateSubscriptionAttributesForAsyncUpgrade_BillingContactNotExist()
            throws Exception {
        // given
        doReturn(paymentInfo).when(bean.dataManager)
                .getReference(eq(PaymentInfo.class), anyLong());
        // when
        Subscription result = bean
                .updateSubscriptionAttributesForAsyncUpgrade(subscription);
        // then
        assertEquals(null, result.getBillingContact());
        assertEquals(paymentInfo, result.getPaymentInfo());
        verify(bean.dataManager, times(6)).remove(any(DomainObject.class));
    }

    @Test
    public void updateSubscriptionAttributesForAsyncUpgrade_PaymentInfoNotExist()
            throws Exception {
        // given
        doReturn(billingContact).when(bean.dataManager)
                .getReference(eq(BillingContact.class), anyLong());
        // when
        Subscription result = bean
                .updateSubscriptionAttributesForAsyncUpgrade(subscription);
        // then
        assertEquals(null, result.getPaymentInfo());
        assertEquals(billingContact, result.getBillingContact());
        verify(bean.dataManager, times(6)).remove(any(DomainObject.class));
    }

    @Test
    public void updateSubscriptionAttributesForAsyncUpdate_WithModifiedEntities()
            throws Exception {
        // when
        Subscription result = bean
                .updateSubscriptionAttributesForAsyncUpdate(subscription);
        // then
        assertEquals("SubscriptionId", result.getSubscriptionId());
        assertEquals("123456", result.getPurchaseOrderNumber());
        assertEquals(null, result.getOwner());
        verify(bean.dataManager, times(6)).remove(any(DomainObject.class));
    }

    @Test
    public void updateSubscriptionAttributesForAsyncUpdate_WithoutModifiedEntities()
            throws Exception {
        // given
        doReturn(new ArrayList<ModifiedEntity>()).when(modifiedEntityDao)
                .retrieveModifiedEntities(any(Subscription.class));
        doReturn(new ArrayList<Uda>()).when(bean)
                .getExistingUdas(eq(subscription));
        // when
        Subscription result = bean
                .updateSubscriptionAttributesForAsyncUpdate(subscription);
        // then
        assertEquals("oldId", result.getSubscriptionId());
        assertEquals(null, result.getPurchaseOrderNumber());
        assertEquals(null, result.getOwner());
        verify(bean.dataManager, never()).remove(any(DomainObject.class));
    }

    private ModifyAndUpgradeSubscriptionBean initMocksAndSpys()
            throws Exception {
        bean = new ModifyAndUpgradeSubscriptionBean();
        spyInjected(bean, givenSpyClasses());
        mockEJBs(bean);
        mockResources(bean);
        return spy(bean);
    }

    private List<Class<?>> givenSpyClasses() {
        List<Class<?>> spys = new ArrayList<>();
        spys.add(DataService.class);
        spys.add(CommunicationServiceLocal.class);
        spys.add(SessionContext.class);
        spys.add(SubscriptionUtilBean.class);
        return spys;
    }

    private PlatformUser givenCurrentUser() {
        PlatformUser user = new PlatformUser();
        Organization org = new Organization();
        org.setKey(2L);
        List<TechnicalProduct> technicalProducts = new ArrayList<>();
        technicalProducts.add(givenTechProduct());
        org.setTechnicalProducts(technicalProducts);
        user.setOrganization(org);
        user.setLocale(LOCALE);
        return user;
    }

    private TechnicalProduct givenTechProduct() {
        TechnicalProduct techProduct = new TechnicalProduct();
        techProduct.setKey(1L);
        techProduct.setAccessType(ServiceAccessType.DIRECT);
        return techProduct;
    }

    private List<ModifiedEntity> givenModifiedEntities() {
        ModifiedEntity billingContactEntity = new ModifiedEntity();
        billingContactEntity.setTargetObjectType(
                ModifiedEntityType.SUBSCRIPTION_BILLINGCONTACT);
        billingContactEntity.setValue("1000");
        ModifiedEntity paymentInfoEntity = new ModifiedEntity();
        paymentInfoEntity.setTargetObjectType(
                ModifiedEntityType.SUBSCRIPTION_PAYMENTINFO);
        paymentInfoEntity.setValue("1001");
        ModifiedEntity subscriptionIdEntity = new ModifiedEntity();
        subscriptionIdEntity.setTargetObjectType(
                ModifiedEntityType.SUBSCRIPTION_SUBSCRIPTIONID);
        subscriptionIdEntity.setValue("SubscriptionId");
        ModifiedEntity purchaseNumberEntity = new ModifiedEntity();
        purchaseNumberEntity.setTargetObjectType(
                ModifiedEntityType.SUBSCRIPTION_PURCHASEORDERNUMBER);
        purchaseNumberEntity.setValue("123456");
        ModifiedEntity ownerIdEntity = new ModifiedEntity();
        ownerIdEntity
                .setTargetObjectType(ModifiedEntityType.SUBSCRIPTION_OWNERID);
        ownerIdEntity.setValue(null);
        List<ModifiedEntity> modifiedEntities = new ArrayList<>();
        modifiedEntities.add(billingContactEntity);
        modifiedEntities.add(paymentInfoEntity);
        modifiedEntities.add(subscriptionIdEntity);
        modifiedEntities.add(purchaseNumberEntity);
        modifiedEntities.add(ownerIdEntity);
        return modifiedEntities;
    }

    private List<Uda> givenUdas() {
        List<Uda> udas = new ArrayList<>();
        UdaDefinition udaDef = new UdaDefinition();
        udaDef.setTargetType(UdaTargetType.CUSTOMER_SUBSCRIPTION);
        Uda uda = new Uda();
        uda.setKey(10000);
        uda.setUdaDefinition(udaDef);
        udas.add(uda);
        return udas;
    }

}
