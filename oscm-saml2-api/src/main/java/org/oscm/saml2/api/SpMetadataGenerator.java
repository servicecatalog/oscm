/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 11.06.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import javax.xml.bind.JAXBElement;

import org.w3c.dom.Document;

import org.oscm.saml2.api.model.metadata.EntityDescriptorType;
import org.oscm.saml2.api.model.metadata.IndexedEndpointType;
import org.oscm.saml2.api.model.metadata.MetadataFactory;
import org.oscm.saml2.api.model.metadata.SPSSODescriptorType;

/**
 * @author kulle
 * 
 */
public class SpMetadataGenerator {

    private final String PROTOCOL_SUPPORT_ENUMERATION = "urn:oasis:names:tc:SAML:2.0:protocol";
    private final String BINDING = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
    private final String NAME_ID_FORMAT = "urn:oasis:names:tc:SAML:2.0:nameid-format:transient";
    private final String acs0;
    private final String acs1;
    private final String entityId;

    public SpMetadataGenerator(String entityId, String acs_https,
            String acs_http) {

        if (acs_https != null && acs_https.trim().length() > 0) {
            acs_https += acs_https.endsWith("/") ? "" : "/";
        } else {
            acs_https = null;
        }

        if (acs_http != null && acs_http.trim().length() > 0) {
            acs_http += acs_http.endsWith("/") ? "" : "/";
        } else {
            acs_http = null;
        }

        this.entityId = entityId;
        this.acs0 = acs_https;
        this.acs1 = acs_http;
    }

    public Document generate() throws Exception {
        JAXBElement<EntityDescriptorType> spEntityDescriptor = buildObjectTree();
        Document document = marshall(spEntityDescriptor);
        return document;
    }

    private Document marshall(
            JAXBElement<EntityDescriptorType> spEntityDescriptor)
            throws Exception {
        Marshalling<EntityDescriptorType> marshaller = new Marshalling<EntityDescriptorType>();
        Document document = marshaller.marshallElement(spEntityDescriptor);
        return document;
    }

    private JAXBElement<EntityDescriptorType> buildObjectTree() {
        MetadataFactory factory = new MetadataFactory();

        IndexedEndpointType acsEndpoint_0 = null;
        IndexedEndpointType acsEndpoint_1 = null;
        if (acs0 != null) {
            acsEndpoint_0 = factory.createIndexedEndpointType();
            acsEndpoint_0.setBinding(BINDING);
            acsEndpoint_0.setIndex(0);
            acsEndpoint_0.setLocation(acs0);
            acsEndpoint_0.setIsDefault(Boolean.TRUE);
        }

        if (acs1 != null) {
            acsEndpoint_1 = factory.createIndexedEndpointType();
            acsEndpoint_1.setBinding(BINDING);
            acsEndpoint_1.setIndex(1);
            acsEndpoint_1.setLocation(acs1);
        }

        SPSSODescriptorType spDescriptor = factory.createSPSSODescriptorType();
        spDescriptor.getProtocolSupportEnumeration().add(
                PROTOCOL_SUPPORT_ENUMERATION);
        spDescriptor.setAuthnRequestsSigned(Boolean.FALSE);
        spDescriptor.setWantAssertionsSigned(Boolean.TRUE);
        spDescriptor.getNameIDFormat().add(NAME_ID_FORMAT);
        if (acsEndpoint_0 != null) {
            spDescriptor.getAssertionConsumerService().add(acsEndpoint_0);
        }
        if (acsEndpoint_1 != null) {
            spDescriptor.getAssertionConsumerService().add(acsEndpoint_1);
        }

        EntityDescriptorType entityDescriptor = factory
                .createEntityDescriptorType();
        entityDescriptor.setEntityID(entityId);
        entityDescriptor.getRoleDescriptorOrIDPSSODescriptorOrSPSSODescriptor()
                .add(spDescriptor);

        return factory.createEntityDescriptor(entityDescriptor);
    }
}
