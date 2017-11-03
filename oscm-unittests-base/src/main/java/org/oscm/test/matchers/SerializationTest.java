/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.matchers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Test;


public class SerializationTest {
    @Test
    public void testIsSerializable_ok() {
        assertTrue(Serialization.isSerializable(new SerializableClass("abc")));
    }

    @Test
    public void testIsSerializable_false() throws Exception {
        assertFalse(Serialization.isSerializable(new NotFullySerializableClass()));
    }

    @Test
    public void serializeAndUnserialize() throws Exception {
        SerializableClass sc1 = new SerializableClass("def");
        byte[] serialized = Serialization.serialize(sc1);
        assertNotNull(serialized);

        SerializableClass sc2 = Serialization.unserialize(serialized,
                SerializableClass.class);
        assertTrue(sc1.equals(sc2));
    }
}

class SerializableClass implements Serializable {
    private static final long serialVersionUID = -7261683600792346982L;
    private String id;

    public SerializableClass(String id) {
        this.id = id;
    }

    public boolean equals(Object obj) {
        if (obj instanceof SerializableClass) {
            return id.equals(((SerializableClass) obj).id);
        }
        return false;
    }

    public int hashCode() {
        int hash = 7;
        return 31 * hash + id.hashCode();
    }
}

class NotFullySerializableClass implements Serializable {
    private static final long serialVersionUID = 1808405421428657172L;

    ClassLoader classLoader;

    public NotFullySerializableClass() throws Exception {
        classLoader = new URLClassLoader(new URL[] { new URL(
                "http://www.google.de") });
    }
}

class NotSerializableClass {
}
