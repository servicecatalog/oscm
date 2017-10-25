/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: stavreva                                   
 *                                                                              
 *  Creation Date: 24.04.2013                                                     
 *                                                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.interceptor;

import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.oscm.auditlog.AuditLogData;
import org.oscm.auditlog.bean.AuditLogServiceBean;

public class AuditLogDataInterceptor {

    @EJB
    AuditLogServiceBean auditlogService;

    @AroundInvoke
    public Object logAuditLogData(InvocationContext context) throws Exception {
        AuditLogData.clear();
        Object result = context.proceed();
        auditlogService.log(AuditLogData.get());
        return result;
    }

}
