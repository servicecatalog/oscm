/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 06.07.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.applicationservice.adapter;

import org.oscm.applicationservice.data.SupportedProvisioningVersions;
import org.oscm.applicationservice.provisioning.adapter.ProvisioningServiceAdapterV1_8;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ws.WSPortConnector;

import javax.wsdl.WSDLException;
import javax.xml.ws.WebServiceException;
import java.net.URL;

/**
 * Retrieves the provisioning service adapter according to the concrete
 * technical product WSDL.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class ProvisioningServiceAdapterFactory {

    private final static Log4jLogger logger = LoggerFactory
            .getLogger(ProvisioningServiceAdapterFactory.class);

    public static ProvisioningServiceAdapter getProvisioningServiceAdapter(
            TechnicalProduct techProduct, Integer wsTimeout)
            throws TechnicalServiceNotAliveException {
        ProvisioningServiceAdapter adapter = null;
        try {
            String username = techProduct.getProvisioningUsername();
            String password = techProduct.getProvisioningPassword();
            WSPortConnector portConnector = new WSPortConnector(
                    techProduct.getProvisioningURL(), username, password);

            Class<?> serviceClass = SupportedProvisioningVersions.VERSION_1_8.getServiceClass();
            adapter = new ProvisioningServiceAdapterV1_8();
            URL localWsdlURL = adapter.getLocalWSDL();

            try {
                final Object port = portConnector.getPort(localWsdlURL,
                        serviceClass, wsTimeout);
                adapter.setProvisioningService(port);
            } catch (WebServiceException e) {
                TechnicalServiceNotAliveException tse = new TechnicalServiceNotAliveException(
                        TechnicalServiceNotAliveException.Reason.ENDPOINT, e);
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        tse,
                        LogMessageIdentifier.WARN_EX_TECHNICAL_SERVICE_NOT_ALIVE_EXCEPTION_ENDPOINT);
                throw tse;
            }

        } catch (TechnicalServiceNotAliveException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_TECH_SERVICE_NOT_AVAILABLE,
                    techProduct.getTechnicalProductId());
            throw e;
        } catch (WSDLException e) {
            TechnicalServiceNotAliveException ex = new TechnicalServiceNotAliveException(
                    TechnicalServiceNotAliveException.Reason.CONNECTION_REFUSED,
                    e.getCause());
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    e,
                    LogMessageIdentifier.WARN_TECH_SERVICE_NOT_ALIVE_CONNECTION_REFUSED);
            throw ex;
        } catch (Exception e) {
            TechnicalServiceNotAliveException ex = new TechnicalServiceNotAliveException(
                    TechnicalServiceNotAliveException.Reason.CONNECTION_REFUSED,
                    e);
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    e,
                    LogMessageIdentifier.WARN_TECH_SERVICE_NOT_ALIVE_CONNECTION_REFUSED);
            throw ex;
        }

        return adapter;
    }

}
