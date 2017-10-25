/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 06.12.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.domobjects.BillingResult;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.exceptions.BillingRunFailed;

/**
 * Auxiliary class to handle subtask checks.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class BillingConditionsEvaluator {

    private static final Log4jLogger LOG = LoggerFactory
            .getLogger(BillingConditionsEvaluator.class);

    public static boolean isValidBillingResult(BillingInput billingInput,
            Schema schema, BillingResult billingResult) {

        if (billingResult.getResultXML() == null
                || "".equals(billingResult.getResultXML().trim())) {
            return false;
        }

        String billingDataXml = embedIntoBillingDataElement(billingResult
                .getResultXML());
        Source source = new StreamSource(new StringReader(billingDataXml));
        Validator validator = schema.newValidator();
        try {
            validator.validate(source);
        } catch (SAXException ex) {
            LOG.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    ex,
                    LogMessageIdentifier.WARN_BILLINGRESULT_VALIDATION_PARSER_ERROR,
                    String.valueOf(billingInput.getOrganizationKey()),
                    String.valueOf(billingInput.getSubscriptionKey()),
                    String.valueOf(ex.getMessage()),
                    billingResult.getResultXML());
            return false;
        } catch (IOException e) {
            throw new BillingRunFailed(e);
        }

        return true;
    }

    private static String embedIntoBillingDataElement(String billingResultXml) {
        return "<Billingdata>" + billingResultXml + "</Billingdata>";
    }

}
