/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 03.02.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.constants;

/**
 * Interface to provide all element and attribute names for the billing related
 * XML result structure.
 * 
 * @author Mike J&auml;ger
 * 
 */
public interface BillingResultXMLTags {

    public static final String PRICE_MODEL_NODE_NAME = "PriceModel";
    public static final String PRICE_MODEL_COSTS_NODE_NAME = "PriceModelCosts";
    public static final String ONE_TIME_FEE_NODE_NAME = "OneTimeFee";
    public static final String PRICE_MODELS_NODE_NAME = "PriceModels";
    public static final String SUBSCRIPTION_NODE_NAME = "Subscription";
    public static final String SUBSCRIPTIONS_NODE_NAME = "Subscriptions";
    public static final String USER_ASSIGNMENT_COSTS_NODE_NAME = "UserAssignmentCosts";
    public static final String USER_ASSIGNMENT_COSTS_BY_USER_NODE_NAME = "UserAssignmentCostsByUser";
    public static final String USAGE_PERIOD_NODE_NAME = "UsagePeriod";
    public static final String PERIOD_FEE_NODE_NAME = "PeriodFee";
    public static final String EVENT_NODE_NAME = "Event";
    public static final String EVENT_DESCRIPTION_NODE_NAME = "Description";
    public static final String GATHERED_EVENTS_NODE_NAME = "GatheredEvents";
    public static final String GATHERED_EVENTS_COSTS_NODE_NAME = "GatheredEventsCosts";
    public static final String COST_FOR_EVENT_TYPE_NODE_NAME = "CostForEventType";
    public static final String NUMBER_OF_OCCURRENCE_NODE_NAME = "NumberOfOccurrence";
    public static final String SINGLE_COST_NODE_NAME = "SingleCost";
    public static final String BILLING_DETAILS_NODE_NAME = "BillingDetails";
    public static final String ADDRESS_NODE_NAME = "Address";
    public static final String NAME_NODE_NAME = "Name";
    public static final String ORGANIZATION_DETAILS_NODE_NAME = "OrganizationDetails";
    public static final String PERIOD_NODE_NAME = "Period";
    public static final String NUMBER_OF_USERS_TOTAL_ATTRIBUTE_NAME = "numberOfUsersTotal";
    public static final String PRICE_ATTRIBUTE_NAME = "price";
    public static final String FACTOR_ATTRIBUTE_NAME = "factor";
    public static final String BASE_PERIOD_ATTRIBUTE_NAME = "basePeriod";
    public static final String BASE_PRICE_ATTRIBUTE_NAME = "basePrice";
    public static final String END_DATE_ATTRIBUTE_NAME = "endDate";
    public static final String END_DATE_ISO_ATTRIBUTE_NAME = "endDateIsoFormat";
    public static final String START_DATE_ATTRIBUTE_NAME = "startDate";
    public static final String START_DATE_ISO_ATTRIBUTE_NAME = "startDateIsoFormat";
    public static final String AMOUNT_ATTRIBUTE_NAME = "amount";
    public static final String BASE_AMOUNT_ATTRIBUTE_NAME = "baseAmount";
    public static final String ID_ATTRIBUTE_NAME = "id";
    public static final String USER_ID_ATTRIBUTE_NAME = "userId";
    public static final String PON_ATTRIBUTE_NAME = "purchaseOrderNumber";
    public static final String NET_AMOUNT_ATTRIBUTE_NAME = "netAmount";
    public static final String CURRENCY_ATTRIBUTE_NAME = "currency";
    public static final String PARAMETERS_NODE_NAME = "Parameters";
    public static final String PARAMETER_NODE_NAME = "Parameter";
    public static final String PARAMETER_COSTS_NODE_NAME = "ParameterCosts";
    public static final String PARAMETER_VALUE_NODE_NAME = "ParameterValue";
    public static final String TYPE_ATTRIBUTE_NAME = "type";
    public static final String VALUE_FACTOR_ATTRIBUTE_NAME = "valueFactor";
    public static final String PARAMETERS_COSTS_NODE_NAME = "ParametersCosts";
    public static final String PARAMETERS_USAGE_PERIOD_NODE_NAME = "ParameterUsagePeriod";
    public static final String OPTIONS_NODE_NAME = "Options";
    public static final String OPTION_COSTS_NODE_NAME = "OptionCosts";
    public static final String OPTION_NODE_NAME = "Option";
    public static final String DISCOUNT_NODE_NAME = "Discount";
    public static final String PERCENT_ATTRIBUTE_NAME = "percent";
    public static final String DISCOUNT_NET_AMOUNT_ATTRIBUTE_NAME = "discountNetAmount";
    public static final String AMOUNT_BEFORE_DISCOUNT = "netAmountBeforeDiscount";
    public static final String AMOUNT_AFTER_DISCOUNT = "netAmountAfterDiscount";
    public static final String ROLE_COSTS_NODE_NAME = "RoleCosts";
    public static final String ROLE_COST_NODE_NAME = "RoleCost";
    public static final String TOTAL_ATTRIBUTE_NAME = "total";
    public static final String STEPPED_PRICES_NODE_NAME = "SteppedPrices";
    public static final String STEPPED_PRICE_NODE_NAME = "SteppedPrice";
    public static final String LIMIT_ATTRIBUTE_NAME = "limit";
    public static final String FREE_AMOUNT_ATTRIBUTE_NAME = "freeAmount";
    public static final String ADDITIONAL_PRICE_ATTRIBUTE_NAME = "additionalPrice";
    public static final String UDAS_NODE_NAME = "Udas";
    public static final String UDA_NODE_NAME = "Uda";
    public static final String UDA_VALUE_ATTRIBUTE_NAME = "value";
    public static final String VAT_ELEMENT_NAME = "VAT";
    public static final String GROSS_AMOUNT_ATTRIBUTE_NAME = "grossAmount";
    public static final String BILLING_RESULT_KEY_ATTRIBUTE_NAME = "key";
    public static final String EMAIL_NODE_NAME = "Email";
    public static final String PAYMENTTYPE_NODE_NAME = "Paymenttype";
    public static final String OVERALLCOST_NODE_NAME = "OverallCosts";
    public static final String CURRENCY_NODE_NAME = "Currency";
    public static final String UNIT_ATTRIBUTE_NAME = "unit";
    public static final String STEP_AMOUNT_ATTRIBUTE_NAME = "stepAmount";
    public static final String STEP_ENTITY_COUNT_ATTRIBUTE_NAME = "stepEntityCount";
    public static final String CALCULATION_MODE_ATTRIBUTE_NAME = "calculationMode";
    public static final String TIMEZONE_ATTRIBUTE_NAME = "timeZone";
}
