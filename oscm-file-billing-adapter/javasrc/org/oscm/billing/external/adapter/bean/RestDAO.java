/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  Creation Date: 03.08.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.adapter.bean;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.oscm.billing.external.exception.BillingException;

/**
 * Manages REST communication with the file billing application.
 */
public class RestDAO {

    /**
     * Create a web resource for the given URL
     */
    public WebTarget createWebResource(String serviceURL) {

        Client client = ClientBuilder.newClient();
        WebTarget webResource = client.target(serviceURL);
        return webResource;
    }

    /**
     * Create a web resource with the given parameters for the given URL
     */
    public WebTarget createWebResource(String serviceURL,
            Map<String, String> queryParams) {
        Client client = ClientBuilder.newClient();
        WebTarget webResource = client.target(serviceURL);
        for (String key : queryParams.keySet()) {
            webResource = webResource.queryParam(key, queryParams.get(key));
        }
        return webResource;
    }

    /**
     * Create a web resource with the given multi-valued parameters for the
     * given URL
     */
    public WebTarget createMultiValueWebResource(String serviceURL,
            QueryParamMultiValuedMap queryParams) {
        Client client = ClientBuilder.newClient();
        WebTarget webResource = client.target(serviceURL);
        for (Map.Entry entry : queryParams.getMap().entrySet()) {
            webResource = webResource.queryParam(entry.getKey().toString(), entry.getValue());
        }
        return webResource;
    }

    /**
     * Send a GET request and get the plain text in the response
     * 
     * @param webResource
     *            a web resource
     * @return the text from the response
     * @throws BillingException
     *             if the GET request fails
     */
    public String getTextResponse(WebTarget webResource)
            throws BillingException {

        Invocation invocation = webResource
                .request(MediaType.TEXT_PLAIN_TYPE).buildGet();
        try {
            return invocation.invoke(String.class);
        } catch (RuntimeException e) {
            throw new BillingException(
                    "Error when processing response from File Billing Application",
                    e);
        }
    }

    /**
     * Send a GET request and get the file content, which is contained in the
     * response as octet stream
     * 
     * @param webResource
     *            a web resource
     * @return the file content
     * @throws BillingException
     *             if the GET request fails
     */
    public byte[] getFileResponse(WebTarget webResource)
            throws BillingException {

        Invocation invocation = webResource
            .request(MediaType.APPLICATION_OCTET_STREAM).buildGet();

        try {
            InputStream is = invocation.invoke(InputStream.class);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            int next = is.read();
            while (next > -1) {
                bos.write(next);
                next = is.read();
            }
            bos.flush();

            byte[] result = bos.toByteArray();
            if (result.length > 0) {
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new BillingException(
                    "Error when processing file response from File Billing Application",
                    e);
        }
    }

    /**
     * Send a GET request and decode the price model data, which are contained
     * as XML String in the GET response
     * 
     * @param webResource
     *            a web resource
     * @return the price model data
     * @throws BillingException
     *             if the GET request fails
     */
    @SuppressWarnings("unchecked")
    public List<String> getPriceModelData(WebTarget webResource)
            throws BillingException {

        String xml = getTextResponse(webResource);
        return (List<String>) decodeXml(xml);
    }

    /**
     * Decode a Java object, which is contained in the given XML
     * 
     * @param xml
     *            a XML string
     * @return a Java object
     * @throws BillingException
     *             if the XML decoding fails
     */
    Object decodeXml(String xml) throws BillingException {

        if (xml != null) {
            try (XMLDecoder xmlDecoder = new XMLDecoder(
                    new ByteArrayInputStream(
                            xml.getBytes(StandardCharsets.UTF_8)))) {
                return xmlDecoder.readObject();
            } catch (Exception e) {
                throw new BillingException(
                        "File application response decoding error.");
            }
        } else {
            return null;
        }
    }

}
