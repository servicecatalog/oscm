/********************************************************************************
 *                                                                             
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: schmid
 *                                                                              
 *  Creation Date: 20.01.2009                                                      
 *                                                                             
 *  Completion Time:                       
 *                                                                           
 ********************************************************************************/
package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.oscm.test.Numbers.TIMESTAMP;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.persistence.Query;

import org.junit.Test;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.ReflectiveCompare;
import org.oscm.test.data.CatalogEntries;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.TechnicalProducts;

/**
 * Tests of the product-related domain objects (incl. auditing functionality)
 * 
 * @author schmid
 * 
 */
public class ProductIT extends DomainObjectTestBase {

    private ArrayList<DomainObject<?>> domObjects = new ArrayList<DomainObject<?>>();
    private ArrayList<DomainObject<?>> oldObjects = new ArrayList<DomainObject<?>>();

    private String organizationId;
    private Organization organization;
    private Organization supplier;

    private static final String WRONG_MP_ID = "anyMarketplace";
    private static final String MP_ID = "MP_ID";

    @Override
    protected void dataSetup() throws Exception {
        SupportedCurrency sc = new SupportedCurrency();
        sc.setCurrency(Currency.getInstance("EUR"));
        mgr.persist(sc);
    }

    /**
     * <b>Testcase:</b> Add new Product objects <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>All objects can be retrieved from DB and are identical to provided
     * Product objects</li>
     * <li>Cascaded objects (i.e. PriceModel) is also stored</li>
     * <li>A history object is created for each product stored</li>
     * <li>History objects are created for CascadeAudit-annotated associated
     * objects</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testAdd() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestAdd();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    doTestAddCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestAdd() throws NonUniqueBusinessKeyException,
            ObjectNotFoundException {
        // Enter new Products
        domObjects.clear();
        if (organizationId == null) {
            supplier = Organizations.createOrganization(mgr);
            organization = Organizations.createOrganization(mgr);
            organizationId = organization.getOrganizationId();
        } else {
            organization = Organizations.findOrganization(mgr, organizationId);
        }
        TechnicalProduct tProd = TechnicalProducts.createTechnicalProduct(mgr,
                organization, "TP_ID", false, ServiceAccessType.LOGIN);

        // create a role definition for the technical product
        RoleDefinition rd = new RoleDefinition();
        rd.setRoleId("TP-ROLE");
        rd.setTechnicalProduct(tProd);
        mgr.persist(rd);
        tProd.setRoleDefinitions(Collections.singletonList(rd));

        ParameterDefinition paramDef = new ParameterDefinition();
        paramDef.setParameterId("param");
        paramDef.setParameterType(ParameterType.SERVICE_PARAMETER);
        paramDef.setValueType(ParameterValueType.STRING);
        paramDef.setTechnicalProduct(tProd);
        mgr.persist(paramDef);

        // add one event to that price model
        Event evt = new Event();
        evt.setEventIdentifier("event");
        evt.setEventType(EventType.SERVICE_EVENT);
        evt.setTechnicalProduct(tProd);
        mgr.persist(evt);

        Product prod;
        for (int i = 1; i < 10; i++) {
            prod = new Product();
            prod.setVendor(supplier);
            prod.setProductId("Product" + i);
            prod.setTechnicalProduct(tProd);
            prod.setProvisioningDate(TIMESTAMP);
            prod.setStatus(ServiceStatus.ACTIVE);
            prod.setType(ServiceType.TEMPLATE);
            ParameterSet ps = new ParameterSet();
            prod.setParameterSet(ps);
            PriceModel pi = new PriceModel();
            if (i > 5) {
                pi.setType(PriceModelType.PRO_RATA);
                pi.setPeriod(PricingPeriod.MONTH);
                pi.setPricePerPeriod(new BigDecimal(12091L));

                PricedEvent pricedEvt = new PricedEvent();
                pricedEvt.setEvent(evt);
                pricedEvt.setEventPrice(new BigDecimal(123));
                pricedEvt.setPriceModel(pi);

                List<PricedEvent> consideredEvents = new ArrayList<PricedEvent>();
                consideredEvents.add(pricedEvt);

                pi.setConsideredEvents(consideredEvents);

                // add priced product role
                PricedProductRole pmPricedRole = new PricedProductRole();
                pmPricedRole.setPricePerUser(new BigDecimal(111L));
                pmPricedRole.setRoleDefinition(rd);
                pmPricedRole.setPriceModel(pi);
                pi.setRoleSpecificUserPrices(Collections
                        .singletonList(pmPricedRole));

                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance("EUR"));
                sc = (SupportedCurrency) mgr.find(sc);
                pi.setCurrency(sc);
            }
            prod.setPriceModel(pi);
            mgr.persist(prod);

            Parameter param = new Parameter();
            param.setParameterSet(ps);
            param.setValue("someValue");
            param.setParameterDefinition(paramDef);
            param.setParameterSet(ps);
            mgr.persist(param);

            domObjects.add((Product) ReflectiveClone.clone(prod));
        }
        mgr.flush();
    }

    private void doTestAddCheck() {
        Product saved = null;
        Product qry = new Product();
        for (DomainObject<?> org : domObjects) {
            // Load product and check values
            Product orgProduct = (Product) org;
            qry.setVendor(supplier);
            qry.setProductId(orgProduct.getProductId());
            saved = (Product) mgr.find(qry);
            assertNotNull("Cannot find '" + orgProduct.getProductId()
                    + "' in DB", saved);
            assertTrue(ReflectiveCompare.showDiffs(saved, orgProduct),
                    ReflectiveCompare.compare(saved, orgProduct));
            // Load history objects and check them
            List<DomainHistoryObject<?>> histObjs = mgr.findHistory(saved);
            assertNotNull("History entry 'null' for product "
                    + orgProduct.getProductId());
            assertEquals("Only one history entry expected for product "
                    + orgProduct.getProductId(), 1, histObjs.size());
            DomainHistoryObject<?> hist = histObjs.get(0);
            assertEquals(ModificationType.ADD, hist.getModtype());
            assertEquals("modUser", "guest", hist.getModuser());
            assertTrue(ReflectiveCompare.showDiffs(orgProduct, hist),
                    ReflectiveCompare.compare(orgProduct, hist));
            assertEquals("OBJID in history different", orgProduct.getKey(),
                    hist.getObjKey());
            // Check cascaded objects (i.e. PriceModel)
            PriceModel savedPI = saved.getPriceModel();
            PriceModel orgPI = orgProduct.getPriceModel();
            assertTrue(ReflectiveCompare.showDiffs(savedPI, orgPI),
                    ReflectiveCompare.compare(savedPI, orgPI));
            // Load cascades History Object and check
            histObjs = mgr.findHistory(savedPI);
            assertNotNull("History entry 'null' for PriceModel of product "
                    + orgProduct.getProductId());
            assertEquals(
                    "Only one history entry expected for PriceModel of product "
                            + orgProduct.getProductId(), 1, histObjs.size());
            hist = histObjs.get(0);
            assertEquals(ModificationType.ADD, hist.getModtype());
            assertEquals("modUser", "guest", hist.getModuser());
            assertTrue(ReflectiveCompare.showDiffs(orgPI, hist),
                    ReflectiveCompare.compare(orgPI, hist));
            assertEquals("OBJID in history different", orgPI.getKey(),
                    hist.getObjKey());
        }
    }

    /**
     * <b>Testcase:</b> Modify an existing product object <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>Modification is saved to the DB</li>
     * <li>History object created for the product</li>
     * <li>priceModel unchanged</li>
     * <li>No new history object for PriceModel</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testModifyProduct() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModifyProductPrepare("TP_ID_0");
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    doTestModifyProduct();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    doTestModifyProductCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestModifyProductPrepare(String technicalProductID)
            throws Exception {
        domObjects.clear();
        Organization organization;
        if (organizationId == null) {
            organization = Organizations.createOrganization(mgr);
            organizationId = organization.getOrganizationId();
        } else {
            organization = Organizations.findOrganization(mgr, organizationId);
        }
        // insert new product
        TechnicalProduct tProd = TechnicalProducts.createTechnicalProduct(mgr,
                organization, technicalProductID, false,
                ServiceAccessType.LOGIN);
        Product prod = new Product();
        prod.setVendor(organization);
        prod.setProductId("TestModifyProduct");
        prod.setTechnicalProduct(tProd);
        prod.setProvisioningDate(TIMESTAMP);
        prod.setType(ServiceType.TEMPLATE);
        PriceModel pi = new PriceModel();
        prod.setPriceModel(pi);
        prod.setStatus(ServiceStatus.ACTIVE);
        ParameterSet emptyPS = new ParameterSet();
        prod.setParameterSet(emptyPS);
        mgr.persist(prod);
    }

    private void doTestModifyProduct() {
        // Change only product data
        Organization organization = Organizations.findOrganization(mgr,
                organizationId);
        Product prod = new Product();
        prod.setVendor(organization);
        prod.setProductId("TestModifyProduct");
        Product prod2 = (Product) mgr.find(prod);
        prod2.setProvisioningDate(System.currentTimeMillis());
        domObjects.clear();
        domObjects.add(prod2);
        load(prod2.getPriceModel());
    }

    private void doTestModifyProductCheck() {
        // Load modified
        Product orgProduct = (Product) domObjects.get(0);
        Organization organization = Organizations.findOrganization(mgr,
                organizationId);
        Product cid = new Product();
        cid.setVendor(organization);
        cid.setProductId("TestModifyProduct");
        Product saved = (Product) mgr.find(cid);
        // Check product data
        assertNotNull("Cannot find '" + orgProduct.getProductId() + "' in DB",
                saved);
        assertTrue(ReflectiveCompare.showDiffs(saved, orgProduct),
                ReflectiveCompare.compare(saved, orgProduct));
        // Load history objects and check them
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(saved);
        assertNotNull(
                "History entry 'null' for product " + orgProduct.getProductId(),
                histObjs);
        assertEquals("Exactly 2 history entries expected for product "
                + orgProduct.getProductId(), 2, histObjs.size());
        // load modified history object (should be second)
        DomainHistoryObject<?> hist = histObjs.get(1);
        assertEquals(ModificationType.MODIFY, hist.getModtype());
        assertEquals("modUser", "guest", hist.getModuser());
        assertTrue(ReflectiveCompare.showDiffs(orgProduct, hist),
                ReflectiveCompare.compare(orgProduct, hist));
        assertEquals("OBJID in history different", orgProduct.getKey(),
                hist.getObjKey());
        // Check cascaded objects
        PriceModel orgPI = orgProduct.getPriceModel();
        PriceModel savedPI = saved.getPriceModel();
        // should be unchanged ...
        assertTrue(ReflectiveCompare.showDiffs(orgPI, savedPI),
                ReflectiveCompare.compare(orgPI, savedPI));
        // ... and therefore not contain a new history entry
        histObjs = mgr.findHistory(orgPI);
        assertNotNull("History entry 'null' for PriceModel of product "
                + orgProduct.getProductId(), histObjs);
        assertEquals(
                "Exactly 1 history entries expected for PriceModel of product "
                        + orgProduct.getProductId(), 1, histObjs.size());
    }

    /**
     * <b>Testcase:</b> Modify the priceModel of an existing product object <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>Modification is saved to the DB</li>
     * <li>History object created for the PriceModel</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testModifyPriceModel() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModifyPriceModelPrepare();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    doTestModifyPriceModel();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    doTestModifyPriceModelCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestModifyPriceModelPrepare() throws Exception {
        domObjects.clear();
        Organization organization;
        if (organizationId == null) {
            organization = Organizations.createOrganization(mgr);
            organizationId = organization.getOrganizationId();
        } else {
            organization = Organizations.findOrganization(mgr, organizationId);
        }
        TechnicalProduct tProd = TechnicalProducts.createTechnicalProduct(mgr,
                organization, "TP_ID_2", false, ServiceAccessType.LOGIN);
        // insert new product
        Product prod = new Product();
        prod.setVendor(organization);
        prod.setType(ServiceType.TEMPLATE);
        prod.setProductId("TestModifyPriceModel");
        prod.setTechnicalProduct(tProd);
        prod.setProvisioningDate(TIMESTAMP);
        PriceModel pi = new PriceModel();
        prod.setPriceModel(pi);
        prod.setStatus(ServiceStatus.ACTIVE);
        ParameterSet emptyPS = new ParameterSet();
        prod.setParameterSet(emptyPS);
        mgr.persist(prod);
    }

    private void doTestModifyPriceModel() {
        // Change Payment Info
        Product prod = new Product();
        prod.setVendor(Organizations.findOrganization(mgr, organizationId));
        prod.setProductId("TestModifyPriceModel");
        Product prod2 = (Product) mgr.find(prod);
        PriceModel newPI = prod2.getPriceModel();
        newPI.setOneTimeFee(new BigDecimal(1000));
        domObjects.clear();
        domObjects.add((Product) ReflectiveClone.clone(prod2));
    }

    private void doTestModifyPriceModelCheck() {
        // Load modified
        Product orgProduct = (Product) domObjects.get(0);
        Product cid = new Product();
        cid.setVendor(Organizations.findOrganization(mgr, organizationId));
        cid.setProductId("TestModifyPriceModel");
        Product saved = (Product) mgr.find(cid);
        // Check product data
        assertNotNull("Cannot find '" + orgProduct.getProductId() + "' in DB",
                saved);
        assertTrue(ReflectiveCompare.showDiffs(saved, orgProduct),
                ReflectiveCompare.compare(saved, orgProduct));
        // Load history objects and check them
        // Check cascaded objects
        PriceModel orgPI = orgProduct.getPriceModel();
        PriceModel savedPI = saved.getPriceModel();
        assertTrue(ReflectiveCompare.showDiffs(orgPI, savedPI),
                ReflectiveCompare.compare(orgPI, savedPI));
        // as we have a cascading style all for this
        // association a new history entry should be created for
        // the PriceModel (but with unchanged values !)
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(orgPI);
        assertNotNull("History entry 'null' for PriceModel of product "
                + orgProduct.getProductId(), histObjs);
        assertEquals(
                "Exactly 2 history entries expected for PriceModel of product "
                        + orgProduct.getProductId(), 2, histObjs.size());
        // load modified history object (should be second)
        DomainHistoryObject<?> hist = histObjs.get(1);
        assertEquals(ModificationType.MODIFY, hist.getModtype());
        assertEquals("modUser", "guest", hist.getModuser());
        assertTrue(ReflectiveCompare.showDiffs(orgPI, hist),
                ReflectiveCompare.compare(orgPI, hist));
        assertEquals("OBJID in history different", orgPI.getKey(),
                hist.getObjKey());
    }

    /**
     * <b>Testcase:</b> Delete priceModel of an existing product object and add
     * a new PriceModel<br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>Old PriceModel marked as deleted in the DB</li>
     * <li>History object created for the deleted object</li>
     * <li>New PriceModel stored in the DB</li>
     * <li>History object created for the new PriceModel</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testNewPriceModel() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestNewPriceModelPrepare();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestNewPriceModel();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    doTestNewPriceModelCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestNewPriceModelPrepare() throws Exception {
        domObjects.clear();
        Organization organization;
        if (organizationId == null) {
            organization = Organizations.createOrganization(mgr);
            organizationId = organization.getOrganizationId();
        } else {
            organization = Organizations.findOrganization(mgr, organizationId);
        }
        TechnicalProduct tProd = TechnicalProducts.createTechnicalProduct(mgr,
                organization, "TP_ID_3", false, ServiceAccessType.LOGIN);
        // insert new product
        Product prod = new Product();
        prod.setVendor(organization);
        prod.setProductId("TestNewPriceModel");
        prod.setTechnicalProduct(tProd);
        prod.setProvisioningDate(TIMESTAMP);
        prod.setStatus(ServiceStatus.ACTIVE);
        prod.setType(ServiceType.TEMPLATE);
        PriceModel pi = new PriceModel();
        prod.setPriceModel(pi);
        ParameterSet emptyPS = new ParameterSet();
        prod.setParameterSet(emptyPS);
        mgr.persist(prod);
    }

    private void doTestNewPriceModel() throws NonUniqueBusinessKeyException {
        // Change Payment Info
        Product prod = new Product();
        prod.setVendor(Organizations.findOrganization(mgr, organizationId));
        prod.setProductId("TestNewPriceModel");
        Product prod2 = (Product) mgr.find(prod);
        PriceModel oldPI = prod2.getPriceModel();
        // delete old PriceModel
        oldObjects.clear();
        oldObjects.add(prod2.getPriceModel());
        // create new one
        PriceModel newPI = new PriceModel();
        newPI.setType(PriceModelType.PRO_RATA);
        newPI.setPeriod(PricingPeriod.MONTH);
        newPI.setPricePerPeriod(new BigDecimal(12345678999L));
        SupportedCurrency sc = new SupportedCurrency();
        sc.setCurrency(Currency.getInstance("EUR"));
        sc = (SupportedCurrency) mgr.find(sc);
        newPI.setCurrency(sc);
        prod2.setPriceModel(newPI);
        domObjects.clear();
        mgr.persist(newPI);
        mgr.remove(oldPI);
        domObjects.add((Product) ReflectiveClone.clone(prod2));
    }

    private void doTestNewPriceModelCheck() {
        // Load modified
        Product orgProduct = (Product) domObjects.get(0);
        Product cid = new Product();
        cid.setVendor(Organizations.findOrganization(mgr, organizationId));
        cid.setProductId("TestNewPriceModel");
        Product saved = (Product) mgr.find(cid);
        // Check product data
        assertNotNull("Cannot find '" + orgProduct.getProductId() + "' in DB",
                saved);
        assertTrue(ReflectiveCompare.showDiffs(saved, orgProduct),
                ReflectiveCompare.compare(saved, orgProduct));
        // Load history objects and check them
        // Product itself is unchanged !
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(saved);
        assertNotNull(
                "History entry 'null' for product " + orgProduct.getProductId(),
                histObjs);
        assertEquals("Exactly 2 history entries expected for product "
                + orgProduct.getProductId(), 2, histObjs.size());
        DomainHistoryObject<?> hist = histObjs.get(0);
        assertEquals(ModificationType.ADD, hist.getModtype());
        assertEquals("modUser", "guest", hist.getModuser());
        assertTrue(ReflectiveCompare.showDiffs(orgProduct, hist),
                ReflectiveCompare.compare(orgProduct, hist));
        assertEquals("OBJID in history different", orgProduct.getKey(),
                hist.getObjKey());
        // Check cascaded objects
        PriceModel orgPI = orgProduct.getPriceModel();
        PriceModel savedPI = saved.getPriceModel();
        assertTrue(ReflectiveCompare.showDiffs(orgPI, savedPI),
                ReflectiveCompare.compare(orgPI, savedPI));
        // a new history entry should be created for
        // the PriceModel (but with unchanged values !)
        histObjs = mgr.findHistory(savedPI);
        assertNotNull("History entry 'null' for new PriceModel of product "
                + orgProduct.getProductId(), histObjs);
        assertEquals(
                "Exactly 1 history entries expected for new PriceModel of product "
                        + orgProduct.getProductId(), 1, histObjs.size());
        // load modified history object
        hist = histObjs.get(0);
        assertEquals(ModificationType.ADD, hist.getModtype());
        assertEquals("modUser", "guest", hist.getModuser());
        assertTrue(ReflectiveCompare.showDiffs(orgPI, hist),
                ReflectiveCompare.compare(orgPI, hist));
        assertEquals("OBJID in history different", orgPI.getKey(),
                hist.getObjKey());
        // Last but not least check if old PriceModel has been deleted
        // Load the already deleted object via PrimaryKey-find
        PriceModel old = (PriceModel) oldObjects.get(0);
        PriceModel oldPI = mgr.find(PriceModel.class, old.getKey());
        assertNull("Could still load old Payment Info", oldPI);
        // load history
        histObjs = mgr.findHistory(old);
        assertNotNull("History entry 'null' for old PriceModel of product "
                + orgProduct.getProductId(), histObjs);
        assertEquals(
                "Exactly 2 history entries expected for new PriceModel of product "
                        + orgProduct.getProductId(), 2, histObjs.size());
        // Second entry should have DELETE type
        assertEquals(ModificationType.DELETE, histObjs.get(1).getModtype());
        assertEquals("OBJID in history different", old.getKey(), histObjs
                .get(1).getObjKey());
    }

    /**
     * <b>Testcase:</b> Delete an existing product object <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>Product marked as deleted in the DB</li>
     * <li>History object created for the deleted product</li>
     * <li>PriceModel (usedPayment) marked as deleted in the DB</li>
     * <li>History object created for the deleted PriceModel</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testDeleteProduct() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestDeleteProductPrepare();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    doTestDeleteProduct();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    doTestDeleteProductCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestDeleteProductPrepare() throws Exception {
        domObjects.clear();
        Organization organization;
        if (organizationId == null) {
            organization = Organizations.createOrganization(mgr);
            organizationId = organization.getOrganizationId();
        } else {
            organization = Organizations.findOrganization(mgr, organizationId);
        }
        TechnicalProduct tProd = TechnicalProducts.createTechnicalProduct(mgr,
                organization, "TP_ID_4", false, ServiceAccessType.LOGIN);
        // insert new product
        Product prod = new Product();
        prod.setVendor(organization);
        prod.setProductId("TestDeleteProduct");
        prod.setTechnicalProduct(tProd);
        prod.setProvisioningDate(TIMESTAMP);
        prod.setStatus(ServiceStatus.ACTIVE);
        prod.setType(ServiceType.TEMPLATE);
        PriceModel pi = new PriceModel();
        prod.setPriceModel(pi);
        ParameterSet emptyPS = new ParameterSet();
        prod.setParameterSet(emptyPS);
        mgr.persist(prod);
    }

    private void doTestDeleteProduct() {
        // delete the product
        Product prod = new Product();
        prod.setVendor(Organizations.findOrganization(mgr, organizationId));
        prod.setProductId("TestDeleteProduct");
        Product prod2 = (Product) mgr.find(prod);
        domObjects.clear();
        mgr.remove(prod2);
        domObjects.add((Product) ReflectiveClone.clone(prod2));
    }

    private void doTestDeleteProductCheck() {
        // Try to load deleted
        Product orgProduct = (Product) domObjects.get(0);
        Product cid = new Product();
        cid.setVendor(Organizations.findOrganization(mgr, organizationId));
        cid.setProductId("TestDeleteProduct");
        Product saved = (Product) mgr.find(cid);
        // Check product data
        assertNull("Deleted Product '" + orgProduct.getProductId()
                + "' can still be accessed via DataManager.find", saved);
        // Check deletion of PriceModel (CascadeType.ALL, should also be
        // deleted)
        PriceModel deleted = mgr.find(PriceModel.class, orgProduct
                .getPriceModel().getKey());
        assertNull("PriceModel not deleted", deleted);
        // Load Product history objects and check them
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(orgProduct);
        assertNotNull(
                "History entry 'null' for product " + orgProduct.getProductId(),
                histObjs);
        assertEquals("Exactly 2 history entries expected for product "
                + orgProduct.getProductId(), 2, histObjs.size());
        // load deleted history object (should be second)
        DomainHistoryObject<?> hist = histObjs.get(1);
        assertEquals(ModificationType.DELETE, hist.getModtype());
        assertEquals("OBJID in history different", orgProduct.getKey(),
                hist.getObjKey());
        // Load PriceModel history and check
        histObjs = mgr.findHistory(orgProduct.getPriceModel());
        assertNotNull("History entry 'null' for PriceModel of product "
                + orgProduct.getProductId(), histObjs);
        assertEquals(
                "Exactly 2 history entries expected for PriceModel of product "
                        + orgProduct.getProductId(), 2, histObjs.size());
        // load deleted history object (should be second)
        hist = histObjs.get(1);
        assertEquals(ModificationType.DELETE, hist.getModtype());
        assertEquals("OBJID in history different", orgProduct.getPriceModel()
                .getKey(), hist.getObjKey());

    }

    /**
     * <b>Testcase:</b> Try to insert two products with the same productId<br>
     * <b>ExpectedResult:</b> SaasNonUniqueBusinessKeyException
     * 
     * @throws Throwable
     */
    @Test(expected = NonUniqueBusinessKeyException.class)
    public void testViolateUniqueConstraint() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestViolateUniqueConstraintPrepare();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestViolateUniqueConstraint();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestViolateUniqueConstraintPrepare()
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        domObjects.clear();
        Organization organization;
        if (organizationId == null) {
            organization = Organizations.createOrganization(mgr);
            organizationId = organization.getOrganizationId();
        } else {
            organization = Organizations.findOrganization(mgr, organizationId);
        }
        // insert new product
        TechnicalProduct tProd = TechnicalProducts.createTechnicalProduct(mgr,
                organization, "TP_ID_5", false, ServiceAccessType.LOGIN);
        Product prod = new Product();
        prod.setVendor(organization);
        prod.setProductId("TestUniqueProduct");
        prod.setTechnicalProduct(tProd);
        prod.setProvisioningDate(TIMESTAMP);
        prod.setStatus(ServiceStatus.ACTIVE);
        prod.setType(ServiceType.TEMPLATE);
        PriceModel pi = new PriceModel();
        prod.setPriceModel(pi);
        ParameterSet emptyPS = new ParameterSet();
        prod.setParameterSet(emptyPS);
        mgr.persist(prod);
    }

    private void doTestViolateUniqueConstraint()
            throws NonUniqueBusinessKeyException {
        // Change only product data
        Organization organization = Organizations.findOrganization(mgr,
                organizationId);
        TechnicalProduct tProd = TechnicalProducts.createTechnicalProduct(mgr,
                organization, "TP_ID_6", false, ServiceAccessType.LOGIN);
        Product prod = new Product();
        prod.setVendor(organization);
        prod.setProductId("TestUniqueProduct");
        prod.setTechnicalProduct(tProd);
        prod.setProvisioningDate(TIMESTAMP);
        prod.setStatus(ServiceStatus.ACTIVE);
        PriceModel pi = new PriceModel();
        prod.setPriceModel(pi);
        mgr.persist(prod);
    }

    /**
     * Creates a product and sets a reference to another product to indicate it
     * was copied from that one. The test only checks if the reference is stored
     * correctly in the object itself as well as in its history.
     * 
     * @throws Exception
     */
    @Test
    public void testSetTemplateRef() throws Exception {
        Product targetProduct = (Product) runTX(new Callable<DomainObject<?>>() {
            @Override
            public DomainObject<?> call() throws Exception {
                doTestAdd();
                return mgr.find(domObjects.get(1));
            }
        });

        Product storedProduct = (Product) runTX(new Callable<DomainObject<?>>() {
            @Override
            public DomainObject<?> call() {
                Product source = (Product) mgr.find(domObjects.get(0));
                Product target = (Product) mgr.find(domObjects.get(1));
                source.setTemplate(target);
                return mgr.find(Product.class, source.getKey());
            }
        });

        assertEquals("Object hasn't changed", 1, storedProduct.getVersion());
        assertNotNull("Template reference must be set",
                storedProduct.getTemplate());
        assertEquals("Stored template does not match the one specified",
                targetProduct.getKey(), storedProduct.getTemplate().getKey());

        // also assert that a new history entry was created
        List<DomainHistoryObject<?>> history = getHistory(storedProduct);
        assertEquals("History entry for latest modification was not created",
                2, history.size());
        assertEquals("Wrong mod type stored", ModificationType.MODIFY, history
                .get(1).getModtype());
        assertEquals("Reference to template not set",
                Long.valueOf(targetProduct.getKey()),
                ((ProductHistory) history.get(1)).getTemplateObjKey());
    }

    /**
     * Sets a reference to an organization (customer) for the product and checks
     * if the objects and its history are updated accordingly.
     * 
     * @throws Exception
     */
    @Test
    public void testSetTargetCustomer() throws Exception {

        Product storedProduct = (Product) runTX(new Callable<DomainObject<?>>() {
            @Override
            public DomainObject<?> call() throws Exception {
                doTestAdd();
                Organization customer = Organizations.findOrganization(mgr,
                        organizationId);
                Product prod = (Product) mgr.find(domObjects.get(2));
                prod.setTargetCustomer(customer);
                return mgr.find(prod);
            }
        });

        Organization customer = runTX(new Callable<Organization>() {
            @Override
            public Organization call() {
                return Organizations.findOrganization(mgr, organizationId);
            }
        });

        assertEquals("Object hasn't changed", 1, storedProduct.getVersion());
        assertNotNull("Template reference must be set",
                storedProduct.getTargetCustomer());
        assertEquals("Stored customer does not match the one specified",
                customer.getKey(), storedProduct.getTargetCustomer().getKey());

        // also assert that a new history entry was created
        List<DomainHistoryObject<?>> history = getHistory(storedProduct);
        assertEquals("History entry for latest modification was not created",
                2, history.size());
        assertEquals("Wrong mod type stored", ModificationType.MODIFY, history
                .get(1).getModtype());
        assertEquals("Reference to customer not set",
                Long.valueOf(customer.getKey()),
                ((ProductHistory) history.get(1)).getTargetCustomerObjKey());
    }

    /**
     * Tries to remove a target customer an existing product references. Due to
     * the referential constraints, the operation must not succeed.
     * 
     * @throws Exception
     */
    @Test(expected = EJBTransactionRolledbackException.class)
    public void testRemoveReferencedTargetCustomer() throws Exception {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestAdd();
                // don't use the current organization id, as this one is also
                // supplier, and its deletion causes cascading deletion of
                // products, too (would destroy the test)
                Organization customer = Organizations.findOrganization(mgr,
                        organization.getOrganizationId());
                Product prod = (Product) mgr.find(domObjects.get(2));
                prod.setTargetCustomer(customer);

                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Organization customer = Organizations.findOrganization(mgr,
                        organization.getOrganizationId());
                mgr.remove(customer);
                return null;
            }
        });
    }

    private List<DomainHistoryObject<?>> getHistory(final DomainObject<?> obj)
            throws Exception {
        return runTX(new Callable<List<DomainHistoryObject<?>>>() {
            @Override
            public List<DomainHistoryObject<?>> call() {
                return mgr.findHistory(obj);
            }
        });
    }

    /**
     * Creates a product and copies it. Validation is that new domain objects
     * are checked to have the same content as the original ones but are
     * separate objects (new keys). Check is for product, price model, priced
     * events, parameter set and parameters.
     */
    @Test
    public void testCopy() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestAdd();
                Product prod = (Product) domObjects.get(6);
                ProductReference prodRef = new ProductReference(prod,
                        (Product) domObjects.get(7));
                mgr.persist(prodRef);
                return null;
            }
        });
        final Product copy = runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                domObjects.set(6,
                        mgr.find(Product.class, domObjects.get(6).getKey()));
                Product prod = (Product) domObjects.get(6);
                Product copy = prod.copyForCustomer(Organizations
                        .findOrganization(mgr, organizationId));
                copy.setStatus(ServiceStatus.ACTIVE);
                mgr.persist(copy);
                return copy;
            }
        });
        Organization customer = runTX(new Callable<Organization>() {
            @Override
            public Organization call() {
                return Organizations.findOrganization(mgr, organizationId);
            }
        });

        final Product original = (Product) domObjects.get(6);

        // Assert handling of product do and its history
        assertTrue("Wrong product id set for copy", copy.getProductId()
                .startsWith("Product7#"));
        assertEquals("Wrong customer set", customer.getKey(), copy
                .getTargetCustomer().getKey());
        assertNull("Wrong subscription reference set",
                copy.getOwningSubscription());
        assertTrue("Copied object has identical key as the original",
                original.getKey() != copy.getKey());
        assertNull("Deprovisioning date must not be set",
                copy.getDeprovisioningDate());
        assertTrue("Provisioning date must be after the one of the original",
                copy.getProvisioningDate() >= original.getProvisioningDate());
        assertEquals("Wrong status of copy", ServiceStatus.ACTIVE,
                copy.getStatus());
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Product orig = mgr.find(Product.class, original.getKey());
                Product copied = mgr.find(Product.class, copy.getKey());
                assertEquals("Wrong reference to technical product", orig
                        .getTechnicalProduct().getKey(), copied
                        .getTechnicalProduct().getKey());
                // assert correct product reference to indicate compatibility
                assertEquals("Wrong number of compatible products stored", orig
                        .getAllCompatibleProducts().size(), copied
                        .getAllCompatibleProducts().size());
                for (int i = 0; i < orig.getCompatibleProductsList().size(); i++) {
                    Product product = orig.getCompatibleProductsList().get(i);
                    assertEquals(
                            "Wrong reference to compatible products stored",
                            product.getKey(), copied
                                    .getCompatibleProductsList().get(i)
                                    .getKey());
                }
                return null;
            }
        });
        assertEquals("Wrong reference to template", original.getKey(), copy
                .getTemplate().getKey());
        assertEquals("Wrong reference to supplier", original.getVendorKey(),
                copy.getVendor().getKey());
        List<DomainHistoryObject<?>> history = getHistory(copy);
        assertEquals("Wrong number of history entries found", 1, history.size());
        assertEquals("Wrong modification type", ModificationType.ADD, history
                .get(0).getModtype());

        // assert correct copy of parameter set
        ParameterSet parameterSet = copy.getParameterSet();
        assertEquals(
                "No parameter found, although the original product had one", 1,
                parameterSet.getParameters().size());
        Parameter parameter = parameterSet.getParameters().get(0);
        assertTrue("Object was not copied or persisted",
                parameter.getKey() > original.getParameterSet().getParameters()
                        .get(0).getKey());
        assertEquals("Wrong param value stored", "someValue",
                parameter.getValue());
        assertEquals("Wrong param definition specified", original
                .getParameterSet().getParameters().get(0)
                .getParameterDefinition().getKey(), parameter
                .getParameterDefinition().getKey());
        assertEquals("Wrong reference to parameter set", parameterSet.getKey(),
                parameter.getParameterSet().getKey());
        List<DomainHistoryObject<?>> parameterHist = getHistory(parameter);
        assertEquals("No history entry found for copied parameter", 1,
                parameterHist.size());
        assertEquals("Wrong modification type stored for parameter",
                ModificationType.ADD, parameterHist.get(0).getModtype());

        // assert correct copy of price model
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PriceModel copiedPriceModel = mgr.find(PriceModel.class, copy
                        .getPriceModel().getKey());
                PriceModel origPriceModel = mgr.find(PriceModel.class, original
                        .getPriceModel().getKey());
                assertTrue("Price model was not copied or not stored",
                        copiedPriceModel.getKey() > origPriceModel.getKey());
                assertEquals("Price model charging has changed",
                        origPriceModel.getType(), copiedPriceModel.getType());
                List<PricedEvent> copiedEvents = copiedPriceModel
                        .getConsideredEvents();
                List<PricedEvent> origEvents = origPriceModel
                        .getConsideredEvents();
                assertEquals("Number of events doesn't match in orig and copy",
                        origEvents.size(), copiedEvents.size());
                for (int i = 0; i < origEvents.size(); i++) {
                    PricedEvent copiedEvent = copiedEvents.get(i);
                    PricedEvent origEvent = origEvents.get(i);
                    assertTrue("Priced event was not copied",
                            copiedEvent.getKey() > origEvent.getKey());
                    assertEquals("Wrong event reference copied",
                            origEvent.getEventKey(), copiedEvent.getEventKey());
                    assertEquals("Wrong event price copied",
                            origEvent.getEventPrice(),
                            copiedEvent.getEventPrice());
                    assertEquals("Wrong price model reference stored",
                            copiedPriceModel.getKey(),
                            copiedEvent.getPriceModelKey());
                    List<DomainHistoryObject<?>> copiedEventHist = getHistory(copiedEvent);
                    assertEquals("Wrong history information for copied event",
                            1, copiedEventHist.size());
                    assertEquals(
                            "Wrong modification type stored for history entry",
                            ModificationType.ADD, copiedEventHist.get(0)
                                    .getModtype());
                }

                // assert priced roles for price model
                List<PricedProductRole> copiedPMRolePrices = copiedPriceModel
                        .getRoleSpecificUserPrices();
                assertEquals("priced product role setting was not copied", 1,
                        copiedPMRolePrices.size());
                PricedProductRole copiedPricedProductRole = copiedPMRolePrices
                        .get(0);
                assertEquals(new BigDecimal(111L),
                        copiedPricedProductRole.getPricePerUser());
                PricedProductRole origPricedProductRole = origPriceModel
                        .getRoleSpecificUserPrices().get(0);
                assertEquals(origPricedProductRole.getRoleDefinition(),
                        copiedPricedProductRole.getRoleDefinition());
                assertFalse(origPricedProductRole.getKey() == copiedPricedProductRole
                        .getKey());
                assertEquals(copiedPriceModel.getKey(), copiedPricedProductRole
                        .getPriceModel().getKey());
                List<PricedProductRoleHistory> historizedPricedProducts = ParameterizedTypes
                        .list(mgr.findHistory(copiedPricedProductRole),
                                PricedProductRoleHistory.class);
                assertEquals(1, historizedPricedProducts.size());
                assertEquals(ModificationType.ADD, historizedPricedProducts
                        .get(0).getModtype());

                return null;
            }
        });

    }

    @Test
    public void testGetCompatibleProductsThroughTemplate() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestAdd();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                domObjects.set(0,
                        mgr.find(Product.class, domObjects.get(0).getKey()));
                domObjects.set(1,
                        mgr.find(Product.class, domObjects.get(1).getKey()));
                domObjects.set(2,
                        mgr.find(Product.class, domObjects.get(2).getKey()));
                final Product prod1 = (Product) domObjects.get(0);
                final Product prod2 = (Product) domObjects.get(1);
                Product child = (Product) domObjects.get(2);
                ProductReference reference = new ProductReference(prod1, prod2);
                mgr.persist(reference);
                child.setProductId("Prod1Child");
                child.setTemplate(prod1);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                domObjects.set(0,
                        mgr.find(Product.class, domObjects.get(0).getKey()));
                domObjects.set(1,
                        mgr.find(Product.class, domObjects.get(1).getKey()));
                domObjects.set(2,
                        mgr.find(Product.class, domObjects.get(2).getKey()));
                Product child = (Product) domObjects.get(2);
                List<ProductReference> products = child
                        .getAllCompatibleProducts();
                assertEquals(1, products.size());
                assertEquals(domObjects.get(1).getKey(), products.get(0)
                        .getTargetProduct().getKey());
                return null;
            }
        });
    }

    @Test
    public void testGetCompatibleProductsDirect() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestAdd();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                domObjects.set(0,
                        mgr.find(Product.class, domObjects.get(0).getKey()));
                domObjects.set(1,
                        mgr.find(Product.class, domObjects.get(1).getKey()));
                final Product prod1 = (Product) domObjects.get(0);
                final Product prod2 = (Product) domObjects.get(1);
                ProductReference reference = new ProductReference(prod1, prod2);
                mgr.persist(reference);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                domObjects.set(0,
                        mgr.find(Product.class, domObjects.get(0).getKey()));
                domObjects.set(1,
                        mgr.find(Product.class, domObjects.get(1).getKey()));
                Product product = (Product) domObjects.get(0);
                List<ProductReference> products = product
                        .getAllCompatibleProducts();
                assertEquals(1, products.size());
                assertEquals(domObjects.get(1).getKey(), products.get(0)
                        .getTargetProduct().getKey());
                return null;
            }
        });
    }

    @Test
    public void testGetCompatibleProductsListForPartnerTemplate()
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestAdd();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                domObjects.set(0,
                        mgr.find(Product.class, domObjects.get(0).getKey()));
                domObjects.set(1,
                        mgr.find(Product.class, domObjects.get(1).getKey()));
                domObjects.set(2,
                        mgr.find(Product.class, domObjects.get(2).getKey()));
                final Product prod1 = (Product) domObjects.get(0);
                final Product prod2 = (Product) domObjects.get(1);
                Product child = (Product) domObjects.get(2);
                ProductReference reference = new ProductReference(prod1, prod2);
                mgr.persist(reference);
                child.setProductId("Prod1Child");
                child.setTemplate(prod1);
                child.setType(ServiceType.PARTNER_TEMPLATE);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                domObjects.set(0,
                        mgr.find(Product.class, domObjects.get(0).getKey()));
                domObjects.set(1,
                        mgr.find(Product.class, domObjects.get(1).getKey()));
                domObjects.set(2,
                        mgr.find(Product.class, domObjects.get(2).getKey()));
                Product child = (Product) domObjects.get(2);
                List<Product> products = child.getCompatibleProductsList();
                assertEquals(0, products.size());
                return null;
            }
        });
    }

    @Test
    public void testGetCompatibleProductsListThroughTemplate() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestAdd();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                domObjects.set(0,
                        mgr.find(Product.class, domObjects.get(0).getKey()));
                domObjects.set(1,
                        mgr.find(Product.class, domObjects.get(1).getKey()));
                domObjects.set(2,
                        mgr.find(Product.class, domObjects.get(2).getKey()));
                final Product prod1 = (Product) domObjects.get(0);
                final Product prod2 = (Product) domObjects.get(1);
                Product child = (Product) domObjects.get(2);
                ProductReference reference = new ProductReference(prod1, prod2);
                mgr.persist(reference);
                child.setProductId("Prod1Child");
                child.setTemplate(prod1);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                domObjects.set(0,
                        mgr.find(Product.class, domObjects.get(0).getKey()));
                domObjects.set(1,
                        mgr.find(Product.class, domObjects.get(1).getKey()));
                domObjects.set(2,
                        mgr.find(Product.class, domObjects.get(2).getKey()));
                Product child = (Product) domObjects.get(2);
                List<Product> products = child.getCompatibleProductsList();
                assertEquals(1, products.size());
                assertEquals(domObjects.get(1).getKey(), products.get(0)
                        .getKey());
                return null;
            }
        });
    }

    @Test
    public void testGetCompatibleProductsListDirect() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestAdd();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                domObjects.set(0,
                        mgr.find(Product.class, domObjects.get(0).getKey()));
                domObjects.set(1,
                        mgr.find(Product.class, domObjects.get(1).getKey()));
                final Product prod1 = (Product) domObjects.get(0);
                final Product prod2 = (Product) domObjects.get(1);
                ProductReference reference = new ProductReference(prod1, prod2);
                mgr.persist(reference);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                domObjects.set(0,
                        mgr.find(Product.class, domObjects.get(0).getKey()));
                domObjects.set(1,
                        mgr.find(Product.class, domObjects.get(1).getKey()));
                Product product = (Product) domObjects.get(0);
                List<Product> products = product.getCompatibleProductsList();
                assertEquals(1, products.size());
                assertEquals(domObjects.get(1).getKey(), products.get(0)
                        .getKey());
                return null;
            }
        });
    }

    @Test
    public void testGetProductsForCustomerOnMarketplace_servicesNotPublishedOnMp()
            throws Exception {
        // given
        final PlatformUser supplierUser = createOrgWithUserAndLogin();
        Marketplace marketplace = createMarketplace(supplierUser
                .getOrganization());
        createProducts(supplierUser.getOrganization(), marketplace);

        // when
        List<Product> dbProducts = executeGetProductsForCustomerOnMarketplace(WRONG_MP_ID);

        // then
        assertEquals(0, dbProducts.size());
    }

    @Test
    public void testGetActivePublishedProducts() throws Exception {
        // given
        PlatformUser supplierUser = createOrgWithUserAndLogin();
        Marketplace marketplace = createMarketplace(supplierUser
                .getOrganization());
        createProducts(supplierUser.getOrganization(), marketplace);

        // when
        List<Product> result = executeGetActivePublishedProducts(MP_ID);

        // then
        assertEquals(1, result.size());
        assertEquals(ServiceType.CUSTOMER_TEMPLATE, result.get(0).getType());
        assertEquals(ServiceStatus.ACTIVE, result.get(0).getStatus());
    }

    @SuppressWarnings("unchecked")
    private List<Product> executeGetActivePublishedProducts(
            final String marketplaceId) throws Exception {
        return runTX(new Callable<List<Product>>() {
            @Override
            public List<Product> call() {
                Query query = mgr
                        .createNamedQuery("Product.getActivePublishedProducts");
                query.setParameter("marketplaceId", marketplaceId);
                query.setParameter("customer", mgr.getCurrentUser()
                        .getOrganization());
                return query.getResultList();
            }
        });
    }

    private PlatformUser createOrgWithUserAndLogin() throws Exception {
        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization org = Organizations.createOrganization(mgr);
                PlatformUser user = Organizations.createUserForOrg(mgr, org,
                        true, "admin");
                return user;
            }
        });
        container.login(String.valueOf(user.getKey()));
        return user;
    }

    private Marketplace createMarketplace(
            final Organization supplierOrganization) throws Exception {
        Marketplace marketplace = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws NonUniqueBusinessKeyException {
                return Marketplaces.createGlobalMarketplace(
                        supplierOrganization, MP_ID, mgr);
            }
        });
        return marketplace;
    }

    @SuppressWarnings("unchecked")
    private List<Product> executeGetProductsForCustomerOnMarketplace(
            final String marketplaceId) throws Exception {
        return runTX(new Callable<List<Product>>() {
            @Override
            public List<Product> call() {
                Query query = mgr
                        .createNamedQuery("Product.getProductsForCustomerOnMarketplace");
                query.setParameter("customer", mgr.getCurrentUser()
                        .getOrganization());
                query.setParameter("marketplaceId", marketplaceId);
                return query.getResultList();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private List<Product> getCustomerSpecificCopiesForTemplate(
            final Product template) throws Exception {
        return runTX(new Callable<List<Product>>() {
            @Override
            public List<Product> call() {
                Set<ServiceType> types = EnumSet
                        .of(ServiceType.CUSTOMER_TEMPLATE);
                Query query = mgr
                        .createNamedQuery("Product.getCustomerSpecificCopiesForTemplate");
                query.setParameter("template", template);
                query.setParameter("serviceType", types);
                return query.getResultList();
            }
        });
    }

    private Set<Long> createProducts(final Organization supplier,
            final Marketplace marketplace) throws Exception {
        return runTX(new Callable<Set<Long>>() {
            @Override
            @SuppressWarnings("boxing")
            public Set<Long> call() throws Exception {
                Set<Long> resultProductKeys = new HashSet<Long>();

                // create template product
                Product templateProduct = Products.createProduct(
                        supplier.getOrganizationId(), "productId",
                        "techProductId", mgr);
                resultProductKeys.add(templateProduct.getKey());
                CatalogEntries.create(mgr, marketplace, templateProduct);

                // create customer product
                Product customerProduct = Products
                        .createCustomerSpecifcProduct(mgr, supplier,
                                templateProduct, ServiceStatus.ACTIVE);
                resultProductKeys.add(customerProduct.getKey());

                // create broker product
                Organization broker = Organizations.createOrganization(mgr);
                Product brokerProduct = Products.createProductResaleCopy(
                        templateProduct, broker, marketplace, mgr);
                resultProductKeys.add(brokerProduct.getKey());

                // create reseller product
                Organization reseller = Organizations.createOrganization(mgr);
                Product resellerProduct = Products.createProductResaleCopy(
                        templateProduct, reseller, marketplace, mgr);
                resultProductKeys.add(resellerProduct.getKey());
                return resultProductKeys;
            }
        });
    }

    @SuppressWarnings("boxing")
    @Test
    public void testGetProductsForCustomerOnMarketplace() throws Exception {
        // given
        PlatformUser supplierUser = createOrgWithUserAndLogin();
        Marketplace marketplace = createMarketplace(supplierUser
                .getOrganization());
        Set<Long> givenProductKeys = createProducts(
                supplierUser.getOrganization(), marketplace);

        // when
        List<Product> dbProducts = executeGetProductsForCustomerOnMarketplace(MP_ID);

        // then
        assertEquals(givenProductKeys.size(), dbProducts.size());
        for (Product dbProduct : dbProducts) {
            assertTrue(givenProductKeys.contains(dbProduct.getKey()));
        }
    }

    @Test
    public void testGetProductsForCustomerOnMarketplace_filterSubscriptionProducts()
            throws Exception {
        // given
        PlatformUser supplierUser = createOrgWithUserAndLogin();
        Marketplace marketplace = createMarketplace(supplierUser
                .getOrganization());
        List<Product> givenProducts = createTemplateAndSubscriptionProduct(
                supplierUser.getOrganization(), marketplace);
        assertEquals(2, givenProducts.size());
        assertEquals(ServiceType.TEMPLATE, givenProducts.get(0).getType());
        assertEquals(ServiceType.SUBSCRIPTION, givenProducts.get(1).getType());

        // when
        List<Product> dbProductsFromMp = executeGetProductsForCustomerOnMarketplace(MP_ID);

        // then
        assertEquals(1, dbProductsFromMp.size());
        assertEquals(ServiceType.TEMPLATE, dbProductsFromMp.get(0).getType());
    }

    @Test
    public void getCustomerSpecificProducts() throws Exception {
        // given
        PlatformUser supplierUser = createOrgWithUserAndLogin();
        Marketplace marketplace = createMarketplace(supplierUser
                .getOrganization());
        Product template = createTemplateProducts(
                supplierUser.getOrganization(), marketplace);

        // when
        List<Product> dbProducts = getCustomerSpecificCopiesForTemplate(template);

        // then
        assertEquals(4, dbProducts.size());
    }

    private Product createTemplateProducts(final Organization supplier,
            final Marketplace marketplace) throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {

                // create template product
                Product templateProduct = Products.createProduct(
                        supplier.getOrganizationId(), "productId",
                        "techProductId", mgr);
                CatalogEntries.create(mgr, marketplace, templateProduct);

                // create customer products
                Products.createCustomerSpecifcProduct(mgr, supplier,
                        templateProduct, ServiceStatus.INACTIVE);

                Products.createCustomerSpecifcProduct(mgr, supplier,
                        templateProduct, ServiceStatus.INACTIVE);

                Products.createCustomerSpecifcProduct(mgr, supplier,
                        templateProduct, ServiceStatus.INACTIVE);

                Products.createCustomerSpecifcProduct(mgr, supplier,
                        templateProduct, ServiceStatus.INACTIVE);
                return templateProduct;
            }
        });
    }

    private List<Product> createTemplateAndSubscriptionProduct(
            final Organization supplier, final Marketplace marketplace)
            throws Exception {
        return runTX(new Callable<List<Product>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<Product> call() throws Exception {
                Organization customer = supplier;

                // create and subscribe template product
                Product templateProduct = Products.createProduct(
                        supplier.getOrganizationId(), "productId",
                        "techProductId", mgr);
                CatalogEntries.create(mgr, marketplace, templateProduct);
                Subscriptions.createSubscription(mgr,
                        customer.getOrganizationId(), templateProduct);

                mgr.flush();
                return mgr
                        .createQuery("SELECT p FROM Product p ORDER BY p.key")
                        .getResultList();
            }
        });
    }
}
