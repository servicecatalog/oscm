/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 29.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * @author kulle
 * 
 */
public class Keystores {

    public static final String KEYSTORE_TYPE_JKS = "JKS";

    public static KeyStore initializeKeyStore(String keystoreFile,
            String password) throws KeyStoreException,
            NoSuchAlgorithmException, CertificateException,
            FileNotFoundException, IOException {

        KeyStore keystore = KeyStore.getInstance(KEYSTORE_TYPE_JKS);
        FileInputStream in = null;
        try {
            in = new FileInputStream(new File(keystoreFile));
            keystore.load(in, password.toCharArray());
        } finally {
            if (in != null)
                in.close();
        }
        return keystore;
    }

}
