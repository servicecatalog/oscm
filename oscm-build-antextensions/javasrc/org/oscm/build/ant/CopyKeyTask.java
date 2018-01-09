/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.ProtectionParameter;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Copies a keystore entry from one keystore to another keystore.
 * 
 * @author hoffmann
 */
public class CopyKeyTask extends Task {

    public static class EntryDescriptor {

        private File keystore;

        private String password;

        private String alias;

        private String type = "JKS";

        public File getKeystore() {
            return keystore;
        }

        public void setKeystore(File keystore) {
            this.keystore = keystore;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }

    private EntryDescriptor source;

    private EntryDescriptor target;

    public EntryDescriptor createSource() {
        return source = new EntryDescriptor();
    }

    public EntryDescriptor createTarget() {
        return target = new EntryDescriptor();
    }

    @Override
    public void execute() throws BuildException {
        if (source == null) {
            throw new BuildException("No source specified.");
        }
        if (target == null) {
            throw new BuildException("No target specified.");
        }
        try {
            final Entry entry = loadEntry(source);
            final KeyStore targetKeystore = loadKeyStore(target);
            targetKeystore.setEntry(target.getAlias(), entry,
                    createProtection(target));
            saveKeyStore(targetKeystore, target);
        } catch (IOException e) {
            throw new BuildException(e);
        } catch (GeneralSecurityException e) {
            throw new BuildException(e);
        }
    }

    private Entry loadEntry(final EntryDescriptor descr) throws IOException,
            GeneralSecurityException {
        final KeyStore keystore = loadKeyStore(descr);
        final Entry entry = keystore.getEntry(descr.getAlias(),
                createProtection(descr));
        if (entry == null) {
            throw new BuildException(String.format(
                    "No entry %s found in keystore %s.", descr.getAlias(),
                    descr.getKeystore()));
        }
        return entry;
    }

    private KeyStore loadKeyStore(final EntryDescriptor descr)
            throws IOException, GeneralSecurityException {
        final KeyStore keystore = KeyStore.getInstance(descr.getType());
        InputStream in = null;
        try {
            in = new FileInputStream(descr.getKeystore());
            keystore.load(in, descr.getPassword().toCharArray());
        } finally {
            if (in != null)
                in.close();
        }
        return keystore;
    }

    private void saveKeyStore(final KeyStore keystore,
            final EntryDescriptor descr) throws IOException,
            GeneralSecurityException {
        OutputStream out = null;
        try {
            out = new FileOutputStream(descr.getKeystore());
            keystore.store(out, descr.getPassword().toCharArray());
        } finally {
            if (out != null)
                out.close();
        }

    }

    private ProtectionParameter createProtection(final EntryDescriptor descr) {
        return new PasswordProtection(descr.getPassword().toCharArray());
    }

}
