/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2013-8-2                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.samlsp.ws.base;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.oscm.converter.api.EnumConverter;
import org.oscm.converter.api.VOConverter;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.intf.TenantService;
import org.oscm.internal.vo.VOTenant;
import org.oscm.test.setup.PropertiesReader;
import org.oscm.types.enumtypes.OrganizationRoleType;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOUserDetails;
import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.VOFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.ws.base.WebserviceTestSetup;
import org.oscm.xml.Transformers;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * @author Qiu
 */
public class WebserviceSAMLSPTestSetup extends WebserviceTestSetup {

    private static final String STSConfigTemplateFileName = "MockSTSServiceTemplate.xml";
    private static final String STSConfigFileName = "MockSTSService.xml";

    private static VOFactory factory = new VOFactory();
    private String supplierUserId;
    
    private static OperatorService operator;
    private static TenantService tenantService;
    
    public WebserviceSAMLSPTestSetup() {
        setJKSLocation(getExampleDomainPath());
    }

    private void setJKSLocation(String domainPath) {
        URL fileUrl = Thread.currentThread().getContextClassLoader()
                .getResource(STSConfigTemplateFileName);

        File file = new File(fileUrl.getFile());
        if (!file.exists()) {
            System.out.println("MockSTSServiceTemplate.xml not exists");
            return;
        }
        try {
            Document doc = parse(file);
            XPathFactory xpfactory = XPathFactory.newInstance();
            XPath path = xpfactory.newXPath();
            updateElementValue(path, doc,
                    "/definitions/Policy/ExactlyOne/All/KeyStore", "location",
                    domainPath + "/config/keystore.jks");
            updateElementValue(path, doc,
                    "/definitions/Policy/ExactlyOne/All/TrustStore",
                    "location", domainPath + "/config/cacerts.jks");
            doc2Xml(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doc2Xml(Document doc)
            throws TransformerFactoryConfigurationError,
            TransformerConfigurationException, FileNotFoundException,
            TransformerException, IOException {

        Transformer transformer = Transformers.newTransformer();
        DOMSource domSource = new DOMSource(doc);
        OutputStream out = new FileOutputStream(Thread.currentThread()
                .getContextClassLoader().getResource(STSConfigTemplateFileName)
                .getPath()
                .replace(STSConfigTemplateFileName, STSConfigFileName));
        StreamResult result = new StreamResult(out);
        transformer.transform(domSource, result);
        out.close();
    }

    private void updateElementValue(XPath path, Document doc, String pathValue,
            String attribute, String value) throws XPathExpressionException {
        Element node = (Element) path.evaluate(pathValue, doc,
                XPathConstants.NODE);
        if (node != null) {
            node.setAttribute(attribute, value);
        }
    }

    protected String getExampleDomainPath() {
        PropertiesReader reader = new PropertiesReader();
        Properties props = new Properties();
        try {
            props = reader.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return props.getProperty("glassfish.example.domain");
    }

    private Document parse(File file) throws ParserConfigurationException,
            SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(file);
    }
    
    @Override
    public VOOrganization createSupplier(String namePrefix) throws Exception {
        supplierUserId = namePrefix + "_"
                + WebserviceTestBase.createUniqueKey();
        VOOrganization supplier = createOrganization(supplierUserId,
                namePrefix, OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        identitySrvAsSupplier = ServiceFactory.getSTSServiceFactory()
                .getIdentityService(supplierUserId,
                        WebserviceTestBase.DEFAULT_PASSWORD);

        List<UserRoleType> userRoles = new ArrayList<UserRoleType>();
        userRoles.add(UserRoleType.SERVICE_MANAGER);
        VOUserDetails user = factory.createUserVO("ServiceManager" + "_"
                + WebserviceTestBase.createUniqueKey());
        user.setOrganizationId(supplier.getOrganizationId());
        identitySrvAsSupplier.createUser(user, userRoles, null);
        return supplier;
    }

    @Override
    public String getSupplierUserId() {
        return supplierUserId;
    }

    public static VOOrganization createOrganization(String administratorId,
            String name, OrganizationRoleType... rolesToGrant) throws Exception {

        VOUserDetails adminUser = factory.createUserVO(administratorId);
        VOOrganization organization = factory.createOrganizationVO();
        if (name != null && name.trim().length() > 0) {
            organization.setName(name);
        }

        List<org.oscm.internal.types.enumtypes.OrganizationRoleType> convertedRoles = new ArrayList<>();
        for (OrganizationRoleType r : rolesToGrant) {
            convertedRoles
                    .add(EnumConverter
                            .convert(
                                    r,
                                    org.oscm.internal.types.enumtypes.OrganizationRoleType.class));
        }

        org.oscm.internal.vo.VOOrganization internalVOOrg = VOConverter
                .convertToUp(organization);
        if (Arrays.asList(rolesToGrant).contains(OrganizationRoleType.SUPPLIER)) {
            internalVOOrg.setOperatorRevenueShare(BigDecimal.ZERO);
        }

        organization = VOConverter
                .convertToApi(getOperator()
                        .registerOrganization(
                                internalVOOrg,
                                null,
                                VOConverter.convertToUp(adminUser),
                                null,
                                null,
                                convertedRoles
                                        .toArray(new org.oscm.internal.types.enumtypes.OrganizationRoleType[convertedRoles
                                                .size()])));
        System.out.println("created organization, adminId=" + administratorId);
        return organization;
    }

    public static OperatorService getOperator() throws Exception {
        synchronized (WebserviceSAMLSPTestSetup.class) {
            if (operator == null) {
                operator = ServiceFactory.getSTSServiceFactory()
                        .getOperatorService();
            }
        }
        return operator;
    }
    
    private static TenantService getTenantService() throws Exception {
        synchronized (WebserviceSAMLSPTestSetup.class) {
            if (tenantService == null) {
                tenantService = ServiceFactory.getDefault().getTenantService();
            }
        }
        return tenantService;
    }
    
    public static void createTenant(String tenantId) throws Exception  {
        
        VOTenant voTenant = factory.createTenantVo(tenantId);
        getTenantService().addTenant(voTenant);   
    }
    
}
