/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 16.07.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.mocksts;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;

//import com.sun.xml.ws.api.security.trust.Claims;
//import com.sun.xml.ws.api.security.trust.STSAttributeProvider;

/**
 * This implementation of STSAttributeProvider is invoked by Metro when a SAML
 * assertion is generated. It adds the user name given for authentication to the
 * NameID and AttributeStatement elements of the assertion.
 * 
 * Must be referred to in META-INF/services.
 * 
 * @author roderus
 * 
 */
public class MockSTSAttributeProvider {// implements STSAttributeProvider {
    
    private static final String COMMON_PROPERTIES_PATH = "common.properties";
    private static final String TENANT_ID = "tenantID";
    
    
//    @Override
    public Map<QName, List<String>> getClaimedAttributes(Subject subject,
            String appliesTo, String tokenType, Object claims) {
        String name = null;
        
        Set<Principal> principals = subject.getPrincipals();
        if (principals != null) {
            final Iterator<Principal> iterator = principals.iterator();
            while (iterator.hasNext()) {
                String cnName = principals.iterator().next().getName();
                int pos = cnName.indexOf("=");
                name = cnName.substring(pos + 1);
                break;
            }
        }

        Map<QName, List<String>> attributes = new HashMap<QName, List<String>>();
        
//        addAttribute(attributes, STSAttributeProvider.NAME_IDENTIFIER, name);
        addAttribute(attributes, "dummy_id1", "test_dummy_attribute1");
        addAttribute(attributes, "userid", name);
        addAttribute(attributes, "dummy_id2", "test_dummy_attribute2");
        
        String tenantId = PropertyLoader.getInstance()
                .load(COMMON_PROPERTIES_PATH).getProperty(TENANT_ID);

        addAttribute(attributes, TENANT_ID, tenantId);
        // claims not considered here

        return attributes;
    }
    
    private void addAttribute(Map<QName, List<String>> attributes, String name, String value){
        
        ArrayList<String> namedAttributes = new ArrayList<String>();
        namedAttributes.add(value);
        
        QName qName = new QName(name);
        
        attributes.put(qName, namedAttributes);
    }
}
