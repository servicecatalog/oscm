/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Oct 11, 2011                                                      
 *                                                                              
 *  Completion Time: Oct 11, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.operationslog;

/**
 * @author tokoda
 * 
 */
public enum UserOperationLogEntityType {

    SUBSCRIPTION(new SubscriptionQuery(), new SubscriptionUserQuery(),
            new SubscriptionPriceQuery()),

    // SUBSCRIPTION(new SubscriptionQuery(), new SubscriptionUserQuery(),
    // new SubscriptionPriceQuery(), new SubscriptionPriceEventQuery(),
    // new SubscriptionPriceEventSteppedQuery(),
    // new SubscriptionPriceParameterQuery(),
    // new SubscriptionPriceParameterSteppedQuery(),
    // new SubscriptionPriceOptionQuery(),
    // new SubscriptionPriceRoleQuery(),
    // new SubscriptionPriceRoleParameterQuery(),
    // new SubscriptionPriceRoleOptionQuery(), new SubscriptionUdaQuery()),

    ORGANIZATION(new OrganizationQuery(), new OrganizationUserQuery(),
            new OrganizationUserRoleQuery(), new OrganizationReferenceQuery(),
            new OrganizationDiscountQuery(), new OrganizationVatQuery(),
            new OrganizationUdaQuery(), new OrganizationBillingContactQuery(),
            new OrganizationPaymentInfoQuery()),

    SERVICE(new ServiceQuery(), new ServiceParameterQuery(),
            new ServiceOptionQuery(), new ServiceUpgradeQuery(),
            new ServiceReviewQuery(), new ServicePriceQuery(),
            new ServicePriceEventQuery(), new ServicePriceEventSteppedQuery(),
            new ServicePriceParameterQuery(),
            new ServicePriceParameterSteppedQuery(),
            new ServicePriceOptionQuery(), new ServicePriceRoleQuery(),
            new ServicePriceRoleParameterQuery(),
            new ServicePriceRoleOptionQuery()),

    TECHNICAL_SERVICE(new TechnicalServiceQuery(),
            new TechnicalServiceRoleQuery(), new TechnicalServiceEventQuery(),
            new TechnicalServiceParameterQuery(),
            new TechnicalServiceOperationQuery()),

    MARKETPLACE(new MarketplaceQuery(), new MarketplaceEntryQuery());

    UserOperationLogQuery[] logQueries;

    private UserOperationLogEntityType(UserOperationLogQuery... logQueries) {
        this.logQueries = logQueries;
    }

    public UserOperationLogQuery[] getLogQueries() {
        return logQueries;
    }
}
