/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-7-31                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.oscm.converter.XMLConverter;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.OperationParameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.types.enumtypes.OperationParameterType;
import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.vo.VOLocalizedText;

/**
 * @author Wenxin Gao
 * 
 */
public class TechnicalProductXmlConverterTest {

    private TechnicalProductXmlConverter converter;
    private DataService mgr;
    private LocalizerServiceLocal localizer;
    private ParameterModificationType currentModificationType;
    private Document xmldoc;
    private TechnicalProductOperation tpo;
    private final String PROVISIONING_USER_NAME = "admin";
    private final String PROVISIONING_PASSWORD = "adminadmin";

    @Before
    public void setup() {
        XmlDocument doc = new XmlDocument(
                "oscm.serviceprovisioning/1.9/TechnicalService.xsd",
                "tns",
                "oscm.serviceprovisioning/1.9/TechnicalService.xsd ../../oscm-serviceprovisioning/javares/TechnicalServices.xsd",
                "TechnicalServices");
        xmldoc = doc.getDomDocument();

        converter = spy(new TechnicalProductXmlConverter());
        mgr = mock(DataService.class);
        localizer = mock(LocalizerServiceLocal.class);

        tpo = new TechnicalProductOperation();
    }

    @Test
    public void technicalProductToXml_ModificationTypeIsOneTime()
            throws Exception {
        // given
        currentModificationType = ParameterModificationType.ONE_TIME;
        List<TechnicalProduct> techProds = createTechnicalProduct();

        // when
        byte[] content = converter.technicalProductToXml(techProds, localizer,
                mgr);

        // then
        String xmlString = new String(content, "UTF-8");
        verifyXmlForModificationType(xmlString);
    }

    @Test
    public void technicalProductToXml_ModificationTypeIsStandard()
            throws Exception {
        currentModificationType = ParameterModificationType.STANDARD;
        List<TechnicalProduct> techProds = createTechnicalProduct();
        byte[] content = converter.technicalProductToXml(techProds, localizer,
                mgr);
        String xmlString = new String(content);
        verifyXmlForModificationType(xmlString);
    }

    @Test
    public void technicalProductToXml_NoModificationType() throws Exception {
        currentModificationType = null;
        List<TechnicalProduct> techProds = createTechnicalProduct();
        byte[] content = converter.technicalProductToXml(techProds, localizer,
                mgr);
        String xmlString = new String(content);
        verifyXmlForModificationType(xmlString);
    }

    @Test
    public void appendOperationParameters() {
        initForAppendOperationParameters();

        converter.appendOperationParameters(xmldoc,
                xmldoc.getDocumentElement(), localizer, tpo);

        verifyOperationParameters(xmldoc.getDocumentElement());
    }

    @Test
    public void appendOperationParameters_Empty() {
        converter.appendOperationParameters(xmldoc,
                xmldoc.getDocumentElement(), localizer, tpo);

        NodeList list = xmldoc.getDocumentElement().getChildNodes();
        assertEquals(0, list.getLength());
    }

    @Test
    public void technicalProductToXml_bug10911() throws Exception {
        List<TechnicalProduct> techProds = createTechnicalProduct();
        byte[] content = converter.technicalProductToXml(techProds, localizer,
                mgr);
        String xmlString = new String(content);
        verifyXmlForUserAndPwd(xmlString);
    }

    private void verifyOperationParameters(Element e) {
        NodeList list = e.getChildNodes();
        for (int index = 0; index < list.getLength(); index++) {
            Node item = list.item(index);
            OperationParameter op = tpo.getParameters().get(index);

            assertEquals("OperationParameter", item.getNodeName());
            assertEquals(op.getId(), item.getAttributes().getNamedItem("id")
                    .getNodeValue());
            assertEquals(op.getType().name(), item.getAttributes()
                    .getNamedItem("type").getNodeValue());
            assertEquals(
                    op.isMandatory(),
                    Boolean.parseBoolean(item.getAttributes()
                            .getNamedItem("mandatory").getNodeValue()));

            NodeList names = item.getChildNodes();
            Node en = names.item(0);
            assertEquals("LocalizedName", en.getNodeName());
            assertEquals("en", en.getAttributes().getNamedItem("locale")
                    .getNodeValue());
            assertEquals("param name", en.getTextContent());

            Node de = names.item(1);
            assertEquals("LocalizedName", de.getNodeName());
            assertEquals("de", de.getAttributes().getNamedItem("locale")
                    .getNodeValue());
            assertEquals("Param Name", de.getTextContent());
        }
    }

    private void initForAppendOperationParameters() {
        when(
                localizer
                        .getLocalizedValues(
                                anyLong(),
                                eq(LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_PARAMETER_NAME)))
                .thenReturn(
                        Arrays.asList(new VOLocalizedText("en", "param name"),
                                new VOLocalizedText("de", "Param Name")));

        OperationParameter op = new OperationParameter();
        op.setId("param1");
        op.setMandatory(true);
        op.setType(OperationParameterType.REQUEST_SELECT);
        tpo.getParameters().add(op);

        op = new OperationParameter();
        op.setId("param2");
        op.setMandatory(false);
        op.setType(OperationParameterType.INPUT_STRING);
        tpo.getParameters().add(op);
    }

    /**
     * parse the XML content and verify the ModificationType has been exported
     * as expected
     * 
     * @param xmlString
     * @throws Exception
     */
    private void verifyXmlForModificationType(String xmlString)
            throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(
                xmlString)));
        assertEquals(1, document.getChildNodes().getLength());
        Element root = document.getDocumentElement();
        assertEquals("tns:TechnicalServices", root.getNodeName());

        List<?> productNodes = XMLConverter.getNodeList(root.getChildNodes(),
                "tns:TechnicalService");
        assertEquals(1, productNodes.size());

        Node n = (Node) productNodes.get(0);
        List<Node> serviceNode = XMLConverter.getNodeList(n.getChildNodes(),
                "ParameterDefinition");
        assertEquals(1, serviceNode.size());

        NamedNodeMap parameterDefinitionAttrs = serviceNode.get(0)
                .getAttributes();
        // if the currentModificationType is not OneTime, the modificationType
        // attribute should not be exported
        if (currentModificationType == null
                || currentModificationType
                        .equals(ParameterModificationType.STANDARD)) {
            assertNull(parameterDefinitionAttrs
                    .getNamedItem("modificationType"));
        } else {
            // if the currentModificationType is OneTime, verify the value is
            // correct
            assertEquals(currentModificationType.name(),
                    parameterDefinitionAttrs.getNamedItem("modificationType")
                            .getTextContent());
        }
    }

    /**
     * parse the XML content and verify the provisioningUsername and
     * provisioningPassword have been exported as expected
     * 
     * @param xmlString
     * @throws Exception
     */
    private void verifyXmlForUserAndPwd(String xmlString) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(
                xmlString)));
        assertEquals(1, document.getChildNodes().getLength());
        Element root = document.getDocumentElement();
        assertEquals("tns:TechnicalServices", root.getNodeName());
        List<?> productNodes = XMLConverter.getNodeList(root.getChildNodes(),
                "tns:TechnicalService");
        assertEquals(1, productNodes.size());
        Element e = (Element) productNodes.get(0);
        assertEquals(PROVISIONING_USER_NAME,
                e.getAttribute("provisioningUsername"));
        assertEquals(PROVISIONING_PASSWORD,
                e.getAttribute("provisioningPassword"));
    }

    /**
     * use currentModificationType to create ParameterDefinition of the
     * TechnicalProduct
     * 
     * @return
     */
    private List<TechnicalProduct> createTechnicalProduct() {
        TechnicalProduct product = new TechnicalProduct();
        product.setProvisioningUsername(PROVISIONING_USER_NAME);
        product.setProvisioningPassword(PROVISIONING_PASSWORD);
        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();
        ParameterDefinition def = new ParameterDefinition();
        def.setParameterId("Def_ID");
        def.setValueType(ParameterValueType.BOOLEAN);
        if (currentModificationType != null) {
            def.setModificationType(currentModificationType);
        }
        parameterDefinitions.add(def);
        product.setParameterDefinitions(parameterDefinitions);
        return Arrays.asList(product);
    }
}
