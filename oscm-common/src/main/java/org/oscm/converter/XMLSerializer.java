/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.converter;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import org.oscm.encrypter.AESEncrypter;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

public class XMLSerializer {

    private static final Log4jLogger LOGGER = LoggerFactory
            .getLogger(XMLSerializer.class);

    private static class BigDecimalPersistenceDelegate
            extends DefaultPersistenceDelegate {
        @Override
        protected boolean mutatesTo(Object oldInstance, Object newInstance) {
            return oldInstance.equals(newInstance);
        }

        @Override
        protected Expression instantiate(Object oldInstance, Encoder out) {
            BigDecimal bd = (BigDecimal) oldInstance;
            return new Expression(bd, bd.getClass(), "new",
                    new Object[] { bd.toString() });
        }
    }

    private static class EnumPersistenceDelegate
            extends DefaultPersistenceDelegate {
        @Override
        protected boolean mutatesTo(Object oldInstance, Object newInstance) {
            return oldInstance == newInstance;
        }

        @Override
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Enum<?> e = (Enum<?>) oldInstance;
            return new Expression(e, e.getClass(), "valueOf",
                    new Object[] { e.name() });
        }
    }

    public static class ByteArrayPersistenceDelegate
            extends DefaultPersistenceDelegate {
        @Override
        protected Expression instantiate(Object oldInstance, Encoder out) {
            byte[] e = (byte[]) oldInstance;
            return new Expression(e, ByteArrayPersistenceDelegate.class,
                    "decode",
                    new Object[] { ByteArrayPersistenceDelegate.encode(e) });
        }

        @Override
        protected boolean mutatesTo(Object oldInstance, Object newInstance) {
            return Arrays.equals((byte[]) oldInstance, (byte[]) newInstance);
        }

        public static byte[] decode(String encoded) {
            return org.apache.commons.codec.binary.Base64.decodeBase64(encoded);
        }

        public static String encode(byte[] data) {
            return org.apache.commons.codec.binary.Base64
                    .encodeBase64String(data);
        }
    }

    private static class UUIDDelegate extends DefaultPersistenceDelegate {
        @Override
        protected boolean mutatesTo(Object oldInstance, Object newInstance) {
            return oldInstance.equals(newInstance);
        }

        @Override
        protected Expression instantiate(Object oldInstance, Encoder out) {
            UUID bd = (UUID) oldInstance;
            return new Expression(bd, bd.getClass(), "fromString",
                    new Object[] { bd.toString() });
        }
    }

    public static String toXml(Object source) {
        return toXml(source, null);
    }

    /**
     * Serializes an object to an XML string.
     * 
     * @param source
     *            the object to serialize.
     * @return the XML string representing the object.
     * @throws UnsupportedEncodingException
     */
    public static synchronized String toXml(Object source, Class<?>[] types) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            XMLEncoder encoder = new XMLEncoder(out);

            setPersistenceDelegates(encoder, types);

            // Handle private Collections.xyz classes
            Object valueToWrite = source;
            if (valueToWrite.getClass() == Collections.EMPTY_LIST.getClass()) {
                valueToWrite = new ArrayList<>();
            }
            if (valueToWrite.getClass() == Collections.singletonList(null)
                    .getClass()) {
                valueToWrite = new ArrayList<>((Collection<?>) source);
            }
            if (valueToWrite.getClass() == Arrays.asList(new Object[] {})
                    .getClass()) {
                valueToWrite = new ArrayList<>((Collection<?>) source);
            }

            encoder.writeObject(valueToWrite);
            encoder.close();

        } finally {
            close(out);
        }

        String result = null;
        try {
            result = AESEncrypter.encrypt(XmlStringCleaner
                    .cleanString(new String(out.toByteArray(), "UTF-8")));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private static void setPersistenceDelegates(XMLEncoder encoder,
            Class<?>[] types) {
        if (types != null && types.length > 0) {
            PersistenceDelegate persistenceDelegate = new EnumPersistenceDelegate();
            for (int i = 0; i < types.length; i++) {
                encoder.setPersistenceDelegate(types[i], persistenceDelegate);
            }
        }

        // Handle "BiGDecimal" manually (has no default constructor)
        encoder.setPersistenceDelegate(BigDecimal.class,
                new BigDecimalPersistenceDelegate());

        encoder.setPersistenceDelegate(byte[].class,
                new ByteArrayPersistenceDelegate());
        encoder.setPersistenceDelegate(UUID.class, new UUIDDelegate());
    }

    /**
     * Close the closeable if it is not null.
     * 
     * @param closeable
     *            the closeable to close.
     */
    static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            LOGGER.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR);
        }
    }

    public static Object toObject(String xml) {
        Object result = null;
        XMLDecoder decoder = null;
        try {
            byte[] bytes;
            if (xml.contains("<")) {
                bytes = xml.getBytes();
            } else {
                String decrypted = AESEncrypter.decrypt(xml);
                bytes = decrypted.getBytes();
            }
            decoder = new XMLDecoder(new ByteArrayInputStream(bytes));
            result = decoder.readObject();
        } catch (Exception e) {
            LOGGER.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR);
        } finally {
            if (decoder != null) {
                decoder.close();
            }
        }
        return result;
    }

}
