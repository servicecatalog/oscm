/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 28.06.16 08:10
 *
 ******************************************************************************/

package org.oscm.saml2.api;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

import org.oscm.internal.types.exception.SaaSApplicationException;

/**
 * Created by PLGrubskiM on 2016-06-23.
 *
 * Java util class for retrieving keys from the keystore used for signing SAML messages.
 */
public class SamlKeyLoader {
    /**
     * Used for retrieving the private key stored in file outside of keystore.
     * Requires key to be in DER pkcs8 RSA encoded format.
     *
     *
     * @param path - path to the key file.
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static PrivateKey loadPrivateKey(String path)
            throws GeneralSecurityException, IOException {
        byte[] clear = loadPrivateKeyFromFile(path);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clear);
        KeyFactory fact = KeyFactory.getInstance("RSA");
        PrivateKey priv = fact.generatePrivate(keySpec);
        Arrays.fill(clear, (byte) 0);
        return priv;
    }

    private static byte[] loadPrivateKeyFromFile(String privateKeyPath)
            throws IOException {
        BufferedInputStream in = new BufferedInputStream(
                new FileInputStream(privateKeyPath));
        return load(in);
    }

    private static byte[] load(InputStream inputStream) throws IOException {
        try {
            byte[] bytes = new byte[inputStream.available()];
            int len = inputStream.read(bytes);
            if (len > -1) {
                return bytes;
            }
            return new byte[0];
        } finally {
            inputStream.close();
        }
    }

    /**
     * Used for retrieving the private key stored the keystore (.jks) file.
     * Requires key to be in DER pkcs8 RSA encoded format.
     *
     * @param keystorePath - path to the keystore file.
     * @param password - password to the keystore.
     * @param alias - alias of the keypair stored in the keystore.
     * @return
     * @throws SaaSApplicationException
     * @throws IOException
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    public static PrivateKey loadPrivateKeyFromStore(String keystorePath, String password,
                                              String alias) throws SaaSApplicationException, IOException,
                    KeyStoreException, CertificateException,
                    NoSuchAlgorithmException, UnrecoverableKeyException {
        FileInputStream is = new FileInputStream(keystorePath);
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        char[] passwd = password.toCharArray();
        keystore.load(is, passwd);
        Key key = keystore.getKey(alias, passwd);
        if (key instanceof PrivateKey) {
            return (PrivateKey) key;
        }
        throw new SaaSApplicationException(
                "Private key with alias " + alias + " cannot be found");
    }

}
