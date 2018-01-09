/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jan 13, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.handler;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

/**
 * Class responsible to add a version SOAP Handler into the SOAP Handler chain.
 */
public class ClientVersionHandler {

    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ClientVersionHandler() {

    }

    public ClientVersionHandler(String version) {
        this.version = version;
    }

    /**
     * This method adds a version SOAP Handler into the handler chain of a web
     * service. The version SOAP Handler is responsible to add a version
     * information in the header of the outbound SOAP message.
     * 
     * @param service
     *            set HandlerResolver for service by invoking service
     *            <code>setHandlerResolver</code> method.
     * @return service with handler chain for handling version information.
     */
    public Service addVersionInformationToClient(Service service) {
        service.setHandlerResolver(new HandlerResolver() {
            @SuppressWarnings("rawtypes")
            @Override
            public List<Handler> getHandlerChain(PortInfo portInfo) {
                List<Handler> handlerList = new ArrayList<Handler>();
                handlerList.add(new VersionHandler(version));
                return handlerList;
            }
        });
        return service;
    }

}
