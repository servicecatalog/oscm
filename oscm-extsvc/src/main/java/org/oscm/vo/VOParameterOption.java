/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-01-05                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;


/**
 * Represents an option of a parameter defined for a technical service.
 * 
 */
public class VOParameterOption extends BaseVO {

    private static final long serialVersionUID = 2643703914324384975L;

    String optionId;

    String optionDescription;

    String paramDefId;

    /**
     * Default constructor.
     */
    public VOParameterOption() {
    }

    /**
     * Constructs a parameter option with the given settings.
     * 
     * @param optionId
     *            the identifier of the parameter option
     * @param optionDescription
     *            the text describing the parameter option
     * @param paramDefId
     *            the identifier of the parameter definition to which the option
     *            belongs
     */
    public VOParameterOption(String optionId, String optionDescription,
            String paramDefId) {
        this.optionId = optionId;
        this.optionDescription = optionDescription;
        this.paramDefId = paramDefId;
    }

    /**
     * Retrieves the identifier of the parameter option.
     * 
     * @return the option ID
     */
    public String getOptionId() {
        return optionId;
    }

    /**
     * Sets the identifier of the parameter option.
     * 
     * @param optionId
     *            the option ID
     */
    public void setOptionId(String optionId) {
        this.optionId = optionId;
    }

    /**
     * Retrieves the text describing the parameter option.
     * 
     * @return the option description
     */
    public String getOptionDescription() {
        return optionDescription;
    }

    /**
     * Sets the text describing the parameter option.
     * 
     * @param optionDescription
     *            the option description
     */
    public void setOptionDescription(String optionDescription) {
        this.optionDescription = optionDescription;
    }

    /**
     * Retrieves the identifier of the parameter definition to which the option
     * belongs.
     * 
     * @return the parameter definition ID
     */
    public String getParamDefId() {
        return paramDefId;
    }

    /**
     * Sets the identifier of the parameter definition to which the option
     * belongs.
     * 
     * @param paramDefId
     *            the parameter definition ID
     */
    public void setParamDefId(String paramDefId) {
        this.paramDefId = paramDefId;
    }
}
