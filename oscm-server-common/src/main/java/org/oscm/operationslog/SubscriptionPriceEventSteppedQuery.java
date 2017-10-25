/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Oct 13, 2011                                                      
 *                                                                              
 *  Completion Time: Oct 13, 2011                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.operationslog;

import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * 
 * @author tokoda
 * 
 */
public class SubscriptionPriceEventSteppedQuery extends UserOperationLogQuery {

    private static final String SUBSCR_PRICE_EVENT_STEPPED = "";

    private static final String[] fieldNames = new String[] {
            COMMON_COLUMN_MODDATE, "op", "user", COMMON_COLUMN_OBJVERSION,
            "subscription", "customer", "customer id", "event", "price",
            "currency", "upper limit" };

    @Override
    public LogMessageIdentifier getLogMessageIdentifier() {
        return LogMessageIdentifier.INFO_OPERATION_LOG_SUBSCR_PRICE_EVENT_STEPPED;
    }

    @Override
    public String getQuery() {
        return SUBSCR_PRICE_EVENT_STEPPED;
    }

    @Override
    public String[] getFieldNames() {
        return fieldNames;
    }

    @Override
    public String getLogType() {
        return "SUBSCR_PRICE_EVENT_STEPPED";
    }
}
