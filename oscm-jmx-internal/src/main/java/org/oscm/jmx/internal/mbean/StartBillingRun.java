/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 27.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.jmx.internal.mbean;

import java.text.ParseException;
import java.text.SimpleDateFormat;

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

@MBean
public class StartBillingRun implements DynamicMBean {
    private EJBClientFacade ejbClientFacade;
    private static final String START_BILLING_RUN = "startBillingRun";
    private static final String DATE_FORMAT = "dd.MM.yyyy";

    public StartBillingRun(EJBClientFacade clientFacade) {
        this.ejbClientFacade = clientFacade;
    }

    @Override
    public synchronized MBeanInfo getMBeanInfo() {
        MBeanParameterInfo[] parameter = new MBeanParameterInfo[] { new MBeanParameterInfo(
                "currentDate", "java.lang.String",
                "The current date for the billing run. Format is "
                        + DATE_FORMAT.toUpperCase()) };

        MBeanOperationInfo[] operations = { new MBeanOperationInfo(
                START_BILLING_RUN, "Start billing run", parameter,
                "java.lang.String", MBeanOperationInfo.ACTION) };

        return new MBeanInfo(this.getClass().getName(), "Billing Run MBean",
                null, null, operations, null);
    }

    @Override
    public Object invoke(String name, Object[] args, String[] sig)
            throws MBeanException, ReflectionException {
        Object result = null;
        if (name.equals(START_BILLING_RUN)) {
            result = startBillingRun((String) args[0]);

        } else {
            throw new ReflectionException(new NoSuchMethodException(name));
        }
        return result;
    }

    public String startBillingRun(String currentDate) {
        String result;
        try {
            boolean status = ejbClientFacade
                    .startBillingRun(getCurrentTime(currentDate));
            result = "BillingService.startBillingRun successful executed.\nReturn state is "
                    + status;

        } catch (ParseException e) {
            result = "BillingService.startBillingRun failed.\nReason:\nThe format for currentDate is "
                    + DATE_FORMAT.toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
            result = "BillingService.startBillingRun failed.\nException:\n" + e;
        }
        return result;
    }

    private long getCurrentTime(String currentDate) throws ParseException {
        return new SimpleDateFormat(DATE_FORMAT).parse(currentDate).getTime();
    }

    @Override
    public synchronized String getAttribute(String name)
            throws AttributeNotFoundException {
        return null;
    }

    @Override
    public synchronized void setAttribute(Attribute attribute)
            throws InvalidAttributeValueException, MBeanException,
            AttributeNotFoundException {
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
