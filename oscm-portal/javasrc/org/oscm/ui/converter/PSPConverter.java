/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2020
 *******************************************************************************/

package org.oscm.ui.converter;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.oscm.internal.vo.VOPSP;
import org.oscm.ui.beans.operator.ManagePSPModel;

@ManagedBean
@RequestScoped
public class PSPConverter implements Converter {

    @ManagedProperty(value = "#{managePSPModel}")
    private ManagePSPModel model;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {
        VOPSP retVal = null;
        for (VOPSP vopsp : model.getPSPs()) {
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

    public ManagePSPModel getModel() {
        return model;
    }

    public void setModel(ManagePSPModel mspsm) {
        this.model = mspsm;
    }
}
