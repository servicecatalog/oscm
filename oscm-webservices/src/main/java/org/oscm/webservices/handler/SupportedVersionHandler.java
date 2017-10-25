/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.webservices.handler;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;

import org.oscm.apiversioning.soapmgmt.parser.SoapRequestParser;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.string.Strings;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * SOAP handler to check if the version value in the incoming SOAP message is
 * supported by the server.
 * 
 * @author stavreva
 * 
 */
public class SupportedVersionHandler
        implements SOAPHandler<SOAPMessageContext> {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(SupportedVersionHandler.class);

    @Override
    public boolean handleMessage(SOAPMessageContext context) {

        Boolean outbound = (Boolean) context
                .get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if (!outbound.booleanValue()) {
            String version = null;
            try {
                version = SoapRequestParser.parseApiVersion(context);
            } catch (SOAPException e) {
                logger.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR_SOAP_GET_VERSION_FROM_HEADER);
            }

            if (isSupportedVersion(version)) {
                context.put("version", version);
                return true;
            } else {
                SOAPFault versionFault = null;
                try {
                    SOAPMessage message = context.getMessage();
                    versionFault = message.getSOAPBody().addFault();
                    versionFault.setFaultString("The API version " + version
                            + " specified in the header of the SOAP message in not supported by the server. Supported versions: "
                            + SupportedVersions.getSupportedVersionsAsString());
                    throw new SOAPFaultException(versionFault);
                } catch (SOAPException e) {
                    logger.logError(Log4jLogger.SYSTEM_LOG, e,
                            LogMessageIdentifier.ERROR_SOAP_ADD_VERSION_FAULT);
                }

            }
        }

        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    @Override
    public void close(MessageContext context) {
    }

    boolean isSupportedVersion(String version) {
        if (Strings.isEmpty(version) || SupportedVersions.contains(version)) {
            return true;
        }
        return false;
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }
}
