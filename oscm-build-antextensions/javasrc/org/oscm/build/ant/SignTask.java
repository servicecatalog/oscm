/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

/**
 * Signs a given certificate signing request (CSR).
 * 
 * @author hoffmann
 */
public class SignTask extends Task {

    private static final long DAY = 24 * 60 * 60 * 1000;

    private File csr;

    private File destfile;

    private File keystore;

    private String alias;

    private String password;

    private int validity = 365;

    public File getCsr() {
        return csr;
    }

    public void setCsr(File csr) {
        this.csr = csr;
    }

    public File getDestfile() {
        return destfile;
    }

    public void setDestfile(File destfile) {
        this.destfile = destfile;
    }

    public File getKeystore() {
        return keystore;
    }

    public void setKeystore(File keystore) {
        this.keystore = keystore;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getValidity() {
        return validity;
    }

    public void setValidity(int validity) {
        this.validity = validity;
    }

    @Override
    public void execute() throws BuildException {

        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

            final PKCS10CertificationRequest request = loadCSR();

            PrivateKeyEntry rootentry = loadCAKeyEntry();
            X509Certificate rootcert = (X509Certificate) rootentry
                    .getCertificate();

            log("Subject: "
                    + request.getCertificationRequestInfo().getSubject());
            log("Signer:  " + rootcert.getIssuerDN());

            // Creating signed certificate:

            X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

            final long now = System.currentTimeMillis();
            certGen.setSerialNumber(BigInteger.valueOf(now));
            certGen.setIssuerDN(rootcert.getSubjectX500Principal());
            certGen.setNotBefore(new Date(now));
            final Date validUntil = new Date(now + (validity * DAY));
            log("Until:   " + validUntil);
            certGen.setNotAfter(validUntil);
            certGen.setSubjectDN(request.getCertificationRequestInfo()
                    .getSubject());
            certGen.setPublicKey(request.getPublicKey("BC"));
            certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

            certGen.addExtension(
                    X509Extensions.AuthorityKeyIdentifier,
                    false,
                    new AuthorityKeyIdentifierStructure(rootcert.getPublicKey()));

            certGen.addExtension(
                    X509Extensions.SubjectKeyIdentifier,
                    false,
                    new SubjectKeyIdentifierStructure(request
                            .getPublicKey("BC")));

            certGen.addExtension(X509Extensions.BasicConstraints, true,
                    new BasicConstraints(false));

            certGen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(
                    KeyUsage.digitalSignature | KeyUsage.keyEncipherment));

            X509Certificate issuedCert = certGen.generate(rootentry
                    .getPrivateKey());

            writeCertificate(rootcert, issuedCert);
        } catch (IOException e) {
            throw new BuildException(e);
        } catch (GeneralSecurityException e) {
            throw new BuildException(e);
        }
    }

    private KeyStore loadKeyStore() throws IOException,
            GeneralSecurityException {
        final KeyStore keystore = KeyStore.getInstance("JKS");
        InputStream in = null;
        try {
            in = new FileInputStream(this.keystore);
            keystore.load(in, this.password.toCharArray());
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return keystore;
    }

    private PrivateKeyEntry loadCAKeyEntry() throws IOException,
            GeneralSecurityException {
        final KeyStore keystore = loadKeyStore();
        final Entry entry = keystore.getEntry(this.alias,
                new PasswordProtection(this.password.toCharArray()));
        return (PrivateKeyEntry) entry;
    }

    private PKCS10CertificationRequest loadCSR() throws IOException {
        InputStream in = null;
        final PKCS10CertificationRequest request;
        try {
            in = new FileInputStream(this.csr);
            request = (PKCS10CertificationRequest) new PEMReader(
                    new InputStreamReader(in)).readObject();
        } finally {
            if (in != null)
                in.close();
        }

        return request;
    }

    private void writeCertificate(Certificate... certificates)
            throws IOException {
        final PEMWriter writer = new PEMWriter(new FileWriter(destfile));
        for (final Certificate c : certificates) {
            writer.writeObject(c);
        }
        writer.close();
    }

}
