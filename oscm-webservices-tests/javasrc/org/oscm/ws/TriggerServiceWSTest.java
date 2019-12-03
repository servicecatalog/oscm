/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2012-8-29                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.VOFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.ws.base.WebserviceTestSetup;
import org.oscm.ws.unitrule.Order;
import org.oscm.ws.unitrule.OrderedRunner;
import org.oscm.converter.utils.JaxbConverter;
import org.oscm.intf.IdentityService;
import org.oscm.intf.MarketplaceService;
import org.oscm.intf.SubscriptionService;
import org.oscm.intf.TriggerDefinitionService;
import org.oscm.intf.TriggerService;
import org.oscm.types.enumtypes.ParameterValueType;
import org.oscm.types.enumtypes.PriceModelType;
import org.oscm.types.enumtypes.TriggerProcessParameterType;
import org.oscm.types.enumtypes.TriggerProcessStatus;
import org.oscm.types.enumtypes.TriggerTargetType;
import org.oscm.types.enumtypes.TriggerType;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.types.exceptions.ExecutionTargetException;
import org.oscm.vo.VOMarketplace;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOParameter;
import org.oscm.vo.VOParameterDefinition;
import org.oscm.vo.VOParameterOption;
import org.oscm.vo.VOPriceModel;
import org.oscm.vo.VOService;
import org.oscm.vo.VOSubscription;
import org.oscm.vo.VOTriggerDefinition;
import org.oscm.vo.VOTriggerProcess;
import org.oscm.vo.VOTriggerProcessParameter;
import org.oscm.vo.VOUda;
import org.oscm.vo.VOUsageLicense;
import org.oscm.vo.VOUserDetails;
import com.google.common.collect.Lists;
// import com.sun.xml.ws.fault.ServerSOAPFaultException;

/** @author yuyin */
@RunWith(OrderedRunner.class)
public class TriggerServiceWSTest {

  private static WebserviceTestSetup setup;
  private static TriggerDefinitionService serviceSupplier;
  private static TriggerDefinitionService tdServiceCustomer;
  private static TriggerService tpServiceSupplier;
  private static TriggerService tpServiceCustomer;
  private static IdentityService is;
  private static IdentityService isSP2;
  private static VOOrganization supplier1;
  private static VOUserDetails customerUser;
  private static VOMarketplace mpLocal;
  private static SubscriptionService subscrServiceForCustomer;
  private static VOService freeService;
  private static VOFactory factory = new VOFactory();
  private static VOTriggerDefinition suspendedTriggerDef;

  private static final String UPDATED_SERVICE_NAME = "Updated name";

  @BeforeClass
  public static void setUp() throws Exception {
    // clean the mails
    WebserviceTestBase.getMailReader().deleteMails();
    setup = new WebserviceTestSetup();

    // create supplier1
    supplier1 = setup.createSupplier("Supplier1");
    is =
        ServiceFactory.getDefault()
            .getIdentityService(setup.getSupplierUserKey(), WebserviceTestBase.DEFAULT_PASSWORD);
    serviceSupplier =
        ServiceFactory.getDefault()
            .getTriggerDefinitionService(
                setup.getSupplierUserKey(), WebserviceTestBase.DEFAULT_PASSWORD);
    tpServiceSupplier =
        ServiceFactory.getDefault()
            .getTriggerService(setup.getSupplierUserKey(), WebserviceTestBase.DEFAULT_PASSWORD);
    WebserviceTestBase.getMailReader().deleteMails();

    // create mp
    setup.createTechnicalService();
    MarketplaceService mpSrvOperator =
        ServiceFactory.getDefault()
            .getMarketPlaceService(
                WebserviceTestBase.getPlatformOperatorKey(),
                WebserviceTestBase.getPlatformOperatorPassword());

    mpLocal =
        mpSrvOperator.createMarketplace(
            factory.createMarketplaceVO(supplier1.getOrganizationId(), false, "Local Marketplace"));

    VOPriceModel priceModel = new VOPriceModel();
    priceModel.setType(PriceModelType.FREE_OF_CHARGE);
    freeService = setup.createAndActivateService("Service", mpLocal, priceModel);

    // create supplier2
    setup.createSupplier("Supplier2");
    isSP2 =
        ServiceFactory.getDefault()
            .getIdentityService(setup.getSupplierUserKey(), WebserviceTestBase.DEFAULT_PASSWORD);
    WebserviceTestBase.getMailReader().deleteMails();

    // create user
    setup.createCustomer("Customer");
    customerUser = setup.getCustomerUser();
    tdServiceCustomer =
        ServiceFactory.getDefault()
            .getTriggerDefinitionService(
                String.valueOf(customerUser.getKey()), WebserviceTestBase.DEFAULT_PASSWORD);
    tpServiceCustomer =
        ServiceFactory.getDefault()
            .getTriggerService(
                String.valueOf(customerUser.getKey()), WebserviceTestBase.DEFAULT_PASSWORD);
    subscrServiceForCustomer =
        ServiceFactory.getDefault()
            .getSubscriptionService(
                String.valueOf(customerUser.getKey()), WebserviceTestBase.DEFAULT_PASSWORD);

    suspendedTriggerDef = new VOTriggerDefinition();
    suspendedTriggerDef.setName("name");
    suspendedTriggerDef.setTarget(
        WebserviceTestBase.getConfigSetting(WebserviceTestBase.EXAMPLE_BASE_URL)
            + "/oscm-integrationtests-mockproduct/NotificationService?wsdl");
    suspendedTriggerDef.setType(TriggerType.SUBSCRIBE_TO_SERVICE);
    suspendedTriggerDef.setTargetType(TriggerTargetType.WEB_SERVICE);
    suspendedTriggerDef.setSuspendProcess(true);

    tdServiceCustomer.createTriggerDefinition(suspendedTriggerDef);
  }

  @Test(expected = ExecutionTargetException.class)
  @Order(order = 2)
  public void getTriggerTypes_supplierAdminBug9237() throws Exception {
    // given
    VOTriggerDefinition voDef = createVOTriggerDefinition(TriggerType.REGISTER_OWN_USER, true);
    serviceSupplier.createTriggerDefinition(voDef);
    VOUserDetails u = createUniqueUser();
    u.setEMail(WebserviceTestBase.getMailReader().getMailAddress());
    is.createUser(u, Arrays.asList(UserRoleType.SERVICE_MANAGER), null);
    isSP2.createUser(u, Arrays.asList(UserRoleType.SERVICE_MANAGER), null);
    waitForJmsQueueToStartTrigger();

    // when
    List<VOTriggerProcess> triggerProcessList = tpServiceSupplier.getAllActions();
    try {
      VOTriggerProcess voTriggerProcess = triggerProcessList.get(0);
      tpServiceSupplier.approveAction(voTriggerProcess.getKey());
    } catch (ExecutionTargetException e) {
      // then
      triggerProcessList = tpServiceSupplier.getAllActions();
      VOTriggerProcess voTriggerProcess = triggerProcessList.get(0);
      assertEquals(TriggerProcessStatus.FAILED, voTriggerProcess.getStatus());
      assertTrue(voTriggerProcess.getReason().contains("already exists with unique business"));
      throw e;
    }
  }

  /**
   * The JMS queue needs some time to change the trigger state from initial to ready_for_approval.
   * This is a design problem. See bug 8852.
   *
   * @throws InterruptedException
   */
  private void waitForJmsQueueToStartTrigger() throws InterruptedException {
    Thread.sleep(1000); // Bug 8852
  }

  @Test
  @Order(order = 1)
  public void getAllActions_NoTriggerProcessBug9319() throws Exception {
    // given
    VOTriggerDefinition voDef = createVOTriggerDefinition(TriggerType.SUBSCRIPTION_CREATION, false);
    serviceSupplier.createTriggerDefinition(voDef);
    List<VOUsageLicense> usageLicences = new ArrayList<VOUsageLicense>();
    usageLicences.add(factory.createUsageLicenceVO(customerUser));
    VOSubscription createdSubscription = createSubscription();
    subscrServiceForCustomer.subscribeToService(
        createdSubscription, freeService, usageLicences, null, null, new ArrayList<VOUda>());
    // when
    List<VOTriggerProcess> triggerProcessListCus = tpServiceCustomer.getAllActions();
    List<VOTriggerProcess> triggerProcessListSp = tpServiceSupplier.getAllActionsForOrganization();
    // then
    for (VOTriggerProcess voTriggerProcess : triggerProcessListSp) {
      if (voTriggerProcess
          .getTriggerDefinition()
          .getType()
          .equals(TriggerType.SUBSCRIPTION_CREATION)) {
        assertEquals(customerUser.getKey(), voTriggerProcess.getUser().getKey());
      }
    }
    assertEquals(1, triggerProcessListCus.size());
  }

  @Test
  @Order(order = 3)
  public void updateActionParameters_ServiceName_ShouldNotUpdate() throws Exception {
    // given
    VOTriggerProcess tpFromAction = createTriggerProcess(null);
    waitForJmsQueueToStartTrigger();
    assertNotNull(tpFromAction);

    VOTriggerProcessParameter actionParameter =
        tpServiceCustomer.getActionParameter(
            tpFromAction.getKey(), TriggerProcessParameterType.PRODUCT);

    String value = actionParameter.getValue();
    VOService service = JaxbConverter.fromXML(value, VOService.class);

    service.setName(UPDATED_SERVICE_NAME);

    String updatedValue = JaxbConverter.toXML(service);

    actionParameter.setValue(updatedValue);

    List<VOTriggerProcessParameter> voParams = new ArrayList<>();
    voParams.add(actionParameter);

    // when
    tpServiceCustomer.updateActionParameters(tpFromAction.getKey(), voParams);

    VOTriggerProcessParameter updatedActionParameter =
        tpServiceCustomer.getActionParameter(
            tpFromAction.getKey(), TriggerProcessParameterType.PRODUCT);

    String latestValue = updatedActionParameter.getValue();

    // then
    assertTrue(!latestValue.equals(updatedValue));
  }

  @Test
  @Order(order = 4)
  public void updateActionParameters_IntegerParameter_ShouldPass() throws Exception {
    // given
    List<VOParameter> parameters =
        getVOParameters(ParameterValueType.INTEGER, "123", Long.valueOf(0L), Long.valueOf(200L));

    VOTriggerProcess triggerProcess = createTriggerProcess(parameters);
    waitForJmsQueueToStartTrigger();
    assertNotNull(triggerProcess);

    VOTriggerProcessParameter actionParameter =
        tpServiceCustomer.getActionParameter(
            triggerProcess.getKey(), TriggerProcessParameterType.PRODUCT);

    String value = actionParameter.getValue();
    VOService service = JaxbConverter.fromXML(value, VOService.class);

    service.setParameters(parameters);

    String updatedValue = JaxbConverter.toXML(service);
    actionParameter.setValue(updatedValue);

    // when
    tpServiceCustomer.updateActionParameters(
        triggerProcess.getKey(), Lists.newArrayList(actionParameter));

    VOTriggerProcessParameter updatedActionParameter =
        tpServiceCustomer.getActionParameter(
            triggerProcess.getKey(), TriggerProcessParameterType.PRODUCT);

    String latestValue = updatedActionParameter.getValue();

    // then
    assertEquals(updatedValue, latestValue);
  }

  @Test(expected = org.oscm.types.exceptions.ValidationException.class)
  @Order(order = 5)
  public void updateActionParameters_IntegerParameterOutOfRange_ShouldFail() throws Exception {
    // given
    VOTriggerProcess triggerProcess = createTriggerProcess(null);
    waitForJmsQueueToStartTrigger();
    assertNotNull(triggerProcess);

    VOTriggerProcessParameter actionParameter =
        tpServiceCustomer.getActionParameter(
            triggerProcess.getKey(), TriggerProcessParameterType.PRODUCT);

    String value = actionParameter.getValue();
    VOService service = JaxbConverter.fromXML(value, VOService.class);

    List<VOParameter> parameters =
        getVOParameters(ParameterValueType.INTEGER, "123", Long.valueOf(0L), Long.valueOf(100L));
    service.setParameters(parameters);

    String updatedValue = JaxbConverter.toXML(service);
    actionParameter.setValue(updatedValue);

    // when
    tpServiceCustomer.updateActionParameters(
        triggerProcess.getKey(), Lists.newArrayList(actionParameter));

    // then exception
  }

  /**
   * Here parameter is mandatory and empty. As parameter can not be empty when it is mandatory then
   * this should fail.
   *
   * @throws Exception
   */
  @Test(expected = org.oscm.types.exceptions.ValidationException.class)
  @Order(order = 10)
  public void updateActionParameter_EmptyParamValue_ShouldFail() throws Exception {
    // given
    VOTriggerProcess triggerProcess = createTriggerProcess(null);
    waitForJmsQueueToStartTrigger();
    assertNotNull(triggerProcess);

    VOTriggerProcessParameter actionParameter =
        tpServiceCustomer.getActionParameter(
            triggerProcess.getKey(), TriggerProcessParameterType.PRODUCT);

    String value = actionParameter.getValue();
    VOService service = JaxbConverter.fromXML(value, VOService.class);

    VOParameterDefinition paramDefinition = new VOParameterDefinition();
    paramDefinition.setValueType(ParameterValueType.INTEGER);
    paramDefinition.setMandatory(true);

    VOParameter parameter = new VOParameter();
    parameter.setParameterDefinition(paramDefinition);
    parameter.setConfigurable(true);
    parameter.setValue("");

    service.setParameters(Collections.singletonList(parameter));

    String updatedValue = JaxbConverter.toXML(service);
    actionParameter.setValue(updatedValue);

    // when
    tpServiceCustomer.updateActionParameters(
        triggerProcess.getKey(), Lists.newArrayList(actionParameter));

    // then exception
  }

  /**
   * Here parameter is optional and empty. In this case empty parameter is acceptable.
   *
   * @throws Exception
   */
  @Test
  @Order(order = 11)
  public void updateActionParameter_EmptyParamValue_ShouldPass() throws Exception {
    // given
    VOParameterDefinition paramDefinition = new VOParameterDefinition();
    paramDefinition.setValueType(ParameterValueType.INTEGER);
    paramDefinition.setMandatory(false);

    VOParameter parameter = new VOParameter();
    parameter.setParameterDefinition(paramDefinition);
    parameter.setConfigurable(true);
    parameter.setValue("");

    VOTriggerProcess triggerProcess = createTriggerProcess(Collections.singletonList(parameter));
    waitForJmsQueueToStartTrigger();
    assertNotNull(triggerProcess);

    VOTriggerProcessParameter actionParameter =
        tpServiceCustomer.getActionParameter(
            triggerProcess.getKey(), TriggerProcessParameterType.PRODUCT);

    String value = actionParameter.getValue();
    VOService service = JaxbConverter.fromXML(value, VOService.class);

    service.setParameters(Collections.singletonList(parameter));

    String updatedValue = JaxbConverter.toXML(service);
    actionParameter.setValue(updatedValue);

    // when
    tpServiceCustomer.updateActionParameters(
        triggerProcess.getKey(), Lists.newArrayList(actionParameter));

    // then

    VOTriggerProcessParameter updatedActionParameter =
        tpServiceCustomer.getActionParameter(
            triggerProcess.getKey(), TriggerProcessParameterType.PRODUCT);

    String latestValue = updatedActionParameter.getValue();

    // then
    assertEquals(updatedValue, latestValue);
  }

  /**
   * Here parameter is configurable but it is mandatory and null. Parameter can not be null so test
   * should fail.
   *
   * @throws Exception
   */
  @Test(expected = org.oscm.types.exceptions.ValidationException.class)
  @Order(order = 12)
  public void updateActionParameter_NullParamValue_ShouldFail() throws Exception {
    // given
    VOTriggerProcess triggerProcess = createTriggerProcess(null);
    waitForJmsQueueToStartTrigger();
    assertNotNull(triggerProcess);

    VOTriggerProcessParameter actionParameter =
        tpServiceCustomer.getActionParameter(
            triggerProcess.getKey(), TriggerProcessParameterType.PRODUCT);

    String value = actionParameter.getValue();
    VOService service = JaxbConverter.fromXML(value, VOService.class);

    VOParameterDefinition paramDefinition = new VOParameterDefinition();
    paramDefinition.setValueType(ParameterValueType.INTEGER);
    paramDefinition.setMandatory(true);

    VOParameter parameter = new VOParameter();
    parameter.setParameterDefinition(paramDefinition);
    parameter.setConfigurable(true);
    parameter.setValue(null);

    service.setParameters(Collections.singletonList(parameter));

    String updatedValue = JaxbConverter.toXML(service);
    actionParameter.setValue(updatedValue);

    // when
    tpServiceCustomer.updateActionParameters(
        triggerProcess.getKey(), Lists.newArrayList(actionParameter));

    // then exception
  }

  /**
   * Here parameter is configurable and optional. In this case null parameter value is acceptable.
   *
   * @throws Exception
   */
  @Test
  @Order(order = 13)
  public void updateActionParameter_NullParamValue_ShouldPass() throws Exception {
    // given
    VOParameterDefinition paramDefinition = new VOParameterDefinition();
    paramDefinition.setValueType(ParameterValueType.INTEGER);
    paramDefinition.setMandatory(false);

    VOParameter parameter = new VOParameter();
    parameter.setParameterDefinition(paramDefinition);
    parameter.setConfigurable(true);
    parameter.setValue(null);

    VOTriggerProcess triggerProcess = createTriggerProcess(Collections.singletonList(parameter));
    waitForJmsQueueToStartTrigger();
    assertNotNull(triggerProcess);

    VOTriggerProcessParameter actionParameter =
        tpServiceCustomer.getActionParameter(
            triggerProcess.getKey(), TriggerProcessParameterType.PRODUCT);

    String value = actionParameter.getValue();
    VOService service = JaxbConverter.fromXML(value, VOService.class);

    service.setParameters(Collections.singletonList(parameter));

    String updatedValue = JaxbConverter.toXML(service);
    actionParameter.setValue(updatedValue);

    // when
    tpServiceCustomer.updateActionParameters(
        triggerProcess.getKey(), Lists.newArrayList(actionParameter));

    // then
    VOTriggerProcessParameter updatedActionParameter =
        tpServiceCustomer.getActionParameter(
            triggerProcess.getKey(), TriggerProcessParameterType.PRODUCT);

    String latestValue = updatedActionParameter.getValue();

    // then
    assertEquals(updatedValue, latestValue);
  }

  @Test
  @Order(order = 14)
  public void updateActionParameter_Duration_ShouldPass() throws Exception {
    // given
    VOParameterDefinition paramDefinition = new VOParameterDefinition();
    paramDefinition.setValueType(ParameterValueType.DURATION);
    paramDefinition.setMandatory(false);

    VOParameter parameter = new VOParameter();
    parameter.setParameterDefinition(paramDefinition);
    parameter.setConfigurable(true);
    parameter.setValue("86400000"); // 1 day in ms

    VOTriggerProcess triggerProcess = createTriggerProcess(Collections.singletonList(parameter));
    waitForJmsQueueToStartTrigger();
    assertNotNull(triggerProcess);

    VOTriggerProcessParameter actionParameter =
        tpServiceCustomer.getActionParameter(
            triggerProcess.getKey(), TriggerProcessParameterType.PRODUCT);

    String value = actionParameter.getValue();
    VOService service = JaxbConverter.fromXML(value, VOService.class);

    service.setParameters(Collections.singletonList(parameter));

    String updatedValue = JaxbConverter.toXML(service);
    actionParameter.setValue(updatedValue);

    // when
    tpServiceCustomer.updateActionParameters(
        triggerProcess.getKey(), Lists.newArrayList(actionParameter));

    // then
    VOTriggerProcessParameter updatedActionParameter =
        tpServiceCustomer.getActionParameter(
            triggerProcess.getKey(), TriggerProcessParameterType.PRODUCT);

    String latestValue = updatedActionParameter.getValue();

    // then
    assertEquals(updatedValue, latestValue);
  }

  @Test(expected = org.oscm.types.exceptions.ValidationException.class)
  @Order(order = 15)
  public void updateActionParameter_DurationException_ShouldFail() throws Exception {
    // given
    VOParameterDefinition paramDefinition = new VOParameterDefinition();
    paramDefinition.setValueType(ParameterValueType.DURATION);
    paramDefinition.setMandatory(false);

    VOParameter parameter = new VOParameter();
    parameter.setParameterDefinition(paramDefinition);
    parameter.setConfigurable(true);
    parameter.setValue("86400001");

    VOTriggerProcess triggerProcess = createTriggerProcess(Collections.singletonList(parameter));
    waitForJmsQueueToStartTrigger();
    assertNotNull(triggerProcess);

    VOTriggerProcessParameter actionParameter =
        tpServiceCustomer.getActionParameter(
            triggerProcess.getKey(), TriggerProcessParameterType.PRODUCT);

    String value = actionParameter.getValue();
    VOService service = JaxbConverter.fromXML(value, VOService.class);

    service.setParameters(Collections.singletonList(parameter));

    String updatedValue = JaxbConverter.toXML(service);
    actionParameter.setValue(updatedValue);

    // when
    tpServiceCustomer.updateActionParameters(
        triggerProcess.getKey(), Lists.newArrayList(actionParameter));

    // then
    VOTriggerProcessParameter updatedActionParameter =
        tpServiceCustomer.getActionParameter(
            triggerProcess.getKey(), TriggerProcessParameterType.PRODUCT);

    String latestValue = updatedActionParameter.getValue();

    // then
    assertEquals(updatedValue, latestValue);
  }

  //    @Test(expected = ServerSOAPFaultException.class)
  //    @Order(order = 7)
  //    public void getActionParameter_NullParam_ShouldFail() throws Exception {
  //        // given
  //        VOTriggerProcess triggerProcess = createTriggerProcess(null);
  //        waitForJmsQueueToStartTrigger();
  //        assertNotNull(triggerProcess);
  //
  //        // when
  //        tpServiceCustomer.getActionParameter(triggerProcess.getKey(), null);
  //
  //        // then exception
  //    }

  @Test
  @Order(order = 8)
  public void getActionParameter_Exist_ShouldPass() throws Exception {
    // given
    VOTriggerProcess triggerProcess = createTriggerProcess(null);
    waitForJmsQueueToStartTrigger();
    assertNotNull(triggerProcess);

    // when
    VOTriggerProcessParameter actionParameter =
        tpServiceCustomer.getActionParameter(
            triggerProcess.getKey(), TriggerProcessParameterType.PRODUCT);

    // then
    assertNotNull(actionParameter);
    assertEquals(actionParameter.getTriggerProcessKey().longValue(), triggerProcess.getKey());
  }

  @Test(expected = org.oscm.types.exceptions.ObjectNotFoundException.class)
  @Order(order = 9)
  public void getActionParameter_NotExist_ShouldFail() throws Exception {
    // given
    VOTriggerProcess triggerProcess = createTriggerProcess(null);
    waitForJmsQueueToStartTrigger();
    assertNotNull(triggerProcess);

    // when
    tpServiceCustomer.getActionParameter(0, TriggerProcessParameterType.PRODUCT);

    // then exception
  }

  private VOTriggerDefinition createVOTriggerDefinition(
      TriggerType triggerType, boolean suspendProcess) throws Exception {
    VOTriggerDefinition triggerCreate = new VOTriggerDefinition();
    triggerCreate.setName("name");
    triggerCreate.setTarget(
        WebserviceTestBase.getConfigSetting(WebserviceTestBase.EXAMPLE_BASE_URL)
            + "/oscm-integrationtests-mockproduct/NotificationService?wsdl");
    triggerCreate.setType(triggerType);
    triggerCreate.setTargetType(TriggerTargetType.WEB_SERVICE);
    triggerCreate.setSuspendProcess(suspendProcess);
    return triggerCreate;
  }

  private static VOUserDetails createUniqueUser() throws Exception {
    VOUserDetails u = factory.createUserVO(Long.toHexString(System.currentTimeMillis()));
    u.setOrganizationId(supplier1.getOrganizationId());
    return u;
  }

  private VOSubscription createSubscription() {
    // Subscribe to service
    VOSubscription subscription = new VOSubscription();
    String subscriptionId = Long.toHexString(System.currentTimeMillis());
    subscription.setSubscriptionId(subscriptionId);

    return subscription;
  }

  private VOTriggerProcess createTriggerProcess(List<VOParameter> parameters) throws Exception {
    List<VOUsageLicense> usageLicences = new ArrayList<>();
    usageLicences.add(factory.createUsageLicenceVO(customerUser));
    VOSubscription createdSubscription = createSubscription();

    freeService.setParameters(parameters);

    subscrServiceForCustomer.subscribeToService(
        createdSubscription, freeService, usageLicences, null, null, new ArrayList<VOUda>());

    List<VOTriggerProcess> triggerProcessListCus = tpServiceCustomer.getAllActions();

    if (triggerProcessListCus == null || triggerProcessListCus.size() == 0) {
      return null;
    }

    return triggerProcessListCus.get(0);
  }

  public static List<VOParameter> getVOParameters(ParameterValueType valueType, String paramValue) {
    VOParameter parameter = new VOParameter();
    VOParameterDefinition parameterDefinition = new VOParameterDefinition();

    parameterDefinition.setValueType(valueType);
    parameter.setValue(paramValue);
    parameter.setConfigurable(true);

    parameter.setParameterDefinition(parameterDefinition);

    return Lists.newArrayList(parameter);
  }

  public static List<VOParameter> getVOParameters(
      ParameterValueType valueType, String paramValue, Long minRange, Long maxRange) {
    List<VOParameter> result = getVOParameters(valueType, paramValue);

    VOParameter parameter = result.get(0);
    parameter.getParameterDefinition().setMinValue(minRange);
    parameter.getParameterDefinition().setMaxValue(maxRange);

    return result;
  }

  public static List<VOParameter> getVOParameters(
      ParameterValueType valueType, String paramValue, String optionId) {
    List<VOParameter> result = getVOParameters(valueType, paramValue);

    VOParameterOption option = new VOParameterOption();
    option.setOptionId(optionId);

    result.get(0).getParameterDefinition().setParameterOptions(Lists.newArrayList(option));

    return result;
  }

  @After
  public void tearDown() {}
}
