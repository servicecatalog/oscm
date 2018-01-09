/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 16.10.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;

/**
 * @author baumann
 * 
 */
public class ProductCopyTest {

    private static final String SUPPLIER_ID = "MySupplier";
    private static final String BROKER_ID = "MyBroker";
    private static final String PRODUCT_ID = "MyProduct";
    private static final String PARAMETER_ID1 = "Parameter1";
    private static final String PARAMETER_ID2 = "Parameter2";
    private static final String PARAMETER_VALUE1 = "ParValue1";
    private static final String PARAMETER_VALUE2 = "ParValue2";
    private static final BigDecimal PRICE_PER_USER1 = new BigDecimal(10);
    private static final BigDecimal PRICE_PER_USER2 = new BigDecimal(20);
    private static final BigDecimal PRICE_PER_SUBSCRIPTION1 = new BigDecimal(87);
    private static final BigDecimal PRICE_PER_SUBSCRIPTION2 = new BigDecimal(88);

    private Organization supplier;
    private TechnicalProduct techProduct;
    private Product template;

    @Before
    public void setup() {
        supplier = createSupplier();
        techProduct = createTechnicalProduct(ServiceAccessType.LOGIN);
        template = createProductTemplate(techProduct, supplier);
    }

    private Organization createSupplier() {
        Organization supplier = new Organization();
        supplier.setOrganizationId(SUPPLIER_ID);
        return supplier;
    }

    private Organization createBroker() {
        Organization broker = new Organization();
        broker.setOrganizationId(BROKER_ID);
        return broker;
    }

    private TechnicalProduct createTechnicalProduct(ServiceAccessType accessType) {
        TechnicalProduct techProduct = new TechnicalProduct();
        techProduct.setAccessType(accessType);
        return techProduct;
    }

    private Product createProductTemplate(TechnicalProduct techProduct,
            Organization supplier) {
        Product product = new Product();
        product.setType(ServiceType.TEMPLATE);
        product.setTechnicalProduct(techProduct);
        product.setProductId(PRODUCT_ID);
        product.setStatus(ServiceStatus.ACTIVE);
        product.setVendor(supplier);
        product.setTemplate(null);
        product.setAutoAssignUserEnabled(Boolean.TRUE);

        ParameterSet parameterSet = createParameterSet();
        product.setParameterSet(parameterSet);
        product.setPriceModel(createPriceModel(product));

        return product;
    }

    private ParameterSet createParameterSet() {
        ParameterSet parameterSet = new ParameterSet();

        ParameterDefinition parDef1 = new ParameterDefinition();
        parDef1.setParameterId(PARAMETER_ID1);
        parDef1.setValueType(ParameterValueType.STRING);
        parDef1.setKey(4711);
        Parameter par1 = new Parameter();
        par1.setParameterSet(parameterSet);
        par1.setParameterDefinition(parDef1);
        par1.setValue(PARAMETER_VALUE1);

        ParameterDefinition parDef2 = new ParameterDefinition();
        parDef2.setParameterId(PARAMETER_ID2);
        parDef2.setValueType(ParameterValueType.STRING);
        parDef2.setKey(4712);
        Parameter par2 = new Parameter();
        par2.setParameterSet(parameterSet);
        par2.setParameterDefinition(parDef2);
        par2.setValue(PARAMETER_VALUE2);

        parameterSet.setParameters(Arrays
                .asList(new Parameter[] { par1, par2 }));
        return parameterSet;
    }

    private PriceModel createPriceModel(Product product) {
        PriceModel pm = new PriceModel();
        pm.setProduct(product);

        List<Parameter> parameters = product.getParameterSet().getParameters();
        List<PricedParameter> selectedParameters = new ArrayList<PricedParameter>();

        PricedParameter pricedPar1 = new PricedParameter();
        pricedPar1.setParameter(parameters.get(0));
        pricedPar1.setPricePerUser(PRICE_PER_USER1);
        pricedPar1.setPricePerSubscription(PRICE_PER_SUBSCRIPTION1);
        selectedParameters.add(pricedPar1);

        PricedParameter pricedPar2 = new PricedParameter();
        pricedPar2.setParameter(parameters.get(1));
        pricedPar2.setPricePerUser(PRICE_PER_USER2);
        pricedPar2.setPricePerSubscription(PRICE_PER_SUBSCRIPTION2);
        selectedParameters.add(pricedPar2);

        pm.setSelectedParameters(selectedParameters);

        return pm;
    }

    @Test
    public void copyForResale() {
        // given
        Organization broker = createBroker();

        // when
        Product resaleCopy = template.copyForResale(broker);

        // then
        checkCopy(resaleCopy, broker, template, null, null);
        assertEquals(ServiceType.PARTNER_TEMPLATE, resaleCopy.getType());
        assertNull("Resale copy has no price model", resaleCopy.getPriceModel());
        assertNull("Resale copy has no parameters",
                resaleCopy.getParameterSet());
        assertTrue(resaleCopy.isAutoAssignUserEnabled().booleanValue());
        assertNull(resaleCopy.dataContainer.isAutoAssignUserEnabled());
    }

    @Test
    public void copy_partnerSubscription() {
        // given
        Organization broker = createBroker();
        Product resaleCopy = template.copyForResale(broker);
        Organization customer = new Organization();
        Subscription subscription = new Subscription();

        // when
        Product partnerSubscrCopy = resaleCopy.copyForSubscription(customer,
                subscription);

        // then
        checkCopy(partnerSubscrCopy, broker, resaleCopy, customer, subscription);
        checkCopiedParameters(partnerSubscrCopy.getParameterSet(),
                partnerSubscrCopy.getPriceModel(), template.getParameterSet(),
                template.getPriceModel());
    }

    @Test
    public void copy_subscription() {
        // given
        Organization customer = new Organization();
        Subscription subscription = new Subscription();

        // when
        Product subscrCopy = template.copyForSubscription(customer,
                subscription);
        subscrCopy.setType(ServiceType.SUBSCRIPTION);
        // then
        checkCopy(subscrCopy, supplier, template, customer, subscription);
        checkCopiedParameters(subscrCopy.getParameterSet(),
                subscrCopy.getPriceModel(), template.getParameterSet(),
                template.getPriceModel());
        assertTrue(subscrCopy.isAutoAssignUserEnabled().booleanValue());
        assertTrue(subscrCopy.dataContainer.isAutoAssignUserEnabled()
                .booleanValue());
    }

    @Test
    public void copy_customerSubscription() {
        // given
        Organization customer = new Organization();
        Subscription subscription = new Subscription();

        // when
        Product custCopy = template.copyForSubscription(customer, subscription);
        custCopy.setType(ServiceType.CUSTOMER_SUBSCRIPTION);
        // then
        checkCopy(custCopy, supplier, template, customer, subscription);
        checkCopiedParameters(custCopy.getParameterSet(),
                custCopy.getPriceModel(), template.getParameterSet(),
                template.getPriceModel());
        assertTrue(custCopy.isAutoAssignUserEnabled().booleanValue());
        assertTrue(custCopy.dataContainer.isAutoAssignUserEnabled()
                .booleanValue());
    }

    private void checkCopy(Product copy, Organization vendor, Product template,
            Organization customer, Subscription owningSubscription) {
        assertTrue("Wrong product ID", copy.getProductId().contains(PRODUCT_ID));
        assertEquals("Wrong vendor", vendor, copy.getVendor());
        assertEquals("Wrong technical product", techProduct,
                copy.getTechnicalProduct());
        assertEquals("Wrong template", template, copy.getTemplate());
        assertEquals("Wrong customer", customer, copy.getTargetCustomer());
        assertEquals("Wrong owning subscription", owningSubscription,
                copy.getOwningSubscription());
    }

    private void checkCopiedParameters(ParameterSet copiedParSet,
            PriceModel copiedPm, ParameterSet parSet, PriceModel pm) {
        assertNotSame("Parameter sets must be different", copiedParSet, parSet);
        assertNotSame("Price models must be different", copiedPm, pm);

        List<Parameter> copiedPars = copiedParSet.getParameters();
        List<Parameter> pars = parSet.getParameters();
        assertEquals("Both parameter sets must have the same size",
                copiedPars.size(), pars.size());

        for (int i = 0; i < copiedPars.size(); i++) {
            Parameter copiedPar = copiedPars.get(i);
            Parameter par = pars.get(i);
            assertNotSame("Parameters must be different", copiedPar, par);
            assertSame(
                    "Copied parameter has the same definition as the original",
                    copiedPar.getParameterDefinition(),
                    par.getParameterDefinition());
            assertEquals("Copied parameter has the same value as the original",
                    copiedPar.getValue(), par.getValue());
        }

        List<PricedParameter> copiedSelectedPars = copiedPm
                .getSelectedParameters();
        List<PricedParameter> selectedPars = pm.getSelectedParameters();
        assertEquals(
                "Both price model selected parameter lists must have the same size",
                copiedSelectedPars.size(), selectedPars.size());

        for (int i = 0; i < copiedSelectedPars.size(); i++) {
            PricedParameter copiedSelectedPar = copiedSelectedPars.get(i);
            PricedParameter selectedPar = selectedPars.get(i);
            assertNotSame("Priced parameters must be different",
                    copiedSelectedPar, selectedPar);
            assertNotSame("Priced parameters refer to different parameters",
                    copiedSelectedPar.getParameter(),
                    selectedPar.getParameter());
            assertTrue("Priced parameter must refer to a copied parameter",
                    copiedPars.contains(copiedSelectedPar.getParameter()));
            assertSame(
                    "Priced parameters must have the same parameter definition",
                    copiedSelectedPar.getParameter().getParameterDefinition(),
                    selectedPar.getParameter().getParameterDefinition());
            assertEquals("Priced parameters must have the same price per user",
                    copiedSelectedPar.getPricePerUser(),
                    selectedPar.getPricePerUser());
            assertEquals(
                    "Priced parameters must have the same price per subscription",
                    copiedSelectedPar.getPricePerSubscription(),
                    selectedPar.getPricePerSubscription());
        }
    }
}
