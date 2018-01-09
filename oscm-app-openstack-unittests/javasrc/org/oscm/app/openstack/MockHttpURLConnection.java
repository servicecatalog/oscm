/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 31.07.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author afschar
 * 
 */
public class MockHttpURLConnection extends HttpURLConnection {
    private String output;
    private String error;
    private String input;
    protected boolean connected = false;
    private StringWriter inputWriter = new StringWriter();
    private URL url;
    private Exception throwException;
    protected String locationHeader;
    protected int responseCode;

    public MockHttpURLConnection(int responseCode, String output) {
        super(null);
        this.output = output;
        this.responseCode = responseCode;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public boolean usingProxy() {
        return false;
    }

    @Override
    public void connect() throws IOException {
        if (throwException != null) {
            if (throwException instanceof IOException) {
                throw (IOException) throwException;
            }
            throw (RuntimeException) throwException;
        }
        connected = true;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        connect();
        if (output == null) {
            output = "";
        }
        return new ByteArrayInputStream(output.getBytes());
    }

    @Override
    public InputStream getErrorStream() {
        if (error == null) {
            error = "";
        }
        return new ByteArrayInputStream(error.getBytes());
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                inputWriter.write(b);
            }
        };
    }

    @Override
    public String getHeaderField(String name) {
        if ("Location".equals(name)) {
            return locationHeader;
        }
        return super.getHeaderField(name);
    }

    @Override
    public int getResponseCode() throws IOException {
        return responseCode;
    }

    public String getInput() {
        if (!connected) {
            throw new IllegalStateException("Not connected");
        }
        return input;
    }

    public void setIOException(IOException throwException) {
        this.throwException = throwException;
        if (throwException != null) {
            error = throwException.getMessage();
        }
    }

    public void setRuntimeException(RuntimeException throwException) {
        this.throwException = throwException;
        if (throwException != null) {
            error = throwException.getMessage();
        }
    }

    public String getRequestUri() {
        return url.toString();
    }
}
