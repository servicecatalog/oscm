/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2019                                           
 *                                                                                                                                 
 *  Creation Date: 24.01.2019                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.vmware.business.balancer;

import org.w3c.dom.Node;

/**
 * @author goebel
 *
 */
class XMLHelper {

    static String getAttributeValue(Node node, String name) {
        return getAttributeValue(node, name, "");
    }
    
    
    static String getAttributeValue(Node node, String name , String defaultValue) {
        Node attr = node.getAttributes().getNamedItem("name");
        if (attr != null)
            return attr.getNodeValue();
        return defaultValue;

    }

}