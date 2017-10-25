/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.share;

import java.io.ByteArrayOutputStream;
import java.util.List;

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

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.billingservice.business.calculation.revenue.RevenueCalculatorBean;
import org.oscm.billingservice.business.model.brokershare.BrokerRevenueShareResult;
import org.oscm.billingservice.business.model.brokershare.BrokerShareResultAssembler;
import org.oscm.billingservice.business.model.mpownershare.MarketplaceOwnerRevenueShareResult;
import org.oscm.billingservice.business.model.mpownershare.MpOwnerShareResultAssembler;
import org.oscm.billingservice.business.model.resellershare.ResellerRevenueShareResult;
import org.oscm.billingservice.business.model.resellershare.ResellerShareResultAssembler;
import org.oscm.billingservice.business.model.suppliershare.SupplierRevenueShareResult;
import org.oscm.billingservice.business.model.suppliershare.SupplierShareResultAssembler;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.billingservice.dao.SharesDataRetrievalServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingSharesResult;
import org.oscm.interceptor.DateFactory;
import org.oscm.string.Strings;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.enumtypes.BillingSharesResultType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

/**
 * Session Bean implementation class SharesCalculatorBean
 */
@Stateless
@Local(SharesCalculatorLocal.class)
public class SharesCalculatorBean implements SharesCalculatorLocal {

    private static final Log4jLogger LOGGER = LoggerFactory
            .getLogger(RevenueCalculatorBean.class);

    @EJB(beanInterface = DataService.class)
    DataService ds;

    @EJB
    SharesDataRetrievalServiceLocal sharesRetrievalService;

    @EJB
    BillingDataRetrievalServiceLocal billingRetrievalService;

    @Resource
    private SessionContext sessionCtx;

    /**
     * Check if billing shares result for the given organization type and period
     * exists.
     * 
     * Note: As the period is fix for one month, the start and end time must
     * match exactly!
     */
    private boolean existBillingSharesResultEntry(long startOfLastMonth,
            long endOfLastMonth, Long orgKey, BillingSharesResultType resultType) {
        List<BillingSharesResult> billingSharesResult = sharesRetrievalService
                .loadBillingSharesResultForOrganization(orgKey, resultType,
                        Long.valueOf(startOfLastMonth),
                        Long.valueOf(endOfLastMonth));
        if (billingSharesResult.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    private void saveBillingSharesResult(long startOfLastMonth,
            long endOfLastMonth, BillingSharesResultType resultType,
            Object shareResult, Long sellerKey)
            throws NonUniqueBusinessKeyException, JAXBException {
        BillingSharesResult bsr = new BillingSharesResult();
        bsr.setCreationTime(DateFactory.getInstance().getTransactionTime());
        bsr.setOrganizationTKey(sellerKey.longValue());
        bsr.setPeriodStartTime(startOfLastMonth);
        bsr.setPeriodEndTime(endOfLastMonth);
        bsr.setResultType(resultType);
        byte[] xmlResult = marshallRevenueShareResults(shareResult);
        bsr.setResultXML(Strings.toString(xmlResult));
        ds.persist(bsr);
    }

    Marshaller createMarshaller(Object obj) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(obj.getClass());
        return context.createMarshaller();
    }

    byte[] marshallRevenueShareResults(Object obj) throws JAXBException {
        Marshaller marshaller = createMarshaller(obj);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        marshaller.marshal(obj, bos);
        return bos.toByteArray();
    }

    @Override
    public boolean performBrokerSharesCalculationRun(long startOfLastMonth,
            long endOfLastMonth) {
        boolean success = true;

        // get all brokers for which a shares calculation has to be performed
        // for the given billing period
        for (Long brokerKey : sharesRetrievalService
                .loadAllBrokerKeysWithinPeriod(endOfLastMonth)) {
            try {
                // run in separate transaction

                if (existBillingSharesResultEntry(startOfLastMonth,
                        endOfLastMonth, brokerKey,
                        BillingSharesResultType.BROKER)) {
                    // If there is already a billing shares result for the given
                    // organization, type and period, skip calculation for this
                    // organization and continue with the next organization.
                    continue;
                }

                prepareForNewTransaction().performBrokerShareCalculationRun(
                        startOfLastMonth, endOfLastMonth, brokerKey);
            } catch (Exception e) {
                success = false;
                LOGGER.logError(
                        Log4jLogger.SYSTEM_LOG,
                        e,
                        LogMessageIdentifier.ERROR_BROKER_REVENUE_SHARES_CALCULATION_FAILED_NO_BILL_GENERATED,
                        brokerKey.toString());
            }
        }
        return success;
    }

    private SharesCalculatorLocal prepareForNewTransaction() {
        DateFactory.getInstance().takeCurrentTime();
        return sessionCtx.getBusinessObject(SharesCalculatorLocal.class);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void performBrokerShareCalculationRun(long startOfLastMonth,
            long endOfLastMonth, Long brokerKey) throws Exception {
        // build the result object tree
        BrokerRevenueShareResult brokerShareResult = new BrokerShareResultAssembler(
                sharesRetrievalService, billingRetrievalService).build(
                brokerKey, startOfLastMonth, endOfLastMonth);

        // calculate all shares
        brokerShareResult.calculateAllShares();

        // serialize the result object and persist
        saveBillingSharesResult(startOfLastMonth, endOfLastMonth,
                BillingSharesResultType.BROKER, brokerShareResult, brokerKey);
    }

    @Override
    public boolean performResellerSharesCalculationRun(long startOfLastMonth,
            long endOfLastMonth) {
        boolean success = true;

        // get all resellers for which a shares calculation has to be performed
        // for the given billing period
        for (Long resellerKey : sharesRetrievalService
                .loadAllResellerKeysWithinPeriod(endOfLastMonth)) {
            try {
                // run in separate transaction

                if (existBillingSharesResultEntry(startOfLastMonth,
                        endOfLastMonth, resellerKey,
                        BillingSharesResultType.RESELLER)) {
                    // If there is already a billing shares result for the given
                    // organization, type and period, skip calculation for this
                    // organization and continue with the next organization.
                    continue;
                }

                prepareForNewTransaction().performResellerSharesCalculationRun(
                        startOfLastMonth, endOfLastMonth, resellerKey);
            } catch (Exception e) {
                success = false;
                LOGGER.logError(
                        Log4jLogger.SYSTEM_LOG,
                        e,
                        LogMessageIdentifier.ERROR_RESELLER_REVENUE_SHARES_CALCULATION_FAILED_NO_BILL_GENERATED,
                        resellerKey.toString());
            }
        }
        return success;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void performResellerSharesCalculationRun(long startOfLastMonth,
            long endOfLastMonth, Long resellerKey) throws Exception {
        // build the result object tree
        ResellerRevenueShareResult resellerShareResult = new ResellerShareResultAssembler(
                sharesRetrievalService, billingRetrievalService).build(
                resellerKey, startOfLastMonth, endOfLastMonth);

        // calculate all shares
        resellerShareResult.calculateAllShares();

        // serialize the result object and persist
        saveBillingSharesResult(startOfLastMonth, endOfLastMonth,
                BillingSharesResultType.RESELLER, resellerShareResult,
                resellerKey);
    }

    @Override
    public boolean performSupplierSharesCalculationRun(long startOfLastMonth,
            long endOfLastMonth) {
        boolean success = true;

        // get all resellers for which a shares calculation has to be performed
        // for the given billing period
        for (Long supplierKey : sharesRetrievalService
                .loadAllSupplierKeysWithinPeriod(endOfLastMonth)) {
            try {
                // run in separate transaction

                if (existBillingSharesResultEntry(startOfLastMonth,
                        endOfLastMonth, supplierKey,
                        BillingSharesResultType.SUPPLIER)) {
                    // If there is already a billing shares result for the given
                    // organization, type and period, skip calculation for this
                    // organization and continue with the next organization.
                    continue;
                }

                prepareForNewTransaction().performSupplierSharesCalculationRun(
                        startOfLastMonth, endOfLastMonth, supplierKey);
            } catch (Exception e) {
                success = false;
                LOGGER.logError(
                        Log4jLogger.SYSTEM_LOG,
                        e,
                        LogMessageIdentifier.ERROR_SUPPLIER_REVENUE_SHARES_CALCULATION_FAILED_NO_BILL_GENERATED,
                        supplierKey.toString());
            }
        }
        return success;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void performSupplierSharesCalculationRun(long startOfLastMonth,
            long endOfLastMonth, Long supplierKey) throws Exception {

        SupplierShareResultAssembler assembler = new SupplierShareResultAssembler(
                sharesRetrievalService);
        SupplierRevenueShareResult supplierShareResult = assembler.build(
                supplierKey, startOfLastMonth, endOfLastMonth);

        supplierShareResult.calculateAllShares();

        saveBillingSharesResult(startOfLastMonth, endOfLastMonth,
                BillingSharesResultType.SUPPLIER, supplierShareResult,
                supplierKey);
    }

    @Override
    public boolean performMarketplacesSharesCalculationRun(
            long startOfLastMonth, long endOfLastMonth) {
        boolean success = true;

        // get a list of all marketplaces references in the billing results of
        // the given period
        for (Long mpOwnerKey : sharesRetrievalService
                .loadAllMpOwnerKeysWithinPeriod(endOfLastMonth)) {
            try {
                // run in separate transaction

                if (existBillingSharesResultEntry(startOfLastMonth,
                        endOfLastMonth, mpOwnerKey,
                        BillingSharesResultType.MARKETPLACE_OWNER)) {
                    // If there is already a billing shares result for the given
                    // organization, type and period, skip calculation for this
                    // organization and continue with the next organization.
                    continue;
                }

                prepareForNewTransaction().performMpOwnerSharesCalculationRun(
                        startOfLastMonth, endOfLastMonth, mpOwnerKey);
            } catch (Exception e) {
                success = false;
                LOGGER.logError(
                        Log4jLogger.SYSTEM_LOG,
                        e,
                        LogMessageIdentifier.ERROR_MPOWNER_REVENUE_SHARES_CALCULATION_FAILED_NO_BILL_GENERATED,
                        mpOwnerKey.toString());
            }
        }
        return success;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void performMpOwnerSharesCalculationRun(long startOfLastMonth,
            long endOfLastMonth, Long mpOwnerKey) throws Exception {
        // build the result object tree
        MarketplaceOwnerRevenueShareResult mpOwnerShareResult = new MpOwnerShareResultAssembler(
                sharesRetrievalService, ds).build(mpOwnerKey, startOfLastMonth,
                endOfLastMonth);

        // calculate all shares
        mpOwnerShareResult.calculateAllShares();

        // serialize the result object and persist
        saveBillingSharesResult(startOfLastMonth, endOfLastMonth,
                BillingSharesResultType.MARKETPLACE_OWNER, mpOwnerShareResult,
                mpOwnerKey);
    }
}
