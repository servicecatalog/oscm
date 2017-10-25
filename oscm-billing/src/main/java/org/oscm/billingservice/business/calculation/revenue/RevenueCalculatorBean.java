/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;

import org.oscm.billingservice.business.calculation.BigDecimals;
import org.oscm.billingservice.business.calculation.revenue.model.BillingItemCosts;
import org.oscm.billingservice.business.calculation.revenue.model.OverallCosts;
import org.oscm.billingservice.business.calculation.revenue.model.ParameterCosts;
import org.oscm.billingservice.business.calculation.revenue.model.PriceModelInput;
import org.oscm.billingservice.business.calculation.revenue.model.SubscriptionInput;
import org.oscm.billingservice.business.calculation.revenue.model.UserAssignmentFactors;
import org.oscm.billingservice.business.model.billingresult.BasePeriodType;
import org.oscm.billingservice.business.model.billingresult.BillingDetailsType;
import org.oscm.billingservice.business.model.billingresult.BillingResultAssembler;
import org.oscm.billingservice.business.model.billingresult.BillingdataType;
import org.oscm.billingservice.business.model.billingresult.DiscountType;
import org.oscm.billingservice.business.model.billingresult.NormalizedCostsType;
import org.oscm.billingservice.business.model.billingresult.ObjectFactory;
import org.oscm.billingservice.business.model.billingresult.OrganizationalUnitType;
import org.oscm.billingservice.business.model.billingresult.ParameterType;
import org.oscm.billingservice.business.model.billingresult.ParametersType;
import org.oscm.billingservice.business.model.billingresult.PeriodFeeType;
import org.oscm.billingservice.business.model.billingresult.PriceModelType;
import org.oscm.billingservice.business.model.billingresult.PriceModelsType;
import org.oscm.billingservice.business.model.billingresult.SteppedPriceType;
import org.oscm.billingservice.business.model.billingresult.SteppedPricesType;
import org.oscm.billingservice.business.model.billingresult.SubscriptionType;
import org.oscm.billingservice.business.model.billingresult.UdaType;
import org.oscm.billingservice.business.model.billingresult.UserAssignmentCostsType;
import org.oscm.billingservice.business.model.billingresult.VATType;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.billingservice.dao.model.OrganizationAddressData;
import org.oscm.billingservice.dao.model.RolePricingData;
import org.oscm.billingservice.dao.model.RolePricingDetails;
import org.oscm.billingservice.dao.model.SteppedPriceData;
import org.oscm.billingservice.dao.model.SteppedPriceDetail;
import org.oscm.billingservice.dao.model.UdaBillingData;
import org.oscm.billingservice.dao.model.VatRateDetails;
import org.oscm.billingservice.dao.model.XParameterData;
import org.oscm.billingservice.dao.model.XParameterIdData;
import org.oscm.billingservice.dao.model.XParameterPeriodValue;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.billingservice.service.model.BillingInput.BillingContextType;
import org.oscm.converter.PriceConverter;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.PriceModelData;
import org.oscm.domobjects.PriceModelHistory;
import org.oscm.domobjects.RoleDefinitionHistory;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.UsageLicenseHistory;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.types.exceptions.BillingRunFailed;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * Session Bean implementation class BillingServiceBean
 */
@Stateless
@Local(RevenueCalculatorLocal.class)
public class RevenueCalculatorBean implements RevenueCalculatorLocal {

    private static final BigDecimal ZERO_NORMALIZED = BigDecimal.ZERO
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);

    private final ObjectFactory factory = new ObjectFactory();
    private final BillingResultAssembler assembler = new BillingResultAssembler();

    @EJB(beanInterface = LocalizerServiceLocal.class)
    private LocalizerServiceLocal localizer;

    @EJB(beanInterface = BillingDataRetrievalServiceLocal.class)
    BillingDataRetrievalServiceLocal bdr;

    @Resource
    SessionContext sessionCtx;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public BillingResult performBillingRunForSubscription(
            BillingInput billingInput) throws ObjectNotFoundException,
            NonUniqueBusinessKeyException {

        if (billingInput.getCurrencyIsoCode() == null) {
            // In this billing period only services with a free price model are
            // subscribed -> no billing but update status if timer based billing
            if (billingInput.isStoreBillingResult()) {
                bdr.updateBillingSubscriptionStatus(
                        billingInput.getSubscriptionKey(),
                        billingInput.getBillingPeriodEnd());
            }

            return createBillingResult(billingInput);
        }

        BillingResult result = null;
        try {
            result = initBillingResult(billingInput);
            BillingDetailsType billingDetails = createBillingDataForOrganization(
                    billingInput, result);

            if (billingDetailsIsNotEmpty(billingDetails)) {
                serializeBillingDetails(result, billingDetails);

                if (billingInput.isStoreBillingResult()) {
                    Schema schema = bdr.loadSchemaFiles();
                    if (BillingConditionsEvaluator.isValidBillingResult(
                            billingInput, schema, result)) {
                        bdr.updateBillingSubscriptionStatus(
                                billingInput.getSubscriptionKey(),
                                billingInput.getBillingPeriodEnd());
                    } else {
                        bdr.removeBillingResult(result);
                    }
                }
            } else {
                if (billingInput.isStoreBillingResult()) {
                    // No billing result, price model has a free period
                    bdr.removeBillingResult(result);
                    bdr.updateBillingSubscriptionStatus(
                            billingInput.getSubscriptionKey(),
                            billingInput.getBillingPeriodEnd());
                }
            }

            result.setUsergroupKey(billingInput.getUserGroupKey());
            return result;
        } catch (Exception e) {
            if (result != null) {
                // remove result manually since it is not done by rollback
                // action
                bdr.removeBillingResult(result);
            }

            sessionCtx.setRollbackOnly();

            throw e;
        }
    }

    private boolean billingDetailsIsNotEmpty(BillingDetailsType billingDetails) {
        return (billingDetails != null
                && billingDetails.getSubscriptions().getSubscription() != null && billingDetails
                .getSubscriptions().getSubscription().size() > 0);
    }

    private void serializeBillingDetails(BillingResult billingResult,
            BillingDetailsType billingDetails) {

        try {
            final JAXBContext context = JAXBContext
                    .newInstance(BillingdataType.class);
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", Boolean.FALSE);
            final BillingdataType billingdataType = new BillingdataType();
            billingdataType.getBillingDetails().add(billingDetails);
            marshaller.marshal(factory.createBillingdata(billingdataType), out);
            final String xml = new String(out.toByteArray(), "UTF-8");
            billingResult.setResultXML(xml.substring(
                    xml.indexOf("<Billingdata>") + 13,
                    xml.indexOf("</Billingdata>")).trim());
            billingResult.setGrossAmount(billingDetails.getOverallCosts()
                    .getGrossAmount());
            billingResult.setNetAmount(billingDetails.getOverallCosts()
                    .getNetAmount());
        } catch (JAXBException | UnsupportedEncodingException ex) {
            throw new BillingRunFailed(ex);
        }
    }

    /**
     * Initializes a new billing result. An empty billing result may be
     * persisted already.
     */
    private BillingResult initBillingResult(BillingInput billingInput)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException {

        BillingResult result = createBillingResult(billingInput);

        long chargingOrgKey = bdr
                .loadChargingOrgKeyForSubscription(billingInput
                        .getSubscriptionKey());
        long vendorKey = bdr.loadVendorKeyForSubscription(billingInput
                .getSubscriptionKey());
        result.setChargingOrgKey(chargingOrgKey);
        result.setVendorKey(vendorKey);
        result.setCurrency(bdr.loadSupportedCurrency(billingInput
                .getCurrencyIsoCode()));

        if (billingInput.isStoreBillingResult()) {
            bdr.persistBillingResult(result);
        }

        return result;
    }

    private BillingResult createBillingResult(BillingInput billingInput) {

        BillingResult result = new BillingResult();
        result.setCreationTime(System.currentTimeMillis());
        result.setOrganizationTKey(billingInput.getOrganizationKey());
        result.setPeriodStartTime(billingInput.getBillingPeriodStart());
        result.setPeriodEndTime(billingInput.getBillingPeriodEnd());
        result.setSubscriptionKey(Long.valueOf(billingInput
                .getSubscriptionKey()));
        result.setUsergroupKey(billingInput.getUserGroupKey());

        // as the result xml and grossAmout are a mandatory fields of the
        // BillingResult object, set an empty XML document for the time being.
        result.setNetAmount(ZERO_NORMALIZED);
        result.setGrossAmount(ZERO_NORMALIZED);
        result.setResultXML("");

        return result;
    }

    /**
     * Gathers all billing related information for the given organization in the
     * given time frame. The result will be a generated XML-document containing
     * the information.
     */
    BillingDetailsType createBillingDataForOrganization(
            BillingInput billingInput, BillingResult billingResult) {

        SubscriptionHistoryEvaluator subHistoryEvaluator = new SubscriptionHistoryEvaluator(
                billingInput, bdr);

        if (billingInput.getSubscriptionHistoryEntries().isEmpty()) {
            return null;
        }

        if (!subHistoryEvaluator.evaluateHistories()) {
            return null;
        }

        BillingDetailsType billingDetails = createBasicBillDocument(
                billingInput, billingResult);
        OverallCosts overallCosts = OverallCosts.newInstance();

        for (SubscriptionInput subscriptionInput : subHistoryEvaluator
                .getSubscriptions()) {
            BigDecimal costforSubscription = billSubscription(billingInput,
                    subscriptionInput, billingDetails, billingResult);
            overallCosts = overallCosts.add(billingInput.getCurrencyIsoCode(),
                    costforSubscription);
        }

        BigDecimal costsBeforeDiscount = overallCosts.get(billingInput
                .getCurrencyIsoCode());
        if (costsBeforeDiscount == null) {
            costsBeforeDiscount = BigDecimal.ZERO;
        }

        assembler.initializeOverallCostsType(billingInput.getCurrencyIsoCode(),
                billingDetails);
        overallCosts = allowDiscount(billingDetails, costsBeforeDiscount,
                billingInput, billingResult.getChargingOrgKey(), overallCosts);
        includeVat(billingDetails, billingInput, billingResult, overallCosts);

        return billingDetails;
    }

    private BillingDetailsType createBasicBillDocument(
            BillingInput billingInput, BillingResult billingResult) {
        long organizationKey = billingInput.getOrganizationKey();
        long subscriptionKey = billingInput.getSubscriptionKey();
        final OrganizationAddressData orgData = bdr
                .loadOrganizationBillingDataFromHistory(organizationKey,
                        subscriptionKey);
        List<UdaBillingData> udasForCustomer = bdr.loadUdasForCustomer(
                organizationKey, billingResult.getChargingOrgKey());
        return assembler
                .createBasicBillDocumentForOrganization(orgData,
                        udasForCustomer, organizationKey, subscriptionKey,
                        billingResult,
                        getPeriodStartForReportHeader(billingInput),
                        getPeriodEndForReportHeader(billingInput),
                        billingInput.isStoreBillingResult());
    }

    OverallCosts allowDiscount(BillingDetailsType billingDetails,
            BigDecimal costsBeforeDiscount, BillingInput billingInput,
            long chargingOrgKey, OverallCosts overallCosts) {

        BigDecimal costsAfterDiscount = costsBeforeDiscount;

        long periodStart = billingInput.getBillingPeriodStart();
        long periodEnd = billingInput.getBillingPeriodEnd();
        BigDecimal discount = bdr.loadDiscountValue(
                billingInput.getOrganizationKey(), periodStart, periodEnd,
                chargingOrgKey);

        if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
            costsAfterDiscount = CostCalculator.calculateDiscountedCosts(
                    costsBeforeDiscount, discount);

            DiscountType discountType = factory.createDiscountType();

            BigDecimal discountNetAmount = costsBeforeDiscount
                    .subtract(costsAfterDiscount);
            discountType.setDiscountNetAmount(discountNetAmount);

            BigDecimal netAfterDiscount = costsAfterDiscount;
            discountType.setNetAmountAfterDiscount(netAfterDiscount);

            discountType.setNetAmountBeforeDiscount(costsBeforeDiscount);
            discountType.setPercent(discount.floatValue());
            billingDetails.getOverallCosts().setDiscount(discountType);
        }

        billingDetails.getOverallCosts().setNetAmount(costsAfterDiscount);
        return overallCosts.set(billingInput.getCurrencyIsoCode(),
                costsAfterDiscount);
    }

    /**
     * Compute VAT and include it in the billing result. The billing calculation
     * is performed with the VAT, that is valid at the end of the billing
     * period.
     */
    void includeVat(BillingDetailsType billingDetails,
            BillingInput billingInput, BillingResult billingResult,
            OverallCosts overallCosts) {

        VatRateDetails vatForCustomer = bdr.loadVATForCustomer(
                billingInput.getOrganizationKey(),
                billingInput.getBillingPeriodEnd(),
                billingResult.getChargingOrgKey());

        vatForCustomer.setNetCosts(overallCosts.get(billingInput
                .getCurrencyIsoCode()));
        vatForCustomer = calculateVatCosts(vatForCustomer);

        if (vatForCustomer.getEffectiveVatRateForCustomer() != null) {
            VATType vatType = factory.createVATType();
            vatType.setPercent(vatForCustomer.getEffectiveVatRateForCustomer()
                    .floatValue());
            vatType.setAmount(vatForCustomer.getVatAmount());
            billingDetails.getOverallCosts().setVAT(vatType);
        }

        billingDetails.getOverallCosts().setGrossAmount(
                vatForCustomer.getTotalCosts());
    }

    VatRateDetails calculateVatCosts(VatRateDetails vatForCustomer) {
        return CostCalculator.calculateVATCosts(vatForCustomer);
    }

    private long getPeriodStartForReportHeader(BillingInput billingInput) {
        if (billingInput.getBillingContext() == BillingContextType.BILLING_FOR_ANY_PERIOD) {
            return billingInput.getInitialBillingPeriodStart();
        } else {
            return billingInput.getBillingPeriodStart();
        }
    }

    private long getPeriodEndForReportHeader(BillingInput billingInput) {
        if (billingInput.getBillingContext() == BillingContextType.BILLING_FOR_ANY_PERIOD) {
            return billingInput.getInitialBillingPeriodEnd();
        } else {
            return billingInput.getBillingPeriodEnd();
        }
    }

    /**
     * Bills the price models for the given subscription in the given timeframe.
     */
    private BigDecimal billSubscription(BillingInput billingInput,
            SubscriptionInput subscriptionInput,
            BillingDetailsType billingDetails, BillingResult result) {

        BigDecimal overallCosts = CostCalculator.ZERO_NORMALIZED;

        if (subscriptionInput.getHistories().isEmpty()) {
            return overallCosts;
        }

        PriceModelEvaluator priceModelEvaluator = new PriceModelEvaluator(
                billingInput, bdr, subscriptionInput);
        priceModelEvaluator.evaluatePriceModels();

        SubscriptionType subscriptionType = initializeSubscriptionType(subscriptionInput);
        PriceModelsType priceModelsType = subscriptionType.getPriceModels();

        for (PriceModelInput pmInput : priceModelEvaluator.getPriceModels()) {
            BigDecimal costForSubscriptionPriceModel = billPriceModel(
                    billingInput, pmInput, priceModelsType, result);
            overallCosts = overallCosts.add(costForSubscriptionPriceModel);
        }

        if (priceModelsType.getPriceModel().size() > 0) {
            addSubscriptionRelatedUdas(result, subscriptionInput.getHistories()
                    .get(0), subscriptionType);
            billingDetails.getSubscriptions().getSubscription()
                    .add(subscriptionType);
        }

        return overallCosts;
    }

    private SubscriptionType initializeSubscriptionType(
            SubscriptionInput subscriptionInput) {
        SubscriptionType subscriptionType = factory.createSubscriptionType();
        subscriptionType.setId(subscriptionInput.getSubscriptionId());
        subscriptionType.setPurchaseOrderNumber(subscriptionInput
                .getPurchaseOrderNumber());
        if (subscriptionInput.getUserGroupHistory() != null) {
            OrganizationalUnitType orgUnitType = factory
                    .createOrganizationalUnitType();
            orgUnitType.setName(subscriptionInput.getUserGroupHistory()
                    .getName());
            orgUnitType.setReferenceID(subscriptionInput.getUserGroupHistory()
                    .getReferenceId());
            subscriptionType.setOrganizationalUnit(orgUnitType);
        }
        subscriptionType.setPriceModels(factory.createPriceModelsType());
        return subscriptionType;
    }

    private void addSubscriptionRelatedUdas(BillingResult result,
            SubscriptionHistory subscription, SubscriptionType subscriptionType) {

        List<UdaBillingData> udasForSubscription = bdr.loadUdasForSubscription(
                subscription.getObjKey(), result.getChargingOrgKey());
        if (udasForSubscription != null && !udasForSubscription.isEmpty()) {
            subscriptionType.setUdas(factory.createUdasType());
            for (UdaBillingData uda : udasForSubscription) {
                final UdaType u = factory.createUdaType();
                u.setId(uda.getIdentifier());
                u.setValue(uda.getValue());
                subscriptionType.getUdas().getUda().add(u);
            }
        }
    }

    /**
     * Bills for the price model specific events and base prices in the given
     * time frame.
     * 
     * @return The costs for the subscription's price model in the given period.
     */
    BigDecimal billPriceModel(final BillingInput billingInput,
            final PriceModelInput priceModelInput,
            final PriceModelsType priceModelsType, final BillingResult result) {

        BigDecimal overallPriceModelCosts = ZERO_NORMALIZED;

        if (priceModelInput.isChargeablePriceModel()
                && !priceModelInput.isFreePriceModel()) {
            final PriceModelType priceModelType = assembler
                    .initializePriceModelType(billingInput, priceModelInput,
                            priceModelsType);

            final BigDecimal oneTimeFeeCosts = calculateOnetimeFeeCosts(
                    priceModelInput, priceModelType);

            final BigDecimal eventCosts = new EventCalculator(bdr, localizer)
                    .calculateEventCosts(billingInput, priceModelInput, result,
                            priceModelType);

            final BigDecimal costsForPeriodUsage = calculatePeriodCosts(
                    billingInput, priceModelInput, priceModelType);

            final BigDecimal costsForUsers = calculateUserCosts(billingInput,
                    priceModelInput, priceModelType);

            final BigDecimal costsForParameters = calculateParameterCosts(
                    billingInput, priceModelInput, priceModelType);

            overallPriceModelCosts = eventCosts
                    .add(costsForPeriodUsage)
                    .add(costsForUsers)
                    .add(oneTimeFeeCosts)
                    .add(costsForParameters)
                    .setScale(PriceConverter.NORMALIZED_PRICE_SCALING,
                            RoundingMode.HALF_UP);

            priceModelType.getPriceModelCosts().setAmount(
                    overallPriceModelCosts);
        }

        return overallPriceModelCosts;
    }

    private BigDecimal calculateOnetimeFeeCosts(
            final PriceModelInput priceModelInput,
            final PriceModelType priceModelType) {

        if (priceModelInput.isOneTimeFeeCharged()) {
            return assembler.addOneTimeFee(priceModelType,
                    priceModelInput.getPriceModelHistory());
        } else {
            return ZERO_NORMALIZED;
        }
    }

    private BigDecimal calculatePeriodCosts(final BillingInput billingInput,
            final PriceModelInput priceModelInput,
            final PriceModelType priceModelType) {

        final CostCalculator calculator = priceModelInput.getCostCalculator();
        final double usageCostsFactor = calculator.computeFactorForUsageTime(
                priceModelInput.getPricingPeriod(), billingInput,
                priceModelInput.getPmStartAdjustedToFreePeriod(),
                priceModelInput.getPriceModelPeriodEnd());

        return determineUsageCostsForPeriod(
                priceModelType.getPeriodFee(),
                priceModelInput.getPriceModelHistory(), usageCostsFactor);
    }

    private BigDecimal calculateUserCosts(final BillingInput billingInput,
            final PriceModelInput priceModelInput,
            final PriceModelType priceModelType) {

        final long startTimeForPeriod = priceModelInput
                .getPmStartAdjustedToFreePeriod();
        final long endTimeForPeriod = priceModelInput.getPriceModelPeriodEnd();
        final CostCalculator calculator = priceModelInput.getCostCalculator();

        UserAssignmentCostsType userAssigmentCostsType = assembler
                .initializeUserAssignmentCosts(priceModelType);

        final List<UsageLicenseHistory> ulHistList = bdr.loadUsageLicenses(
                billingInput.getSubscriptionKey(), startTimeForPeriod,
                endTimeForPeriod);
        final UserAssignmentFactors userAssignmentsFactors = calculator
                .computeUserAssignmentsFactors(ulHistList,
                        priceModelInput.getPriceModelHistory(), billingInput,
                        startTimeForPeriod, endTimeForPeriod);

        assembler.addCostsPerUser(userAssigmentCostsType,
                userAssignmentsFactors);

        final BigDecimal userAssignmentCosts = calculateUserAssignmentCosts(
                priceModelInput, userAssigmentCostsType, userAssignmentsFactors);
        final BigDecimal roleCosts = calculateRoleCosts(calculator,
                priceModelInput, userAssigmentCostsType, userAssignmentsFactors);

        final BigDecimal totalUserAssignmentCosts = userAssignmentCosts
                .add(roleCosts);
        userAssigmentCostsType.setTotal(totalUserAssignmentCosts);

        return totalUserAssignmentCosts;
    }

    private BigDecimal calculateUserAssignmentCosts(
            final PriceModelInput priceModelInput,
            final UserAssignmentCostsType userAssigmentCostsType,
            final UserAssignmentFactors userAssignmentsFactors) {

        final BigDecimal basicUserAssignmentFactor = BigDecimal
                .valueOf(userAssignmentsFactors.getBasicFactor());
        int numberOfUserTotal = userAssignmentsFactors.getNumberOfUsers();
        long endTimeForPeriod = priceModelInput.getPriceModelPeriodEnd();
        PriceModelHistory priceModelHistory = priceModelInput
                .getPriceModelHistory();
        return determineCostsForUserAssignments(
                priceModelHistory, userAssigmentCostsType,
                basicUserAssignmentFactor, numberOfUserTotal, endTimeForPeriod);
    }

    private BigDecimal calculateRoleCosts(CostCalculator calculator,
            PriceModelInput priceModelInput,
            final UserAssignmentCostsType userAssigmentCostsType,
            final UserAssignmentFactors userAssignmentsFactors) {

        final Map<Long, RoleDefinitionHistory> roleDefinitions = bdr
                .loadRoleDefinitionsForPriceModel(
                        priceModelInput.getPriceModelKey(),
                        priceModelInput.getPriceModelPeriodEnd());

        final Map<Long, RolePricingDetails> rolePrices = bdr
                .loadRoleRelatedCostsForPriceModel(
                        priceModelInput.getPriceModelKey(),
                        priceModelInput.getPriceModelPeriodEnd());

        final Map<Long, Double> roleFactors = userAssignmentsFactors
                .getRoleFactors();

        final Map<Long, RolePricingDetails> roleCosts = calculator
                .calculateRoleRelatedCostsForPriceModel(rolePrices,
                        roleFactors, roleDefinitions);

        return assembler.addRolesCosts(
                userAssigmentCostsType, roleCosts);
    }

    private BigDecimal calculateParameterCosts(final BillingInput billingInput,
            final PriceModelInput priceModelInput,
            final PriceModelType priceModelType) {

        final long startTimeForPeriod = priceModelInput
                .getPmStartAdjustedToFreePeriod();
        final long endTimeForPeriod = priceModelInput.getPriceModelPeriodEnd();
        final CostCalculator calculator = priceModelInput.getCostCalculator();

        XParameterData parameterData = bdr.loadParameterData(billingInput,
                priceModelInput);
        calculator.computeParameterPeriodFactor(billingInput, parameterData,
                startTimeForPeriod, endTimeForPeriod);
        calculator.computeParameterUserFactorAndRoleFactor(bdr, billingInput,
                parameterData, startTimeForPeriod, endTimeForPeriod);
        BigDecimal costsForParameters = calculateAllParameterCosts(calculator,
                parameterData).getNormalizedOverallCosts();
        writeParameterData(priceModelInput, priceModelType, parameterData);

        return costsForParameters;
    }

    ParameterCosts calculateAllParameterCosts(CostCalculator calculator,
            XParameterData parameters) {

        ParameterCosts result = new ParameterCosts();
        if (parameters != null) {
            for (XParameterIdData parameterData : parameters.getIdData()) {
                for (XParameterPeriodValue parameterPeriodValue : parameterData
                        .getPeriodValues()) {

                    final BigDecimal valueMultplier = calculator
                            .determineParameterValueMultiplier(
                                    parameterPeriodValue.getValueType(),
                                    parameterPeriodValue.getValue());
                    parameterPeriodValue.setValueFactor(valueMultplier
                            .doubleValue());

                    BigDecimal parameterUserCosts = calculator
                            .calculateParameterUserCosts(parameterPeriodValue,
                                    valueMultplier).setScale(
                                    PriceConverter.NORMALIZED_PRICE_SCALING,
                                    RoundingMode.HALF_UP);
                    parameterPeriodValue
                            .addTotalCostsForUser(parameterUserCosts);

                    BigDecimal parameterRoleCosts = calculateParameterUserRoleCosts(
                            parameterPeriodValue.getRolePrices(),
                            valueMultplier);

                    BigDecimal parameterPeriodCosts = calculateParameterPeriodCosts(
                            calculator, parameterPeriodValue, valueMultplier)
                            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING,
                                    RoundingMode.HALF_UP);
                    parameterPeriodValue
                            .addTotalCostsForSubscription(parameterPeriodCosts);

                    BigDecimal finalParameterCosts = parameterPeriodCosts.add(
                            parameterUserCosts).add(parameterRoleCosts);

                    BillingItemCosts<XParameterPeriodValue> billedItem = new BillingItemCosts<>();
                    billedItem.setBillingItemCosts(finalParameterCosts);
                    billedItem.setBillingItem(parameterPeriodValue);
                    result.addBilledItem(billedItem);
                }
            }
        }
        return result;
    }

    BigDecimal calculateParameterUserRoleCosts(
            final RolePricingData rolePricingData,
            final BigDecimal valueMultiplier) {

        if (rolePricingData == null) {
            return ZERO_NORMALIZED;
        }

        Set<Long> parameterKeys = rolePricingData.getContainerKeys();
        for (Long paramKey : parameterKeys) {
            Collection<RolePricingDetails> allRolePrices = rolePricingData
                    .getAllRolePrices(paramKey);
            for (RolePricingDetails rolePricingDetails : allRolePrices) {
                BigDecimal currentCosts = rolePricingDetails
                        .getPricePerUser()
                        .multiply(
                                BigDecimal.valueOf(rolePricingDetails
                                        .getFactor()))
                        .multiply(valueMultiplier)
                        .setScale(PriceConverter.NORMALIZED_PRICE_SCALING,
                                RoundingMode.HALF_UP);
                rolePricingDetails.addCosts(currentCosts);
            }
        }
        return rolePricingData.getCosts();
    }

    /**
     * Determines the period related costs caused by the parameter. It is
     * calculation parameter costs based on subscription price - cost of
     * subscription usage. For parameter type INTEGER and LONG can be defined
     * stepped prices for usage. If stepped prices are defined, only these
     * prices have to be used for cost calculation. If there are no stepped
     * price - base subscription price has to be used for cost calculation.
     */
    private BigDecimal calculateParameterPeriodCosts(CostCalculator calculator,
            XParameterPeriodValue periodValue, BigDecimal valueMultplier) {

        ParameterValueType type = periodValue.getValueType();
        BigDecimal costs = ZERO_NORMALIZED;

        // for these type stepped prices can be defined
        // this parameter can has stepped price
        BigDecimal valueForStepEvaluation = ZERO_NORMALIZED;
        if (type.equals(ParameterValueType.INTEGER)
                || type.equals(ParameterValueType.LONG)) {
            if (!periodValue.isParameterOption()) {
                valueForStepEvaluation = new BigDecimal(periodValue.getValue());
            }
        }
        SteppedPriceDetail steppedPricesDetail = periodValue
                .getSteppedPricesForParameter();
        // there are stepped prices, define exactly cost for the step
        if (steppedPricesDetail.areSteppedPricesDefined()) {
            SteppedPriceDetail stepData = calculator.calculateStepCost(
                    steppedPricesDetail, valueForStepEvaluation);
            // stepped price exist, no more common cost calculation is
            // needed
            costs = stepData.getNormalizedCost();
        } else {
            // The first initialization. Just take base price for all
            // parameters type and define further for INTEGER and LONG a
            // special stepped prices
            BigDecimal price = periodValue.getPricePerSubscription();
            costs = price.multiply(valueMultplier);
        }
        costs = BigDecimals.multiply(costs, periodValue.getPeriodFactor());

        return costs;
    }

    private void writeParameterData(final PriceModelInput priceModelInput,
            final PriceModelType priceModelType,
            final XParameterData parameterData) {

        if (parameterData != null && !parameterData.getIdData().isEmpty()) {
            final ParametersType parametersType = factory
                    .createParametersType();

            for (XParameterIdData currentParam : parameterData.getIdData()) {
                for (XParameterPeriodValue periodValue : currentParam
                        .getPeriodValues()) {
                    final ParameterType parameterType = assembler
                            .initParameter(periodValue, priceModelInput);
                    parametersType.getParameter().add(parameterType);

                    parameterType
                            .setPeriodFee(assembler.initParameterPeriodFee(
                                    periodValue, parameterData));

                    parameterType.setUserAssignmentCosts(assembler
                            .initParameterUserAssignmentCosts(periodValue,
                                    parameterData));

                    parameterType.setOptions(assembler.initParameterOptions(
                            periodValue, parameterData));

                    parameterType.setParameterCosts(factory
                            .createNormalizedCostsType());
                    parameterType.getParameterCosts().setAmount(
                            periodValue.getTotalCosts());
                }
            }

            if (!parametersType.getParameter().isEmpty()) {
                priceModelType.setParameters(parametersType);
                NormalizedCostsType normalizedCostsType = factory
                        .createNormalizedCostsType();
                normalizedCostsType.setAmount(parameterData.getCosts());
                parametersType.setParametersCosts(normalizedCostsType);
            }
        }
    }

    /**
     * Checks which users have been assigned to the subscription in the given
     * period. If those were marked as active, their assignment duration will be
     * considered for the calculation of the costs. In case stepped price are
     * defined, algorithm for cost calculation is based on these prices.
     */
    private BigDecimal determineCostsForUserAssignments(
            PriceModelHistory referencePMHistory,
            UserAssignmentCostsType userAssignment, BigDecimal overallFactor,
            int pNumberOfUserTotal, long endTimeForPeriod) {

        BigDecimal costsForUserAssignments = ZERO_NORMALIZED;
        CostCalculator calculator = CostCalculator.get(referencePMHistory);
        PriceModelData historyData = referencePMHistory.getDataContainer();

        if (referencePMHistory.isChargeable()) {
            // only in case the price model is chargeable, start determining the
            // user assignment costs

            // define stepped price for price model specific for number of user
            List<SteppedPriceData> steppedPriceList = bdr
                    .loadSteppedPricesForPriceModel(
                            referencePMHistory.getObjKey(), endTimeForPeriod);

            // price per user
            BigDecimal costsPerUserAssignment = ZERO_NORMALIZED;

            boolean isStepPriceDefined = false;
            if (steppedPriceList != null && steppedPriceList.size() > 0) {
                // there are stepped prices
                isStepPriceDefined = true;
                SteppedPriceDetail steppedPriceDetail = new SteppedPriceDetail(
                        ZERO_NORMALIZED);
                steppedPriceDetail.setPriceData(steppedPriceList);
                SteppedPriceDetail stepData = calculator.calculateStepCost(
                        steppedPriceDetail, overallFactor);
                if (stepData != null && stepData.getNormalizedCost() != null) {
                    BigDecimal cost = stepData.getNormalizedCost();
                    BigDecimal stepCost = ZERO_NORMALIZED;
                    if (cost != null) {
                        stepCost = cost;
                    }
                    costsPerUserAssignment = stepCost;
                }
            } else {
                // there is no stepped price
                // use price per user from price model in this case
                costsPerUserAssignment = historyData
                        .getPricePerUserAssignment();
            }
            // initial values are the same
            BigDecimal basePrice = costsPerUserAssignment;
            PricingPeriod periodSetting = historyData.getPeriod();
            // for now only calculate the final costs, omit the details on
            // user level
            if (isStepPriceDefined) {
                // this is a case of stepped price, factor was already used
                // for calculation
                costsForUserAssignments = costsPerUserAssignment;
                // normalize costs
                costsForUserAssignments = costsForUserAssignments.setScale(
                        PriceConverter.NORMALIZED_PRICE_SCALING,
                        RoundingMode.HALF_UP);
                if (steppedPriceList != null && !steppedPriceList.isEmpty()) {
                    final SteppedPricesType steppedPricesType = factory
                            .createSteppedPricesType();

                    for (SteppedPriceData stepPrice : steppedPriceList) {
                        // add sub tags for every step
                        final SteppedPriceType steppedPriceType = factory
                                .createSteppedPriceType();
                        steppedPriceType
                                .setLimit(stepPrice.getLimit() == null ? "null"
                                        : String.valueOf(stepPrice.getLimit()));
                        steppedPriceType.setFreeAmount(stepPrice
                                .getFreeEntityCount());
                        steppedPriceType.setBasePrice(stepPrice.getBasePrice());
                        steppedPriceType.setAdditionalPrice(stepPrice
                                .getAdditionalPrice());
                        steppedPriceType.setStepAmount(stepPrice
                                .getStepAmount());
                        steppedPriceType.setStepEntityCount(stepPrice
                                .getStepEntityCount());
                        steppedPricesType.getSteppedPrice().add(
                                steppedPriceType);
                    }
                    steppedPricesType.setAmount(costsPerUserAssignment);
                    userAssignment.setSteppedPrices(steppedPricesType);
                }
            } else {
                // this is a case without stepped price, calculate cast in
                // common way
                costsForUserAssignments = basePrice.multiply(overallFactor)
                        .setScale(PriceConverter.NORMALIZED_PRICE_SCALING,
                                RoundingMode.HALF_UP);
            }

            // finally add the information to the XML structure
            userAssignment.setBasePeriod(BasePeriodType.valueOf(periodSetting
                    .name()));
            if (!isStepPriceDefined) {
                userAssignment.setBasePrice(basePrice);
            }
            userAssignment.setNumberOfUsersTotal(Long
                    .valueOf(pNumberOfUserTotal));
            userAssignment.setFactor(overallFactor);
            userAssignment.setPrice(costsForUserAssignments);
        }
        return costsForUserAssignments;
    }

    /**
     * Calculates the amount that has to be paid for the subscription, as it was
     * used in the given period.
     */
    private BigDecimal determineUsageCostsForPeriod(PeriodFeeType periodFee,
            PriceModelHistory referenceHistory, double factor) {

        BigDecimal pFee = ZERO_NORMALIZED;
        if (referenceHistory.isChargeable()) {
            final BigDecimal pricePerPeriod = referenceHistory
                    .getDataContainer().getPricePerPeriod();

            pFee = pricePerPeriod.multiply(BigDecimal.valueOf(factor))
                    .setScale(PriceConverter.NORMALIZED_PRICE_SCALING,
                            RoundingMode.HALF_UP);
            periodFee.setFactor(BigDecimal.valueOf(factor));
            periodFee.setPrice(pFee);
        }
        return pFee;
    }

}
