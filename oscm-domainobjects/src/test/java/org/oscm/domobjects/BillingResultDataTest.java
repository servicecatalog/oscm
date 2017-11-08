/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 1, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

public class BillingResultDataTest {

    BillingResultData billingData;

    @Before
    public void setup() throws Exception {
        billingData = new BillingResultData();
    }

    @Test
    public void getDocument() throws Exception {
        // given
        billingData.setResultXML("<r></r>");

        // when
        Document document = billingData.getDocument();

        // then
        assertEquals(document, billingData.document);
        assertEquals("<r></r>".hashCode(), billingData.xmlHash);
    }

    @Test
    public void getDocument_modifiedXml() throws Exception {
        // given
        billingData.setResultXML("<r></r>");
        billingData.getDocument();
        billingData.setResultXML("<x></x>");

        // when
        Document document = billingData.getDocument();

        // then
        assertEquals(document, billingData.document);
        assertEquals("<x></x>".hashCode(), billingData.xmlHash);
    }

    @Test
    public void getDocument_documentNull() throws Exception {
        // given
        billingData.setResultXML("<r></r>");
        billingData.getDocument();
        billingData.document = null;

        // when
        Document document = billingData.getDocument();

        // then
        assertEquals(document, billingData.document);
        assertEquals("<r></r>".hashCode(), billingData.xmlHash);
    }

    @Test
    public void getDocument_nullXml() throws Exception {
        // given
        billingData.setResultXML(null);

        // when
        Document document = billingData.getDocument();

        // then
        assertNull(document);
    }

}
