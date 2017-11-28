/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 11, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.jmx.internal.mbean;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

import org.oscm.jmx.internal.bean.EJBClientFacade;
import org.oscm.jmx.internal.common.MBean;

/**
 * @author tokoda
 * 
 */
@MBean
public class SetConfigurationSetting implements DynamicMBean {

    private final EJBClientFacade ejbClientFacade;
    private static final String SET_CONFIGURATION_SETTING = "setConfigurationSetting";

    public SetConfigurationSetting(EJBClientFacade clientFacade) {
        this.ejbClientFacade = clientFacade;
    }

    @Override
    public synchronized MBeanInfo getMBeanInfo() {
        MBeanParameterInfo[] parameter = new MBeanParameterInfo[] {
                new MBeanParameterInfo("informationId", "java.lang.String",
                        "The information ID which the value will be changed. e.g. AUTH_MODE"),
                new MBeanParameterInfo("value", "java.lang.String",
                        "The value to be set for the information ID. e.g. INTERNAL, SAML_SP") };

        MBeanOperationInfo[] operations = { new MBeanOperationInfo(
                SET_CONFIGURATION_SETTING, "Set configuration setting",
                parameter, "java.lang.String", MBeanOperationInfo.ACTION) };

        return new MBeanInfo(this.getClass().getName(),
                "Set Configuration Setting MBean", null, null, operations, null);
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature)
            throws MBeanException, ReflectionException {
        Object result = null;
        if (actionName.equals(SET_CONFIGURATION_SETTING)) {
            result = setConfigurationSetting((String) params[0],
                    (String) params[1]);
        } else {
            throw new ReflectionException(new NoSuchMethodException(actionName));
        }
        return result;
    }

    private String setConfigurationSetting(String informationId, String value) {
        String result;
        try {
            ejbClientFacade.setConfigurationSetting(informationId, value);
            result = "ConfigurationServiceInternal.setConfigurationSetting successful executed.";
        } catch (Exception e) {
            e.printStackTrace();
            result = "ConfigurationServiceInternal.setConfigurationSetting failed.\nException:\n"
                    + e;
        }
        return result;
    }

    @Override
    public synchronized Object getAttribute(String attribute)
            throws AttributeNotFoundException, MBeanException {
        return null;
    }

    @Override
    public synchronized void setAttribute(Attribute attribute)
            throws AttributeNotFoundException, InvalidAttributeValueException,
            MBeanException {
    }

    @Override
    public synchronized AttributeList getAttributes(String[] names) {
        return null;
    }

    @Override
    public synchronized AttributeList setAttributes(AttributeList list) {
        return list;
    }
}
