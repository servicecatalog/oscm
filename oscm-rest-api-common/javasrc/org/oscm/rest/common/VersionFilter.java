/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: May 9, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.rest.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

/**
 * Request filter for validating the requested version and comparing with
 * endpoint annotations (Since and Until).
 * 
 * @author miethaner
 */
@Provider
public class VersionFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    private VersionValidator versionValidator = new VersionValidator();

    @Override
    public void filter(ContainerRequestContext request)
            throws WebApplicationException {

        MultivaluedMap<String, String> params = request.getUriInfo()
                .getPathParameters();

        if (params.containsKey(CommonParams.PARAM_VERSION)
                && !params.get(CommonParams.PARAM_VERSION).isEmpty()) {

            String version = params.get(CommonParams.PARAM_VERSION).get(0);

            int vnr = versionValidator.doIt(version);

            Method method = getResourceInfo().getResourceMethod();

            if (method.isAnnotationPresent(Since.class)) {

                Annotation annotation = method.getAnnotation(Since.class);
                Since since = (Since) annotation;

                if (vnr < since.value()) {
                    throw WebException.notFound()
                            .message(CommonParams.ERROR_METHOD_VERSION).build();
                }
            }
            if (method.isAnnotationPresent(Until.class)) {

                Annotation annotation = method.getAnnotation(Until.class);
                Until until = (Until) annotation;

                if (vnr >= until.value()) {
                    throw WebException.notFound()
                            .message(CommonParams.ERROR_METHOD_VERSION).build();
                }
            }
        } else {
            throw WebException.notFound()
                    .message(CommonParams.ERROR_INVALID_VERSION).build();
        }
    }

    public ResourceInfo getResourceInfo() {
        return resourceInfo;
    }
}
