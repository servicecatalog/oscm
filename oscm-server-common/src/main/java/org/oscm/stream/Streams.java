/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.stream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validation.Invariants;

/**
 * Utility class for working with streams.
 * 
 */
public class Streams {

    public static final Log4jLogger logger = LoggerFactory
            .getLogger(Streams.class);

    /**
     * Reads all bytes from the given stream.
     * 
     * @param stream
     *            to read from
     * @return byte[]
     * @throws IOException
     * @throws InterruptedException
     */
    public static byte[] readFrom(InputStream stream)
            throws InterruptedException, IOException {
        Invariants.assertNotNull(stream);
        BufferedInputStream is = new BufferedInputStream(stream);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            int aByte;
            while ((aByte = is.read()) != -1) {
                Streams.checkIfCanceled();
                bos.write(aByte);
            }
        } finally {
            close(is);
            close(bos);
        }
        return bos.toByteArray();
    }

    /**
     * Writes all bytes to the given stream.
     * 
     * @param stream
     *            to be written to
     * @param value
     *            to be written to the given stream
     * @throws IOException
     * @throws InterruptedException
     */
    public static void writeTo(OutputStream stream, byte[] value)
            throws IOException, InterruptedException {
        BufferedOutputStream os = new BufferedOutputStream(stream);
        try {
            for (byte element : value) {
                Streams.checkIfCanceled();
                os.write(element);
            }
            os.flush();
        } finally {
            close(os);
        }
    }

    /**
     * Copies all of input stream to output stream
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    public static void copyStreams(InputStream inputStream,
            OutputStream outputStream) throws IOException, InterruptedException {
        InputStream is = new BufferedInputStream(inputStream, 100000);
        OutputStream os = new BufferedOutputStream(outputStream, 100000);
        int aByte;
        try {
            while ((aByte = is.read()) != -1) {
                Streams.checkIfCanceled();
                os.write(aByte);
            }
            os.flush();
        } finally {
            close(is);
            close(os);
        }
    }

    /**
     * Closes the given stream.
     * 
     * @param stream
     *            to be closed
     */
    public static void close(Closeable stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException ignore) {
            logger.logError(Log4jLogger.SYSTEM_LOG, ignore,
                    LogMessageIdentifier.ERROR_CLOSE_RESOURCE_FAILED);
        }
    }

    /**
     * Throws InterruptedException if thread is interrupted.
     * 
     * @throws InterruptedException
     */
    static void checkIfCanceled() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }
}
