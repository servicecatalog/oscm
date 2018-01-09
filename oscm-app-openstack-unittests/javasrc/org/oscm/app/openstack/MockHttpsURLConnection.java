/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 26.07.2016
 *
 *******************************************************************************/
package org.oscm.app.openstack;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

public class MockHttpsURLConnection extends HttpsURLConnection {
    private String error;
    private String input;
	private String output;
    protected int responseCode;
    private StringWriter inputWriter = new StringWriter();

	public MockHttpsURLConnection(int responseCode, String output) {
		super(null);
        this.output = output;
        this.responseCode = responseCode;

	}

	@Override
	public String getCipherSuite() {
		return null;
	}

	@Override
	public Certificate[] getLocalCertificates() {
		return null;
	}

	@Override
	public Certificate[] getServerCertificates()
			throws SSLPeerUnverifiedException {
		return null;
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
    public String getInput() {
        if (!connected) {
            throw new IllegalStateException("Not connected");
        }
        return input;
    }

    @Override
    public int getResponseCode() throws IOException {
        return responseCode;
    }

    @Override
    public String getHeaderField(String name){
    	if((name).equals("X-Subject-Token")){
    		return "authToken";
        }
        return super.getHeaderField(name);
    }
}
