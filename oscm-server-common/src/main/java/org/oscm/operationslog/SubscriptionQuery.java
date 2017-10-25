/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: Oct 13, 2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.operationslog;

import java.math.BigInteger;

import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * Query for subscriptions.
 * 
 * @author barzu
 */
public class SubscriptionQuery extends UserOperationLogQuery {

    static final String SUBSCR_COMMON_SELECT = ", s.subscriptionId, o.name, o.organizationId";
    static final String SUBSCR_COMMON_FROM = " FROM SubscriptionHistory s JOIN OrganizationHistory o ON s.organizationObjKey = o.objKey";
    static final String SUBSCR_COMMON_AND = " AND o.objVersion = (SELECT MAX(o2.objVersion) FROM OrganizationHistory o2 WHERE s.organizationObjKey = o2.objKey AND o2.modDate <=";
    static final String SUBSCR_COMMON_ORDERBY = " ORDER BY modDate DESC, objVersion DESC";

    private static final String SUBSCR = "SELECT s.modDate, s.modType, (CASE WHEN pu.userId IS NOT NULL THEN pu.userId ELSE s.modUser END) AS userId, s.objVersion"
            + SUBSCR_COMMON_SELECT
            + ", p.productId, s.activationDate, s.status, s.deactivationDate"
            + ", m.marketplaceId, pt.paymentTypeId, bc.billingContactId"
            + ", s.purchaseOrderNumber, s.accessInfo, s.baseURL, s.loginPath, s.timeoutMailSent"
            + SUBSCR_COMMON_FROM
            + " JOIN ProductHistory p ON s.productObjKey = p.objKey"
            + " LEFT JOIN MarketplaceHistory m ON s.marketplaceObjKey = m.objKey"
            + " LEFT JOIN PaymentInfoHistory pi ON s.paymentInfoObjKey = pi.objKey"
            + " LEFT JOIN PaymentTypeHistory pt ON pi.paymentTypeObjKey = pt.objKey"
            + " LEFT JOIN BillingContactHistory bc ON s.billingContactObjKey = bc.objKey"
            + " LEFT JOIN PlatformUserHistory pu ON s.modUser = CAST(pu.objKey AS VARCHAR(255))"
            + " WHERE s.modDate >= :startDate AND s.modDate <= :endDate"
            + SUBSCR_COMMON_AND
            + " s.modDate)"
            + " AND p.objVersion = (SELECT MAX(p2.objVersion) FROM ProductHistory p2 WHERE s.productObjKey = p2.objKey AND p2.modDate <= s.modDate)"
            + " AND (m.objVersion IS NULL OR m.objVersion = (SELECT MAX(m2.objVersion) FROM MarketplaceHistory m2 WHERE s.marketplaceObjKey = m2.objKey AND m2.modDate <= s.modDate))"
            + " AND (pi.objVersion IS NULL OR pi.objVersion = (SELECT MAX(pi2.objVersion) FROM PaymentInfoHistory pi2 WHERE s.paymentInfoObjKey = pi2.objKey AND pi2.modDate <= s.modDate))"
            + " AND (pt.objVersion IS NULL OR pt.objVersion = (SELECT MAX(pt2.objVersion) FROM PaymentTypeHistory pt2 WHERE pi.paymentTypeObjKey = pt2.objKey AND pt2.modDate <= s.modDate))"
            + " AND (bc.objVersion IS NULL OR bc.objVersion = (SELECT MAX(bc2.objVersion) FROM BillingContactHistory bc2 WHERE s.billingContactObjKey = bc2.objKey AND bc2.modDate <= s.modDate))"
            + " AND (pu.objVersion IS NULL OR pu.objVersion = (SELECT MAX(pu2.objVersion) FROM PlatformUserHistory pu2 WHERE s.modUser = CAST(pu2.objKey AS VARCHAR(255)) AND pu2.modDate <= s.modDate))"
            + SUBSCR_COMMON_ORDERBY;

    private static final String[] fieldNames = new String[] {
            COMMON_COLUMN_MODDATE, "op", "user", COMMON_COLUMN_OBJVERSION,
            "subscription", "customer", "customer id", "service", "activation",
            "status", "deactivation", "marketplace", "payment type",
            "billing contact", "reference", "access", "url", "login path",
            "timeoutmailsent" };

    @Override
    public LogMessageIdentifier getLogMessageIdentifier() {
        return LogMessageIdentifier.INFO_OPERATION_LOG_SUBSCR;
    }

    @Override
    public String getQuery() {
        return SUBSCR;
    }

    @Override
    public String[] getFieldNames() {
        return fieldNames;
    }

    @Override
    public String getLogType() {
        return "SUBSCR";
    }

    @Override
    protected void formatRow(Object[] row) {
        // activation date
        if (row[8] instanceof BigInteger) {
            row[8] = formatDate((BigInteger) row[8]);
        }
        // deactivation date
        if (row[10] instanceof BigInteger) {
            row[10] = formatDate((BigInteger) row[10]);
        }
    }
}
