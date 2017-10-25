/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 10.10.2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.paymentservice.bean;

import java.io.IOException;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import org.oscm.dataservice.local.DataService;
import org.oscm.paymentservice.adapter.PaymentServiceProviderAdapter;
import org.oscm.paymentservice.adapter.PaymentServiceProviderAdapterImpl;
import org.oscm.paymentservice.data.SupportedVersions;
import org.oscm.paymentservice.local.PortLocatorLocal;
import org.oscm.ws.WSPortConnector;
import org.oscm.ws.WSPortDescription;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * @author afschar
 * 
 */
@Stateless
@Local(PortLocatorLocal.class)
public class PortLocatorBean implements PortLocatorLocal {

    Integer timeout = new Integer(10000);

    @EJB
    protected DataService ds;

    @Override
    public PaymentServiceProviderAdapter getPort(String wsdl)
            throws IOException, WSDLException, ParserConfigurationException {

        WSPortConnector portConnector = new WSPortConnector(wsdl, null, null);

        SupportedVersions supportedNS = getSupportedVersion(portConnector);
        PaymentServiceProviderAdapter adapter = getAdapterForNamespace(supportedNS);

        final Object port = portConnector.getPort(supportedNS.getLocalWSDL(),
                supportedNS.getServiceClass(), timeout);
        adapter.setPaymentServiceProviderService(port);

        return adapter;
    }

    SupportedVersions getSupportedVersion(WSPortConnector portConnector) {
        WSPortDescription portDescription = portConnector.getPortDescription();
        String targetNamespaceFromWsdl = portDescription.getTargetNamespace();
        SupportedVersions supportedNS = SupportedVersions
                .getForVersion(portDescription.getVersion());
        if (supportedNS == null) {
            supportedNS = SupportedVersions
                    .getForNamespaceString(targetNamespaceFromWsdl);
        }
        if (supportedNS == null) {
            String message = "Unsupported namespace for PaymentServiceProvider: %s";
            throw new SaaSSystemException(String.format(message,
                    targetNamespaceFromWsdl));
        }
        return supportedNS;
    }

    PaymentServiceProviderAdapter getAdapterForNamespace(
            SupportedVersions supportedNS) {
        switch (supportedNS) {
        case CURRENT_VERSION:
            return new PaymentServiceProviderAdapterImpl();
        default:
            return new PaymentServiceProviderAdapterImpl();
        }
    }

}
