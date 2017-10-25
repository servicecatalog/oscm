/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 19, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import java.sql.Types;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.converter.DateConverter;
import org.oscm.converter.XMLConverter;
import org.oscm.reportingservice.dao.ReportResultData;
import org.oscm.reportingservice.dao.SubscriptionDao;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * @author kulle
 * 
 */
public class ReportDataConverter {

    static final Log4jLogger logger = LoggerFactory
            .getLogger(ReportDataConverter.class);
    private final SubscriptionDao dao;

    private static final String[] REPORTS_DATE_FIELDS = new String[] {
            "ACTIVATIONDATE", "ASSIGNMENTDATE", "BILLINGDATE",
            "DEACTIVATIONDATE", "OCCURRENCETIME", "PROCESSINGTIME",
            "PERIODSTARTTIME", "PERIODENDTIME", "REGISTRATIONDATE" };
    private static final Set<String> dateColumnNames = new HashSet<String>(
            Arrays.asList(REPORTS_DATE_FIELDS));

    public ReportDataConverter(SubscriptionDao dao) {
        this.dao = dao;
    }

    public void convertToXml(List<ReportResultData> reportData,
            List<Object> result, Map<String, String> xmlMap)
            throws XPathExpressionException, ParserConfigurationException {

        Document document = newEmptyDocument();
        Map<String, String> lastSubIdMap = dao
                .retrieveLastValidSubscriptionIdMap();

        for (ReportResultData data : reportData) {
            Element row = document.createElement("row");
            for (int columnIndex = 0; columnIndex < data.getColumnCount(); columnIndex++) {
                String columnName = data.getColumnName().get(columnIndex);
                Object value = data.getColumnValue().get(columnIndex);
                Element node = document.createElement(columnName.toUpperCase());
                if (value != null) {

                    if (columnName.equalsIgnoreCase("SUBSCRIPTIONID")) {
                        String id = lastSubIdMap.get(value);
                        if (id != null) {
                            value = id;
                        }
                    } else if (columnName.equalsIgnoreCase("PRODUCTID")) {
                        String productId = (String) value;
                        String[] split = productId.split("#");
                        value = split[0];
                    } else if (dateColumnNames.contains(columnName
                            .toUpperCase()) && value instanceof Long) {
                        Long dateFieldValue = (Long) value;

                        value = DateConverter.convertLongToDateTimeFormat(
                                dateFieldValue.longValue(),
                                TimeZone.getDefault(),
                                DateConverter.DTP_WITHOUT_MILLIS);
                    }
                    readColumnValue(document, xmlMap, columnName, value, data,
                            columnIndex, node);
                } else {
                    int columnType = data.getColumnType().get(columnIndex)
                            .intValue();
                    if (columnType == Types.VARCHAR
                            || columnType == Types.CLOB
                            || (columnType == Types.BIGINT && dateColumnNames
                                    .contains(columnName.toUpperCase()))) {
                        node.appendChild(document.createTextNode(""));
                    } else {
                        node.appendChild(document.createTextNode("null"));
                    }
                }
                row.appendChild(node);
            }
            result.add(row);
        }
    }

    private Document newEmptyDocument() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.newDocument();
    }

    private void readColumnValue(Document doc,
            Map<String, String> xmlFieldXPaths, String columnName,
            Object value, ReportResultData reportData, int index, Element node)
            throws XPathExpressionException {
        if ("RESULTXML".equalsIgnoreCase(columnName)
                || "PROCESSINGRESULT".equalsIgnoreCase(columnName)) {
            Document columnValueAsDoc = parseXML((String) reportData
                    .getColumnValue().get(index));

            if (xmlFieldXPaths.containsKey(columnName.toLowerCase())) {
                String xpathEvaluationResult = XMLConverter
                        .getNodeTextContentByXPath(columnValueAsDoc,
                                xmlFieldXPaths.get(columnName));
                // don't store null values but empty strings instead, to ensure
                // proper display on client side
                if (xpathEvaluationResult == null) {
                    xpathEvaluationResult = "";
                }
                node.appendChild(doc.createTextNode(xpathEvaluationResult));
            } else {
                appendXMLStructureToNode(node, columnValueAsDoc);
            }
        } else {
            node.appendChild(doc.createTextNode(value.toString()));
        }
    }

    private Document parseXML(String xmlAsString) {
        try {
            return XMLConverter.convertToDocument(xmlAsString, false);
        } catch (Exception e) {
            throw new SaaSSystemException(
                    "Parsing failed for:\n" + xmlAsString, e);
        }
    }

    /**
     * Clones the document parameter and appends the result to the given parent
     * node.
     * 
     * @param parentNode
     *            The node to append the document to.
     * @param doc
     *            The document to be appended.
     */
    private void appendXMLStructureToNode(Element parentNode, Document doc) {
        if (doc != null) {
            Node cloneNode = doc.getFirstChild().cloneNode(true);
            cloneNode = parentNode.getOwnerDocument().importNode(cloneNode,
                    true);
            parentNode.appendChild(cloneNode);
        }
    }

}
