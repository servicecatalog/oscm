/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import org.oscm.string.Strings;

public class StreamsTest {

    boolean closed;

    /**
     * Read all bytes from stream
     * 
     * @throws Exception
     */
    @Test
    public void readFrom() throws Exception {
        InputStream is = new ByteArrayInputStream(
                Strings.toBytes("inputstream"));
        byte[] bytes = Streams.readFrom(is);
        assertEquals("inputstream", Strings.toString(bytes));
    }

    /**
     * Close stream in case of exception
     * 
     * @throws Exception
     */
    @Test
    public void readFrom_close() throws Exception {
        InputStream is = new ByteArrayInputStream("".getBytes()) {
            @Override
            public synchronized int read(byte b[], int off, int len) {
                throw new RuntimeException("Mock Exception for testing"); //$NON-NLS-1$
            }

            @Override
            public void close() throws IOException {
                super.close();
                closed = true;
            }

        };
        try {
            Streams.readFrom(is);
        } catch (Exception ignore) {
        }
        assertTrue(closed);
    }

    /**
     * Write to output stream
     * 
     * @throws Exception
     */
    @Test
    public void writeTo() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Streams.writeTo(stream, Strings.toBytes("asdf"));
        assertEquals("asdf", Strings.toString(stream.toByteArray()));
    }

    /**
     * Copy from input to output
     * 
     * @throws Exception
     */
    @Test
    public void copyTo() throws Exception {
        InputStream is = new ByteArrayInputStream(Strings.toBytes("hello"));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Streams.copyStreams(is, os);
        assertEquals("hello", Strings.toString(os.toByteArray()));
    }

    @Test
    public void close() throws Exception {
        // given
        Closeable closeable = mock(Closeable.class);

        // when
        Streams.close(closeable);

        // then
        verify(closeable).close();
    }

    /**
     * Nothing should happen if exception occurs during close
     */
    @Test
    public void close_exception() {
        Streams.close(new Closeable() {

            public void close() throws IOException {
                throw new IOException("Mock Exception for testing"); //$NON-NLS-1$
            }
        });
    }

    @Test
    public void close_null() {
        Streams.close(null);
    }

}
