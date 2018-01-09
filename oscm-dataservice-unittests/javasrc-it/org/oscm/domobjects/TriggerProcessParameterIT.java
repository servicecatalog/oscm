/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Peter Pock                                         
 *                                                                              
 *  Creation Date: 15.06.2010                                                      
 *                                                                              
 *  Completion Time: 15.06.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.oscm.test.Numbers.L_TIMESTAMP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.converter.ResourceLoader;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.BaseVO;
import org.oscm.internal.vo.VOEventDefinition;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOOrganizationPaymentConfiguration;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOParameterOption;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOPricedEvent;
import org.oscm.internal.vo.VOPricedOption;
import org.oscm.internal.vo.VOPricedParameter;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.test.ReflectiveClone;
import org.oscm.types.enumtypes.TriggerProcessParameterName;

/**
 * Test of the TriggerProcess domain object.
 * 
 * @author pock
 * 
 */
public class TriggerProcessParameterIT extends DomainObjectTestBase {

    private final List<TriggerProcessParameter> objList = new ArrayList<>();

    private void verify(ModificationType modType) throws Exception {
        verify(modType, objList, TriggerProcessParameter.class);
    }

    @Test
    public void testAdd() throws Exception {
        final TriggerProcess clone = createTriggerProcessWithParams();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerProcess triggerProcess = mgr
                        .getReference(TriggerProcess.class, clone.getKey());
                TriggerProcessParameter param = triggerProcess
                        .getTriggerProcessParameters().get(0);
                Assert.assertEquals(TEST_MAIL_ADDRESS,
                        param.getValue(String.class));
                return null;
            }
        });
        verify(ModificationType.ADD);
    }

    @Test
    public void testModify() throws Exception {
        final TriggerProcess clone = createTriggerProcessWithParams();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerProcess triggerProcess = mgr
                        .getReference(TriggerProcess.class, clone.getKey());

                TriggerProcessParameter param = triggerProcess
                        .getTriggerProcessParameters().get(0);
                param.setName(TriggerProcessParameterName.OBJECT_ID);
                param.setValue("test");
                objList.remove(0);
                objList.add(
                        (TriggerProcessParameter) ReflectiveClone.clone(param));

                Assert.assertEquals(clone.getKey(),
                        param.getTriggerProcess().getKey());
                return null;
            }
        });
        verify(ModificationType.MODIFY);
    }

    @Test
    public void testDelete() throws Exception {
        createTriggerProcessWithParams();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mgr.remove(mgr.getReference(TriggerProcessParameter.class,
                        objList.get(0).getKey()));
                return null;
            }
        });
        verify(ModificationType.DELETE);
    }

    @Test
    public void testSerializeVOParameterOption() throws Exception {
        VOParameterOption voIn = new VOParameterOption("optionId",
                "optionDescription", "paramDefId");
        createTriggerProcessWithParam(voIn);
        VOParameterOption voOut = getTriggerProcessParameterValue(
                VOParameterOption.class);

        String result = doCompare(voIn, voOut);
        Assert.assertTrue(result, result.length() == 0);
    }

    @Test
    public void testSerializeVOPricesOption() throws Exception {
        VOPricedOption voIn = new VOPricedOption();
        voIn.setPricePerSubscription(BigDecimal.valueOf(100));
        voIn.setPricePerUser(BigDecimal.valueOf(10));

        createTriggerProcessWithParam(voIn);
        VOPricedOption voOut = getTriggerProcessParameterValue(
                VOPricedOption.class);

        String result = doCompare(voIn, voOut);
        Assert.assertTrue(result, result.length() == 0);
    }

    @Test
    public void testSerializeVOParameterDefinition() throws Exception {
        VOParameterDefinition voIn = new VOParameterDefinition(
                ParameterType.PLATFORM_PARAMETER, "id", "description",
                ParameterValueType.INTEGER, "16", new Long(4), new Long(40),
                false, true, null);

        createTriggerProcessWithParam(voIn);
        VOParameterDefinition voOut = getTriggerProcessParameterValue(
                VOParameterDefinition.class);

        String result = doCompare(voIn, voOut);
        Assert.assertTrue(result, result.length() == 0);
    }

    @Test
    public void testSerializeVOParameter() throws Exception {
        VOParameterDefinition def = new VOParameterDefinition(
                ParameterType.PLATFORM_PARAMETER, "id", "description",
                ParameterValueType.INTEGER, "16", new Long(4), new Long(40),
                false, true, null);
        VOParameter voIn = new VOParameter(def);
        voIn.setConfigurable(true);
        voIn.setValue("20");

        createTriggerProcessWithParam(voIn);
        VOParameter voOut = getTriggerProcessParameterValue(VOParameter.class);

        String result = doCompare(voIn, voOut);
        Assert.assertTrue(result, result.length() == 0);
    }

    @Test
    public void testSerializeVOPricedParameter() throws Exception {
        VOParameterDefinition def = new VOParameterDefinition(
                ParameterType.PLATFORM_PARAMETER, "id", "description",
                ParameterValueType.INTEGER, "16", new Long(4), new Long(40),
                false, true, null);
        VOPricedParameter voIn = new VOPricedParameter(def);
        voIn.setParameterKey(10);
        voIn.setPricePerSubscription(BigDecimal.valueOf(100));
        voIn.setPricePerUser(BigDecimal.valueOf(10));

        createTriggerProcessWithParam(voIn);
        VOPricedParameter voOut = getTriggerProcessParameterValue(
                VOPricedParameter.class);

        String result = doCompare(voIn, voOut);
        Assert.assertTrue(result, result.length() == 0);
    }

    @Test
    public void testSerializeVOPricedParameterWithOption() throws Exception {
        List<VOParameterOption> options = new ArrayList<>();
        options.add(new VOParameterOption("optionId", "optionDescription",
                "paramDefId"));

        VOParameterDefinition def = new VOParameterDefinition(
                ParameterType.SERVICE_PARAMETER, "id", "description",
                ParameterValueType.ENUMERATION, null, null, null, false, true,
                options);
        VOPricedParameter voIn = new VOPricedParameter(def);
        voIn.setParameterKey(10);
        voIn.setPricePerSubscription(BigDecimal.valueOf(100));
        voIn.setPricePerUser(BigDecimal.valueOf(10));

        List<VOPricedOption> pricedOptions = new ArrayList<>();
        VOPricedOption priceOption = new VOPricedOption();
        priceOption.setPricePerSubscription(BigDecimal.valueOf(100));
        priceOption.setPricePerUser(BigDecimal.valueOf(10));
        pricedOptions.add(priceOption);
        voIn.setPricedOptions(pricedOptions);

        createTriggerProcessWithParam(voIn);
        VOPricedParameter voOut = getTriggerProcessParameterValue(
                VOPricedParameter.class);

        String result = doCompare(voIn, voOut);
        Assert.assertTrue(result, result.length() == 0);
    }

    @Test
    public void testSerializeVOEventDefinition() throws Exception {
        VOEventDefinition voIn = new VOEventDefinition();
        voIn.setEventDescription("description");
        voIn.setEventId("eventId");
        voIn.setEventType(EventType.PLATFORM_EVENT);

        createTriggerProcessWithParam(voIn);
        VOEventDefinition voOut = getTriggerProcessParameterValue(
                VOEventDefinition.class);

        String result = doCompare(voIn, voOut);
        Assert.assertTrue(result, result.length() == 0);
    }

    @Test
    public void testSerializeVOPricedEvent() throws Exception {
        VOPricedEvent voIn = new VOPricedEvent();
        voIn.setEventDefinition(new VOEventDefinition());
        voIn.getEventDefinition().setEventDescription("eventDescription");
        voIn.getEventDefinition().setEventId("eventId");
        voIn.setEventPrice(BigDecimal.valueOf(10));

        createTriggerProcessWithParam(voIn);
        VOPricedEvent voOut = getTriggerProcessParameterValue(
                VOPricedEvent.class);

        String result = doCompare(voIn, voOut);
        Assert.assertTrue(result, result.length() == 0);
    }

    @Test
    public void testSerializeVOPriceModel() throws Exception {
        VOPriceModel voIn = new VOPriceModel();
        voIn.setType(PriceModelType.PRO_RATA);
        voIn.setCurrencyISOCode("EUR");
        voIn.setDescription("description");
        voIn.setOneTimeFee(BigDecimal.valueOf(100l));
        voIn.setPeriod(PricingPeriod.MONTH);
        voIn.setPricePerPeriod(BigDecimal.valueOf(6l));
        voIn.setPricePerUserAssignment(BigDecimal.valueOf(4l));

        createTriggerProcessWithParam(voIn);
        VOPriceModel voOut = getTriggerProcessParameterValue(
                VOPriceModel.class);

        String result = doCompare(voIn, voOut);
        Assert.assertTrue(result, result.length() == 0);
    }

    @Test
    public void testSerializeVOService() throws Exception {
        VOService voIn = new VOService();
        voIn.setKey(1);
        voIn.setVersion(10);
        voIn.setParameters(null);
        voIn.setPriceModel(null);
        voIn.setAccessType(ServiceAccessType.LOGIN);
        voIn.setStatus(ServiceStatus.ACTIVE);

        createTriggerProcessWithParam(voIn);
        VOService voOut = getTriggerProcessParameterValue(VOService.class);

        String result = doCompare(voIn, voOut);
        Assert.assertTrue(result, result.length() == 0);
    }

    @Test
    public void testSerializeVOUserDetails() throws Exception {
        VOUserDetails voIn = new VOUserDetails();
        voIn.setAdditionalName("additionalName");
        voIn.setAddress("address");
        voIn.setEMail("mail");
        voIn.setFirstName("firstName");
        voIn.setLastName("lastName");
        voIn.setLocale("en");
        voIn.addUserRole(UserRoleType.ORGANIZATION_ADMIN);
        voIn.setOrganizationId("organizationId");
        voIn.setPhone("phone");
        voIn.setRemoteLdapAttributes(new ArrayList<>(
                Arrays.asList(SettingType.LDAP_ATTR_FIRST_NAME,
                        SettingType.LDAP_ATTR_LAST_NAME)));
        voIn.setSalutation(Salutation.MR);
        voIn.setStatus(UserAccountStatus.ACTIVE);
        voIn.setUserId("userId");
        voIn.getUserRoles().add(UserRoleType.SERVICE_MANAGER);

        createTriggerProcessWithParam(voIn);
        VOUserDetails voOut = getTriggerProcessParameterValue(
                VOUserDetails.class);

        String result = doCompare(voIn, voOut);
        Assert.assertTrue(result, result.length() == 0);
    }

    @Test
    public void testSerializeVOUsageLicense() throws Exception {
        VOUsageLicense voIn = new VOUsageLicense();
        voIn.setApplicationUserId("applicationUserId");

        createTriggerProcessWithParam(voIn);
        VOUsageLicense voOut = getTriggerProcessParameterValue(
                VOUsageLicense.class);

        String result = doCompare(voIn, voOut);
        Assert.assertTrue(result, result.length() == 0);
    }

    @Test
    public void testSerializeVOSubscriptionDetails() throws Exception {
        VOSubscriptionDetails voIn = new VOSubscriptionDetails();
        voIn.setActivationDate(L_TIMESTAMP);
        voIn.setCreationDate(L_TIMESTAMP);
        voIn.setServiceAccessInfo("serviceAccessInfo");
        voIn.setServiceAccessType(ServiceAccessType.LOGIN);
        voIn.setServiceBaseURL("http://");
        voIn.setServiceId("serviceId");
        voIn.setServiceKey(1234);
        voIn.setServiceInstanceId("serviceInstanceId");
        voIn.setServiceLoginPath("serviceLoginPath");
        voIn.setPurchaseOrderNumber("purchaseOrderNumber");
        voIn.setStatus(SubscriptionStatus.ACTIVE);
        voIn.setSubscriptionId("subscriptionId");
        voIn.setTimeoutMailSent(true);

        createTriggerProcessWithParam(voIn);
        VOSubscriptionDetails voOut = getTriggerProcessParameterValue(
                VOSubscriptionDetails.class);

        String result = doCompare(voIn, voOut);
        Assert.assertTrue(result, result.length() == 0);
    }

    @Test
    public void testSerializeVOPaymentType() throws Exception {
        VOPaymentType voIn = new VOPaymentType();
        voIn.setCollectionType(PaymentCollectionType.ORGANIZATION);
        voIn.setPaymentTypeId(CREDIT_CARD);

        createTriggerProcessWithParam(voIn);
        VOPaymentType voOut = getTriggerProcessParameterValue(
                VOPaymentType.class);

        String result = doCompare(voIn, voOut);
        Assert.assertTrue(result, result.length() == 0);
    }

    @Test
    public void testSerializeVOOrganizationPaymentConfiguration()
            throws Exception {
        VOPaymentType paymentType = new VOPaymentType();
        paymentType.setCollectionType(PaymentCollectionType.ORGANIZATION);
        paymentType.setPaymentTypeId(CREDIT_CARD);

        VOOrganizationPaymentConfiguration voIn = new VOOrganizationPaymentConfiguration();
        voIn.setEnabledPaymentTypes(
                new HashSet<>(Arrays.asList(paymentType)));
        voIn.setOrganization(new VOOrganization());

        List<VOOrganizationPaymentConfiguration> listIn = new ArrayList<>();
        listIn.add(voIn);

        createTriggerProcessWithParam(listIn);
        List<?> listOut = getTriggerProcessParameterValue(List.class);

        Assert.assertEquals(listIn.size(), listOut.size());
        String result = doCompare(listOut, listIn);
        Assert.assertTrue(result, result.length() == 0);
    }

    @Test(expected = ClassCastException.class)
    public void testSerializeVOServiceComplex() throws Exception {
        String serializedData = getSerializedDataFromFile(
                "SerializedVOService.xml");

        TriggerProcessParameter param = new TriggerProcessParameter();
        param.getDataContainer().setSerializedValue(serializedData);

        param.getValue(VOService.class);
    }

    @Test
    public void testSerializeVOServiceComplex2() throws Exception {
        String serializedData = getSerializedDataFromFile(
                "SerializedVOService.xml");

        TriggerProcessParameter param = new TriggerProcessParameter();
        param.getDataContainer()
                .setSerializedValue(AESEncrypter.encrypt(serializedData));

        VOService service = TriggerProcessParameterData.getVOFromSerialization(
                VOService.class, param.getValue(String.class));
        Assert.assertNotNull(service);
    }

    private TriggerProcess createTriggerProcessWithParams() throws Exception {
        return runTX(new Callable<TriggerProcess>() {
            @Override
            public TriggerProcess call() throws Exception {
                TriggerProcess triggerProcess = TriggerProcessIT
                        .createTriggerProcess(mgr, TriggerType.ACTIVATE_SERVICE,
                                "http://localhost", true);

                TriggerProcessParameter param = triggerProcess
                        .addTriggerProcessParameter(
                                TriggerProcessParameterName.OBJECT_ID,
                                TEST_MAIL_ADDRESS);

                mgr.flush();
                objList.add(
                        (TriggerProcessParameter) ReflectiveClone.clone(param));

                return (TriggerProcess) ReflectiveClone.clone(triggerProcess);
            }
        });
    }

    private TriggerProcess createTriggerProcessWithParam(final Object obj)
            throws Exception {
        return runTX(new Callable<TriggerProcess>() {
            @Override
            public TriggerProcess call() throws Exception {
                TriggerProcess triggerProcess = TriggerProcessIT
                        .createTriggerProcess(mgr, TriggerType.ACTIVATE_SERVICE,
                                "http://localhost", true);

                TriggerProcessParameter param = triggerProcess
                        .addTriggerProcessParameter(
                                TriggerProcessParameterName.OBJECT_ID, obj);

                mgr.flush();
                objList.add(
                        (TriggerProcessParameter) ReflectiveClone.clone(param));

                return (TriggerProcess) ReflectiveClone.clone(triggerProcess);
            }
        });
    }

    private <T> T getTriggerProcessParameterValue(final Class<T> clazz)
            throws Exception {
        return runTX(new Callable<T>() {
            @Override
            public T call() throws Exception {
                TriggerProcessParameter param = mgr.getReference(
                        TriggerProcessParameter.class, objList.get(0).getKey());
                return param.getValue(clazz);
            }
        });
    }

    private String doCompare(Object first, Object second) throws Exception {
        return doCompare(new ArrayList<>(), first, second);
    }

    private String doCompare(List<Object> parents, Object first, Object second)
            throws Exception {
        String result = "";

        for (Method m : first.getClass().getMethods()) {
            if ((!m.getName().startsWith("get")
                    && !m.getName().startsWith("is"))
                    || "getClass".equals(m.getName())
                    || m.getGenericParameterTypes().length > 0) {
                continue;
            }
            Object firstObj = m.invoke(first, (Object[]) null);
            Object secondObj = second.getClass()
                    .getMethod(m.getName(), (Class[]) null)
                    .invoke(second, (Object[]) null);
            if (firstObj instanceof BaseVO && secondObj instanceof BaseVO) {
                if (!parents.contains(firstObj)) {
                    parents.add(first);
                    result += doCompare(firstObj, secondObj);
                    parents.remove(first);
                }
            } else if (firstObj instanceof List<?>
                    && secondObj instanceof List<?>) {
                List<?> firstList = (List<?>) firstObj;
                List<?> secondList = (List<?>) secondObj;
                for (int i = 0; i < firstList.size(); i++) {
                    if (!parents.contains(firstList.get(i))) {
                        parents.add(first);
                        result += doCompare(parents, firstList.get(i),
                                secondList.get(i));
                        parents.remove(first);
                    }
                }
            } else if (firstObj != null || secondObj != null) {
                if (firstObj == null || !firstObj.equals(secondObj)) {
                    result = result + m.getName() + ": (" + firstObj + ") <-> ("
                            + secondObj + ")\n";
                }
            }
        }
        return result;
    }

    private String getSerializedDataFromFile(String fileName)
            throws IOException {
        InputStream is = ResourceLoader.getResourceAsStream(getClass(),
                fileName);
        InputStreamReader isr = new InputStreamReader(is);
        StringBuffer result = new StringBuffer();
        try (BufferedReader br = new BufferedReader(isr);) {
            String line = br.readLine();
            while (line != null) {
                result.append(line);
                line = br.readLine();
            }
        }
        return result.toString();
    }
}
