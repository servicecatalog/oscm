/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: barzu                                          
 *                                                                              
 *  Creation Date: Sep 23, 2011                                                      
 *                                                                              
 *  Completion Time: <date> Sep 23, 2011                                        
 *                                                                              
 *******************************************************************************/

package org.oscm.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.oscm.converter.XMLConverter;
import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.VOFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.ws.base.WebserviceTestSetup;
import org.oscm.intf.MarketplaceService;
import org.oscm.intf.ServiceProvisioningService;
import org.oscm.types.enumtypes.ParameterModificationType;
import org.oscm.types.enumtypes.PriceModelType;
import org.oscm.types.enumtypes.PricingPeriod;
import org.oscm.types.enumtypes.TriggerType;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.OperationPendingException;
import org.oscm.types.exceptions.PriceModelException;
import org.oscm.types.exceptions.UpdateConstraintException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOMarketplace;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOParameter;
import org.oscm.vo.VOParameterDefinition;
import org.oscm.vo.VOPriceModel;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServiceActivation;
import org.oscm.vo.VOServiceDetails;
import org.oscm.vo.VOSubscriptionDetails;
import org.oscm.vo.VOTechnicalService;
import org.oscm.vo.VOTriggerDefinition;
//import com.sun.xml.ws.fault.ServerSOAPFaultException;

/**
 * @author barzu
 */
public class ServiceProvisioningServiceWSTest {

    private WebserviceTestSetup setup;
    private VOServiceDetails serviceDetails;
    private VOOrganization supplier;
    private VOTechnicalService voTS;
    private VOMarketplace mpLocal;
    private static VOFactory factory = new VOFactory();

    @Before
    public void setUp() throws Exception {
        // clean the mails
        WebserviceTestBase.getMailReader().deleteMails();
        // add currencies
        WebserviceTestBase.getOperator().addCurrency("EUR");

        setup = new WebserviceTestSetup();

        supplier = setup.createSupplier("Sicher-und-Heil");
        setup.createTechnologyProvider("Dick-und-Doof");
        MarketplaceService mpSrvOperator = ServiceFactory.getDefault()
                .getMarketPlaceService(
                        WebserviceTestBase.getPlatformOperatorKey(),
                        WebserviceTestBase.getPlatformOperatorPassword());

        // create "Local" marketplace
        mpLocal = mpSrvOperator.createMarketplace(factory.createMarketplaceVO(
                supplier.getOrganizationId(), false, "Local Marketplace"));

        // create technical service
        setup.createTechnicalService();

        // add services to "Local" marketplace
        serviceDetails = setup.createService("ExampleTrial", mpLocal);
    }

    private VOPriceModel createVOPriceModel() {
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrencyISOCode("EUR");
        priceModel.setPeriod(PricingPeriod.DAY);
        return priceModel;
    }

    /**
     * Chargeable PriceModels require the Period (bug #8056)
     */
    @Test(expected = ValidationException.class)
    public void savePriceModel_NoPeriod() throws Exception {
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrencyISOCode("EUR");

        setup.getServiceProvisioningSrvAsSupplier().savePriceModel(
                serviceDetails, priceModel);
    }

    @Test(expected = ValidationException.class)
    public void savePriceModel_FreePeriod() throws Exception {
        VOPriceModel priceModel = createVOPriceModel();

        // set a freePeriod
        priceModel.setFreePeriod(5);
        serviceDetails = setup.getServiceProvisioningSrvAsSupplier()
                .savePriceModel(serviceDetails, priceModel);
        assertEquals("Wrong PriceModel.freePeriod found", 5, serviceDetails
                .getPriceModel().getFreePeriod());
        priceModel = serviceDetails.getPriceModel();

        // reset the freePeriod
        priceModel.setFreePeriod(0);
        serviceDetails = setup.getServiceProvisioningSrvAsSupplier()
                .savePriceModel(serviceDetails, priceModel);
        assertEquals("Wrong PriceModel.freePeriod found", 0, serviceDetails
                .getPriceModel().getFreePeriod());
        priceModel = serviceDetails.getPriceModel();

        // set an invalid freePeriod
        priceModel.setFreePeriod(-5);
        setup.getServiceProvisioningSrvAsSupplier().savePriceModel(
                serviceDetails, priceModel);
    }

    @Test(expected = ValidationException.class)
    public void savePriceModelForCustomer_FreePeriod() throws Exception {
        VOPriceModel priceModel = createVOPriceModel();

        // set a freePeriod
        priceModel.setFreePeriod(5);
        serviceDetails = setup
                .getServiceProvisioningSrvAsSupplier()
                .savePriceModelForCustomer(serviceDetails, priceModel, supplier);
        assertEquals("Wrong PriceModel.freePeriod found for customer "
                + supplier.getOrganizationId(), 5, serviceDetails
                .getPriceModel().getFreePeriod());
        priceModel = serviceDetails.getPriceModel();

        // reset the freePeriod
        priceModel.setFreePeriod(0);
        serviceDetails = setup
                .getServiceProvisioningSrvAsSupplier()
                .savePriceModelForCustomer(serviceDetails, priceModel, supplier);
        assertEquals("Wrong PriceModel.freePeriod found for customer "
                + supplier.getOrganizationId(), 0, serviceDetails
                .getPriceModel().getFreePeriod());
        priceModel = serviceDetails.getPriceModel();

        // set an invalid freePeriod
        priceModel.setFreePeriod(-5);
        setup.getServiceProvisioningSrvAsSupplier().savePriceModelForCustomer(
                serviceDetails, priceModel, supplier);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void savePriceModelForSubscription_FreePeriod() throws Exception {
        VOPriceModel priceModel = createVOPriceModel();
        priceModel.setFreePeriod(5);

        // create service, price model, customer, subscription
        serviceDetails = setup.getServiceProvisioningSrvAsSupplier()
                .savePriceModel(serviceDetails, priceModel);
        priceModel = serviceDetails.getPriceModel();
        VOService service = setup.activateService(serviceDetails);
        setup.createCustomer("Joe_Customer");
        VOSubscriptionDetails subscription = setup.createSubscription(
                "Subscr1", service);
        priceModel = subscription.getSubscribedService().getPriceModel();
        serviceDetails = setup.getServiceProvisioningSrvAsSupplier()
                .getServiceDetails(subscription.getSubscribedService());

        // do NOT change the freePeriod and save price model for subscription
        serviceDetails = setup.getServiceProvisioningSrvAsSupplier()
                .savePriceModelForSubscription(serviceDetails, priceModel);
        priceModel = serviceDetails.getPriceModel();
        assertEquals("Wrong PriceModel.freePeriod found for subscription "
                + subscription.getSubscriptionId(), 5, serviceDetails
                .getPriceModel().getFreePeriod());

        // changing the PriceModel.freePeriod for a subscription
        // is not permitted
        priceModel = subscription.getSubscribedService().getPriceModel();
        priceModel.setFreePeriod(2);
        setup.getServiceProvisioningSrvAsSupplier()
                .savePriceModelForSubscription(serviceDetails, priceModel);
    }

    @Test(expected = PriceModelException.class)
    public void savePriceModelForSubscription_ChangeTimeUnit() throws Exception {
        VOPriceModel priceModel = createVOPriceModel();

        VOSubscriptionDetails subscription = createSubscriptionAndSavaPricemodel(priceModel);

        // changing the time unit for a subscription is not permitted
        priceModel = subscription.getSubscribedService().getPriceModel();
        priceModel.setPeriod(PricingPeriod.WEEK);
        setup.getServiceProvisioningSrvAsSupplier()
                .savePriceModelForSubscription(serviceDetails, priceModel);
    }

    @Test(expected = PriceModelException.class)
    public void savePriceModelForSubscription_ChangeCurrency() throws Exception {
        VOPriceModel priceModel = createVOPriceModel();

        VOSubscriptionDetails subscription = createSubscriptionAndSavaPricemodel(priceModel);

        // changing the currency for a subscription is not permitted
        priceModel = subscription.getSubscribedService().getPriceModel();
        priceModel.setCurrencyISOCode("USD");
        setup.getServiceProvisioningSrvAsSupplier()
                .savePriceModelForSubscription(serviceDetails, priceModel);
    }

    @Test(expected = PriceModelException.class)
    public void savePriceModelForSubscription_ChangePriceModelType()
            throws Exception {
        VOPriceModel priceModel = createVOPriceModel();

        VOSubscriptionDetails subscription = createSubscriptionAndSavaPricemodel(priceModel);

        // changing the price model type for a subscription is not permitted
        priceModel = subscription.getSubscribedService().getPriceModel();
        priceModel.setType(PriceModelType.PER_UNIT);
        setup.getServiceProvisioningSrvAsSupplier()
                .savePriceModelForSubscription(serviceDetails, priceModel);
    }

    // BE06744
    /**
     * Try to create an invalid marketable service
     */
    @Test
    public void createInvalidService() throws Exception {
        ServiceProvisioningService provisioningService = setup
                .getServiceProvisioningSrvAsSupplier();

        try {
            provisioningService.createService(setup.getVoTechnicalService(),
                    new VOService(), null);
            fail("Validation Exception expected because the marketable service has no ID");
        } catch (ValidationException e) {
            assertEquals("Wrong number of exception parameters", 1,
                    e.getMessageParams().length);
            assertEquals("Wrong exception parameter", "serviceId",
                    e.getMessageParams()[0]);
        }
    }

    @Test
    public void updateService() throws Exception {
        // given
        ServiceProvisioningService provisioningService = setup
                .getServiceProvisioningSrvAsSupplier();

        // when
        serviceDetails.setServiceId("ServiceId");
        serviceDetails.setConfiguratorUrl("http://www.confUrl.de");
        VOServiceDetails updatedService = provisioningService.updateService(
                serviceDetails, null);

        assertEquals(serviceDetails.getServiceId(),
                updatedService.getServiceId());
        assertEquals(serviceDetails.getConfiguratorUrl(),
                updatedService.getConfiguratorUrl());
    }

    @Test(expected = RuntimeException.class)
    public void updateService_nullService() throws Exception {
        // given
        ServiceProvisioningService provisioningService = setup
                .getServiceProvisioningSrvAsSupplier();
        try {
            // when
            provisioningService.updateService(null, null);
            // then
            fail();
        } catch (Exception e) {
            throw e;
        }
    }

    @Test(expected = OperationPendingException.class)
    public void activateService_TriggerProcessPending() throws Exception {
        // create a trigger definition on activate service
        VOTriggerDefinition triggerDef = WebserviceTestBase
                .createTriggerDefinition();
        triggerDef.setType(TriggerType.ACTIVATE_SERVICE);
        setup.createTriggerDefinition(triggerDef);

        try {
            // create and activate a service
            VOPriceModel priceModel = factory.createPriceModelVO("EUR");
            serviceDetails = setup.savePriceModel(serviceDetails, priceModel);
            setup.activateService(serviceDetails);

            // re-execution of service activation is not allowed
            setup.activateService(serviceDetails);
        } finally {
            setup.deleteTriggersForUser();
        }
    }

    @Test(expected = OperationPendingException.class)
    public void deactivateService_TriggerProcessPending() throws Exception {
        // create and activate a service
        VOPriceModel priceModel = factory.createPriceModelVO("EUR");
        serviceDetails = setup.savePriceModel(serviceDetails, priceModel);
        setup.activateService(serviceDetails);

        // create a trigger definition on de-activate service
        VOTriggerDefinition triggerDef = WebserviceTestBase
                .createTriggerDefinition();
        triggerDef.setType(TriggerType.DEACTIVATE_SERVICE);
        setup.createTriggerDefinition(triggerDef);

        try {
            // de-activate the service
            setup.deactivateService(serviceDetails);

            // re-execution of service de-activation is not allowed
            setup.deactivateService(serviceDetails);
        } finally {
            setup.deleteTriggersForUser();
        }
    }

    @Test(expected = OperationPendingException.class)
    public void setActivationStates_TriggerProcessPending() throws Exception {
        // create a trigger definition on activate service
        VOTriggerDefinition triggerDef = WebserviceTestBase
                .createTriggerDefinition();
        triggerDef.setType(TriggerType.ACTIVATE_SERVICE);
        setup.createTriggerDefinition(triggerDef);

        try {
            // create and activate a list of services
            VOPriceModel priceModel = factory.createPriceModelVO("EUR");
            serviceDetails = setup.savePriceModel(serviceDetails, priceModel);
            VOServiceActivation activation = new VOServiceActivation();
            activation.setActive(true);
            activation.setService(serviceDetails);
            List<VOServiceActivation> activations = new ArrayList<VOServiceActivation>();
            activations.add(activation);
            setup.setActivationStates(activations);

            // re-execution of service activation is not allowed
            setup.setActivationStates(activations);
        } finally {
            setup.deleteTriggersForUser();
        }
    }

    @Test
    public void createTechnicalService_WithOneTimeParameter() throws Exception {
        voTS = setup.createTechnicalServiceWithParameterDefinition(
                "WebServiceTestExample1.0", ParameterModificationType.ONE_TIME);
        for (VOParameterDefinition para : voTS.getParameterDefinitions()) {
            if (para.getParameterId().equals("TEST")) {
                assertEquals(ParameterModificationType.ONE_TIME,
                        para.getModificationType());
            }
        }
    }

    @Test
    public void createTechnicalService_WithNullModificationType()
            throws Exception {
        voTS = setup.createTechnicalServiceWithParameterDefinition(
                "WebServiceTestExample1.0", null);
        for (VOParameterDefinition para : voTS.getParameterDefinitions()) {
            if (para.getParameterId().equals("TEST")) {
                assertEquals(ParameterModificationType.STANDARD,
                        para.getModificationType());
            }
        }
    }

    @Test
    public void createTechnicalService_WithSTANDARDModificationType()
            throws Exception {
        voTS = setup.createTechnicalServiceWithParameterDefinition(
                "WebServiceTestExample1.0", ParameterModificationType.STANDARD);
        for (VOParameterDefinition para : voTS.getParameterDefinitions()) {
            if (para.getParameterId().equals("TEST")) {
                assertEquals(ParameterModificationType.STANDARD,
                        para.getModificationType());
            }
        }
    }

    @Test
    public void updateTechnicalServiceParameterDefiniton_WithNoProductExisting()
            throws Exception {
        voTS = setup.createTechnicalServiceWithParameterDefinition(
                "WebServiceTestExample2.0", ParameterModificationType.STANDARD);
        setup.createTechnicalServiceWithParameterDefinition(
                "WebServiceTestExample2.0", ParameterModificationType.ONE_TIME);
    }

    @Test
    public void updateTechnicalServiceParameterDefiniton_WithNoChanges()
            throws Exception {
        voTS = setup.createTechnicalServiceWithParameterDefinition("tp1",
                ParameterModificationType.ONE_TIME);
        setup.createService("ExampleTrial", voTS, mpLocal);
        setup.createTechnicalServiceWithParameterDefinition("tp1",
                ParameterModificationType.ONE_TIME);
    }

    @Test(expected = UpdateConstraintException.class)
    public void updateTechnicalServiceParameterDefiniton_WithProductExisting()
            throws Exception {
        voTS = setup.createTechnicalServiceWithParameterDefinition("tp1",
                ParameterModificationType.ONE_TIME);
        setup.createService("ExampleTrial", voTS, mpLocal);
        setup.createTechnicalServiceWithParameterDefinition("tp1",
                ParameterModificationType.STANDARD);
    }

    @Test
    public void exportTechnicalServiceParameterDefiniton_CheckModificationType()
            throws Exception {
        String srvId = "exportTechnicalServiceParameterDefiniton_CheckModificationType"
                .hashCode() + "";
        voTS = setup.createTechnicalServiceWithParameterDefinition(srvId,
                ParameterModificationType.ONE_TIME);
        List<VOTechnicalService> technicalServices = new ArrayList<VOTechnicalService>();
        technicalServices.add(voTS);
        byte[] content = setup.exportTechnicalService(technicalServices);
        String xmlString = new String(content);
        verifyXmlForModificationType(srvId, xmlString,
                ParameterModificationType.ONE_TIME);
    }

    @Test
    public void createService() throws Exception {
        ServiceProvisioningService provisioningService = setup
                .getServiceProvisioningSrvAsSupplier();

        VOTechnicalService voTechService = setup.getVoTechnicalService();
        List<VOParameterDefinition> voParamDefs = voTechService
                .getParameterDefinitions();

        VOService voService = new VOService();
        voService.setName("ServiceName");
        voService.setShortDescription("shortDescription");
        voService.setServiceId("serviceId");
        voService.setDescription("marketingName");

        List<VOParameter> params = new ArrayList<VOParameter>();
        VOParameter parameter = new VOParameter();
        parameter.setParameterDefinition(voParamDefs.get(0));
        parameter.setValue("172800000");
        params.add(parameter);

        voService.setParameters(params);

        VOServiceDetails voServiceDetails = provisioningService.createService(
                setup.getVoTechnicalService(), voService, null);

        assertEquals(voService.getServiceId(), voServiceDetails.getServiceId());
        assertEquals(voService.getName(), voServiceDetails.getName());
    }

    /**
     * parse the XML content and verify the ModificationType has been exported
     * as expected
     * 
     * @param xmlString
     * @throws Exception
     */
    private void verifyXmlForModificationType(String srvId, String xmlString,
            ParameterModificationType currentModificationType) throws Exception {
        // parse the XML content
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(
                xmlString)));

        Element root = (Element) XMLConverter.getNodeByXPath(document,
                "//*[local-name(.)=\'TechnicalService\'][@id=\'" + srvId
                        + "\']");
        NodeList serviceNode = root.getChildNodes();
        for (int index = 0; index < serviceNode.getLength(); index++) {
            if (serviceNode.item(index).getNodeName()
                    .equals("ParameterDefinition")) {
                // verify parameter definition attribute
                NamedNodeMap parameterDefinitionAttrs = serviceNode.item(index)
                        .getAttributes();
                // if the currentModificationType is not OneTime, the
                // modificationType attribute should not be exported
                if (parameterDefinitionAttrs.getNamedItem("id").equals("TEST")) {
                    if (currentModificationType == null
                            || currentModificationType
                                    .equals(ParameterModificationType.STANDARD)) {
                        assertNull(parameterDefinitionAttrs
                                .getNamedItem("modificationType"));
                    } else {
                        // if the currentModificationType is OneTime, verify the
                        // value is correct
                        assertEquals(
                                currentModificationType.name(),
                                parameterDefinitionAttrs.getNamedItem(
                                        "modificationType").getTextContent());
                    }
                }
            }
        }
    }

    private VOSubscriptionDetails createSubscriptionAndSavaPricemodel(
            VOPriceModel priceModel) throws Exception {
        // create service, price model, customer, subscription
        serviceDetails = setup.getServiceProvisioningSrvAsSupplier()
                .savePriceModel(serviceDetails, priceModel);
        priceModel = serviceDetails.getPriceModel();
        VOService service = setup.activateService(serviceDetails);
        setup.createCustomer("Joe_Customer");
        VOSubscriptionDetails subscription = setup.createSubscription(
                "Subscr1", service);
        priceModel = subscription.getSubscribedService().getPriceModel();
        serviceDetails = setup.getServiceProvisioningSrvAsSupplier()
                .getServiceDetails(subscription.getSubscribedService());

        serviceDetails = setup.getServiceProvisioningSrvAsSupplier()
                .savePriceModelForSubscription(serviceDetails, priceModel);
        priceModel = serviceDetails.getPriceModel();

        return subscription;

    }

}
