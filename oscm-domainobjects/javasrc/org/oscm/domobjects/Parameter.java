/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Peter Pock                                                      
 *                                                                              
 *  Creation Date: 29.06.2009                                                      
 *                                                                              
 *  Completion Time: 30.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.security.GeneralSecurityException;
import java.util.List;

import javax.persistence.*;

import org.oscm.encrypter.AESEncrypter;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;


/**
 * A parameter stores information to configure a product. The product has a
 * parameter set which contains the parameters. Parameters of the type
 * PRODUCT_PARAMETER are not evaluated by the platform but sent to the product
 * during tenant provisioning.
 * 
 * @author Peter Pock
 * 
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "Parameter.getParamByName", query = "SELECT p FROM Parameter p WHERE"
        + " p.parameterSet = :parameterSet AND p.parameterDefinition = :parameterDefinition")})
public class Parameter extends DomainObjectWithHistory<ParameterData> {

    private static final long serialVersionUID = 5611780484859016088L;

    private static final transient Log4jLogger logger = LoggerFactory
            .getLogger(Parameter.class);

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parameterSetKey", nullable = false)
    private ParameterSet parameterSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parameterDefinitionKey", nullable = false)
    private ParameterDefinition parameterDefinition;

    public Parameter() {
        super();
        dataContainer = new ParameterData();
    }

    public ParameterSet getParameterSet() {
        return parameterSet;
    }

    public void setParameterSet(ParameterSet parameterSet) {
        this.parameterSet = parameterSet;
    }

    public ParameterDefinition getParameterDefinition() {
        return parameterDefinition;
    }

    public void setParameterDefinition(
            ParameterDefinition parameterDefinition) {

        String value = getValue();

        this.parameterDefinition = parameterDefinition;

        setValue(value);
    }

    /**
     * The value of the parameter as long. If the the value parsing fails -1 is
     * returned.
     * 
     * @return the value of the parameter as long.
     */
    public long getLongValue() {
        try {
            return Long.parseLong(getValue());
        } catch (NumberFormatException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_PARAMETER_PARSING_FAILED);
            return -1l;
        }
    }

    /**
     * The value of the parameter as int. If the the value parsing fails -1 is
     * returned.
     * 
     * @return the value of the parameter as int.
     */
    public int getIntValue() {
        try {
            return Integer.parseInt(getValue());
        } catch (NumberFormatException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_PARAMETER_PARSING_FAILED);
            return -1;
        }
    }

    /**
     * The value of the parameter as boolean.
     * 
     * @return the value of the parameter as boolean.
     */
    public boolean getBooleanValue() {
        return Boolean.parseBoolean(getValue());
    }

    /**
     * Refer to {@link ParameterData#value}
     */
    public String getValue() {
        if (parameterDefinition != null
                && parameterDefinition.getValueType() == ParameterValueType.PWD
                && dataContainer.getValue() != null) {
            try {
                return AESEncrypter.decrypt(dataContainer.getValue());
            } catch (GeneralSecurityException e) {
                return null;
            }
        } else {
            return dataContainer.getValue();
        }
    }

    /**
     * Refer to {@link ParameterData#value}
     */
    public void setValue(String value) {
        if (value == null || value.trim().length() == 0) {
            dataContainer.setValue(null);
        } else if (parameterDefinition != null && parameterDefinition
                .getValueType() == ParameterValueType.PWD) {
            try {
                dataContainer.setValue(AESEncrypter.encrypt(value));
            } catch (GeneralSecurityException e) {
                // ignore
            }
        } else {
            dataContainer.setValue(value);
        }
    }

    public void setConfigurable(boolean configurable) {
        dataContainer.setConfigurable(configurable);
    }

    public boolean isConfigurable() {
        return dataContainer.isConfigurable();
    }

    /**
     * Creates a copy of this parameter and returns it. The reference to the
     * parameter definition remains.
     * 
     * @param owningParameterSet
     *            The parameter set the copied parameter belongs to.
     * 
     * @return A copy of this parameter.
     */
    public Parameter copy(ParameterSet owningParameterSet) {
        Parameter copy = new Parameter();
        copy.setDataContainer(new ParameterData());
        copy.setConfigurable(isConfigurable());
        copy.setValue(getValue());
        copy.setParameterDefinition(this.getParameterDefinition());
        copy.setParameterSet(owningParameterSet);
        return copy;
    }

    /**
     * Checks if a value is set - this is not the case for <code>null</code> and
     * trimmed empty strings
     * 
     * @return <code>false</code> if the value is <code>null</code> or empty
     */
    public boolean isValueSet() {
        String value = dataContainer.getValue();
        return (value != null && value.trim().length() > 0);
    }

    /**
     * Returns the ParameterOption corresponding to the specified parameter
     * option id.
     * 
     * @param optionId
     * @return <code>ParamterOption</code> object. <code>Null</code> if no entry
     *         is found or the option list is empty.
     */
    public ParameterOption getParameterOption(String optionId) {
        List<ParameterOption> options = getParameterDefinition()
                .getOptionList();
        for (ParameterOption parameterOption : options) {
            if (parameterOption.getOptionId().equals(optionId)) {
                return parameterOption;
            }
        }
        return null;

    }
}
