/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich.                                                      
 *                                                                              
 *  Creation Date: 07.07.2010.                                                      
 *                                                                              
 *  Completion Time: 07.07.2010.                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.model;

import java.math.BigDecimal;

import org.oscm.ui.common.JSFUtils;
import org.oscm.internal.vo.VORoleDefinition;

/**
 * Class which holds role specific prices (either for the user assignment a
 * parameter or a parameter option)
 * 
 */
public class RoleSpecificPrice {

    /** Role */
    private VORoleDefinition role;

    /**
     * if the row represents a parameter this is the description of the
     * parameter
     */
    private String parameterDescription;

    /** if the row represents an option this is the description of the option */
    private String optionDescription;

    /** Base price for role */
    private BigDecimal price = BigDecimal.ZERO;

    /**
     * Flag for enumeration parameter. If parameter is enumeration, the member
     * is not null.
     */
    private String enumerationParameter;

    public RoleSpecificPrice() {
        // empty constructor
    }

    public VORoleDefinition getRole() {
        return role;
    }

    public void setRole(VORoleDefinition role) {
        this.role = role;
    }

    public String getParameterDescription() {
        return parameterDescription;
    }

    public void setParameterDescription(String parameterDescription) {
        this.parameterDescription = parameterDescription;
    }

    public String getOptionDescription() {
        return optionDescription;
    }

    public void setOptionDescription(String optionDescription) {
        this.optionDescription = optionDescription;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * @param enumerationParameter
     *            the enumerationParameter to set
     */
    public void setEnumerationParameter(String enumerationParameter) {
        this.enumerationParameter = enumerationParameter;
    }

    /**
     * @return the enumerationParameter
     */
    public String getEnumerationParameter() {
        return enumerationParameter;
    }

    /**
     * Returns the parameter description (if defined), or the fixed message text
     * for the priced user role. Is used to display descriptions for
     * role-specific prices in the rolepricetable.
     */
    public String getParameterDescriptionToDisplay() {
        // was introduced in the context of fixing bug 8899. Should be revised.
        if (parameterDescription == null) {
            return JSFUtils.getText("priceModel.roles.chargePerUser.label",
                    null);
        }
        return parameterDescription;
    }

}
