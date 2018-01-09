/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 15.12.2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ws.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

import org.oscm.test.setup.PropertiesReader;

/**
 * Creates new signed client certificates
 * 
 * @author kulle
 */
public class CertificateHandler {

    private static final String KEYSTORE_TYPE_JKS = "JKS";
    private static final String ALGORITHM_RSA = "RSA";
    private static final String ALGORITHM_SHA1_RSA = "SHA1withRSA";
    private static final String ALGORITHM_SHA256_RSA = "SHA256WithRSAEncryption";

    private static final String FILE_EXTENSION = ".jks";
    private static final String FILE_PREFIX = "keystore-";
    private String certificatesPath; // where certs should be temporarily stored

    private KeyStore rootCaKeystore; // keystore where the rootca is located
    private String rootCaPassword;
    private String rootCaAlias;

    private PrivateKeyEntry rootPrivateKeyEntry;
    private X509Certificate rootCert;

    private static final List<String> createdCerts = new ArrayList<String>();

    public CertificateHandler() throws Exception {
        // read properties from configsettings.properties
        PropertiesReader reader = new PropertiesReader();
        Properties props = reader.load();
        rootCaAlias = props.getProperty("rootca.alias");
        rootCaPassword = props.getProperty("rootca.keystore.password");
        certificatesPath = props.getProperty("certificates.path");

        // add bouncycastle as security provider
        Security.addProvider(new BouncyCastleProvider());

        // load keystore containing the bes root certificate
        initKeyStore(props.getProperty("rootca.keystore"));

        // load the private key of the bes root ca
        loadPrivateKeyEntry();

        // load bes root certificate
        loadRootCertificate();
    }

    private KeyStore initKeyStore(String keystoreFile)
            throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, FileNotFoundException, IOException {

        rootCaKeystore = KeyStore.getInstance(KEYSTORE_TYPE_JKS);
        FileInputStream in = null;
        try {
            in = new FileInputStream(new File(keystoreFile));
            rootCaKeystore.load(in, rootCaPassword.toCharArray());
        } finally {
            if (in != null) {
                in.close();
            }
        }

        return rootCaKeystore;
    }

    private void loadPrivateKeyEntry() throws GeneralSecurityException {
        rootPrivateKeyEntry = (PrivateKeyEntry) rootCaKeystore.getEntry(
                rootCaAlias,
                new PasswordProtection(rootCaPassword.toCharArray()));

        if (rootPrivateKeyEntry == null) {
            throw new RuntimeException(
                    "Could not read private key entry from rootca keystore with alias "
                            + rootCaAlias);
        }
    }

    private void loadRootCertificate() {
        rootCert = (X509Certificate) rootPrivateKeyEntry.getCertificate();
        if (rootCert == null) {
            throw new RuntimeException("Could not retrive the bes rootca");
        }
    }

    public void createSignedClientCertificate(String organizationId)
            throws Exception {

        // generate a keypair
        KeyPair keypair = generateKeyPair();

        // create signing request
        PKCS10CertificationRequest csr = createCertificateSigningRequest(
                organizationId, keypair);

        // create signed certificate using csr and bes root certificate
        X509Certificate issuedCert = generateSignedCertificate(csr);

        // save certificate to a new keystore file
        writeCertificateToKeystoreFile(organizationId, issuedCert, keypair);

        createdCerts.add(organizationId);
    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM_RSA);
        keyGen.initialize(1024, new SecureRandom());
        KeyPair keypair = keyGen.generateKeyPair();
        return keypair;
    }

    private PKCS10CertificationRequest createCertificateSigningRequest(
            String organizationId, KeyPair keypair) throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchProviderException,
            SignatureException {
        X500Principal subject = new X500Principal("CN=" + organizationId);
        return new PKCS10CertificationRequest(ALGORITHM_SHA1_RSA, subject,
                keypair.getPublic(), null, keypair.getPrivate());
    }

    private X509Certificate generateSignedCertificate(
            PKCS10CertificationRequest csr) throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidKeyException,
            CertificateParsingException, CertificateEncodingException,
            SignatureException {

        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        certGen.setIssuerDN(rootCert.getSubjectX500Principal());
        Calendar c = Calendar.getInstance();
        certGen.setNotBefore(c.getTime());
        c.add(Calendar.YEAR, 1);
        certGen.setNotAfter(c.getTime());
        certGen.setSubjectDN(csr.getCertificationRequestInfo().getSubject());
        certGen.setPublicKey(csr.getPublicKey("BC"));
        certGen.setSignatureAlgorithm(ALGORITHM_SHA256_RSA);
        certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false,
                new AuthorityKeyIdentifierStructure(rootCert.getPublicKey()));
        certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false,
                new SubjectKeyIdentifierStructure(csr.getPublicKey("BC")));
        certGen.addExtension(X509Extensions.BasicConstraints, true,
                new BasicConstraints(false));
        certGen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(
                KeyUsage.digitalSignature | KeyUsage.keyEncipherment));

        X509Certificate issuedCert = certGen.generate(rootPrivateKeyEntry
                .getPrivateKey());
        return issuedCert;
    }

    private void writeCertificateToKeystoreFile(String organizationId,
            X509Certificate issuedCert, KeyPair keypair)
            throws KeyStoreException, FileNotFoundException, IOException,
            NoSuchAlgorithmException, CertificateException {

        // request a keystore object
        KeyStore keystore = KeyStore.getInstance(KEYSTORE_TYPE_JKS);

        // before the keystore can be accessed, it must be loaded
        keystore.load(null, rootCaPassword.toCharArray());

        // add trusted certificate entry to keystore
        keystore.setCertificateEntry(organizationId, issuedCert);

        // add private key with certificate chain to the keystore
        List<Certificate> chain = new ArrayList<Certificate>();
        chain.add(issuedCert);
        chain.add(rootCert);
        keystore.setKeyEntry(organizationId, keypair.getPrivate(),
                rootCaPassword.toCharArray(),
                chain.toArray(new Certificate[chain.size()]));

        // finally store keystore to disk
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(getFilePath(organizationId));
            keystore.store(fos, rootCaPassword.toCharArray());
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    public void deleteCreatedCerts() {
        for (String organizationId : createdCerts) {
            File toDelete = new File(getFilePath(organizationId));
            toDelete.delete();
        }
    }

    private String getFilePath(String organizationId) {
        return certificatesPath + File.separator + FILE_PREFIX + organizationId
                + FILE_EXTENSION;
    }
}
