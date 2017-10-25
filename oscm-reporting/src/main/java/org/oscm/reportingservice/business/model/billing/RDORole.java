/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                
 *                                                                              
 *  Creation Date: 07.07.2010                                                      
 *                                                                              
 *  Completion Time: <date>                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business.model.billing;

import java.util.ArrayList;
import java.util.List;

import org.oscm.reportingservice.business.model.RDO;

/**
 * @author weiser
 * 
 */
public class RDORole extends RDO implements RDOBilling {

    private static final long serialVersionUID = -9163003472768384025L;

    private String roleId;
    private String basePrice = "";
    private String price;
    private String factor = "";
    private List<RDOParameter> parameters = new ArrayList<RDOParameter>();

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(String basePrice) {
        this.basePrice = basePrice;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getFactor() {
        return factor;
    }

    public void setFactor(String factor) {
        this.factor = factor;
    }

    public void setParameters(List<RDOParameter> parameters) {
        this.parameters = parameters;
    }

    public List<RDOParameter> getParameters() {
        return parameters;
    }

    public RDOParameter getParameter(String id) {
        for (RDOParameter parameter : parameters) {
            if (parameter.getId().equals(id)) {
                return parameter;
            }
        }
        return null;
    }

}
