/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Event;
import org.oscm.domobjects.OperationParameter;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterOption;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TechnicalProductHistory;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.enums.BillingAdapterIdentifier;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.types.enumtypes.OperationParameterType;
import org.oscm.types.enumtypes.ProvisioningType;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

public class TechnicalProducts {
    
    private static final String EXTERNAL_BILLING_ID = "External Billing System";
    
    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final String PROVISIONING_VERSION = "1.0";
    
    public static TechnicalProduct createTechnicalProduct(DataService mgr,
            Organization organization, String id, boolean asyncProvisioning,
            ServiceAccessType accessType) throws NonUniqueBusinessKeyException {
        return createTechnicalProduct(mgr, organization, id, asyncProvisioning,
                accessType, BillingAdapterIdentifier.NATIVE_BILLING.toString(), false);
    }

    public static TechnicalProduct createTechnicalProduct(DataService mgr,
            Organization organization, String id, boolean asyncProvisioning,
            ServiceAccessType accessType, boolean onBehalfEnabled) throws NonUniqueBusinessKeyException {
        return createTechnicalProduct(mgr, organization, id, asyncProvisioning,
                accessType, BillingAdapterIdentifier.NATIVE_BILLING.toString(), onBehalfEnabled);
    }
    
    public static TechnicalProduct createTechnicalProduct(DataService mgr,
            Organization organization, String id, boolean asyncProvisioning,
            ServiceAccessType accessType, boolean onBehalfEnabled, boolean externalBillinSystem) throws NonUniqueBusinessKeyException {
        
        if(Boolean.TRUE.equals(externalBillinSystem)){
            return createTechnicalProduct(mgr, organization, id, asyncProvisioning,
                    accessType, EXTERNAL_BILLING_ID, onBehalfEnabled);
        }
        
        return createTechnicalProduct(mgr, organization, id, asyncProvisioning,
                accessType, BillingAdapterIdentifier.NATIVE_BILLING.toString(), onBehalfEnabled);
    }

    public static TechnicalProduct createTechnicalProduct(DataService mgr,
                                                          Organization organization, String id, boolean asyncProvisioning,
                                                          ServiceAccessType accessType, String billingIdentifier, boolean onBehalfOf)
                    throws NonUniqueBusinessKeyException {
        TechnicalProduct techProd = new TechnicalProduct();
        techProd.setOrganization(organization);
        techProd.setTechnicalProductId(id);
        techProd.setBaseURL("http://localhost:8080/baseURL");
        techProd.setProvisioningURL(
                "http://someserver:80/someforward/provisioningURL");
        techProd.setProvisioningVersion(PROVISIONING_VERSION);
        techProd.setLoginPath("/loginURL");
        techProd.setAccessType(accessType);
        techProd.setTechnicalProductBuildId("Build " + new Date());
        techProd.setBillingIdentifier(billingIdentifier);
        techProd.setAllowingOnBehalfActing(onBehalfOf);

        if (asyncProvisioning) {
            techProd.setProvisioningType(ProvisioningType.ASYNCHRONOUS);
            techProd.setProvisioningTimeout(new Long(5));
        } else {
            techProd.setProvisioningType(ProvisioningType.SYNCHRONOUS);
        }
        mgr.persist(techProd);
        return techProd;
    }

    public static TechnicalProductOperation addTechnicalProductOperation(
            DataService mgr, TechnicalProduct tProd, String operationId,
            String actionURL) throws NonUniqueBusinessKeyException {
        TechnicalProductOperation op = new TechnicalProductOperation();
        op.setActionUrl(actionURL);
        op.setOperationId(operationId);
        op.setTechnicalProduct(tProd);
        mgr.persist(op);
        tProd.getTechnicalProductOperations().add(op);
        mgr.flush();
        return op;
    }

    public static OperationParameter addOperationParameter(DataService ds,
            TechnicalProductOperation tpo, String id, boolean mandatory,
            OperationParameterType type) throws NonUniqueBusinessKeyException {
        OperationParameter op = createOperationParameter(id, mandatory, type);
        op.setTechnicalProductOperation(tpo);
        ds.persist(op);
        tpo.getParameters().add(op);
        ds.flush();
        return op;
    }

    /**
     * Just creates the domain object, doesn't persist
     */
    public static OperationParameter createOperationParameter(String id,
            boolean mandatory, OperationParameterType type) {
        OperationParameter op = new OperationParameter();
        op.setId(id);
        op.setMandatory(mandatory);
        op.setType(type);
        return op;
    }

    public static TechnicalProduct findTechnicalProduct(DataService mgr,
            Organization organization, String id) {
        TechnicalProduct techProd = new TechnicalProduct();
        techProd.setOrganization(organization);
        techProd.setTechnicalProductId(id);
        return (TechnicalProduct) mgr.find(techProd);
    }

    public static TechnicalProduct findOrCreateTechnicalProduct(DataService mgr,
            Organization organization, String id, ServiceAccessType accessType)
                    throws NonUniqueBusinessKeyException {
        TechnicalProduct tp = findTechnicalProduct(mgr, organization, id);
        if (tp == null) {
            tp = TechnicalProducts.createTechnicalProduct(mgr, organization, id,
                    false, accessType);
        }
        return tp;
    }

    public static ParameterDefinition addParameterDefinition(
            ParameterValueType valueType, String parameterId,
            ParameterType parameterType, TechnicalProduct technicalProduct,
            DataService mgr, Long max, Long min, boolean configurable)
                    throws NonUniqueBusinessKeyException {
        return addParameterDefinition(valueType, parameterId, parameterType,
                technicalProduct, mgr, max, min, configurable,
                ParameterModificationType.STANDARD);
    }

    public static ParameterDefinition addParameterDefinition(
            ParameterValueType valueType, String parameterId,
            ParameterType parameterType, TechnicalProduct technicalProduct,
            DataService mgr, Long max, Long min, boolean configurable,
            ParameterModificationType modificationType)
                    throws NonUniqueBusinessKeyException {
        List<ParameterDefinition> paramDefs = new ArrayList<ParameterDefinition>();
        paramDefs.addAll(technicalProduct.getParameterDefinitions());
        ParameterDefinition paramDef = new ParameterDefinition();
        paramDef.setConfigurable(configurable);
        paramDef.setValueType(valueType);
        paramDef.setParameterId(parameterId);
        paramDef.setParameterType(parameterType);
        paramDef.setMaximumValue(max);
        paramDef.setMinimumValue(min);
        paramDef.setTechnicalProduct(technicalProduct);
        paramDef.setModificationType(modificationType);
        paramDefs.add(paramDef);
        technicalProduct.setParameterDefinitions(paramDefs);
        mgr.persist(paramDef);
        mgr.flush();
        return paramDef;
    }

    public static ParameterOption addParameterOption(
            ParameterDefinition paramDef, String optionId, DataService mgr)
                    throws NonUniqueBusinessKeyException {
        Assert.assertEquals("parameter definition is not an enumeration",
                ParameterValueType.ENUMERATION, paramDef.getValueType());
        ParameterOption option = new ParameterOption();
        option.setOptionId(optionId);
        option.setParameterDefinition(paramDef);
        paramDef.getOptionList().add(option);
        mgr.persist(option);
        mgr.flush();
        return option;
    }

    /**
     * Creates an event definition object for the given technical product.
     * 
     * @param eventIdentifier
     *            The identifier the event should have.
     * @param eventType
     *            The type the event should have.
     * @param tProd
     *            The technical product to add the event to.
     * @param mgr
     *            The data manager reference.
     * @return The created event.
     * @throws NonUniqueBusinessKeyException
     */
    public static Event addEvent(String eventIdentifier, EventType eventType,
            TechnicalProduct tProd, DataService mgr)
                    throws NonUniqueBusinessKeyException {
        Event evt = new Event();
        evt.setEventIdentifier(eventIdentifier);
        evt.setEventType(eventType);
        evt.setTechnicalProduct(tProd);
        tProd.getEvents().add(evt);
        mgr.persist(evt);
        mgr.flush();
        return evt;
    }

    /**
     * Creates a role definition with the given role identifier for the
     * specified technical product.
     * 
     * @param roleIdentifier
     *            The role identifier to be set.
     * @param tProd
     *            The technical product to create the role definition for.
     * @param mgr
     *            The data manager reference.
     * @param historyModificationTime
     *            Time of history modification. Used for changing modification
     *            date of history data.
     * @return The created role definition.
     * @throws NonUniqueBusinessKeyException
     */
    public static RoleDefinition addRoleDefinition(String roleIdentifier,
            TechnicalProduct tProd, DataService mgr,
            Long historyModificationTime) throws NonUniqueBusinessKeyException {
        RoleDefinition rd = new RoleDefinition();
        rd.setRoleId(roleIdentifier);
        rd.setTechnicalProduct(tProd);
        if (historyModificationTime != null) {
            rd.setHistoryModificationTime(historyModificationTime);
        }
        tProd.getRoleDefinitions().add(rd);
        mgr.persist(rd);
        mgr.flush();
        return rd;
    }

    /**
     * Creates a role definition with the given role identifier for the
     * specified technical product.
     * 
     * @param roleIdentifier
     *            The role identifier to be set.
     * @param tProd
     *            The technical product to create the role definition for.
     * @param mgr
     *            The data manager reference. date of history data.
     * @return The created role definition.
     * @throws NonUniqueBusinessKeyException
     */
    public static RoleDefinition addRoleDefinition(String roleIdentifier,
            TechnicalProduct tProd, DataService mgr)
                    throws NonUniqueBusinessKeyException {
        return addRoleDefinition(roleIdentifier, tProd, mgr, null);
    }

    /**
     * Creates the given number of technical services, in case they do not exist
     * yet.
     * 
     * @return List of TechnicalProduct
     * @throws NonUniqueBusinessKeyException
     */
    public static List<TechnicalProduct> createTestData(DataService dm,
            Organization org, int totalNumber)
                    throws NonUniqueBusinessKeyException {
        List<TechnicalProduct> technicalProducts = new ArrayList<TechnicalProduct>();
        for (int i = 0; i < totalNumber; i++) {
            TechnicalProduct tProd = findTechnicalProduct(dm, org,
                    "Mass Data " + i);
            if (tProd == null) {
                tProd = createTechnicalProduct(dm, org, "Mass Data " + i, false,
                        ServiceAccessType.LOGIN);
                RoleDefinition roleDef1 = TechnicalProducts
                        .addRoleDefinition("Role1", tProd, dm);
                LocalizedResources.localizeRoleDefinition(dm,
                        roleDef1.getKey());
                RoleDefinition roleDef2 = TechnicalProducts
                        .addRoleDefinition("Role2", tProd, dm);
                LocalizedResources.localizeRoleDefinition(dm,
                        roleDef2.getKey());
                Event event1 = TechnicalProducts.addEvent("Event1",
                        EventType.SERVICE_EVENT, tProd, dm);
                LocalizedResources.localizeEvent(dm, event1.getKey());
                Event event2 = TechnicalProducts.addEvent("Event2",
                        EventType.SERVICE_EVENT, tProd, dm);
                LocalizedResources.localizeEvent(dm, event2.getKey());
                ParameterDefinition paramDefLong = TechnicalProducts
                        .addParameterDefinition(ParameterValueType.LONG,
                                "ParamLong", ParameterType.SERVICE_PARAMETER,
                                tProd, dm, Long.valueOf(500), Long.valueOf(0),
                                true);
                LocalizedResources.localizeParameterDef(dm,
                        paramDefLong.getKey());
                ParameterDefinition paramDefBool = TechnicalProducts
                        .addParameterDefinition(ParameterValueType.BOOLEAN,
                                "ParamBool", ParameterType.SERVICE_PARAMETER,
                                tProd, dm, null, null, true);
                LocalizedResources.localizeParameterDef(dm,
                        paramDefBool.getKey());
                ParameterDefinition paramDefString = TechnicalProducts
                        .addParameterDefinition(ParameterValueType.STRING,
                                "ParamString", ParameterType.SERVICE_PARAMETER,
                                tProd, dm, null, null, true);
                LocalizedResources.localizeParameterDef(dm,
                        paramDefString.getKey());
                ParameterDefinition paramDefEnum = TechnicalProducts
                        .addParameterDefinition(ParameterValueType.ENUMERATION,
                                "ParamEnum", ParameterType.SERVICE_PARAMETER,
                                tProd, dm, null, null, true);
                LocalizedResources.localizeParameterDef(dm,
                        paramDefEnum.getKey());
                ParameterOption opt1 = TechnicalProducts
                        .addParameterOption(paramDefEnum, "ParamOption1", dm);
                LocalizedResources.localizeParameterDefOption(dm,
                        opt1.getKey());
                ParameterOption opt2 = TechnicalProducts
                        .addParameterOption(paramDefEnum, "ParamOption2", dm);
                LocalizedResources.localizeParameterDefOption(dm,
                        opt2.getKey());
                ParameterDefinition paramDefInt = TechnicalProducts
                        .addParameterDefinition(ParameterValueType.INTEGER,
                                "ParamInt", ParameterType.SERVICE_PARAMETER,
                                tProd, dm, null, null, true);
                LocalizedResources.localizeParameterDef(dm,
                        paramDefInt.getKey());

                TechnicalProducts.addTechnicalProductOperation(dm, tProd,
                        "operation1", "http://actionhost.actionDomain/action");
            }
            technicalProducts.add(tProd);
        }
        return technicalProducts;
    }

    public static void createTechnicalProductHistory(final DataService ds,
            final long organizationKey, final long prdObjKey,
            final String modificationDate, final int version,
            final ModificationType modificationType) throws Exception {
        TechnicalProductHistory prdHist = new TechnicalProductHistory();

        prdHist.setInvocationDate(new Date());
        prdHist.setObjKey(prdObjKey);
        prdHist.setObjVersion(version);
        prdHist.setModdate(
                new SimpleDateFormat(DATE_PATTERN).parse(modificationDate));
        prdHist.setModtype(modificationType);
        prdHist.setModuser("moduser");
        prdHist.getDataContainer().setBillingIdentifier(
                BillingAdapterIdentifier.NATIVE_BILLING.toString());

        prdHist.setOrganizationObjKey(organizationKey);
        prdHist.getDataContainer().setProvisioningURL("http://www.fujitsu.de");
        prdHist.getDataContainer().setTechnicalProductId("technicalProductId");
        prdHist.getDataContainer().setAllowingOnBehalfActing(false);

        ds.persist(prdHist);
    }
}
