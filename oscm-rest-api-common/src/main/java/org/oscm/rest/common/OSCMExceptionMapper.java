/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: August 26, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.rest.common;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.oscm.internal.types.exception.SaaSApplicationException;

@Provider
public class OSCMExceptionMapper implements
        ExceptionMapper<SaaSApplicationException> {

    @Override
    public Response toResponse(SaaSApplicationException e) {
        switch (e.getClass().getName()) {
        case "ObjectNotFoundException":
            return WebException.notFound().message(e.getMessage()).build()
                    .getResponse();
        case "NonUniqueBusinessKeyException":
        case "OperationPendingException":
        case "ConcurrentModificationException":
            return WebException.conflict().message(e.getMessage()).build()
                    .getResponse();
        case "ValidationException":
        case "UserRoleAssignmentException":
            return WebException.badRequest().message(e.getMessage()).build()
                    .getResponse();
        case "OperationNotPermittedException":
            return WebException.forbidden().message(e.getMessage()).build()
                    .getResponse();
        default:
            return WebException.internalServerError().message(e.getMessage())
                    .build().getResponse();
        }
    }
}
