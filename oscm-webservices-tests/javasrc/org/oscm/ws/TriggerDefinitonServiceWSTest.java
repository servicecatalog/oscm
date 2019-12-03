/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Author: Brandstetter
 *
 * <p>*****************************************************************************
 */
package org.oscm.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.ws.soap.SOAPFaultException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.oscm.intf.TriggerDefinitionService;
import org.oscm.types.enumtypes.TriggerTargetType;
import org.oscm.types.enumtypes.TriggerType;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOTriggerDefinition;
import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.ws.base.WebserviceTestSetup;

public class TriggerDefinitonServiceWSTest {

  private static WebserviceTestSetup setup;
  private static TriggerDefinitionService serviceSupplier;
  private static TriggerDefinitionService serviceCustomer;
  private static TriggerDefinitionService serviceServiceManager;

  private static TriggerType[] allowedTriggersForSupplier = {
    TriggerType.ACTIVATE_SERVICE,
    TriggerType.DEACTIVATE_SERVICE,
    TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER,
    TriggerType.SAVE_PAYMENT_CONFIGURATION,
    TriggerType.START_BILLING_RUN,
    TriggerType.SUBSCRIPTION_CREATION,
    TriggerType.SUBSCRIPTION_MODIFICATION,
    TriggerType.SUBSCRIPTION_TERMINATION,
    TriggerType.REGISTER_OWN_USER
  };

  private static TriggerType[] allowedTriggersForCustomer = {
    TriggerType.ADD_REVOKE_USER,
    TriggerType.MODIFY_SUBSCRIPTION,
    TriggerType.SUBSCRIBE_TO_SERVICE,
    TriggerType.UNSUBSCRIBE_FROM_SERVICE,
    TriggerType.UPGRADE_SUBSCRIPTION,
    TriggerType.START_BILLING_RUN,
    TriggerType.REGISTER_OWN_USER
  };

  private VOTriggerDefinition createVOTriggerDefinition() {
    VOTriggerDefinition triggerCreate = new VOTriggerDefinition();
    triggerCreate.setName("name");
    triggerCreate.setTarget("target");
    triggerCreate.setType(TriggerType.ACTIVATE_SERVICE);
    triggerCreate.setTargetType(TriggerTargetType.WEB_SERVICE);
    triggerCreate.setSuspendProcess(true);
    return triggerCreate;
  }

  private void validateExceptionContent(SOAPFaultException ex) throws Exception {
    assertTrue(ex.getMessage().contains("Unauthorized"));
  }

  @BeforeClass
  public static void setUp() throws Exception {
    // clean the mails
    WebserviceTestBase.getMailReader().deleteMails();
    setup = new WebserviceTestSetup();
    setup.createSupplier("Supplier1");
    setup.createCustomer("Customer1");

    // login as supplier administrator
    serviceSupplier =
        ServiceFactory.getDefault()
            .getTriggerDefinitionService(
                setup.getSupplierUserKey(), WebserviceTestBase.DEFAULT_PASSWORD);

    // login as customer administrator
    serviceCustomer =
        ServiceFactory.getDefault()
            .getTriggerDefinitionService(
                Long.toString(setup.getCustomerUser().getKey()),
                WebserviceTestBase.DEFAULT_PASSWORD);

    // login as service manager
    serviceServiceManager =
        ServiceFactory.getDefault()
            .getTriggerDefinitionService(
                setup.getServiceManagerUserKey(), WebserviceTestBase.DEFAULT_PASSWORD);
  }

  @Test
  public void getTriggerTypes_supplierAdmin() throws Exception {
    // given
    // when
    List<TriggerType> result = serviceSupplier.getTriggerTypes();

    // then
    assertNotNull(result);
    Set<TriggerType> triggerTypesSet = new HashSet<TriggerType>();
    triggerTypesSet.addAll(Arrays.asList(allowedTriggersForSupplier));
    // supplier is also customer
    triggerTypesSet.addAll(Arrays.asList(allowedTriggersForCustomer));

    for (TriggerType triggerType : triggerTypesSet) {
      if (!result.contains(triggerType)) {
        fail();
      }
    }
    assertEquals(triggerTypesSet.size(), result.size());
  }

  @Test
  public void getTriggerTypes_customerAdmin() throws Exception {
    // given
    // when
    List<TriggerType> result = serviceCustomer.getTriggerTypes();

    // then
    assertNotNull(result);
    for (TriggerType triggerType : allowedTriggersForCustomer) {
      if (!result.contains(triggerType)) {
        fail();
      }
    }
    assertEquals(allowedTriggersForCustomer.length, result.size());
  }

  @Test
  public void getTriggerTypes_nonAdmin() throws Exception {
    try {
      // given
      // when
      serviceServiceManager.getTriggerTypes();
    } catch (SOAPFaultException ex) {
      // then
      ex.printStackTrace();
      validateExceptionContent(ex);
    }
  }

  @Test
  public void getTriggerDefinitions() throws Exception {

    List<VOTriggerDefinition> triggerDefinitions = serviceSupplier.getTriggerDefinitions();
    assertNotNull(triggerDefinitions);
    assertEquals(0, triggerDefinitions.size());
  }

  @Test
  public void crudTriggerDefinition() throws Exception {
    VOTriggerDefinition triggerCreate = createVOTriggerDefinition();

    // create
    serviceSupplier.createTriggerDefinition(triggerCreate);

    // read
    List<VOTriggerDefinition> triggerDefinitions = serviceSupplier.getTriggerDefinitions();
    assertNotNull(triggerDefinitions);
    assertEquals(1, triggerDefinitions.size());
    assertEquals(triggerCreate.getName(), triggerDefinitions.get(0).getName());
    assertEquals(triggerCreate.getTarget(), triggerDefinitions.get(0).getTarget());
    assertEquals(triggerCreate.getType(), triggerDefinitions.get(0).getType());
    assertEquals(triggerCreate.getTargetType(), triggerDefinitions.get(0).getTargetType());
    assertTrue(triggerCreate.isSuspendProcess() == triggerDefinitions.get(0).isSuspendProcess());

    // update
    String name = "nameUpdate";
    String target = "targetUpdate";
    TriggerType triggerType = TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER;
    TriggerTargetType targetType = TriggerTargetType.WEB_SERVICE;
    boolean suspended = false;

    VOTriggerDefinition triggerUpdate = triggerDefinitions.get(0);
    triggerUpdate.setName(name);
    triggerUpdate.setTarget(target);
    triggerUpdate.setType(triggerType);
    triggerUpdate.setTargetType(targetType);
    triggerUpdate.setSuspendProcess(suspended);
    serviceSupplier.updateTriggerDefinition(triggerUpdate);

    // read
    triggerDefinitions = serviceSupplier.getTriggerDefinitions();
    assertNotNull(triggerDefinitions);
    assertEquals(1, triggerDefinitions.size());
    assertEquals(name, triggerDefinitions.get(0).getName());
    assertEquals(target, triggerDefinitions.get(0).getTarget());
    assertEquals(triggerType, triggerDefinitions.get(0).getType());
    assertEquals(targetType, triggerDefinitions.get(0).getTargetType());
    assertTrue(suspended == triggerDefinitions.get(0).isSuspendProcess());

    // delete
    serviceSupplier.deleteTriggerDefinition(triggerDefinitions.get(0).getKey());

    // read
    triggerDefinitions = serviceSupplier.getTriggerDefinitions();
    assertNotNull(triggerDefinitions);
    assertEquals(0, triggerDefinitions.size());
  }

  @Test(expected = SOAPFaultException.class)
  public void createTriggerDefinition_Null() throws Exception {
    serviceSupplier.createTriggerDefinition(null);
  }

  @Test(expected = SOAPFaultException.class)
  public void updateTriggerDefinition_Null() throws Exception {
    serviceSupplier.updateTriggerDefinition(null);
  }

  @Test(expected = ObjectNotFoundException.class)
  public void deleteTriggerDefinition_Zero() throws Exception {
    serviceSupplier.deleteTriggerDefinition(0);
  }

  //    @Test(expected = ClientTransportException.class)
  //    public void getTriggerTypes_WrongPassword() throws Exception {
  //        TriggerDefinitionService serviceX = ServiceFactory.getDefault()
  //                .getTriggerDefinitionService(setup.getSupplierUserKey(),
  //                        "WrongPassword");
  //        serviceX.getTriggerTypes();
  //        fail();
  //    }

  @Test(expected = ValidationException.class)
  public void createTriggerDefinition_CustomerAdmin() throws Exception {

    VOTriggerDefinition triggerCreate = createVOTriggerDefinition();
    serviceCustomer.createTriggerDefinition(triggerCreate);
  }

  @Test
  public void createTriggerDefinition_NonAdmin() throws Exception {
    VOTriggerDefinition triggerCreate = createVOTriggerDefinition();
    try {
      serviceServiceManager.createTriggerDefinition(triggerCreate);
    } catch (SOAPFaultException ex) {
      validateExceptionContent(ex);
    }
  }

  @Test
  public void updateTriggerDefinition_NonAdmin() throws Exception {

    VOTriggerDefinition triggerCreate = createVOTriggerDefinition();
    serviceSupplier.createTriggerDefinition(triggerCreate);
    List<VOTriggerDefinition> triggerDefinitions = serviceSupplier.getTriggerDefinitions();
    assertNotNull(triggerDefinitions);
    assertEquals(1, triggerDefinitions.size());

    try {
      serviceServiceManager.updateTriggerDefinition(triggerCreate);
    } catch (SOAPFaultException ex) {
      validateExceptionContent(ex);
    } finally {
      // cleanup
      serviceSupplier.deleteTriggerDefinition(triggerDefinitions.get(0).getKey());
    }
  }

  @Test
  public void deleteTriggerDefinition_NonAdmin() throws Exception {

    VOTriggerDefinition triggerCreate = createVOTriggerDefinition();
    serviceSupplier.createTriggerDefinition(triggerCreate);

    List<VOTriggerDefinition> triggerDefinitions = serviceSupplier.getTriggerDefinitions();
    assertNotNull(triggerDefinitions);
    assertEquals(1, triggerDefinitions.size());

    try {
      serviceServiceManager.deleteTriggerDefinition(triggerDefinitions.get(0).getKey());
    } catch (SOAPFaultException ex) {
      validateExceptionContent(ex);
    } finally {
      // cleanup
      serviceSupplier.deleteTriggerDefinition(triggerDefinitions.get(0).getKey());
    }
  }
}
