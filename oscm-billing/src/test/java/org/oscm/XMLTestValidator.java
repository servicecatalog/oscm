/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 04.10.2010                                                      
 *                                                                              
 *  Completion Time: 26.11.2010                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import org.oscm.test.EJBTestBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.converter.XMLConverter;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.test.ejb.TestContainer;

/**
 * Helper class to read the XML data and validate it.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class XMLTestValidator extends EJBTestBase {

    private DataService mgr;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new DataServiceBean());
        mgr = container.get(DataService.class);
    }

    /**
     * Validate the billing result XML structures stored in the database. The
     * found elements will be wrapped in the <i>Billingdata</i> element to match
     * the billing result schema definition.
     * 
     * @throws Exception
     */
    public void validateBillingResultXML() throws Exception {
        List<Document> docs = getBillingDocuments();
        URL schema = XMLValidation.getBillingResultSchemaURL();
        for (Document doc : docs) {
            Document parentDoc = XMLConverter.newDocument();
            Element bdElement = parentDoc.createElement("Billingdata");
            Node childNode = XMLConverter.getLastChildNode(doc, "BillingDetails");
            Node adoptNode = parentDoc.importNode(childNode, true);
            bdElement.appendChild(adoptNode);
            parentDoc.appendChild(bdElement);
            XMLValidation.validateXML(schema, parentDoc);
        }
    }

    /**
     * Validate the billing result XML structure as provided.
     * 
     * @param xml
     *            The billing XML structure.
     * @throws Exception
     */
    public void validateBillingResultXML(String xml) throws Exception {
        Document doc = XMLConverter.convertToDocument(xml, false);
        URL schema = XMLValidation.getBillingResultSchemaURL();
        XMLValidation.validateXML(schema, doc);
    }

    /**
     * Reads the current billing result XML and returns its content as Document.
     * 
     * @return The billing result document.
     * @throws Exception
     */
    private List<Document> getBillingDocuments() throws Exception {
        return runTX(new Callable<List<Document>>() {
            public List<Document> call() throws Exception {

                Query query = mgr
                        .createQuery("SELECT br FROM BillingResult br");
                List<BillingResult> list = ParameterizedTypes.list(query
                        .getResultList(), BillingResult.class);
                List<Document> resultList = new ArrayList<Document>();
                for (BillingResult billingResult : list) {
                    Document doc = XMLConverter.convertToDocument(billingResult
                            .getResultXML(), true);
                    resultList.add(doc);
                }
                return resultList;
            }
        });
    }

}
