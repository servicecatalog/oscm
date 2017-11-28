/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 18, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.serviceprovisioningservice.auditlog.ServiceAuditLogCollector;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOTechnicalService;

/**
 * @author barzu
 */
public class ServiceProvisioningServiceBeanOpRevShareTest {

    private static final BigDecimal OPERATOR_REVENUE_SHARE = BigDecimal
            .valueOf(15);
    private static final BigDecimal SERVICE_SPECIFIC_OPERATOR_REVENUE_SHARE = BigDecimal
            .valueOf(28);

    private ServiceProvisioningServiceBean spsBean;
    private DataService ds;
    private ServiceAuditLogCollector audit;
    private TechnicalProduct techProduct;
    private PlatformUser currentUser;

    @Before
    public void setup() throws Exception {
        spsBean = spy(new ServiceProvisioningServiceBean());
        ds = mock(DataService.class);
        spsBean.dm = ds;
        audit = mock(ServiceAuditLogCollector.class);
        spsBean.serviceAudit = audit;
        spsBean.localizer = mock(LocalizerServiceLocal.class);
        spsBean.irm = mock(ImageResourceServiceLocal.class);

        techProduct = new TechnicalProduct();
        techProduct.setKey(4711);

        currentUser = new PlatformUser();
        Organization currentOrg = new Organization();
        currentOrg.setKey(4712);
        currentUser.setOrganization(currentOrg);
        doReturn(currentUser).when(ds).getCurrentUser();
    }

    @Test
    public void createService() throws Exception {
        // given
        RevenueShareModel operatorPriceModel = createOperatorPriceModel(OPERATOR_REVENUE_SHARE);
        currentUser.getOrganization().setOperatorPriceModel(operatorPriceModel);
        VOTechnicalService techService = createVOTechnicalService(techProduct);
        VOService service = createVOService();
        mockCreateService();

        // when
        spsBean.createService(techService, service, null);

        // then
        verify(spsBean).copyOperatorPriceModel(any(CatalogEntry.class),
                eq(operatorPriceModel));
    }

    @Test
    public void copyService() throws Exception {
        // given
        RevenueShareModel operatorPriceModel = createOperatorPriceModel(OPERATOR_REVENUE_SHARE);
        currentUser.getOrganization().setOperatorPriceModel(operatorPriceModel);
        Product product = createProduct(currentUser.getOrganization(),
                SERVICE_SPECIFIC_OPERATOR_REVENUE_SHARE);
        VOService service = createVOService(product);
        mockCopyService(product);

        // when
        spsBean.copyService(service, "Service Copy");

        // then
        verify(spsBean).copyOperatorPriceModel(any(CatalogEntry.class),
                eq(product.getCatalogEntries().get(0).getOperatorPriceModel()));
    }

    @Test
    public void copyOperatorPriceModel() throws Exception {
        // given
        RevenueShareModel operatorPriceModel = createOperatorPriceModel(OPERATOR_REVENUE_SHARE);
        CatalogEntry catEntry = new CatalogEntry();

        // when
        spsBean.copyOperatorPriceModel(catEntry, operatorPriceModel);

        // then
        RevenueShareModel operatorPriceModelCopy = catEntry
                .getOperatorPriceModel();
        assertNotNull("Operator price model missing", operatorPriceModelCopy);
        assertNotSame("Operator price model at catalog entry must be a copy",
                operatorPriceModel, operatorPriceModelCopy);
        assertEquals("Wrong price model type",
                RevenueShareModelType.OPERATOR_REVENUE_SHARE,
                operatorPriceModelCopy.getRevenueShareModelType());
        assertEquals("Wrong operator revenue share", OPERATOR_REVENUE_SHARE,
                operatorPriceModelCopy.getRevenueShare());
        verify(ds).persist(operatorPriceModelCopy);
    }

    @Test
    public void copyOperatorPriceModel_NonUniqueBusinessKey() throws Exception {
        // given
        doThrow(new NonUniqueBusinessKeyException()).when(ds).persist(
                any(RevenueShareModel.class));

        try {
            // when
            spsBean.copyOperatorPriceModel(new CatalogEntry(),
                    createOperatorPriceModel(OPERATOR_REVENUE_SHARE));
            fail("SaaSSystemException expected");
        } catch (SaaSSystemException e) {
            // then
            assertTrue(e.getMessage().contains(
                    "Caught unexpected NonUniqueBusinessKeyException"));
        }
    }

    private void mockCreateService() throws Exception {
        doReturn(techProduct).when(ds).getReference(TechnicalProduct.class,
                techProduct.getKey());
        Query mockedQuery = mock(Query.class);
        doReturn(mockedQuery).when(ds).createNamedQuery(
                "MarketingPermission.findForSupplierIds");
        doReturn(mockedQuery).when(ds).createNamedQuery(
                "SupportedLanguage.findAllActive");

        doNothing().when(spsBean).verifyTechnicalServiceIsUpToDate(
                any(VOTechnicalService.class), any(TechnicalProduct.class),
                anyBoolean());
        doNothing().when(audit).defineService(eq(ds), any(Product.class),
                anyString(), anyString(), anyString(), anyString());
        doReturn(new VOServiceDetails()).when(spsBean).getServiceDetails(
                any(Product.class), any(LocalizerFacade.class));
    }

    private void mockCopyService(Product product) throws Exception {
        doReturn(product).when(ds)
                .getReference(Product.class, product.getKey());
        doReturn(new VOServiceDetails()).when(spsBean).getServiceDetails(
                any(Product.class), any(LocalizerFacade.class));
    }

    private VOService createVOService() {
        VOService service = new VOService();
        service.setServiceId("testproduct");
        return service;
    }

    private VOService createVOService(Product product) {
        VOService service = new VOService();
        service.setKey(product.getKey());
        service.setVersion(product.getVersion());
        service.setServiceId(product.getProductId());
        return service;
    }

    private Product createProduct(Organization vendor,
            BigDecimal operatorRevenueShare) {
        Product product = new Product();
        product.setKey(4713);
        product.setProductId("testproduct");
        product.setStatus(ServiceStatus.ACTIVE);
        product.setVendor(vendor);

        CatalogEntry catEntry = new CatalogEntry();
        catEntry.setOperatorPriceModel(createOperatorPriceModel(operatorRevenueShare));
        product.getCatalogEntries().add(catEntry);

        return product;
    }

    private VOTechnicalService createVOTechnicalService(TechnicalProduct product) {
        VOTechnicalService techService = new VOTechnicalService();
        techService.setKey(product.getKey());
        techService.setVersion(product.getVersion());
        return techService;
    }

    private RevenueShareModel createOperatorPriceModel(
            BigDecimal operatorRevenueShare) {
        RevenueShareModel operatorPriceModel = new RevenueShareModel();
        operatorPriceModel
                .setRevenueShareModelType(RevenueShareModelType.OPERATOR_REVENUE_SHARE);
        operatorPriceModel.setRevenueShare(operatorRevenueShare);
        return operatorPriceModel;
    }

}
