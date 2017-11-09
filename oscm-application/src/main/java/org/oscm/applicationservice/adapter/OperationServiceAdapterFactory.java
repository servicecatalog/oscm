/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 10.10.2011                                                      
 *                                                                              
 *  Completion Time: 10.10.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.applicationservice.adapter;

import java.io.IOException;

import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import org.oscm.applicationservice.data.SupportedOperationVersions;
import org.oscm.applicationservice.operation.adapter.OperationServiceAdapterV1_0;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.string.Strings;
import org.oscm.ws.WSPortConnector;
import org.oscm.ws.WSPortDescription;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * Retrieves the service operation adapter according to the concrete WSDL.
 * 
 * @author weiser
 * 
 */
public class OperationServiceAdapterFactory {

    public static OperationServiceAdapter getOperationServiceAdapter(
            TechnicalProductOperation operation, Integer wsTimeout,
            String username, String password) throws IOException,
            WSDLException, ParserConfigurationException {

        String target = operation.getActionUrl();
        if (Strings.isEmpty(target)) {
            throw new SaaSSystemException(
                    String.format(
                            "Failed to retrieve service endpoint for service operation '%s', as the target is not defined.",
                        operation.getKey()));
        }
        WSPortConnector portConnector = new WSPortConnector(target, username,
                password);

        SupportedOperationVersions supportedVersion = getSupportedVersion(portConnector);
        OperationServiceAdapter adapter = new OperationServiceAdapterV1_0();
        final Object port = portConnector.getPort(
                supportedVersion.getLocalWSDL(),
                supportedVersion.getServiceClass(), wsTimeout);
        adapter.setOperationService(port);
        return adapter;
    }

    static SupportedOperationVersions getSupportedVersion(
            WSPortConnector portConnector) {
        WSPortDescription portDescription = portConnector.getPortDescription();
        String targetVersionFromWsdl = portDescription.getVersion();
        SupportedOperationVersions supportedVersion = SupportedOperationVersions
                .getForVersionString(targetVersionFromWsdl);
        if (supportedVersion == null) {
            supportedVersion = SupportedOperationVersions.VERSION_1_5;
        }
        return supportedVersion;
    }
}
