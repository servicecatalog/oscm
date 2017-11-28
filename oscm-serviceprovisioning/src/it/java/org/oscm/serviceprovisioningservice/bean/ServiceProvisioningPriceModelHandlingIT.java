/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 11.10.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import org.junit.Test;
import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.converter.BigDecimalComparator;
import org.oscm.converter.PriceConverter;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.vo.*;
import org.oscm.serviceprovisioningservice.assembler.PriceModelAssembler;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.Numbers;
import org.oscm.test.data.Scenario;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Tests for handling of price model data.
 * 
 * @author Mike J&auml;ger
 */
public class ServiceProvisioningPriceModelHandlingIT extends EJBTestBase {

    private ServiceProvisioningService svcProv;
    private DataService dm;
    private LocalizerServiceLocal localizer;

    @Override
    protected void setup(final TestContainer container) throws Exception {

        container.enableInterfaceMocking(true);
        container.addBean(new DataServiceBean());
        container.addBean(new SessionServiceStub());
        container.addBean(new CommunicationServiceStub());
        container.addBean(new ApplicationServiceStub());
        container.addBean(mock(LocalizerServiceLocal.class));
        container.addBean(new ServiceProvisioningServiceLocalizationBean());
        container.addBean(new ImageResourceServiceStub() {

            @Override
            public ImageResource read(long objectKey, ImageType imageType) {
                return null;
            }
        });
        container.addBean(mock(TenantProvisioningServiceBean.class));
        container.addBean(new TriggerQueueServiceStub());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new TagServiceBean());
        container.addBean(mock(MarketingPermissionServiceLocal.class));
        container.addBean(new ServiceProvisioningServiceBean());

        // data setup
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Scenario.setup(container, true);
                return null;
            }
        });
        container.login(Scenario.getSupplierAdminUser().getKey(),
                ROLE_SERVICE_MANAGER);

        // retrieve required resources
        svcProv = container.get(ServiceProvisioningService.class);
        dm = container.get(DataService.class);
        localizer = container.get(LocalizerServiceLocal.class);
    }

    @Test
    public void testSavePriceModelCreateWithLicense() throws Exception {
        final VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();

        // delete reference to price model
        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Product result = dm.getReference(Product.class,
                        service.getKey());
                result.setPriceModel(null);
                dm.flush();
                return null;
            }
        });
        service.setPriceModel(null);

        priceModel.setType(PriceModelType.FREE_OF_CHARGE);

        final String technicalServiceLicenseDe = "old technical service license DE";
        final String technicalServiceLicenseEn = "old technical service license EN";
        String priceModelLicenseEnNew = "price model license NEW EN";

        priceModel.setLicense(priceModelLicenseEnNew);

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                localizer.storeLocalizedResource("de", service
                        .getTechnicalService().getKey(),
                        LocalizedObjectTypes.PRODUCT_LICENSE_DESC,
                        technicalServiceLicenseDe);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                localizer.storeLocalizedResource("en", service
                        .getTechnicalService().getKey(),
                        LocalizedObjectTypes.PRODUCT_LICENSE_DESC,
                        technicalServiceLicenseEn);
                return null;
            }
        });
        VOServiceDetails updatedService = svcProv.savePriceModel(service,
                priceModel);
        final VOPriceModel savedPriceModel = updatedService.getPriceModel();
        String priceModelLicenseActualDe = runTX(new Callable<String>() {
            @Override
            public String call() {
                return localizer.getLocalizedTextFromDatabase("de",
                        savedPriceModel.getKey(),
                        LocalizedObjectTypes.PRICEMODEL_LICENSE);
            }
        });
        String priceModelLicenseActualEn = runTX(new Callable<String>() {
            @Override
            public String call() {
                return localizer.getLocalizedTextFromDatabase("en",
                        savedPriceModel.getKey(),
                        LocalizedObjectTypes.PRICEMODEL_LICENSE);
            }
        });

        assertEquals("License is wrong. De", technicalServiceLicenseDe,
                priceModelLicenseActualDe);
        assertEquals("License is wrong. En", priceModelLicenseEnNew,
                priceModelLicenseActualEn);

    }

    @Test
    public void testSavePriceModelUpdateWithLicense() throws Exception {
        final VOServiceDetails service = getService();
        final VOPriceModel priceModel = service.getPriceModel();
        priceModel.setType(PriceModelType.FREE_OF_CHARGE);

        final String technicalServiceLicenseDe = "old technical service license DE";
        final String technicalServiceLicenseEn = "old technical service license EN";
        final String priceModelLicenseEnOld = "price model license OLD EN";
        final String priceModelLicenseDeOld = "price model license OLD DE";
        String priceModelLicenseEnNew = "price model license NEW EN";

        priceModel.setLicense(priceModelLicenseEnNew);

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                localizer.storeLocalizedResource("de", service
                        .getTechnicalService().getKey(),
                        LocalizedObjectTypes.PRODUCT_LICENSE_DESC,
                        technicalServiceLicenseDe);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                localizer.storeLocalizedResource("en", service
                        .getTechnicalService().getKey(),
                        LocalizedObjectTypes.PRODUCT_LICENSE_DESC,
                        technicalServiceLicenseEn);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                localizer.storeLocalizedResource("de", priceModel.getKey(),
                        LocalizedObjectTypes.PRICEMODEL_LICENSE,
                        priceModelLicenseDeOld);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                localizer.storeLocalizedResource("en", priceModel.getKey(),
                        LocalizedObjectTypes.PRICEMODEL_LICENSE,
                        priceModelLicenseEnOld);
                return null;
            }
        });
        VOServiceDetails updatedService = svcProv.savePriceModel(service,
                priceModel);
        final VOPriceModel savedPriceModel = updatedService.getPriceModel();
        String priceModelLicenseActualDe = runTX(new Callable<String>() {
            @Override
            public String call() {
                return localizer.getLocalizedTextFromDatabase("de",
                        savedPriceModel.getKey(),
                        LocalizedObjectTypes.PRICEMODEL_LICENSE);
            }
        });
        String priceModelLicenseActualEn = runTX(new Callable<String>() {
            @Override
            public String call() {
                return localizer.getLocalizedTextFromDatabase("en",
                        savedPriceModel.getKey(),
                        LocalizedObjectTypes.PRICEMODEL_LICENSE);
            }
        });

        assertEquals("License is wrong. De", priceModelLicenseDeOld,
                priceModelLicenseActualDe);
        assertEquals("License is wrong. En", priceModelLicenseEnNew,
                priceModelLicenseActualEn);

    }

    @Test
    public void testSavePriceModelForCustomerWithTemplateWithLicense()
            throws Exception {
        final VOServiceDetails service = getService();
        final VOPriceModel priceModel_init = service.getPriceModel();

        final String priceModelLicenseDe = "old price model license DE";
        final String priceModelLicenseEn = "old price model license EN";
        String priceModelLicenseEnNew = "old price model license NEW EN";

        priceModel_init.setLicense(priceModelLicenseEnNew);

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                localizer.storeLocalizedResource("de", service
                        .getTechnicalService().getKey(),
                        LocalizedObjectTypes.PRODUCT_LICENSE_DESC,
                        priceModelLicenseDe);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                localizer.storeLocalizedResource("en", service
                        .getTechnicalService().getKey(),
                        LocalizedObjectTypes.PRODUCT_LICENSE_DESC,
                        priceModelLicenseEn);
                return null;
            }
        });
        VOServiceDetails customerService = svcProv.savePriceModelForCustomer(
                service, priceModel_init, Scenario.getVoSecondCustomer());

        final VOPriceModel customerPriceModel = customerService.getPriceModel();
        final VOPriceModel priceModel = getService().getPriceModel();

        String priceModelLicenseActualDe = runTX(new Callable<String>() {
            @Override
            public String call() {
                return localizer.getLocalizedTextFromDatabase("de",
                        customerPriceModel.getKey(),
                        LocalizedObjectTypes.PRICEMODEL_LICENSE);
            }
        });
        String priceModelLicenseActualEn = runTX(new Callable<String>() {
            @Override
            public String call() {
                return localizer.getLocalizedTextFromDatabase("en",
                        customerPriceModel.getKey(),
                        LocalizedObjectTypes.PRICEMODEL_LICENSE);
            }
        });

        assertFalse(priceModel.getKey() == customerPriceModel.getKey());
        assertEquals("License is wrong. De", priceModelLicenseDe,
                priceModelLicenseActualDe);
        assertEquals("License is wrong. En", priceModelLicenseEnNew,
                priceModelLicenseActualEn);
    }

    @Test
    public void testSavePriceModelForCustomerWithoutTemplateWithLicense()
            throws Exception {
        final VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();

        final String technicalServiceLicenseDe = "old technical service license DE";
        final String technicalServiceLicenseEn = "old technical service license EN";

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                localizer.storeLocalizedResource("de", service
                        .getTechnicalService().getKey(),
                        LocalizedObjectTypes.PRODUCT_LICENSE_DESC,
                        technicalServiceLicenseDe);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                localizer.storeLocalizedResource("en", service
                        .getTechnicalService().getKey(),
                        LocalizedObjectTypes.PRODUCT_LICENSE_DESC,
                        technicalServiceLicenseEn);
                return null;
            }
        });
        String priceModelLicenseEnNew = "old price model license NEW EN";

        priceModel.setLicense(priceModelLicenseEnNew);

        // delete reference to price model
        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Product result = dm.getReference(Product.class,
                        service.getKey());
                result.setPriceModel(null);
                dm.flush();
                return null;
            }
        });
        service.setPriceModel(null);

        VOServiceDetails customerService = svcProv.savePriceModelForCustomer(
                service, priceModel, Scenario.getVoSecondCustomer());

        final VOPriceModel customerPriceModel = customerService.getPriceModel();

        String priceModelLicenseActualDe = runTX(new Callable<String>() {
            @Override
            public String call() {
                return localizer.getLocalizedTextFromDatabase("de",
                        customerPriceModel.getKey(),
                        LocalizedObjectTypes.PRICEMODEL_LICENSE);
            }
        });
        String priceModelLicenseActualEn = runTX(new Callable<String>() {
            @Override
            public String call() {
                return localizer.getLocalizedTextFromDatabase("en",
                        customerPriceModel.getKey(),
                        LocalizedObjectTypes.PRICEMODEL_LICENSE);
            }
        });

        assertEquals("License is wrong. De", technicalServiceLicenseDe,
                priceModelLicenseActualDe);
        assertEquals("License is wrong. En", priceModelLicenseEnNew,
                priceModelLicenseActualEn);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testDeletePriceModelForCustomer_Chargeable() throws Exception {
        final VOServiceDetails service = getService();
        final VOPriceModel priceModel = service.getPriceModel();

        // localized strings for the customer price model
        final String licenseEn = "licenseEn";
        final String licenseDe = "licenseDe";
        final String licenseJa = "licenseJa";
        final String descriptionEn = "descriptionEn";
        final String descriptionDe = "descriptionDe";
        final String descriptionJa = "descriptionJa";

        List<VOLocalizedText> descriptions = getLocalizedText(descriptionEn,
                descriptionDe, descriptionJa);
        List<VOLocalizedText> licenses = getLocalizedText(licenseEn, licenseDe,
                licenseJa);

        VOPriceModelLocalization localization = getLocalizationForPriceModel(
                descriptions, licenses);

        // delete reference to price model
        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Product result = dm.getReference(Product.class,
                        service.getKey());
                result.setPriceModel(null);
                dm.flush();
                return null;
            }
        });
        service.setPriceModel(null);

        // create customer service with price model
        VOServiceDetails customerService = svcProv.savePriceModelForCustomer(
                service, priceModel, Scenario.getVoSecondCustomer());

        VOPriceModel customerPriceModel = customerService.getPriceModel();
        customerPriceModel.setType(PriceModelType.PRO_RATA);

        // save price model localization
        svcProv.savePriceModelLocalization(customerPriceModel, localization);

        // get localization for price model
        VOPriceModelLocalization localizationDB = svcProv
                .getPriceModelLocalization(customerPriceModel);
        List<VOLocalizedText> descriptionsDB = localizationDB.getDescriptions();
        List<VOLocalizedText> licensesDB = localizationDB.getLicenses();

        // verify localized resources for the customer price model set
        verifyLocalizedText(descriptions, descriptionsDB);
        verifyLocalizedText(licenses, licensesDB);

        // delete the customer service
        svcProv.deleteService(customerService);

        // get localization for price model, object not found expected
        svcProv.getPriceModelLocalization(customerPriceModel);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testDeletePriceModelForCustomer_NotChargeable()
            throws Exception {
        final VOServiceDetails service = getService();
        final VOPriceModel priceModel = service.getPriceModel();

        // localized strings for the customer price model
        final String licenseEn = "licenseEn";
        final String licenseDe = "licenseDe";
        final String licenseJa = "licenseJa";
        final String descriptionEn = "descriptionEn";
        final String descriptionDe = "descriptionDe";
        final String descriptionJa = "descriptionJa";

        List<VOLocalizedText> descriptions = getLocalizedText(descriptionEn,
                descriptionDe, descriptionJa);
        List<VOLocalizedText> licenses = getLocalizedText(licenseEn, licenseDe,
                licenseJa);

        VOPriceModelLocalization localization = getLocalizationForPriceModel(
                descriptions, licenses);

        // delete reference to price model
        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Product result = dm.getReference(Product.class,
                        service.getKey());
                result.setPriceModel(null);
                dm.flush();
                return null;
            }
        });
        service.setPriceModel(null);

        // create customer service with price model
        VOServiceDetails customerService = svcProv.savePriceModelForCustomer(
                service, priceModel, Scenario.getVoSecondCustomer());

        VOPriceModel customerPriceModel = customerService.getPriceModel();
        customerPriceModel.setType(PriceModelType.FREE_OF_CHARGE);

        // save price model localization
        svcProv.savePriceModelLocalization(customerPriceModel, localization);

        // get localization for price model
        VOPriceModelLocalization localizationDB = svcProv
                .getPriceModelLocalization(customerPriceModel);
        List<VOLocalizedText> descriptionsDB = localizationDB.getDescriptions();
        List<VOLocalizedText> licensesDB = localizationDB.getLicenses();

        // verify localized resources for the customer price model set
        assertTrue(descriptionsDB.isEmpty());
        verifyLocalizedText(licenses, licensesDB);

        // delete the customer service
        svcProv.deleteService(customerService);

        // get localization for price model, object not found expected
        svcProv.getPriceModelLocalization(customerPriceModel);
    }

    @Test
    public void testDeletePriceModelForCustomerTemplate_AllLocalizedResources()
            throws Exception {
        final VOServiceDetails service = getService();
        final VOPriceModel priceModel = service.getPriceModel();

        // localized strings for the customer price model
        final String licenseEn = "licenseEn";
        final String licenseDe = "licenseDe";
        final String licenseJa = "licenseJa";
        final String descriptionEn = "descriptionEn";
        final String descriptionDe = "descriptionDe";
        final String descriptionJa = "descriptionJa";
        String empty = "";

        // delete reference to price model
        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Product result = dm.getReference(Product.class,
                        service.getKey());
                result.setPriceModel(null);
                dm.flush();
                return null;
            }
        });
        service.setPriceModel(null);

        // create customer service with price model
        VOServiceDetails customerService = svcProv.savePriceModelForCustomer(
                service, priceModel, Scenario.getVoSecondCustomer());

        final VOPriceModel customerPriceModel = customerService.getPriceModel();

        // set localized resources for the customer price model
        setLocalizedPriceModel(LocalizedObjectTypes.PRICEMODEL_LICENSE,
                customerPriceModel.getKey(), licenseEn, licenseDe, licenseJa);
        setLocalizedPriceModel(LocalizedObjectTypes.PRICEMODEL_DESCRIPTION,
                customerPriceModel.getKey(), descriptionEn, descriptionDe,
                descriptionJa);

        // verify localized resources for the customer price model set
        verifyLocalizedPriceModel(LocalizedObjectTypes.PRICEMODEL_LICENSE,
                customerPriceModel.getKey(), licenseEn, licenseDe, licenseJa);
        verifyLocalizedPriceModel(LocalizedObjectTypes.PRICEMODEL_DESCRIPTION,
                customerPriceModel.getKey(), descriptionEn, descriptionDe,
                descriptionJa);

        // delete the template service (get it again, version is changed -
        // previous modify)
        VOService service1 = getService();
        svcProv.deleteService(service1);

        // verify if localized resources also deleted
        verifyLocalizedPriceModel(LocalizedObjectTypes.PRICEMODEL_LICENSE,
                customerPriceModel.getKey(), empty, empty, empty);
        verifyLocalizedPriceModel(LocalizedObjectTypes.PRICEMODEL_DESCRIPTION,
                customerPriceModel.getKey(), empty, empty, empty);
    }

    @Test
    public void testDeletePriceModelForCustomer_AllLocalizedResources()
            throws Exception {
        final VOServiceDetails service = getService();
        final VOPriceModel priceModel = service.getPriceModel();

        // localized strings for the customer price model
        final String licenseEn = "licenseEn";
        final String licenseDe = "licenseDe";
        final String licenseJa = "licenseJa";
        final String descriptionEn = "descriptionEn";
        final String descriptionDe = "descriptionDe";
        final String descriptionJa = "descriptionJa";
        String empty = "";

        // delete reference to price model
        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Product result = dm.getReference(Product.class,
                        service.getKey());
                result.setPriceModel(null);
                dm.flush();
                return null;
            }
        });
        service.setPriceModel(null);

        // create customer service with price model
        VOServiceDetails customerService = svcProv.savePriceModelForCustomer(
                service, priceModel, Scenario.getVoSecondCustomer());

        final VOPriceModel customerPriceModel = customerService.getPriceModel();

        // set localized resources for the customer price model
        setLocalizedPriceModel(LocalizedObjectTypes.PRICEMODEL_LICENSE,
                customerPriceModel.getKey(), licenseEn, licenseDe, licenseJa);
        setLocalizedPriceModel(LocalizedObjectTypes.PRICEMODEL_DESCRIPTION,
                customerPriceModel.getKey(), descriptionEn, descriptionDe,
                descriptionJa);

        // verify localized resources for the customer price model set
        verifyLocalizedPriceModel(LocalizedObjectTypes.PRICEMODEL_LICENSE,
                customerPriceModel.getKey(), licenseEn, licenseDe, licenseJa);
        verifyLocalizedPriceModel(LocalizedObjectTypes.PRICEMODEL_DESCRIPTION,
                customerPriceModel.getKey(), descriptionEn, descriptionDe,
                descriptionJa);

        // delete the customer service
        svcProv.deleteService(customerService);

        // verify if localized resources also deleted
        verifyLocalizedPriceModel(LocalizedObjectTypes.PRICEMODEL_LICENSE,
                customerPriceModel.getKey(), empty, empty, empty);
        verifyLocalizedPriceModel(LocalizedObjectTypes.PRICEMODEL_DESCRIPTION,
                customerPriceModel.getKey(), empty, empty, empty);
    }

    private List<VOLocalizedText> getLocalizedText(String en, String de,
            String ja) {
        List<VOLocalizedText> localizedText = new ArrayList<VOLocalizedText>();
        VOLocalizedText textEn = new VOLocalizedText();
        textEn.setVersion(0);
        textEn.setText(en);
        textEn.setLocale("en");
        localizedText.add(textEn);
        VOLocalizedText textDe = new VOLocalizedText();
        textDe.setVersion(0);
        textDe.setText(de);
        textDe.setLocale("de");
        localizedText.add(textDe);
        VOLocalizedText textJa = new VOLocalizedText();
        textJa.setVersion(0);
        textJa.setText(ja);
        textJa.setLocale("ja");
        localizedText.add(textJa);
        return localizedText;
    }

    private VOPriceModelLocalization getLocalizationForPriceModel(
            List<VOLocalizedText> descriptions, List<VOLocalizedText> licenses) {
        VOPriceModelLocalization localization = new VOPriceModelLocalization();
        localization.setDescriptions(descriptions);
        localization.setLicenses(licenses);
        return localization;
    }

    private void verifyLocalizedText(List<VOLocalizedText> text1,
            List<VOLocalizedText> text2) {
        assertEquals(text1.size(), text2.size());
        for (VOLocalizedText iter1 : text1) {
            for (VOLocalizedText iter2 : text2) {
                if (iter1.getLocale().equals(iter2.getLocale()))
                    assertEquals(iter1.getText(), iter2.getText());
            }
        }
    }

    private void setLocalizedPriceModel(final LocalizedObjectTypes objectType,
            final long objectKey, final String en, final String de,
            final String ja) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                localizer.storeLocalizedResource("en", objectKey, objectType,
                        en);
                localizer.storeLocalizedResource("de", objectKey, objectType,
                        de);
                localizer.storeLocalizedResource("ja", objectKey, objectType,
                        ja);
                return null;
            }
        });
    }

    private void verifyLocalizedPriceModel(
            final LocalizedObjectTypes objectType, final long objectKey,
            String en, String de, String ja) throws Exception {
        String fromDatabase;

        fromDatabase = runTX(new Callable<String>() {
            @Override
            public String call() {
                return localizer.getLocalizedTextFromDatabase("de", objectKey,
                        objectType);
            }
        });
        assertEquals(de, fromDatabase);

        fromDatabase = runTX(new Callable<String>() {
            @Override
            public String call() {
                return localizer.getLocalizedTextFromDatabase("en", objectKey,
                        objectType);
            }
        });
        assertEquals(en, fromDatabase);

        fromDatabase = runTX(new Callable<String>() {
            @Override
            public String call() {
                return localizer.getLocalizedTextFromDatabase("ja", objectKey,
                        objectType);
            }
        });
        assertEquals(ja, fromDatabase);
    }

    @Test
    public void testSavePriceModelCreation_WithSteppedPrices() throws Exception {
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        priceModel.setPricePerUserAssignment(BigDecimal.valueOf(987L));
        priceModel.getSteppedPrices().clear();

        VOSteppedPrice step1 = new VOSteppedPrice();
        step1.setLimit(Long.valueOf(5L));
        step1.setPrice(Numbers.BD10);
        VOSteppedPrice step2 = new VOSteppedPrice();
        step2.setLimit(null);
        step2.setPrice(Numbers.BD5);

        List<VOSteppedPrice> steppedPrices = Arrays.asList(step2, step1);
        priceModel.setSteppedPrices(steppedPrices);
        priceModel.setPricePerUserAssignment(BigDecimal.ZERO);
        priceModel.getConsideredEvents().get(0).setSteppedPrices(steppedPrices);
        priceModel.getConsideredEvents().get(0).setEventPrice(BigDecimal.ZERO);
        priceModel.getSelectedParameters().get(0)
                .setSteppedPrices(steppedPrices);
        priceModel.getSelectedParameters().get(0)
                .setPricePerSubscription(BigDecimal.ZERO);

        final VOServiceDetails customerService = svcProv
                .savePriceModelForCustomer(service, priceModel,
                        Scenario.getVoSecondCustomer());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PriceModel pm = dm.getReference(PriceModel.class,
                        customerService.getPriceModel().getKey());
                List<SteppedPrice> steppedPricesForPriceModel = pm
                        .getSteppedPrices();
                validateSteppedPrices(steppedPricesForPriceModel);
                validateSteppedPrices(pm.getConsideredEvents().get(0)
                        .getSteppedPrices());
                validateSteppedPrices(pm.getSelectedParameters().get(0)
                        .getSteppedPrices());
                return null;
            }

            private void validateSteppedPrices(
                    List<SteppedPrice> steppedPricesForPriceModel) {
                assertEquals(2, steppedPricesForPriceModel.size());
                SteppedPrice steppedPrice = steppedPricesForPriceModel.get(0);
                assertEquals(
                        Numbers.BD50
                                .setScale(PriceConverter.NORMALIZED_PRICE_SCALING),
                        steppedPrice.getAdditionalPrice());
                assertEquals(5L, steppedPrice.getFreeEntityCount());
                assertNull(steppedPrice.getLimit());
                steppedPrice = steppedPricesForPriceModel.get(1);
                assertEquals(
                        BigDecimal.ZERO
                                .setScale(PriceConverter.NORMALIZED_PRICE_SCALING),
                        steppedPrice.getAdditionalPrice());
                assertEquals(0L, steppedPrice.getFreeEntityCount());
                assertEquals(5L, steppedPrice.getLimit().longValue());
            }
        });

    }

    @Test
    public void testSavePriceModelCreation() throws Exception {
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        priceModel.setPricePerUserAssignment(BigDecimal.valueOf(987L));

        VOServiceDetails customerService = svcProv.savePriceModelForCustomer(
                service, priceModel, Scenario.getVoSecondCustomer());
        VOPriceModel customerPriceModel = customerService.getPriceModel();
        service = getService();
        priceModel = service.getPriceModel();

        assertFalse(priceModel.getKey() == customerPriceModel.getKey());
        assertEquals(new BigDecimal("34567.00"),
                priceModel.getPricePerUserAssignment());
        assertEquals(BigDecimal.valueOf(987),
                customerPriceModel.getPricePerUserAssignment());
        // as one parameter definition is of type string, only three priced
        // parameters may exist
        assertEquals(4, customerPriceModel.getSelectedParameters().size());
    }

    @Test(expected = ServiceOperationException.class)
    public void testSavePriceModelUpdate() throws Exception {
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        priceModel.setPricePerUserAssignment(BigDecimal.valueOf(987L));

        svcProv.savePriceModelForCustomer(service, priceModel,
                Scenario.getVoCustomer());
    }

    @Test
    public void testSavePriceModelForEventCreation() throws Exception {
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        VOPricedEvent voPricedEvent = priceModel.getConsideredEvents().get(0);
        voPricedEvent.setEventPrice(BigDecimal.valueOf(4444444L));

        VOServiceDetails customerService = svcProv.savePriceModelForCustomer(
                service, priceModel, Scenario.getVoSecondCustomer());
        VOPriceModel customerPriceModel = customerService.getPriceModel();
        service = getService();
        priceModel = service.getPriceModel();
        voPricedEvent = priceModel.getConsideredEvents().get(0);

        assertFalse(voPricedEvent.getKey() == customerPriceModel
                .getConsideredEvents().get(0).getKey());
        assertEquals(new BigDecimal("1111.00"), voPricedEvent.getEventPrice());
        assertEquals(BigDecimal.valueOf(4444444), customerPriceModel
                .getConsideredEvents().get(0).getEventPrice());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSavePriceModelForEventUpdate() throws Exception {
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        VOPricedEvent voPricedEvent = priceModel.getConsideredEvents().get(0);
        voPricedEvent.setEventPrice(BigDecimal.valueOf(4444444L));

        VOServiceDetails custCopy = svcProv.savePriceModelForCustomer(service,
                priceModel, Scenario.getVoSecondCustomer());

        service = getService();
        voPricedEvent = service.getPriceModel().getConsideredEvents().get(0);

        VOPricedEvent copiedEvent = custCopy.getPriceModel()
                .getConsideredEvents().get(0);
        copiedEvent.setKey(voPricedEvent.getKey());
        copiedEvent.setEventPrice(BigDecimal.valueOf(12345L));
        svcProv.savePriceModelForCustomer(custCopy, custCopy.getPriceModel(),
                Scenario.getVoSecondCustomer());
    }

    @Test
    public void testSavePriceModelForParameterCreation() throws Exception {
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        VOPricedParameter voPricedParameter = priceModel
                .getSelectedParameters().get(0);
        voPricedParameter.setPricePerUser(BigDecimal.valueOf(987654L));

        VOServiceDetails customerService = svcProv.savePriceModelForCustomer(
                service, priceModel, Scenario.getVoSecondCustomer());
        VOPriceModel customerPriceModel = customerService.getPriceModel();
        service = getService();
        priceModel = service.getPriceModel();
        voPricedParameter = priceModel.getSelectedParameters().get(0);

        assertFalse(voPricedParameter.getKey() == customerPriceModel
                .getSelectedParameters().get(0).getKey());
        assertEquals(new BigDecimal("111.00"),
                voPricedParameter.getPricePerUser());
        assertEquals(BigDecimal.valueOf(987654L), customerPriceModel
                .getSelectedParameters().get(0).getPricePerUser());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSavePriceModelForParameterUpdate() throws Exception {
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        VOPricedParameter voPricedParameter = priceModel
                .getSelectedParameters().get(0);
        voPricedParameter.setPricePerUser(BigDecimal.valueOf(987654L));

        VOServiceDetails custCopy = svcProv.savePriceModelForCustomer(service,
                priceModel, Scenario.getVoSecondCustomer());

        service = getService();
        voPricedParameter = service.getPriceModel().getSelectedParameters()
                .get(0);

        VOPricedParameter copiedParam = custCopy.getPriceModel()
                .getSelectedParameters().get(0);
        copiedParam.setKey(voPricedParameter.getKey());
        copiedParam.setPricePerUser(BigDecimal.valueOf(54321L));
        svcProv.savePriceModelForCustomer(custCopy, custCopy.getPriceModel(),
                Scenario.getVoSecondCustomer());
    }

    @Test
    public void testSavePriceModelForParameterOptionCreation() throws Exception {
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        VOPricedOption voPricedOption = priceModel.getSelectedParameters()
                .get(2).getPricedOptions().get(0);
        voPricedOption.setPricePerUser(BigDecimal.valueOf(765432L));

        VOServiceDetails customerService = svcProv.savePriceModelForCustomer(
                service, priceModel, Scenario.getVoSecondCustomer());
        VOPriceModel customerPriceModel = customerService.getPriceModel();
        service = getService();
        priceModel = service.getPriceModel();
        voPricedOption = priceModel.getSelectedParameters().get(2)
                .getPricedOptions().get(0);

        assertFalse(voPricedOption.getKey() == customerPriceModel
                .getSelectedParameters().get(2).getPricedOptions().get(0)
                .getKey());
        assertEquals(new BigDecimal("111.00"), voPricedOption.getPricePerUser());
        assertEquals(BigDecimal.valueOf(765432L), customerPriceModel
                .getSelectedParameters().get(2).getPricedOptions().get(0)
                .getPricePerUser());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSavePriceModelForParameterOptionUpdate() throws Exception {
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        VOPricedOption voPricedOption = priceModel.getSelectedParameters()
                .get(2).getPricedOptions().get(0);
        voPricedOption.setPricePerUser(BigDecimal.valueOf(765432L));

        VOServiceDetails custCopy = svcProv.savePriceModelForCustomer(service,
                priceModel, Scenario.getVoSecondCustomer());

        service = getService();
        voPricedOption = service.getPriceModel().getSelectedParameters().get(2)
                .getPricedOptions().get(0);

        VOPricedOption copiedOption = custCopy.getPriceModel()
                .getSelectedParameters().get(2).getPricedOptions().get(0);
        copiedOption.setKey(voPricedOption.getKey());
        copiedOption.setPricePerUser(BigDecimal.valueOf(54321L));
        svcProv.savePriceModelForCustomer(custCopy, custCopy.getPriceModel(),
                Scenario.getVoSecondCustomer());
    }

    @Test
    public void testSavePriceModelForRolePriceOnPriceModelCreation()
            throws Exception {
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        VOPricedRole voPricedRole = priceModel.getRoleSpecificUserPrices().get(
                0);
        voPricedRole.setPricePerUser(BigDecimal.valueOf(13579L));

        VOServiceDetails customerService = svcProv.savePriceModelForCustomer(
                service, priceModel, Scenario.getVoSecondCustomer());
        VOPriceModel customerPriceModel = customerService.getPriceModel();
        service = getService();
        priceModel = service.getPriceModel();
        voPricedRole = priceModel.getRoleSpecificUserPrices().get(0);

        assertFalse(voPricedRole.getKey() == customerPriceModel
                .getRoleSpecificUserPrices().get(0).getKey());
        assertEquals(BigDecimal.valueOf(1), voPricedRole.getPricePerUser());
        assertEquals(BigDecimal.valueOf(13579L), customerPriceModel
                .getRoleSpecificUserPrices().get(0).getPricePerUser());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSavePriceModelForRolePriceOnPriceModelUpdate()
            throws Exception {
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        VOPricedRole voPricedRole = priceModel.getRoleSpecificUserPrices().get(
                0);
        voPricedRole.setPricePerUser(BigDecimal.valueOf(13579L));

        VOServiceDetails custCopy = svcProv.savePriceModelForCustomer(service,
                priceModel, Scenario.getVoSecondCustomer());

        service = getService();
        voPricedRole = service.getPriceModel().getRoleSpecificUserPrices()
                .get(0);

        VOPricedRole copiedRolePrice = custCopy.getPriceModel()
                .getRoleSpecificUserPrices().get(0);
        copiedRolePrice.setKey(voPricedRole.getKey());
        copiedRolePrice.setPricePerUser(BigDecimal.valueOf(54321L));
        svcProv.savePriceModelForCustomer(custCopy, custCopy.getPriceModel(),
                Scenario.getVoSecondCustomer());
    }

    @Test
    public void testSavePriceModelForRolePriceOnPricedParameterCreation()
            throws Exception {
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        VOPricedRole voPricedRole = priceModel.getSelectedParameters().get(0)
                .getRoleSpecificUserPrices().get(0);
        voPricedRole.setPricePerUser(BigDecimal.valueOf(13579L));

        VOServiceDetails customerService = svcProv.savePriceModelForCustomer(
                service, priceModel, Scenario.getVoSecondCustomer());
        VOPriceModel customerPriceModel = customerService.getPriceModel();
        service = getService();
        priceModel = service.getPriceModel();
        voPricedRole = priceModel.getSelectedParameters().get(0)
                .getRoleSpecificUserPrices().get(0);

        assertFalse(voPricedRole.getKey() == customerPriceModel
                .getSelectedParameters().get(0).getRoleSpecificUserPrices()
                .get(0).getKey());
        assertEquals(new BigDecimal("2.00"), voPricedRole.getPricePerUser());
        assertEquals(BigDecimal.valueOf(13579L), customerPriceModel
                .getSelectedParameters().get(0).getRoleSpecificUserPrices()
                .get(0).getPricePerUser());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSavePriceModelForRolePriceOnPricedParameterUpdate()
            throws Exception {
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        VOPricedRole voPricedRole = priceModel.getSelectedParameters().get(0)
                .getRoleSpecificUserPrices().get(0);
        voPricedRole.setPricePerUser(BigDecimal.valueOf(13579L));

        VOServiceDetails custCopy = svcProv.savePriceModelForCustomer(service,
                priceModel, Scenario.getVoSecondCustomer());

        service = getService();
        voPricedRole = service.getPriceModel().getSelectedParameters().get(0)
                .getRoleSpecificUserPrices().get(0);

        VOPricedRole copiedRolePrice = custCopy.getPriceModel()
                .getSelectedParameters().get(0).getRoleSpecificUserPrices()
                .get(0);
        copiedRolePrice.setKey(voPricedRole.getKey());
        copiedRolePrice.setPricePerUser(BigDecimal.valueOf(54321L));
        svcProv.savePriceModelForCustomer(custCopy, custCopy.getPriceModel(),
                Scenario.getVoSecondCustomer());
    }

    @Test
    public void testSavePriceModelForRolePriceOnPricedOptionCreation()
            throws Exception {
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        VOPricedRole voPricedRole = priceModel.getSelectedParameters().get(2)
                .getPricedOptions().get(0).getRoleSpecificUserPrices().get(0);
        voPricedRole.setPricePerUser(BigDecimal.valueOf(13579L));

        VOServiceDetails customerService = svcProv.savePriceModelForCustomer(
                service, priceModel, Scenario.getVoSecondCustomer());
        VOPriceModel customerPriceModel = customerService.getPriceModel();
        service = getService();
        priceModel = service.getPriceModel();
        voPricedRole = priceModel.getSelectedParameters().get(2)
                .getPricedOptions().get(0).getRoleSpecificUserPrices().get(0);

        assertFalse(voPricedRole.getKey() == customerPriceModel
                .getSelectedParameters().get(2).getPricedOptions().get(0)
                .getRoleSpecificUserPrices().get(0).getKey());
        assertEquals(new BigDecimal("3.00"), voPricedRole.getPricePerUser());
        assertEquals(BigDecimal.valueOf(13579L), customerPriceModel
                .getSelectedParameters().get(2).getPricedOptions().get(0)
                .getRoleSpecificUserPrices().get(0).getPricePerUser());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSavePriceModelForRolePriceOnPricedOptionUpdate()
            throws Exception {
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        VOPricedRole voPricedRole = priceModel.getSelectedParameters().get(2)
                .getPricedOptions().get(0).getRoleSpecificUserPrices().get(0);
        voPricedRole.setPricePerUser(BigDecimal.valueOf(13579L));

        VOServiceDetails custCopy = svcProv.savePriceModelForCustomer(service,
                priceModel, Scenario.getVoSecondCustomer());

        service = getService();
        voPricedRole = service.getPriceModel().getSelectedParameters().get(2)
                .getPricedOptions().get(0).getRoleSpecificUserPrices().get(0);

        VOPricedRole copiedRolePrice = custCopy.getPriceModel()
                .getSelectedParameters().get(2).getPricedOptions().get(0)
                .getRoleSpecificUserPrices().get(0);
        copiedRolePrice.setKey(voPricedRole.getKey());
        copiedRolePrice.setPricePerUser(BigDecimal.valueOf(54321L));
        svcProv.savePriceModelForCustomer(custCopy, custCopy.getPriceModel(),
                Scenario.getVoSecondCustomer());
    }

    @Test
    public void testSavePriceModelForSteppedPricesOnPriceModelCreation()
            throws Exception {
        addSteppedPrices();
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        VOSteppedPrice voSteppedPrice = priceModel.getSteppedPrices().get(0);
        voSteppedPrice.setPrice(BigDecimal.valueOf(13579L));

        VOServiceDetails customerService = svcProv.savePriceModelForCustomer(
                service, priceModel, Scenario.getVoSecondCustomer());
        VOPriceModel customerPriceModel = customerService.getPriceModel();
        service = getService();
        priceModel = service.getPriceModel();
        voSteppedPrice = priceModel.getSteppedPrices().get(0);

        assertFalse(voSteppedPrice.getKey() == customerPriceModel
                .getSteppedPrices().get(0).getKey());
        assertEquals(new BigDecimal("50.00"), voSteppedPrice.getPrice());
        assertEquals(BigDecimal.valueOf(13579L), customerPriceModel
                .getSteppedPrices().get(0).getPrice());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSavePriceModelForSteppedPricesOnPriceModelUpdate()
            throws Exception {
        addSteppedPrices();
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        VOSteppedPrice voSteppedPrice = priceModel.getSteppedPrices().get(0);
        voSteppedPrice.setPrice(BigDecimal.valueOf(13579L));

        VOServiceDetails custCopy = svcProv.savePriceModelForCustomer(service,
                priceModel, Scenario.getVoSecondCustomer());

        service = getService();
        voSteppedPrice = service.getPriceModel().getSteppedPrices().get(0);

        VOSteppedPrice copiedSteppedPrice = custCopy.getPriceModel()
                .getSteppedPrices().get(0);
        copiedSteppedPrice.setKey(voSteppedPrice.getKey());
        copiedSteppedPrice.setPrice(BigDecimal.valueOf(54321L));
        svcProv.savePriceModelForCustomer(custCopy, custCopy.getPriceModel(),
                Scenario.getVoSecondCustomer());
    }

    @Test
    public void testSavePriceModelForSteppedPricesOnPricedParameterCreation()
            throws Exception {
        addSteppedPrices();
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        VOSteppedPrice voSteppedPrice = priceModel.getSelectedParameters()
                .get(0).getSteppedPrices().get(0);
        voSteppedPrice.setPrice(BigDecimal.valueOf(13579L));

        VOServiceDetails customerService = svcProv.savePriceModelForCustomer(
                service, priceModel, Scenario.getVoSecondCustomer());
        VOPriceModel customerPriceModel = customerService.getPriceModel();
        service = getService();
        priceModel = service.getPriceModel();
        voSteppedPrice = priceModel.getSelectedParameters().get(0)
                .getSteppedPrices().get(0);

        assertFalse(voSteppedPrice.getKey() == customerPriceModel
                .getSelectedParameters().get(0).getSteppedPrices().get(0)
                .getKey());
        assertEquals(new BigDecimal("52.00"), voSteppedPrice.getPrice());
        assertEquals(BigDecimal.valueOf(13579L), customerPriceModel
                .getSelectedParameters().get(0).getSteppedPrices().get(0)
                .getPrice());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSavePriceModelForSteppedPricesOnPricedParameterUpdate()
            throws Exception {
        addSteppedPrices();
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        VOSteppedPrice voSteppedPrice = priceModel.getSelectedParameters()
                .get(0).getSteppedPrices().get(0);
        voSteppedPrice.setPrice(BigDecimal.valueOf(13579L));

        VOServiceDetails custCopy = svcProv.savePriceModelForCustomer(service,
                priceModel, Scenario.getVoSecondCustomer());

        service = getService();
        voSteppedPrice = service.getPriceModel().getSelectedParameters().get(0)
                .getSteppedPrices().get(0);

        VOSteppedPrice copiedSteppedPrice = custCopy.getPriceModel()
                .getSelectedParameters().get(0).getSteppedPrices().get(0);
        copiedSteppedPrice.setKey(voSteppedPrice.getKey());
        copiedSteppedPrice.setPrice(BigDecimal.valueOf(54321L));
        svcProv.savePriceModelForCustomer(custCopy, custCopy.getPriceModel(),
                Scenario.getVoSecondCustomer());
    }

    @Test
    public void testSavePriceModelForSteppedPricesOnPricedEventCreation()
            throws Exception {
        addSteppedPrices();
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        VOSteppedPrice voSteppedPrice = priceModel.getConsideredEvents().get(0)
                .getSteppedPrices().get(0);
        voSteppedPrice.setPrice(BigDecimal.valueOf(13579L));

        VOServiceDetails customerService = svcProv.savePriceModelForCustomer(
                service, priceModel, Scenario.getVoSecondCustomer());
        VOPriceModel customerPriceModel = customerService.getPriceModel();
        service = getService();
        priceModel = service.getPriceModel();
        voSteppedPrice = priceModel.getConsideredEvents().get(0)
                .getSteppedPrices().get(0);

        assertFalse(voSteppedPrice.getKey() == customerPriceModel
                .getConsideredEvents().get(0).getSteppedPrices().get(0)
                .getKey());
        assertEquals(new BigDecimal("51.00"), voSteppedPrice.getPrice());
        assertEquals(BigDecimal.valueOf(13579L), customerPriceModel
                .getConsideredEvents().get(0).getSteppedPrices().get(0)
                .getPrice());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSavePriceModelForSteppedPricesOnPricedEventUpdate()
            throws Exception {
        addSteppedPrices();
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        VOSteppedPrice voSteppedPrice = priceModel.getConsideredEvents().get(0)
                .getSteppedPrices().get(0);
        voSteppedPrice.setPrice(BigDecimal.valueOf(13579L));

        VOServiceDetails custCopy = svcProv.savePriceModelForCustomer(service,
                priceModel, Scenario.getVoSecondCustomer());

        service = getService();
        voSteppedPrice = service.getPriceModel().getConsideredEvents().get(0)
                .getSteppedPrices().get(0);

        VOSteppedPrice copiedSteppedPrice = custCopy.getPriceModel()
                .getConsideredEvents().get(0).getSteppedPrices().get(0);
        copiedSteppedPrice.setKey(voSteppedPrice.getKey());
        copiedSteppedPrice.setPrice(BigDecimal.valueOf(54321L));
        svcProv.savePriceModelForCustomer(custCopy, custCopy.getPriceModel(),
                Scenario.getVoSecondCustomer());
    }

    // refers to Bug 6629
    @Test
    public void testSavePriceModel_SetNonChargeable() throws Exception {
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        priceModel.setType(PriceModelType.FREE_OF_CHARGE);
        VOServiceDetails updatedService = svcProv.savePriceModel(service,
                priceModel);

        final List<BaseVO> obsoletePriceModelElements = new ArrayList<BaseVO>();
        List<VOPricedEvent> consideredEvents = priceModel.getConsideredEvents();
        List<VOPricedParameter> selectedParameters = priceModel
                .getSelectedParameters();
        obsoletePriceModelElements.addAll(consideredEvents);
        for (VOPricedEvent pEvt : consideredEvents) {
            obsoletePriceModelElements.addAll(pEvt.getSteppedPrices());
        }
        obsoletePriceModelElements.addAll(selectedParameters);
        for (VOPricedParameter pricedParam : selectedParameters) {
            obsoletePriceModelElements.addAll(pricedParam
                    .getRoleSpecificUserPrices());
            obsoletePriceModelElements.addAll(pricedParam.getSteppedPrices());
            List<VOPricedOption> pricedOptions = pricedParam.getPricedOptions();
            obsoletePriceModelElements.addAll(pricedOptions);
            for (VOPricedOption option : pricedOptions) {
                obsoletePriceModelElements.addAll(option
                        .getRoleSpecificUserPrices());
            }
        }
        obsoletePriceModelElements.addAll(priceModel
                .getRoleSpecificUserPrices());
        obsoletePriceModelElements.addAll(priceModel.getSteppedPrices());

        // now ensure that the price model is set to non-chargeable, and that
        // all events, parameters, role and stepped prices have been removed and
        // the primitive members of the price model are 0, currency must be
        // unset
        priceModel = updatedService.getPriceModel();
        assertFalse(priceModel.isChargeable());
        assertNull("no currency must be set", priceModel.getCurrencyISOCode());
        assertTrue(BigDecimalComparator.isZero(priceModel.getOneTimeFee()));
        assertTrue(BigDecimalComparator.isZero(priceModel.getPricePerPeriod()));
        assertTrue(BigDecimalComparator.isZero(priceModel
                .getPricePerUserAssignment()));
        assertTrue(priceModel.getConsideredEvents().isEmpty());
        assertTrue(priceModel.getRoleSpecificUserPrices().isEmpty());
        assertTrue(priceModel.getSelectedParameters().isEmpty());
        assertTrue(priceModel.getSteppedPrices().isEmpty());

        // for every event and parameter and option verify the deletion of
        // the stepped prices and role prices
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                long timestamp = 0;
                for (BaseVO currentElement : obsoletePriceModelElements) {
                    Class<? extends DomainObject<?>> targetClass = getTargetClass(currentElement);
                    DomainObject<?> doInstance = targetClass.getConstructor()
                            .newInstance();
                    doInstance.setKey(currentElement.getKey());
                    List<DomainHistoryObject<?>> hist = dm
                            .findHistory(doInstance);
                    DomainHistoryObject<?> lastHistoryEntry = hist.get(hist
                            .size() - 1);
                    assertEquals("Wrong modification type for type: "
                            + lastHistoryEntry.getClass().getSimpleName(),
                            ModificationType.DELETE,
                            lastHistoryEntry.getModtype());
                    if (timestamp == 0) {
                        timestamp = lastHistoryEntry.getModdate().getTime();
                    }
                    assertEquals("Wrong timestamp for deletion for type: "
                            + lastHistoryEntry.getClass().getSimpleName(),
                            timestamp, lastHistoryEntry.getModdate().getTime());
                }
                return null;
            }
        });
    }

    @Test(expected = PriceModelException.class)
    public void testSavePriceModelForSubscription_FreeToChargeable()
            throws Exception {
        long suppAdminKey = Scenario.getSupplierAdminUser().getKey();
        container.login(suppAdminKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails service = new VOServiceDetails();
        service.setKey(Scenario.getProduct().getKey());
        service = svcProv.getServiceDetails(service);

        service = svcProv.copyService(service, "copiedService");
        VOPriceModel priceModel = service.getPriceModel();
        priceModel.setType(PriceModelType.FREE_OF_CHARGE);
        service = svcProv.savePriceModel(service, priceModel);
        final String serviceId = service.getServiceId();

        Subscription sub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                return Subscriptions.createSubscription(dm, Scenario
                        .getCustomer().getOrganizationId(), serviceId,
                        "freeSub", Scenario.getSupplier());
            }
        });

        // update with new, chargeable price model
        service = svcProv.getServiceForSubscription(Scenario.getVoCustomer(),
                sub.getSubscriptionId());
        VOPriceModel chargeablePM = new VOPriceModel();
        chargeablePM.setType(PriceModelType.PRO_RATA);
        chargeablePM.setCurrencyISOCode("EUR");
        svcProv.savePriceModelForSubscription(service, chargeablePM);
    }

    /**
     * The free trial period cannot be modified for a subscription-specific
     * price model.
     */
    @Test(expected = OperationNotPermittedException.class)
    public void testSavePriceModelForSubscription_FreePeriod_Unmodifiable()
            throws Exception {

        VOServiceDetails service = getService();

        VOPriceModel priceModel = service.getPriceModel();
        int freePeriodOld = 13;
        priceModel.setFreePeriod(freePeriodOld);

        // Save the price model with free trial period: 13 days for the
        // specified service.
        svcProv.savePriceModel(service, priceModel);

        final String serviceId = service.getServiceId();
        // Create a subscription to this service.
        Subscription sub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {

                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setPaymentInfoId("id");
                paymentInfo.setOrganization(Scenario.getCustomer());

                PaymentType paymentType = new PaymentType();
                paymentType.setPaymentTypeId(PaymentType.INVOICE);
                paymentType = (PaymentType) dm.find(paymentType);
                paymentInfo.setPaymentType(paymentType);

                return Subscriptions
                        .createSubscription(dm, Scenario.getCustomer()
                                .getOrganizationId(), serviceId,
                                "Subscription1", Scenario.getSupplier(),
                                paymentInfo, 1);
            }
        });

        // Get the service for the created subscription.
        service = svcProv.getServiceForSubscription(Scenario.getVoCustomer(),
                sub.getSubscriptionId());

        // Fetch its price model.
        priceModel = service.getPriceModel();

        // Modify only the free period (not allowed to be modified for
        // subscription-specific price models).
        int freePeriodNew = 14;
        priceModel.setFreePeriod(freePeriodNew);

        // An OperationNotPermittedException is expected for saving a price
        // model with a modified free trial period.
        svcProv.savePriceModelForSubscription(service, priceModel);
    }

    /**
     * Test the modification of the free trial period when saving the price
     * model of a service.
     */

    @Test
    public void testSavePriceModelForService_Modify_FreePeriod()
            throws Exception {
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();

        int freePeriodOld = 13;
        priceModel.setFreePeriod(freePeriodOld);

        // Save the price model with free trial period: 13 days for the
        // specified service.
        VOServiceDetails updatedService = svcProv.savePriceModel(service,
                priceModel);

        // Fetch the price model of the updated service.
        priceModel = updatedService.getPriceModel();
        assertEquals(priceModel.getFreePeriod(), freePeriodOld);

        // Modify only the free period.
        int freePeriodNew = 14;
        priceModel.setFreePeriod(freePeriodNew);

        // The saving after the modification of the free trial period should be
        // successful and no exception should be thrown.
        updatedService = svcProv.savePriceModel(updatedService, priceModel);
        assertEquals(updatedService.getPriceModel().getFreePeriod(),
                freePeriodNew);

    }

    /**
     * Test the modification of the free trial period when saving the price
     * model of a customer.
     */
    @Test
    public void testSavePriceModelForCustomer_Modify_FreePeriod()
            throws Exception {
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();

        int freePeriodOld = 13;
        priceModel.setFreePeriod(freePeriodOld);

        // Save the price model with free trial period: 13 days for the
        // specified service and customer.
        VOServiceDetails customerService = svcProv.savePriceModelForCustomer(
                service, priceModel, Scenario.getVoSecondCustomer());

        // Fetch the price model of the customer service.
        VOPriceModel customerPriceModel = customerService.getPriceModel();
        assertEquals(customerPriceModel.getFreePeriod(), freePeriodOld);

        // Modify only the free period.
        int freePeriodNew = 14;
        customerPriceModel.setFreePeriod(freePeriodNew);

        // The saving after the modification of the free trial period should be
        // successful and no exception should be thrown.
        customerService = svcProv.savePriceModelForCustomer(customerService,
                customerPriceModel, Scenario.getVoSecondCustomer());
        assertEquals(customerService.getPriceModel().getFreePeriod(),
                freePeriodNew);

    }

    /**
     * A chargeable PriceModel requires the Period.
     */
    @Test
    public void testSavePriceModel_NoPeriod() throws Exception {
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrencyISOCode("EUR");
        priceModel.setPeriod(null);
        try {
            svcProv.savePriceModel(service, priceModel);
        } catch (ValidationException e) {
            if (ValidationException.ReasonEnum.REQUIRED.equals(e.getReason())
                    && PriceModelAssembler.FIELD_NAME_PERIOD.equals(e
                            .getMember())) {
                return;
            }
        }
        fail("ValidationException(reason REQUIRED (parameters=[period])) expected.");
    }

    /**
     * A chargeable PriceModel requires the Period.
     */
    @Test
    public void testSavePriceModelForCustomer_NoPeriod() throws Exception {
        VOServiceDetails service = getService();
        VOPriceModel priceModel = service.getPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrencyISOCode("EUR");
        priceModel.setPeriod(null);
        try {
            svcProv.savePriceModelForCustomer(service, priceModel,
                    Scenario.getVoSecondCustomer());
        } catch (ValidationException e) {
            if (ValidationException.ReasonEnum.REQUIRED.equals(e.getReason())
                    && PriceModelAssembler.FIELD_NAME_PERIOD.equals(e
                            .getMember())) {
                return;
            }
        }
        fail("ValidationException(reason REQUIRED (parameters=[period])) expected.");
    }

    /**
     * A chargeable PriceModel requires the Period.
     */
    @Test(expected = PriceModelException.class)
    public void testSavePriceModelForSubscription_NoPeriod() throws Exception {
        // create a subscription
        VOServiceDetails service = getService();
        final String serviceId = service.getServiceId();
        Subscription sub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setPaymentInfoId("id");
                paymentInfo.setOrganization(Scenario.getCustomer());

                PaymentType paymentType = new PaymentType();
                paymentType.setPaymentTypeId(PaymentType.INVOICE);
                paymentType = (PaymentType) dm.find(paymentType);
                paymentInfo.setPaymentType(paymentType);

                return Subscriptions.createSubscription(dm, Scenario
                        .getCustomer().getOrganizationId(), serviceId,
                        "someSub", Scenario.getSupplier(), paymentInfo, 1);
            }
        });
        // create a chargeable PriceModel with no period
        service = svcProv.getServiceForSubscription(Scenario.getVoCustomer(),
                sub.getSubscriptionId());
        VOPriceModel priceModel = service.getPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrencyISOCode("EUR");
        priceModel.setPeriod(null);
        svcProv.savePriceModelForSubscription(service, priceModel);
    }

    // **********************************************************************+
    // internal methods

    /**
     * Returns the domain model class corresponding to the vo type. Returns
     * <code>null</code> if non is found.
     */
    private Class<? extends DomainObject<?>> getTargetClass(
            BaseVO currentElement) {
        if (currentElement instanceof VOPricedEvent) {
            return PricedEvent.class;
        }
        if (currentElement instanceof VOSteppedPrice) {
            return SteppedPrice.class;
        }
        if (currentElement instanceof VOPricedParameter) {
            return PricedParameter.class;
        }
        if (currentElement instanceof VOPricedOption) {
            return PricedOption.class;
        }
        if (currentElement instanceof VOPricedRole) {
            return PricedProductRole.class;
        }
        return null;
    }

    /**
     * Determines the service provided by the supplier.
     * 
     * @return The service in vo representation.
     */
    private VOServiceDetails getService() throws Exception {
        List<VOService> services = svcProv.getSuppliedServices();
        assertEquals(1, services.size());
        VOService voService = services.get(0);
        VOServiceDetails serviceDetails = svcProv.getServiceDetails(voService);
        return serviceDetails;
    }

    private void addSteppedPrices() throws Exception {
        final VOService svc = getService();
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Product p = dm.getReference(Product.class, svc.getKey());
                PriceModel pm = p.getPriceModel();
                PricedEvent pe = pm.getConsideredEvents().get(0);
                PricedParameter pp = pm.getSelectedParameters().get(0);
                Scenario.addSteppedPrices(pm, pe, pp);
                return null;
            }
        });
    }
}
