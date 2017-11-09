/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.converter;

import org.oscm.ui.beans.operator.OperatorOrgBean;
import org.oscm.internal.vo.VOPSP;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

@ManagedBean
@RequestScoped
public class PSPConverter implements Converter {

    @ManagedProperty(value = "#{operatorOrgBean}")
    private OperatorOrgBean oob;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {
        VOPSP retVal = null;
        for (VOPSP vopsp : oob.getPSPs()) {
            if ((Long.valueOf(vopsp.getKey()).toString().equals(value))) {
                retVal = vopsp;
            }
        }
        return retVal;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component,
                              Object object) {
        String retVal;
        if (!(object instanceof VOPSP)) {
            retVal = "";
        } else {
            retVal = String.valueOf(((VOPSP) object).getKey());
        }
        return retVal;
    }

    public OperatorOrgBean getOob() {
        return oob;
    }

    public void setOob(OperatorOrgBean oob) {
        this.oob = oob;
    }
}
