/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;

import org.junit.Test;

/**
 * Unit tests for {@link PropertiesLoader}.
 * 
 * @author hoffmann
 */
public class PropertiesLoaderTest {

    @Test
    public void loadFromInputStream() throws Exception {
        final boolean[] closed = new boolean[] { false };
        final InputStream in = new ByteArrayInputStream(
                "Hello=World".getBytes("ISO-8859-1")) {
            @Override
            public void close() throws IOException {
                super.close();
                closed[0] = true;
            }
        };
        final Properties p = PropertiesLoader.loadProperties(in);
        assertTrue(closed[0]);
        assertNotNull(p);
        assertEquals("World", p.get("Hello"));
    }

    @Test
    public void loadFromBrokenInputStream() throws Exception {
        final boolean[] closed = new boolean[] { false };
        final InputStream in = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Broken");
            }

            @Override
            public void close() throws IOException {
                super.close();
                closed[0] = true;
            }
        };
        final Properties p = PropertiesLoader.loadProperties(in);
        assertTrue(closed[0]);
        assertNotNull(p);
        assertTrue(p.isEmpty());
    }

    @Test
    public void testLoadFromClass() {
        final Properties p = PropertiesLoader.load(PropertiesLoader.class,
                "org/oscm/converter/testdata.properties");
        assertNotNull(p);
        assertEquals("III", p.get("3"));
    }

    @Test
    public void loadToBundle() throws IOException {
        final ResourceBundle r = PropertiesLoader.loadToBundle(
                PropertiesLoader.class,
                "org/oscm/converter/testdata.properties");
        assertNotNull(r);
        assertEquals("III", r.getString("3"));
    }
}
