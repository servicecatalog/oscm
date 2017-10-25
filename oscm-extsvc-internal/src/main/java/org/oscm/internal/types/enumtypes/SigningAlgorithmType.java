/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *******************************************************************************/
package org.oscm.internal.types.enumtypes;

/**
 * Contains supported hashing algorithms used in SAML communication
 *
 * Created by PLGrubskiM on 2017-07-17.
 */
public enum SigningAlgorithmType {

    SHA1("http://www.w3.org/2000/09/xmldsig#rsa-sha1", "SHA1withRSA"),

    SHA256("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", "SHA256withRSA");

    private final String uri;
    private final String algorithm;

    SigningAlgorithmType(String uri, String algorithm) {
        this.uri = uri;
        this.algorithm = algorithm;
    }

    public String getUri() {
        return uri;
    }

    public String getAlgorithm() {
        return algorithm;
    }
}
