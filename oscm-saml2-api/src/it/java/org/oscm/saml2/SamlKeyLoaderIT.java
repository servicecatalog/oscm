/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 28.06.16 08:31
 *
 ******************************************************************************/
package org.oscm.saml2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.saml2.api.SamlKeyLoader;

/**
 * Created by PLGrubskiM on 2016-06-24.
 */
public class SamlKeyLoaderIT {
    private SamlKeyLoader samlKeyLoader;

    private static final String KEYSTORE_PASSWORD = "changeit";
    private static final String ALIAS = "s1as";
    private static final String KEYSTORE_FILE = "keystore.jks";
    private static final String PRIVATE_KEY_FILE = "privateKey";

    @Before
    public void setUp() {
        samlKeyLoader = new SamlKeyLoader();
    }

    @Test
    public void loadPrivateKeyFromStore_OK() throws CertificateException,
            SaaSApplicationException, UnrecoverableKeyException,
            NoSuchAlgorithmException, IOException, KeyStoreException {
        // when
        samlKeyLoader.loadPrivateKeyFromStore(getPath(KEYSTORE_FILE),
                KEYSTORE_PASSWORD, ALIAS);
        // then
    }

    @Test(expected = IOException.class)
    public void loadPrivateKeyFromStore_invalidFile()
            throws CertificateException, SaaSApplicationException,
            UnrecoverableKeyException, NoSuchAlgorithmException, IOException,
            KeyStoreException {
        // given
        String path = "somePath";
        // when
        samlKeyLoader.loadPrivateKeyFromStore(path, KEYSTORE_PASSWORD, ALIAS);
    }

    @Test(expected = IOException.class)
    public void loadPrivateKeyFromStore_invalidPassword()
            throws CertificateException, SaaSApplicationException,
            UnrecoverableKeyException, NoSuchAlgorithmException, IOException,
            KeyStoreException {
        // given
        String password = "invalidPassword";
        // when
        samlKeyLoader.loadPrivateKeyFromStore(getPath(KEYSTORE_FILE), password,
                ALIAS);
        // then
    }

    @Test(expected = SaaSApplicationException.class)
    public void loadPrivateKeyFromStore_invalidAlias()
            throws CertificateException, SaaSApplicationException,
            UnrecoverableKeyException, NoSuchAlgorithmException, IOException,
            KeyStoreException {
        // when
        samlKeyLoader.loadPrivateKeyFromStore(getPath(KEYSTORE_FILE),
                KEYSTORE_PASSWORD, "invalidAlias");
    }

    @Test
    public void loadPrivateKeyFromFile_OK()
            throws GeneralSecurityException, IOException {
        // when
        // load key in DER format from file, no errors expected.
        SamlKeyLoader.loadPrivateKey(getPath(PRIVATE_KEY_FILE));
    }

    @Test(expected = FileNotFoundException.class)
    public void loadPrivateKeyFromFile_invalidPath()
            throws GeneralSecurityException, IOException {
        // when
        SamlKeyLoader.loadPrivateKey("invalidPath");
    }

    @Test(expected = InvalidKeySpecException.class)
    public void loadPrivateKeyFromFile_invalidFormat()
            throws GeneralSecurityException, IOException {
        // when
        // load key in PEM format and expect the error.
        SamlKeyLoader.loadPrivateKey(getPath("pkcs8PEM"));
    }

    public String getPath(String fileName) {
        return getClass().getClassLoader().getResource("").getPath()
                + "/../javares/" + fileName;
    }

}
