/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: barzu                                   
 *                                                                              
 *  Creation Date: Oct 5, 2011                                                      
 *                                                                              
 *  Completion Time: Oct 7, 2011     
 *                                                                              
 *******************************************************************************/

package org.oscm.interceptor;

import javax.ejb.LocalBean;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;


//TODO TOMEE
//import org.oscm.internal.types.exception.SaaSSystemException;
//import com.sun.ejb.EjbInvocation;


/**
 * Holds the invocation date of remote interface methods. It is set as
 * interceptor for all service beans. The date is set stored in a thread local
 * variable and is retrieved later from the code in the service classes.
 * 
 * @author barzu
 */
public class InvocationDateContainer {

    @AroundInvoke
    public Object setTransactionTime(InvocationContext context)
            throws Exception {
        if (isNoInterfaceView(context)
                || isRemoteOrMessageDrivenInvokation(context)) {
            DateFactory.getInstance().takeCurrentTime();
        }
        return context.proceed();
    }

    boolean isRemoteOrMessageDrivenInvokation(InvocationContext context) {
//        if (!(context instanceof EjbInvocation)) {
//            SaaSSystemException saasEx = new SaaSSystemException(
//                    "Unexcepcted invocation context: "
//                            + context.getClass().getName());
//            // TODO LOG
//            throw saasEx;
//        }
//        EjbInvocation ec = (EjbInvocation) context;
//        return (ec.isMessageDriven || ec.isRemote || ec.isWebService);
        return true;
    }

    boolean isNoInterfaceView(InvocationContext context) {
        return (context.getMethod().getDeclaringClass()
                .getAnnotation(LocalBean.class) != null);
    }
}
