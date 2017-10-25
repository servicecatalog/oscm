/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2015年3月11日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ws;

import javax.wsdl.extensions.ExtensionRegistry;

import com.ibm.wsdl.extensions.http.HTTPAddressSerializer;
import com.ibm.wsdl.extensions.http.HTTPBindingSerializer;
import com.ibm.wsdl.extensions.http.HTTPConstants;
import com.ibm.wsdl.extensions.http.HTTPOperationSerializer;
import com.ibm.wsdl.extensions.http.HTTPUrlEncodedSerializer;
import com.ibm.wsdl.extensions.http.HTTPUrlReplacementSerializer;
import com.ibm.wsdl.extensions.mime.MIMEConstants;
import com.ibm.wsdl.extensions.mime.MIMEContentSerializer;
import com.ibm.wsdl.extensions.mime.MIMEMimeXmlSerializer;
import com.ibm.wsdl.extensions.mime.MIMEMultipartRelatedSerializer;
import com.ibm.wsdl.extensions.soap.SOAPAddressSerializer;
import com.ibm.wsdl.extensions.soap.SOAPBindingSerializer;
import com.ibm.wsdl.extensions.soap.SOAPBodySerializer;
import com.ibm.wsdl.extensions.soap.SOAPConstants;
import com.ibm.wsdl.extensions.soap.SOAPFaultSerializer;
import com.ibm.wsdl.extensions.soap.SOAPHeaderSerializer;
import com.ibm.wsdl.extensions.soap.SOAPOperationSerializer;
import com.ibm.wsdl.extensions.soap12.SOAP12AddressSerializer;
import com.ibm.wsdl.extensions.soap12.SOAP12BindingSerializer;
import com.ibm.wsdl.extensions.soap12.SOAP12BodySerializer;
import com.ibm.wsdl.extensions.soap12.SOAP12Constants;
import com.ibm.wsdl.extensions.soap12.SOAP12FaultSerializer;
import com.ibm.wsdl.extensions.soap12.SOAP12HeaderSerializer;
import com.ibm.wsdl.extensions.soap12.SOAP12OperationSerializer;

/**
 * @author qiu
 * 
 */
public class WSVersionExtensionRegistry extends ExtensionRegistry {

    private static final long serialVersionUID = 9214787195054630324L;

    public WSVersionExtensionRegistry() {
        SOAPAddressSerializer soapaddressserializer = new SOAPAddressSerializer();
        registerSerializer(javax.wsdl.Port.class,
                SOAPConstants.Q_ELEM_SOAP_ADDRESS, soapaddressserializer);
        registerDeserializer(javax.wsdl.Port.class,
                SOAPConstants.Q_ELEM_SOAP_ADDRESS, soapaddressserializer);
        mapExtensionTypes(javax.wsdl.Port.class,
                SOAPConstants.Q_ELEM_SOAP_ADDRESS,
                com.ibm.wsdl.extensions.soap.SOAPAddressImpl.class);
        SOAPBindingSerializer soapbindingserializer = new SOAPBindingSerializer();
        registerSerializer(javax.wsdl.Binding.class,
                SOAPConstants.Q_ELEM_SOAP_BINDING, soapbindingserializer);
        registerDeserializer(javax.wsdl.Binding.class,
                SOAPConstants.Q_ELEM_SOAP_BINDING, soapbindingserializer);
        mapExtensionTypes(javax.wsdl.Binding.class,
                SOAPConstants.Q_ELEM_SOAP_BINDING,
                com.ibm.wsdl.extensions.soap.SOAPBindingImpl.class);
        SOAPHeaderSerializer soapheaderserializer = new SOAPHeaderSerializer();
        registerSerializer(javax.wsdl.BindingInput.class,
                SOAPConstants.Q_ELEM_SOAP_HEADER, soapheaderserializer);
        registerDeserializer(javax.wsdl.BindingInput.class,
                SOAPConstants.Q_ELEM_SOAP_HEADER, soapheaderserializer);
        mapExtensionTypes(javax.wsdl.BindingInput.class,
                SOAPConstants.Q_ELEM_SOAP_HEADER,
                com.ibm.wsdl.extensions.soap.SOAPHeaderImpl.class);
        registerSerializer(javax.wsdl.BindingOutput.class,
                SOAPConstants.Q_ELEM_SOAP_HEADER, soapheaderserializer);
        registerDeserializer(javax.wsdl.BindingOutput.class,
                SOAPConstants.Q_ELEM_SOAP_HEADER, soapheaderserializer);
        mapExtensionTypes(javax.wsdl.BindingOutput.class,
                SOAPConstants.Q_ELEM_SOAP_HEADER,
                com.ibm.wsdl.extensions.soap.SOAPHeaderImpl.class);
        mapExtensionTypes(javax.wsdl.extensions.soap.SOAPHeader.class,
                SOAPConstants.Q_ELEM_SOAP_HEADER_FAULT,
                com.ibm.wsdl.extensions.soap.SOAPHeaderFaultImpl.class);
        SOAPBodySerializer soapbodyserializer = new SOAPBodySerializer();
        registerSerializer(javax.wsdl.BindingInput.class,
                SOAPConstants.Q_ELEM_SOAP_BODY, soapbodyserializer);
        registerDeserializer(javax.wsdl.BindingInput.class,
                SOAPConstants.Q_ELEM_SOAP_BODY, soapbodyserializer);
        mapExtensionTypes(javax.wsdl.BindingInput.class,
                SOAPConstants.Q_ELEM_SOAP_BODY,
                com.ibm.wsdl.extensions.soap.SOAPBodyImpl.class);
        registerSerializer(javax.wsdl.BindingOutput.class,
                SOAPConstants.Q_ELEM_SOAP_BODY, soapbodyserializer);
        registerDeserializer(javax.wsdl.BindingOutput.class,
                SOAPConstants.Q_ELEM_SOAP_BODY, soapbodyserializer);
        mapExtensionTypes(javax.wsdl.BindingOutput.class,
                SOAPConstants.Q_ELEM_SOAP_BODY,
                com.ibm.wsdl.extensions.soap.SOAPBodyImpl.class);
        registerSerializer(javax.wsdl.extensions.mime.MIMEPart.class,
                SOAPConstants.Q_ELEM_SOAP_BODY, soapbodyserializer);
        registerDeserializer(javax.wsdl.extensions.mime.MIMEPart.class,
                SOAPConstants.Q_ELEM_SOAP_BODY, soapbodyserializer);
        mapExtensionTypes(javax.wsdl.extensions.mime.MIMEPart.class,
                SOAPConstants.Q_ELEM_SOAP_BODY,
                com.ibm.wsdl.extensions.soap.SOAPBodyImpl.class);
        SOAPFaultSerializer soapfaultserializer = new SOAPFaultSerializer();
        registerSerializer(javax.wsdl.BindingFault.class,
                SOAPConstants.Q_ELEM_SOAP_FAULT, soapfaultserializer);
        registerDeserializer(javax.wsdl.BindingFault.class,
                SOAPConstants.Q_ELEM_SOAP_FAULT, soapfaultserializer);
        mapExtensionTypes(javax.wsdl.BindingFault.class,
                SOAPConstants.Q_ELEM_SOAP_FAULT,
                com.ibm.wsdl.extensions.soap.SOAPFaultImpl.class);
        SOAPOperationSerializer soapoperationserializer = new SOAPOperationSerializer();
        registerSerializer(javax.wsdl.BindingOperation.class,
                SOAPConstants.Q_ELEM_SOAP_OPERATION, soapoperationserializer);
        registerDeserializer(javax.wsdl.BindingOperation.class,
                SOAPConstants.Q_ELEM_SOAP_OPERATION, soapoperationserializer);
        mapExtensionTypes(javax.wsdl.BindingOperation.class,
                SOAPConstants.Q_ELEM_SOAP_OPERATION,
                com.ibm.wsdl.extensions.soap.SOAPOperationImpl.class);
        SOAP12AddressSerializer soap12addressserializer = new SOAP12AddressSerializer();
        registerSerializer(javax.wsdl.Port.class,
                SOAP12Constants.Q_ELEM_SOAP_ADDRESS, soap12addressserializer);
        registerDeserializer(javax.wsdl.Port.class,
                SOAP12Constants.Q_ELEM_SOAP_ADDRESS, soap12addressserializer);
        mapExtensionTypes(javax.wsdl.Port.class,
                SOAP12Constants.Q_ELEM_SOAP_ADDRESS,
                com.ibm.wsdl.extensions.soap12.SOAP12AddressImpl.class);
        SOAP12BindingSerializer soap12bindingserializer = new SOAP12BindingSerializer();
        registerSerializer(javax.wsdl.Binding.class,
                SOAP12Constants.Q_ELEM_SOAP_BINDING, soap12bindingserializer);
        registerDeserializer(javax.wsdl.Binding.class,
                SOAP12Constants.Q_ELEM_SOAP_BINDING, soap12bindingserializer);
        mapExtensionTypes(javax.wsdl.Binding.class,
                SOAP12Constants.Q_ELEM_SOAP_BINDING,
                com.ibm.wsdl.extensions.soap12.SOAP12BindingImpl.class);
        SOAP12HeaderSerializer soap12headerserializer = new SOAP12HeaderSerializer();
        registerSerializer(javax.wsdl.BindingInput.class,
                SOAP12Constants.Q_ELEM_SOAP_HEADER, soap12headerserializer);
        registerDeserializer(javax.wsdl.BindingInput.class,
                SOAP12Constants.Q_ELEM_SOAP_HEADER, soap12headerserializer);
        mapExtensionTypes(javax.wsdl.BindingInput.class,
                SOAP12Constants.Q_ELEM_SOAP_HEADER,
                com.ibm.wsdl.extensions.soap12.SOAP12HeaderImpl.class);
        registerSerializer(javax.wsdl.BindingOutput.class,
                SOAP12Constants.Q_ELEM_SOAP_HEADER, soap12headerserializer);
        registerDeserializer(javax.wsdl.BindingOutput.class,
                SOAP12Constants.Q_ELEM_SOAP_HEADER, soap12headerserializer);
        mapExtensionTypes(javax.wsdl.BindingOutput.class,
                SOAP12Constants.Q_ELEM_SOAP_HEADER,
                com.ibm.wsdl.extensions.soap12.SOAP12HeaderImpl.class);
        mapExtensionTypes(javax.wsdl.extensions.soap12.SOAP12Header.class,
                SOAP12Constants.Q_ELEM_SOAP_HEADER_FAULT,
                com.ibm.wsdl.extensions.soap12.SOAP12HeaderFaultImpl.class);
        SOAP12BodySerializer soap12bodyserializer = new SOAP12BodySerializer();
        registerSerializer(javax.wsdl.BindingInput.class,
                SOAP12Constants.Q_ELEM_SOAP_BODY, soap12bodyserializer);
        registerDeserializer(javax.wsdl.BindingInput.class,
                SOAP12Constants.Q_ELEM_SOAP_BODY, soap12bodyserializer);
        mapExtensionTypes(javax.wsdl.BindingInput.class,
                SOAP12Constants.Q_ELEM_SOAP_BODY,
                com.ibm.wsdl.extensions.soap12.SOAP12BodyImpl.class);
        registerSerializer(javax.wsdl.BindingOutput.class,
                SOAP12Constants.Q_ELEM_SOAP_BODY, soap12bodyserializer);
        registerDeserializer(javax.wsdl.BindingOutput.class,
                SOAP12Constants.Q_ELEM_SOAP_BODY, soap12bodyserializer);
        mapExtensionTypes(javax.wsdl.BindingOutput.class,
                SOAP12Constants.Q_ELEM_SOAP_BODY,
                com.ibm.wsdl.extensions.soap12.SOAP12BodyImpl.class);
        registerSerializer(javax.wsdl.extensions.mime.MIMEPart.class,
                SOAP12Constants.Q_ELEM_SOAP_BODY, soap12bodyserializer);
        registerDeserializer(javax.wsdl.extensions.mime.MIMEPart.class,
                SOAP12Constants.Q_ELEM_SOAP_BODY, soap12bodyserializer);
        mapExtensionTypes(javax.wsdl.extensions.mime.MIMEPart.class,
                SOAP12Constants.Q_ELEM_SOAP_BODY,
                com.ibm.wsdl.extensions.soap12.SOAP12BodyImpl.class);
        SOAP12FaultSerializer soap12faultserializer = new SOAP12FaultSerializer();
        registerSerializer(javax.wsdl.BindingFault.class,
                SOAP12Constants.Q_ELEM_SOAP_FAULT, soap12faultserializer);
        registerDeserializer(javax.wsdl.BindingFault.class,
                SOAP12Constants.Q_ELEM_SOAP_FAULT, soap12faultserializer);
        mapExtensionTypes(javax.wsdl.BindingFault.class,
                SOAP12Constants.Q_ELEM_SOAP_FAULT,
                com.ibm.wsdl.extensions.soap12.SOAP12FaultImpl.class);
        SOAP12OperationSerializer soap12operationserializer = new SOAP12OperationSerializer();
        registerSerializer(javax.wsdl.BindingOperation.class,
                SOAP12Constants.Q_ELEM_SOAP_OPERATION,
                soap12operationserializer);
        registerDeserializer(javax.wsdl.BindingOperation.class,
                SOAP12Constants.Q_ELEM_SOAP_OPERATION,
                soap12operationserializer);
        mapExtensionTypes(javax.wsdl.BindingOperation.class,
                SOAP12Constants.Q_ELEM_SOAP_OPERATION,
                com.ibm.wsdl.extensions.soap12.SOAP12OperationImpl.class);
        HTTPAddressSerializer httpaddressserializer = new HTTPAddressSerializer();
        registerSerializer(javax.wsdl.Port.class,
                HTTPConstants.Q_ELEM_HTTP_ADDRESS, httpaddressserializer);
        registerDeserializer(javax.wsdl.Port.class,
                HTTPConstants.Q_ELEM_HTTP_ADDRESS, httpaddressserializer);
        mapExtensionTypes(javax.wsdl.Port.class,
                HTTPConstants.Q_ELEM_HTTP_ADDRESS,
                com.ibm.wsdl.extensions.http.HTTPAddressImpl.class);
        HTTPOperationSerializer httpoperationserializer = new HTTPOperationSerializer();
        registerSerializer(javax.wsdl.BindingOperation.class,
                HTTPConstants.Q_ELEM_HTTP_OPERATION, httpoperationserializer);
        registerDeserializer(javax.wsdl.BindingOperation.class,
                HTTPConstants.Q_ELEM_HTTP_OPERATION, httpoperationserializer);
        mapExtensionTypes(javax.wsdl.BindingOperation.class,
                HTTPConstants.Q_ELEM_HTTP_OPERATION,
                com.ibm.wsdl.extensions.http.HTTPOperationImpl.class);
        HTTPBindingSerializer httpbindingserializer = new HTTPBindingSerializer();
        registerSerializer(javax.wsdl.Binding.class,
                HTTPConstants.Q_ELEM_HTTP_BINDING, httpbindingserializer);
        registerDeserializer(javax.wsdl.Binding.class,
                HTTPConstants.Q_ELEM_HTTP_BINDING, httpbindingserializer);
        mapExtensionTypes(javax.wsdl.Binding.class,
                HTTPConstants.Q_ELEM_HTTP_BINDING,
                com.ibm.wsdl.extensions.http.HTTPBindingImpl.class);
        HTTPUrlEncodedSerializer httpurlencodedserializer = new HTTPUrlEncodedSerializer();
        registerSerializer(javax.wsdl.BindingInput.class,
                HTTPConstants.Q_ELEM_HTTP_URL_ENCODED, httpurlencodedserializer);
        registerDeserializer(javax.wsdl.BindingInput.class,
                HTTPConstants.Q_ELEM_HTTP_URL_ENCODED, httpurlencodedserializer);
        mapExtensionTypes(javax.wsdl.BindingInput.class,
                HTTPConstants.Q_ELEM_HTTP_URL_ENCODED,
                com.ibm.wsdl.extensions.http.HTTPUrlEncodedImpl.class);
        HTTPUrlReplacementSerializer httpurlreplacementserializer = new HTTPUrlReplacementSerializer();
        registerSerializer(javax.wsdl.BindingInput.class,
                HTTPConstants.Q_ELEM_HTTP_URL_REPLACEMENT,
                httpurlreplacementserializer);
        registerDeserializer(javax.wsdl.BindingInput.class,
                HTTPConstants.Q_ELEM_HTTP_URL_REPLACEMENT,
                httpurlreplacementserializer);
        mapExtensionTypes(javax.wsdl.BindingInput.class,
                HTTPConstants.Q_ELEM_HTTP_URL_REPLACEMENT,
                com.ibm.wsdl.extensions.http.HTTPUrlReplacementImpl.class);
        MIMEContentSerializer mimecontentserializer = new MIMEContentSerializer();
        registerSerializer(javax.wsdl.BindingInput.class,
                MIMEConstants.Q_ELEM_MIME_CONTENT, mimecontentserializer);
        registerDeserializer(javax.wsdl.BindingInput.class,
                MIMEConstants.Q_ELEM_MIME_CONTENT, mimecontentserializer);
        mapExtensionTypes(javax.wsdl.BindingInput.class,
                MIMEConstants.Q_ELEM_MIME_CONTENT,
                com.ibm.wsdl.extensions.mime.MIMEContentImpl.class);
        registerSerializer(javax.wsdl.BindingOutput.class,
                MIMEConstants.Q_ELEM_MIME_CONTENT, mimecontentserializer);
        registerDeserializer(javax.wsdl.BindingOutput.class,
                MIMEConstants.Q_ELEM_MIME_CONTENT, mimecontentserializer);
        mapExtensionTypes(javax.wsdl.BindingOutput.class,
                MIMEConstants.Q_ELEM_MIME_CONTENT,
                com.ibm.wsdl.extensions.mime.MIMEContentImpl.class);
        registerSerializer(javax.wsdl.extensions.mime.MIMEPart.class,
                MIMEConstants.Q_ELEM_MIME_CONTENT, mimecontentserializer);
        registerDeserializer(javax.wsdl.extensions.mime.MIMEPart.class,
                MIMEConstants.Q_ELEM_MIME_CONTENT, mimecontentserializer);
        mapExtensionTypes(javax.wsdl.extensions.mime.MIMEPart.class,
                MIMEConstants.Q_ELEM_MIME_CONTENT,
                com.ibm.wsdl.extensions.mime.MIMEContentImpl.class);
        MIMEMultipartRelatedSerializer mimemultipartrelatedserializer = new MIMEMultipartRelatedSerializer();
        registerSerializer(javax.wsdl.BindingInput.class,
                MIMEConstants.Q_ELEM_MIME_MULTIPART_RELATED,
                mimemultipartrelatedserializer);
        registerDeserializer(javax.wsdl.BindingInput.class,
                MIMEConstants.Q_ELEM_MIME_MULTIPART_RELATED,
                mimemultipartrelatedserializer);
        mapExtensionTypes(javax.wsdl.BindingInput.class,
                MIMEConstants.Q_ELEM_MIME_MULTIPART_RELATED,
                com.ibm.wsdl.extensions.mime.MIMEMultipartRelatedImpl.class);
        registerSerializer(javax.wsdl.BindingOutput.class,
                MIMEConstants.Q_ELEM_MIME_MULTIPART_RELATED,
                mimemultipartrelatedserializer);
        registerDeserializer(javax.wsdl.BindingOutput.class,
                MIMEConstants.Q_ELEM_MIME_MULTIPART_RELATED,
                mimemultipartrelatedserializer);
        mapExtensionTypes(javax.wsdl.BindingOutput.class,
                MIMEConstants.Q_ELEM_MIME_MULTIPART_RELATED,
                com.ibm.wsdl.extensions.mime.MIMEMultipartRelatedImpl.class);
        registerSerializer(javax.wsdl.extensions.mime.MIMEPart.class,
                MIMEConstants.Q_ELEM_MIME_MULTIPART_RELATED,
                mimemultipartrelatedserializer);
        registerDeserializer(javax.wsdl.extensions.mime.MIMEPart.class,
                MIMEConstants.Q_ELEM_MIME_MULTIPART_RELATED,
                mimemultipartrelatedserializer);
        mapExtensionTypes(javax.wsdl.extensions.mime.MIMEPart.class,
                MIMEConstants.Q_ELEM_MIME_MULTIPART_RELATED,
                com.ibm.wsdl.extensions.mime.MIMEMultipartRelatedImpl.class);
        mapExtensionTypes(
                javax.wsdl.extensions.mime.MIMEMultipartRelated.class,
                MIMEConstants.Q_ELEM_MIME_PART,
                com.ibm.wsdl.extensions.mime.MIMEPartImpl.class);
        MIMEMimeXmlSerializer mimemimexmlserializer = new MIMEMimeXmlSerializer();
        registerSerializer(javax.wsdl.BindingInput.class,
                MIMEConstants.Q_ELEM_MIME_MIME_XML, mimemimexmlserializer);
        registerDeserializer(javax.wsdl.BindingInput.class,
                MIMEConstants.Q_ELEM_MIME_MIME_XML, mimemimexmlserializer);
        mapExtensionTypes(javax.wsdl.BindingInput.class,
                MIMEConstants.Q_ELEM_MIME_MIME_XML,
                com.ibm.wsdl.extensions.mime.MIMEMimeXmlImpl.class);
        registerSerializer(javax.wsdl.BindingOutput.class,
                MIMEConstants.Q_ELEM_MIME_MIME_XML, mimemimexmlserializer);
        registerDeserializer(javax.wsdl.BindingOutput.class,
                MIMEConstants.Q_ELEM_MIME_MIME_XML, mimemimexmlserializer);
        mapExtensionTypes(javax.wsdl.BindingOutput.class,
                MIMEConstants.Q_ELEM_MIME_MIME_XML,
                com.ibm.wsdl.extensions.mime.MIMEMimeXmlImpl.class);
        registerSerializer(javax.wsdl.extensions.mime.MIMEPart.class,
                MIMEConstants.Q_ELEM_MIME_MIME_XML, mimemimexmlserializer);
        registerDeserializer(javax.wsdl.extensions.mime.MIMEPart.class,
                MIMEConstants.Q_ELEM_MIME_MIME_XML, mimemimexmlserializer);
        mapExtensionTypes(javax.wsdl.extensions.mime.MIMEPart.class,
                MIMEConstants.Q_ELEM_MIME_MIME_XML,
                com.ibm.wsdl.extensions.mime.MIMEMimeXmlImpl.class);

    }

}
