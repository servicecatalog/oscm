/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.matchers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

class Serialization {
    public static <T extends Object> boolean isSerializable(T instance) {
        if (!(instance instanceof Serializable)) {
            return false;
        }

        try {
            byte[] serialized = serialize(instance);
            unserialize(serialized, instance.getClass());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static <T extends Object> byte[] serialize(T instance)
            throws IOException {
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(instance);
            return baos.toByteArray();

        } finally {
            close(baos);
            close(oos);
        }
    }

    public static <T extends Object> T unserialize(byte[] b, Class<T> type)
            throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            bais = new ByteArrayInputStream(b);
            ois = new ObjectInputStream(bais);
            return type.cast(ois.readObject());
        } finally {
            close(bais);
            close(ois);
        }
    }

    private static void close(Closeable stream) throws IOException {
        if (stream != null) {
            stream.close();
        }
    }
}
