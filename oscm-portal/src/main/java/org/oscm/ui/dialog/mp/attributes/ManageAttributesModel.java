/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Oct 20, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.attributes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.oscm.ui.model.UdaRow;

/**
 * @author miethaner
 *
 */
@ManagedBean(name = "manageAttributesModel")
@ViewScoped
public class ManageAttributesModel {

    private Map<String, UdaRow> attributeMap = new HashMap<>();

    public Map<String, UdaRow> getAttributeMap() {
        return attributeMap;
    }

    public void setAttributeMap(Map<String, UdaRow> attributeMap) {
        if (attributeMap == null) {
            attributeMap = new HashMap<>();
        } else {
            this.attributeMap = attributeMap;
        }
    }

    public Collection<UdaRow> getAttributes() {
        return attributeMap.values();
    }

    public void setAttributes(Collection<UdaRow> attr) {
        attributeMap = new HashMap<>();

        if (attr != null) {
            for (UdaRow uda : attr) {
                attributeMap.put(uda.getUdaId(), uda);
            }
        }
    }

}
