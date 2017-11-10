/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.reportingservice.bean;

public enum ReportName {
    EVENT("Event"),

    SUBSCRIPTION("Subscription"),

    SUPPLIER_PRODUCT("Supplier_Product"),

    SUPPLIER_CUSTOMER("Supplier_Customer"),

    SUPPLIER_BILLING("Supplier_Billing"),

    SUPPLIER_PAYMENT_RESULT_STATUS("Supplier_PaymentResultStatus"),

    PROVIDER_EVENT("Provider_Event"),

    PROVIDER_SUPPLIER("Provider_Supplier"),

    PROVIDER_SUBSCRIPTION("Provider_Subscription"),

    PROVIDER_INSTANCE("Provider_Instance"),

    SUPPLIER_BILLING_OF_SUPPLIER("Supplier_BillingOfASupplier"),

    SUPPLIER_BILLING_DETAILS_OF_SUPPLIER("Supplier_BillingDetailsOfASupplier"),

    SUPPLIER_CUSTOMER_OF_SUPPLIER("Supplier_CustomerOfASupplier"),

    SUPPLIER_PRODUCT_OF_SUPPLIER("Supplier_ProductOfASupplier");

    private String name;

    private ReportName(String name) {
        this.name = name;
    }

    public String value() {
        return name;
    }
}
