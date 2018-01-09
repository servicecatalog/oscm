/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 06.07.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.applicationservice.adapter;

import java.net.URL;

import javax.wsdl.WSDLException;
import javax.xml.ws.WebServiceException;

import org.oscm.applicationservice.data.SupportedProvisioningVersions;
import org.oscm.applicationservice.provisioning.adapter.ProvisioningServiceAdapterV1_0;
import org.oscm.applicationservice.provisioning.adapter.ProvisioningServiceAdapterV1_8;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ws.WSPortConnector;
import org.oscm.ws.WSPortDescription;

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

            WSPortDescription portDescription = portConnector
                    .getPortDescription();

            String targetVersionFromWsdl = portDescription.getVersion();
            SupportedProvisioningVersions supportedVersion = SupportedProvisioningVersions
                    .getForVersionString(targetVersionFromWsdl);
            if (supportedVersion == null) {
                supportedVersion = SupportedProvisioningVersions.VERSION_1_8;
            }
            Class<?> serviceClass = supportedVersion.getServiceClass();
            adapter = getAdapterForVersion(supportedVersion);
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

    /**
     * Initialized a provisioning service adapter for the version matching the
     * the version retrieved from wsdl.
     * 
     * @param supportedVersion
     * @return
     */
    private static ProvisioningServiceAdapter getAdapterForVersion(
            SupportedProvisioningVersions supportedVersion) {
        switch (supportedVersion) {
        case VERSION_1_7:
            return new ProvisioningServiceAdapterV1_0();
        case VERSION_1_8:
            return new ProvisioningServiceAdapterV1_8();

        default:
            return new ProvisioningServiceAdapterV1_8();
        }
    }

}
