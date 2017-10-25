/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 12.03.2010                                                      
 *                                                                              
 *  Completion Time:  12.03.2010                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.enums.BillingAdapterIdentifier;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.TriggerProcessStatus;
import org.oscm.internal.types.enumtypes.TriggerTargetType;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.types.enumtypes.OperationParameterType;

public class LocalizedResourceIT extends EJBTestBase {

    private DataService mgr;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.login("testuser");
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        mgr = container.get(DataService.class);
    }

    @Test
    public void testCreate() {
        LocalizedResource lr = new LocalizedResource();
        lr.setLocale("locale");
        lr.setObjectKey(1L);
        lr.setValue("value");
        lr.setObjectType(LocalizedObjectTypes.EVENT_DESC);

        Assert.assertEquals("Wrong key value", 1L, lr.getObjectKey());
        Assert.assertEquals("Wrong locale", "locale", lr.getLocale());
        Assert.assertEquals("Wrong value", "value", lr.getValue());
        Assert.assertEquals("Wrong object type",
                LocalizedObjectTypes.EVENT_DESC, lr.getObjectType());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unmodifiableList_Event() {
        Event obj = new Event();
        obj.getLocalizedObjectTypes().add(LocalizedObjectTypes.MAIL_CONTENT);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unmodifiableList_Organization() {
        Organization obj = new Organization();
        obj.getLocalizedObjectTypes().add(LocalizedObjectTypes.MAIL_CONTENT);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unmodifiableList_Marketplace() {
        Marketplace obj = new Marketplace();
        obj.getLocalizedObjectTypes().add(LocalizedObjectTypes.MAIL_CONTENT);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unmodifiableList_ParameterDefinition() {
        ParameterDefinition obj = new ParameterDefinition();
        obj.getLocalizedObjectTypes().add(LocalizedObjectTypes.MAIL_CONTENT);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unmodifiableList_ParameterOption() {
        ParameterOption obj = new ParameterOption();
        obj.getLocalizedObjectTypes().add(LocalizedObjectTypes.MAIL_CONTENT);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unmodifiableList_PaymentType() {
        PaymentType obj = new PaymentType();
        obj.getLocalizedObjectTypes().add(LocalizedObjectTypes.MAIL_CONTENT);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unmodifiableList_PriceModel() {
        PriceModel obj = new PriceModel();
        obj.getLocalizedObjectTypes().add(LocalizedObjectTypes.MAIL_CONTENT);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unmodifiableList_Product() {
        Product obj = new Product();
        obj.getLocalizedObjectTypes().add(LocalizedObjectTypes.MAIL_CONTENT);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unmodifiableList_Report() {
        Report obj = new Report();
        obj.getLocalizedObjectTypes().add(LocalizedObjectTypes.MAIL_CONTENT);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unmodifiableList_RoleDefinition() {
        RoleDefinition obj = new RoleDefinition();
        obj.getLocalizedObjectTypes().add(LocalizedObjectTypes.MAIL_CONTENT);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unmodifiableList_Subscription() {
        Subscription obj = new Subscription();
        obj.getLocalizedObjectTypes().add(LocalizedObjectTypes.MAIL_CONTENT);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unmodifiableList_TechnicalProduct() {
        TechnicalProduct obj = new TechnicalProduct();
        obj.getLocalizedObjectTypes().add(LocalizedObjectTypes.MAIL_CONTENT);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unmodifiableList_TechnicalProductOperation() {
        TechnicalProductOperation obj = new TechnicalProductOperation();
        obj.getLocalizedObjectTypes().add(LocalizedObjectTypes.MAIL_CONTENT);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unmodifiableList_OperationParameter() {
        OperationParameter obj = new OperationParameter();
        obj.getLocalizedObjectTypes().add(LocalizedObjectTypes.MAIL_CONTENT);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unmodifiableList_TriggerProcess() {
        TriggerProcess obj = new TriggerProcess();
        obj.getLocalizedObjectTypes().add(LocalizedObjectTypes.MAIL_CONTENT);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unmodifiableList_CatalogEntry() {
        CatalogEntry obj = new CatalogEntry();
        obj.getLocalizedObjectTypes().add(LocalizedObjectTypes.MAIL_CONTENT);
    }

    @Test
    public void removeLocalization_Product() throws Exception {

        Product product = createProduct(null, true);
        PriceModel pm = product.getPriceModel();
        // remove object without localization
        remove(product);

        product = createProduct(null, true);
        pm = product.getPriceModel();
        persistLocalizedResource(product);
        persistLocalizedResource(pm);

        // clear to detach the entities
        clear();

        // remove object with localization
        remove(product);
        validateLocalizedResources(product, false);
        validateLocalizedResources(pm, false);
    }

    @Test
    public void removeLocalization_Organization() throws Exception {

        Organization org = createOrganization();
        // remove object without localization
        remove(org);

        org = createOrganization();
        persistLocalizedResource(org);

        Marketplace mp1 = createMarketplace(org);
        persistLocalizedResource(mp1);
        Marketplace mp2 = createMarketplace(org);
        persistLocalizedResource(mp2);

        TechnicalProduct tp1 = createTechnicalProductWithDependentEntities(org);
        persistLocalizedResource(tp1);
        TechnicalProduct tp2 = createTechnicalProductWithDependentEntities(org);
        persistLocalizedResource(tp2);

        Product prod1 = createProduct(org, true);
        persistLocalizedResource(prod1);
        PriceModel pm1 = prod1.getPriceModel();
        persistLocalizedResource(pm1);

        Product prod2 = createProduct(org, true);
        persistLocalizedResource(prod2);
        PriceModel pm2 = prod2.getPriceModel();
        persistLocalizedResource(pm2);

        // for subscription not possible, if subscription exists
        // the product and organization cannot be deleted

        // clear to detach the entities
        clear();

        // remove object with localization
        remove(org);
        validateLocalizedResources(org, false);
        validateLocalizedResources(mp1, false);
        validateLocalizedResources(mp2, false);
        validateLocalizedResources(tp1, false);
        validateLocalizedResources(tp2, false);
        validateLocalizedResources(prod1, false);
        validateLocalizedResources(pm1, false);
        validateLocalizedResources(prod2, false);
        validateLocalizedResources(pm2, false);
    }

    @Test
    public void removeLocalization_Event() throws Exception {

        Event event = createEvent(null);
        // remove object without localization
        remove(event);
        event = createEvent(null);
        persistLocalizedResource(event);
        // remove object with localization
        remove(event);
        validateLocalizedResources(event, false);
    }

    @Test
    public void removeLocalization_ParameterDefinition() throws Exception {

        ParameterDefinition pd = createParameterDefinition(null);
        // remove object without localization
        remove(pd);
        pd = createParameterDefinition(null);
        persistLocalizedResource(pd);
        // remove object with localization
        remove(pd);
        validateLocalizedResources(pd, false);
    }

    @Test
    public void removeLocalization_ParameterOption() throws Exception {

        ParameterOption po = createParameterOption(null);
        // remove object without localization
        remove(po);
        po = createParameterOption(null);
        persistLocalizedResource(po);
        // remove object with localization
        remove(po);
        validateLocalizedResources(po, false);
    }

    @Test
    public void removeLocalization_Report() throws Exception {

        Report report = createReport();
        // remove object without localization
        remove(report);
        report = createReport();
        persistLocalizedResource(report);
        // remove object with localization
        remove(report);
        validateLocalizedResources(report, false);
    }

    @Test
    public void removeLocalization_RoleDefinition() throws Exception {

        RoleDefinition roledef = createRoleDefinition();
        // remove object without localization
        remove(roledef);
        roledef = createRoleDefinition();
        persistLocalizedResource(roledef);
        // remove object with localization
        remove(roledef);
        validateLocalizedResources(roledef, false);
    }

    @Test
    public void removeLocalization_TriggerProcess() throws Exception {

        TriggerProcess trigger = createTriggerProcess(null);
        // remove object without localization
        remove(trigger);
        trigger = createTriggerProcess(null);
        persistLocalizedResource(trigger);
        // remove object with localization
        remove(trigger);
        validateLocalizedResources(trigger, false);
    }

    @Test
    public void removeLocalization_PriceModel() throws Exception {

        PriceModel pm = createPriceModel(null);
        // remove object without localization
        remove(pm);
        pm = createPriceModel(null);
        persistLocalizedResource(pm);
        // remove object with localization
        remove(pm);
        validateLocalizedResources(pm, false);
    }

    @Test
    public void removeLocalization_PaymentType() throws Exception {

        PaymentType pt = createPaymentType();
        // remove object without localization
        remove(pt);
        pt = createPaymentType();
        persistLocalizedResource(pt);
        // remove object with localization
        remove(pt);
        validateLocalizedResources(pt, false);
    }

    @Test
    public void removeLocalization_TechnicalProductOperation()
            throws Exception {

        TechnicalProduct tp = createTechnicalProduct(null);
        TechnicalProductOperation tpo = createTechnicalProductOperation(tp);
        // remove object without localization
        remove(tpo);
        tpo = createTechnicalProductOperation(tp);
        persistLocalizedResource(tpo);
        // remove object with localization
        remove(tpo);
        validateLocalizedResources(tpo, false);
    }

    @Test
    public void removeLocalization_OperationParameter() throws Exception {
        TechnicalProductOperation tpo = createTechnicalProductOperation(
                createTechnicalProduct(null));
        OperationParameter op = createOperationParameter(tpo);

        // remove object without localization
        remove(op);
        op = createOperationParameter(tpo);
        persistLocalizedResource(op);
        // remove object with localization
        remove(op);
        validateLocalizedResources(op, false);
    }

    @Test
    public void removeLocalization_Marketplace() throws Exception {

        Marketplace mp = createMarketplace(null);
        // remove object without localization
        remove(mp);
        mp = createMarketplace(null);
        persistLocalizedResource(mp);
        // remove object with localization
        remove(mp);
        validateLocalizedResources(mp, false);
    }

    @Test
    public void removeLocalization_Subscription() throws Exception {

        Subscription sub = createSubscription(null, null);
        // remove object without localization
        remove(sub);
        sub = createSubscription(null, null);
        persistLocalizedResource(sub);
        // remove object with localization
        remove(sub);
        validateLocalizedResources(sub, false);
    }

    @Test
    public void removeLocalization_TechnicalProduct() throws Exception {

        TechnicalProduct tp = createTechnicalProductWithDependentEntities(null);
        // remove object without localization
        remove(tp);

        tp = createTechnicalProductWithDependentEntities(null);
        persistLocalizedResource(tp);

        List<Event> events = tp.getEvents();
        List<TechnicalProductOperation> tpos = tp
                .getTechnicalProductOperations();
        List<ParameterDefinition> pardefs = tp.getParameterDefinitions();

        for (Event e : events) {
            persistLocalizedResource(e);
        }

        for (TechnicalProductOperation tpo : tpos) {
            persistLocalizedResource(tpo);
        }

        for (ParameterDefinition pd : pardefs) {
            persistLocalizedResource(pd);

            for (ParameterOption po : pd.getOptionList()) {
                persistLocalizedResource(po);
            }
        }

        // clear to detach the entities
        clear();

        // remove object with localization
        remove(tp);

        validateLocalizedResources(tp, false);

        for (Event e : events) {
            validateLocalizedResources(e, false);
        }

        for (TechnicalProductOperation tpo : tpos) {
            validateLocalizedResources(tpo, false);
        }

        for (ParameterDefinition pd : pardefs) {
            validateLocalizedResources(pd, false);

            for (ParameterOption po : pd.getOptionList()) {
                validateLocalizedResources(po, false);
            }
        }
    }

    /**
     * Tests the remove for domain object without object types for localization.
     * CatalogEntry is domain object without localization.
     * 
     * @throws Exception
     */
    @Test
    public void removeLocalization_CatalogEntry() throws Exception {

        CatalogEntry catEntry = createCatalogEntry(null);
        // remove object without localization
        remove(catEntry);
    }

    private void remove(final DomainObject<?> domobj) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mgr.remove(mgr.find(domobj.getClass(), domobj.getKey()));
                return null;
            }
        });
    }

    private void persistLocalizedResource(final DomainObject<?> domobj)
            throws Exception {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                long key = domobj.getKey();
                List<LocalizedObjectTypes> ltypes = domobj
                        .getLocalizedObjectTypes();
                assertTrue(ltypes.size() > 0);
                for (LocalizedObjectTypes objectType : ltypes) {
                    LocalizedResource lr;
                    lr = new LocalizedResource("en", key, objectType);
                    lr.setValue("english");
                    mgr.persist(lr);
                    lr = new LocalizedResource("ge", key, objectType);
                    lr.setValue("german");
                    mgr.persist(lr);
                    lr = new LocalizedResource("ja", key, objectType);
                    lr.setValue("japanese");
                    mgr.persist(lr);
                    mgr.flush();
                }
                return null;
            }
        });

        validateLocalizedResources(domobj, true);

    }

    private void validateLocalizedResources(final DomainObject<?> domobj,
            final boolean existExpected) throws Exception {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                long key = domobj.getKey();
                for (LocalizedObjectTypes objectType : domobj
                        .getLocalizedObjectTypes()) {
                    LocalizedResource lr;
                    lr = new LocalizedResource("en", key, objectType);
                    if (existExpected)
                        assertNotNull(mgr.find(lr));
                    else
                        assertNull(mgr.find(lr));
                    lr = new LocalizedResource("ge", key, objectType);
                    if (existExpected)
                        assertNotNull(mgr.find(lr));
                    else
                        assertNull(mgr.find(lr));
                    lr = new LocalizedResource("ja", key, objectType);
                    if (existExpected)
                        assertNotNull(mgr.find(lr));
                    else
                        assertNull(mgr.find(lr));
                }
                return null;
            }
        });
    }

    private Organization createOrganization() throws Exception {

        Organization org = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = new Organization();
                org.setOrganizationId("orgId");
                org.setName("organization");
                org.setCutOffDay(1);
                Organization org1 = (Organization) mgr.find(org);
                if (org1 == null) {
                    mgr.persist(org);
                } else {
                    org = org1;
                }
                return org;
            }
        });

        return org;
    }

    private PriceModel createPriceModel(final Product product)
            throws Exception {

        PriceModel priceModel = runTX(new Callable<PriceModel>() {
            @Override
            public PriceModel call() throws Exception {
                PriceModel priceModel = new PriceModel();
                priceModel.setType(PriceModelType.FREE_OF_CHARGE);
                Product prod = product;
                if (prod == null) {
                    prod = new Product();
                    prod.setKey(1111110000);
                }
                priceModel.setProduct(prod);
                mgr.persist(priceModel);
                return priceModel;
            }
        });

        return priceModel;
    }

    private TechnicalProduct createTechnicalProduct(final Organization org)
            throws Exception {
        TechnicalProduct tp = runTX(new Callable<TechnicalProduct>() {
            @Override
            public TechnicalProduct call() throws Exception {
                Organization provider = null;
                if (org != null) {
                    provider = (Organization) mgr.find(org);
                }
                if (provider == null) {
                    provider = createOrganization();
                }
                TechnicalProduct tp = new TechnicalProduct();
                tp.setBillingIdentifier(
                        BillingAdapterIdentifier.NATIVE_BILLING.toString());
                tp.setTechnicalProductId(
                        "technicalproductId" + System.currentTimeMillis());
                tp.setOrganization(provider);
                tp.setProvisioningURL("http://test.com");
                TechnicalProduct tp1 = (TechnicalProduct) mgr.find(tp);
                if (tp1 == null) {
                    mgr.persist(tp);
                } else {
                    tp = tp1;
                }
                return tp;
            }
        });

        return tp;
    }

    private TechnicalProduct createTechnicalProductWithDependentEntities(
            final Organization org) throws Exception {
        TechnicalProduct tp = runTX(new Callable<TechnicalProduct>() {
            @Override
            public TechnicalProduct call() throws Exception {
                TechnicalProduct tp = createTechnicalProduct(org);
                tp = mgr.find(tp.getClass(), tp.getKey());
                Event event = createEvent(tp);
                List<Event> events = new ArrayList<>();
                events.add(event);
                event = createEvent(tp);
                events.add(event);
                tp.setEvents(events);
                ParameterOption po = createParameterOption(tp);
                List<ParameterDefinition> pds = new ArrayList<>();
                pds.add(po.getParameterDefinition());
                tp.setParameterDefinitions(pds);
                TechnicalProductOperation tpo = createTechnicalProductOperation(
                        tp);
                List<TechnicalProductOperation> tpos = new ArrayList<>();
                tpos.add(tpo);
                tp.setTechnicalProductOperations(tpos);
                mgr.persist(tp);
                return tp;
            }
        });

        return tp;
    }

    private Event createEvent(final TechnicalProduct tp) throws Exception {
        Event event = runTX(new Callable<Event>() {
            @Override
            public Event call() throws Exception {
                TechnicalProduct tp1 = tp;
                if (tp1 != null) {
                    tp1 = mgr.find(tp.getClass(), tp.getKey());
                }
                Event event = new Event();
                event.setEventIdentifier(
                        "eventId" + System.currentTimeMillis());
                event.setEventType(EventType.PLATFORM_EVENT);
                event.setTechnicalProduct(tp1);
                mgr.persist(event);
                return event;
            }
        });
        return event;
    }

    private Report createReport() throws Exception {

        Report report = runTX(new Callable<Report>() {
            @Override
            public Report call() throws Exception {
                OrganizationRole orgrole = new OrganizationRole();
                orgrole.setRoleName(OrganizationRoleType.MARKETPLACE_OWNER);
                OrganizationRole orgrole1 = (OrganizationRole) mgr
                        .find(orgrole);
                if (orgrole1 == null) {
                    mgr.persist(orgrole);
                } else {
                    orgrole = orgrole1;
                }
                Report report = new Report();
                report.setReportName("reportname");
                report.setOrganizationRole(orgrole);
                mgr.persist(report);
                return report;
            }
        });

        return report;
    }

    private Marketplace createMarketplace(final Organization owner)
            throws Exception {

        Marketplace mp = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                Organization org = owner;
                if (org == null) {
                    org = createOrganization();
                } else {
                    org = (Organization) mgr.find(org);
                }
                return Marketplaces.createMarketplace(org,
                        "MpId" + System.currentTimeMillis(), false, mgr);
            }
        });
        return mp;
    }

    private RoleDefinition createRoleDefinition() throws Exception {

        RoleDefinition roledef = runTX(new Callable<RoleDefinition>() {
            @Override
            public RoleDefinition call() throws Exception {
                TechnicalProduct tp = createTechnicalProduct(null);
                RoleDefinition roledef = new RoleDefinition();
                roledef.setRoleId("roleId");
                roledef.setTechnicalProduct(tp);
                mgr.persist(roledef);
                return roledef;
            }
        });
        return roledef;
    }

    private ParameterDefinition createParameterDefinition(
            final TechnicalProduct tp) throws Exception {

        ParameterDefinition pd = runTX(new Callable<ParameterDefinition>() {
            @Override
            public ParameterDefinition call() throws Exception {
                TechnicalProduct tp1 = tp;
                if (tp1 != null) {
                    tp1 = mgr.find(tp.getClass(), tp.getKey());
                }
                ParameterDefinition pd = new ParameterDefinition();
                pd.setParameterId("parameterId");
                pd.setParameterType(ParameterType.PLATFORM_PARAMETER);
                pd.setValueType(ParameterValueType.STRING);
                pd.setTechnicalProduct(tp1);
                mgr.persist(pd);
                return pd;
            }
        });
        return pd;
    }

    private ParameterOption createParameterOption(final TechnicalProduct tp)
            throws Exception {

        ParameterOption po = runTX(new Callable<ParameterOption>() {
            @Override
            public ParameterOption call() throws Exception {
                ParameterDefinition pd = createParameterDefinition(tp);
                ParameterOption po = new ParameterOption();
                po.setOptionId("optionId");
                po.setParameterDefinition(pd);
                mgr.persist(po);
                return po;
            }
        });
        return po;
    }

    private TriggerProcess createTriggerProcess(final Organization org)
            throws Exception {
        TriggerProcess trigger = runTX(new Callable<TriggerProcess>() {
            @Override
            public TriggerProcess call() throws Exception {
                TriggerDefinition td = new TriggerDefinition();
                td.setType(TriggerType.ACTIVATE_SERVICE);
                td.setTarget("target");
                td.setTargetType(TriggerTargetType.WEB_SERVICE);
                if (org != null) {
                    Organization org1 = (Organization) mgr.find(org);
                    td.setOrganization(org1);
                }
                td.setName("testTrigger");
                mgr.persist(td);
                TriggerProcess trigger = new TriggerProcess();
                trigger.setState(TriggerProcessStatus.CANCELLED);
                trigger.setTriggerDefinition(td);
                mgr.persist(trigger);
                return trigger;
            }
        });
        return trigger;
    }

    private TechnicalProductOperation createTechnicalProductOperation(
            final TechnicalProduct tp) throws Exception {
        TechnicalProductOperation tpo = runTX(
                new Callable<TechnicalProductOperation>() {
                    @Override
                    public TechnicalProductOperation call() throws Exception {
                        TechnicalProduct tp1 = null;
                        if (tp != null) {
                            tp1 = mgr.find(tp.getClass(), tp.getKey());
                        }
                        TechnicalProductOperation tpo = new TechnicalProductOperation();
                        tpo.setOperationId("tpoId");
                        tpo.setActionUrl("htpp:\\ttt.de");
                        tpo.setTechnicalProduct(tp1);
                        mgr.persist(tpo);
                        return tpo;
                    }
                });
        return tpo;
    }

    private OperationParameter createOperationParameter(
            final TechnicalProductOperation tpo) throws Exception {
        OperationParameter result = runTX(new Callable<OperationParameter>() {

            @Override
            public OperationParameter call() throws Exception {
                TechnicalProductOperation read = mgr
                        .getReference(tpo.getClass(), tpo.getKey());
                return TechnicalProducts.addOperationParameter(mgr, read,
                        "PARAM1", false, OperationParameterType.INPUT_STRING);
            }
        });
        return result;
    }

    private PaymentType createPaymentType() throws Exception {
        PaymentType pt = runTX(new Callable<PaymentType>() {
            @Override
            public PaymentType call() throws Exception {
                PSP psp = new PSP();
                psp.setIdentifier("pspID");
                psp.setWsdlUrl("http:\\ttt.com");
                PSP psp1 = (PSP) mgr.find(psp);
                if (psp1 == null) {
                    mgr.persist(psp);
                } else {
                    psp = psp1;
                }
                PaymentType pt = new PaymentType();
                pt.setPaymentTypeId("paymenttypeId");
                pt.setCollectionType(PaymentCollectionType.ORGANIZATION);
                pt.setPsp(psp);
                mgr.persist(pt);
                return pt;
            }
        });
        return pt;
    }

    private Product createProduct(final Organization org,
            final boolean withPriceModel) throws Exception {
        Product product = runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                TechnicalProduct tp = createTechnicalProduct(null);
                Product product = new Product();
                product.setProductId(
                        "testproductId" + System.currentTimeMillis());
                product.setStatus(ServiceStatus.SUSPENDED);
                Organization supplier = org;
                if (supplier == null) {
                    supplier = tp.getOrganization();
                } else {
                    supplier = (Organization) mgr.find(supplier);
                }
                product.setVendor(supplier);
                product.setTechnicalProduct(tp);
                product.setType(ServiceType.TEMPLATE);
                Product product1 = (Product) mgr.find(product);
                if (product1 == null) {
                    mgr.persist(product);
                } else {
                    product = product1;
                }
                if (withPriceModel) {
                    PriceModel pm = createPriceModel(product);
                    pm = mgr.find(pm.getClass(), pm.getKey());
                    product.setPriceModel(pm);
                    mgr.persist(product);
                }
                return product;
            }
        });
        return product;
    }

    private Subscription createSubscription(final Product prod,
            final Organization org) throws Exception {
        Subscription subscription = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                Product product = prod;
                Organization owner = org;
                if (org != null) {
                    owner = (Organization) mgr.find(org);
                } else {
                    owner = createOrganization();
                }
                if (prod != null) {
                    product = (Product) mgr.find(prod);
                } else {
                    product = createProduct(null, true);
                }
                Subscription subscription = new Subscription();
                subscription.setCreationDate(Long.valueOf(16546465000L));
                subscription.setStatus(SubscriptionStatus.PENDING);
                subscription.setSubscriptionId("subscriptionId");
                subscription.setOrganization(owner);
                subscription.setProduct(product);
                subscription.setCutOffDay(1);
                subscription.setUuid(UUID.randomUUID());
                mgr.persist(subscription);
                return subscription;
            }
        });
        return subscription;
    }

    private CatalogEntry createCatalogEntry(final Product prod)
            throws Exception {
        CatalogEntry catEntry = runTX(new Callable<CatalogEntry>() {
            @Override
            public CatalogEntry call() throws Exception {
                Product product = prod;
                if (prod != null) {
                    product = (Product) mgr.find(prod);
                } else {
                    product = createProduct(null, true);
                }
                CatalogEntry catEntry = new CatalogEntry();
                catEntry.setProduct(product);
                mgr.persist(catEntry);
                return catEntry;
            }
        });
        return catEntry;
    }

    private void clear() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mgr.clear();
                return null;
            }
        });
    }
}
