/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-7-31                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

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
import org.xml.sax.InputSource;

import org.oscm.converter.XMLConverter;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ParameterValueType;

/**
 * @author Wenxin Gao
 * 
 */
public class TechnicalProductXmlConverterModificationTypeTest {

    private TechnicalProductXmlConverter converter;
    private DataService mgr;
    private LocalizerServiceLocal localizer;
    private ParameterModificationType currentModificationType;

    @Before
    public void setup() {
        converter = spy(new TechnicalProductXmlConverter());
        mgr = mock(DataService.class);
        localizer = mock(LocalizerServiceLocal.class);
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
     * use currentModificationType to create ParameterDefinition of the
     * TechnicalProduct
     * 
     * @return
     */
    private List<TechnicalProduct> createTechnicalProduct() {
        TechnicalProduct product = new TechnicalProduct();
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
