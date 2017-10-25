/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 14.10.2011                                                      
 *                                                                              
 *  Completion Time: 14.10.2011                                            
 *                                                                              
 *******************************************************************************/

package org.oscm.paymentservice.retrieval;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PSPAccountHistory;
import org.oscm.domobjects.PSPHistory;
import org.oscm.domobjects.PSPSettingHistory;
import org.oscm.domobjects.PaymentInfoHistory;
import org.oscm.domobjects.PaymentTypeHistory;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductHistory;
import org.oscm.paymentservice.data.PaymentHistoryData;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validation.ArgumentValidator;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.PSPProcessingException;

/**
 * Class retrieving history data required for the payment processing.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class PaymentHistoryReader {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(PaymentHistoryReader.class);

    private final DataService ds;

    public PaymentHistoryReader(DataService ds) {
        ArgumentValidator.notNull("ds", ds);
        this.ds = ds;
    }

    /**
     * Determines the history entries for the payment info objects for the
     * specified subscription key
     * 
     * @param subscriptionKey
     *            The subscription the payment info history is needed for.
     * @return The payment info history object for the customer.
     * @throws PSPProcessingException
     *             Thrown in case no payment information history data can be
     *             found for the subscription.
     */
    public PaymentHistoryData getPaymentHistory(long subscriptionKey)
            throws PSPProcessingException {
        
        Query query = ds
                .createNamedQuery("PaymentInfoHistory.findForSubscriptionKeyDescVersion");
        query.setParameter("subscriptionKey", Long.valueOf(subscriptionKey));
        List<Object[]> paymentHistoryEntries = ParameterizedTypes.list(
                query.getResultList(), Object[].class);
        if (paymentHistoryEntries == null || paymentHistoryEntries.size() != 1) {
            PSPProcessingException ppe = new PSPProcessingException(
                    "No unique payment information history data found for subscription with key '"
                            + subscriptionKey
                            + "'! "
                            + (paymentHistoryEntries == null ? 0
                                    : paymentHistoryEntries.size())
                            + " entries found, must be 1!");
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    ppe,
                    LogMessageIdentifier.WARN_INVALID_PAYMENT_PROCESSING_NO_RELATED_PAYMENT_DATA);
            throw ppe;
        }

        Object[] entry = paymentHistoryEntries.get(0);
        PSPHistory pspHistory = (PSPHistory) entry[2];
        query = ds.createNamedQuery("PSPSettingHistory.findForPSP");
        query.setParameter("pspObjKey", Long.valueOf(pspHistory.getObjKey()));
        List<PSPSettingHistory> settings = ParameterizedTypes.list(
                query.getResultList(), PSPSettingHistory.class);

        ProductHistory prod = (ProductHistory) entry[3];
        long vendorKey = prod.getVendorObjKey();
        if (prod.getDataContainer().getType() == ServiceType.PARTNER_SUBSCRIPTION) {
            try {
                Organization vendor = ds.getReference(Organization.class,
                        prod.getVendorObjKey());
                if (vendor.hasRole(OrganizationRoleType.BROKER)) {
                    Product p = ds.getReference(Product.class, prod
                            .getTemplateObjKey().longValue());
                    vendorKey = p.getTemplate().getVendorKey();
                }
            } catch (ObjectNotFoundException ex) {
                PSPProcessingException ppe = new PSPProcessingException(
                        "Failed to retrieve vendor of partner subscription (key: "
                                + subscriptionKey + ") with this exception: "
                                + ex.getMessage());
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        ppe,
                        LogMessageIdentifier.WARN_INVALID_PAYMENT_PROCESSING_NO_RELATED_PAYMENT_DATA);
                throw ppe;
            }
        }
        query = ds.createNamedQuery("PaymentInfoHistory.findPSPAccount");
        query.setParameter("organizationKey", Long.valueOf(vendorKey));
        query.setParameter("pspKey", Long.valueOf(pspHistory.getObjKey()));

        PaymentHistoryData result = new PaymentHistoryData();
        result.setPaymentInfoHistory((PaymentInfoHistory) entry[0]);
        result.setPaymentTypeHistory((PaymentTypeHistory) entry[1]);
        result.setPspHistory(pspHistory);
        try {
            result.setPspAccountHistory((PSPAccountHistory) query
                    .getSingleResult());
        } catch (NoResultException ex) {
            result.setPspAccountHistory(null);
        }
        result.setPspSettingsHistory(settings);
        
        return result;
    }
}
