/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Michael Falkenhahn                                         
 *                                                                              
 *  Creation Date: 09.12.2011                                                      
 *                                                                              
 *  Completion Time: 09.12.2011                                    
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class EnumConverter implements Converter {

    public Object getAsObject(FacesContext context, UIComponent component,
            String value) {

        if (value == null || component.getValueExpression("value") == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>) component
                .getValueExpression("value").getType(context.getELContext());
        for (Enum<?> e : enumType.getEnumConstants()) {
            if (e.toString().equals(value)) {
                return e;
            }
        }
        return null;
    }

    public String getAsString(FacesContext context, UIComponent component,
            Object object) {
        if (object == null) {
            return null;
        }
        Enum<?> e = (Enum<?>) object;
        return e.toString();
    }

}
