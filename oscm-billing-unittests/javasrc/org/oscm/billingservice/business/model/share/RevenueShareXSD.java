/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.billingservice.business.model.share;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;

import org.junit.Test;
import org.xml.sax.SAXParseException;

import org.oscm.billingservice.service.BillingServiceBean;
import org.oscm.test.XMLValidation;

public abstract class RevenueShareXSD {
    protected URL schemaUrl;
    private static String NAMESPACE = "http://oscm.org/xsd/billingservice/partnermodel";

    protected void validateXml(String xmlFile, String expectedErrorMessage)
            throws Exception {
        try {
            String relativeFilePath = getXmlFolder() + xmlFile + ".xml";
            XMLValidation.validateXML(getSchemaUrl(),
                    new File(relativeFilePath));

            if (expectedErrorMessage != null) {
                fail();
            }

        } catch (SAXParseException e) {
            e.printStackTrace();
            assertNotNull(expectedErrorMessage);
            assertTrue(e.getMessage().contains(expectedErrorMessage));
        }
    }

    protected abstract String getSchemaName();

    private String getXmlFolder() {
        URL url = RevenueShareXSD.class
                .getResource("/" + getSchemaName() + "/");
        assertNotNull(url);
        return url.getPath();
    }

    protected URL getSchemaUrl() {
        if (schemaUrl == null) {
            schemaUrl = BillingServiceBean.class.getResource("/"
                    + getSchemaName() + ".xsd");
        }
        assertNotNull(schemaUrl);
        return schemaUrl;
    }

    protected String expectedErrorMessage(String xmlElement) {
        return NAMESPACE + "\":" + xmlElement + "}";
    }

    @Test
    public void onlyPeriod() throws Exception {
        validateXml("onlyPeriod", null);
    }

    @Test
    public void empty() throws Exception {
        validateXml("empty", expectedErrorMessage("Period"));
    }

    @Test
    public void valid() throws Exception {
        validateXml("valid", null);
    }

    @Test
    public void validReq() throws Exception {
        validateXml("validReq", null);
    }
}
