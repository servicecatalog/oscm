/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 16.12.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.stubs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Mike J&auml;ger
 * 
 */
public class HttpServletResponseStub implements HttpServletResponse {

    private int bufferSize;
    private PrintWriter printWriter;
    private ByteArrayOutputStream baos;
    private boolean useWriter = true;

    /**
     * Flag to indicate whether an exception should be thrown in case the writer
     * object is used.
     */
    private boolean throwExceptionOnWriterUsage = false;

    @Override
    public void addCookie(Cookie arg0) {

    }

    @Override
    public void addDateHeader(String arg0, long arg1) {

    }

    @Override
    public void addHeader(String arg0, String arg1) {

    }

    @Override
    public void addIntHeader(String arg0, int arg1) {

    }

    @Override
    public boolean containsHeader(String arg0) {

        return false;
    }

    @Override
    public String encodeRedirectURL(String arg0) {

        return null;
    }

    // Overwritten deprecated method: make both Eclipse and javac happy
    @Override
    @SuppressWarnings({ "all", "deprecation" })
    public String encodeRedirectUrl(String arg0) {

        return null;
    }

    @Override
    public String encodeURL(String arg0) {

        return null;
    }

    // For javac: Overwritten deprecated method
    @Override
    @SuppressWarnings("all")
    public String encodeUrl(String arg0) {

        return null;
    }

    @Override
    public void sendError(int arg0) throws IOException {

    }

    @Override
    public void sendError(int arg0, String arg1) throws IOException {

    }

    @Override
    public void sendRedirect(String arg0) throws IOException {

    }

    @Override
    public void setDateHeader(String arg0, long arg1) {

    }

    @Override
    public void setHeader(String arg0, String arg1) {

    }

    @Override
    public void setIntHeader(String arg0, int arg1) {

    }

    @Override
    public void setStatus(int arg0) {

    }

    // For javac: Overwritten deprecated method
    @Override
    @SuppressWarnings("all")
    public void setStatus(int arg0, String arg1) {

    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public int getBufferSize() {
        return bufferSize;
    }

    @Override
    public String getCharacterEncoding() {

        return null;
    }

    @Override
    public String getContentType() {

        return null;
    }

    @Override
    public Locale getLocale() {

        return null;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {

        return null;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (throwExceptionOnWriterUsage) {
            throw new IOException();
        }
        if (!useWriter) {
            return null;
        }
        baos = new ByteArrayOutputStream();
        printWriter = new PrintWriter(baos);
        return printWriter;
    }

    @Override
    public boolean isCommitted() {

        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public void setBufferSize(int size) {
        bufferSize = size;
    }

    @Override
    public void setCharacterEncoding(String arg0) {

    }

    @Override
    public void setContentLength(int arg0) {

    }

    @Override
    public void setContentType(String arg0) {

    }

    @Override
    public void setLocale(Locale arg0) {

    }

    /**
     * Returns the content of the writer.
     * 
     * @return Content of the writer.
     * @throws UnsupportedEncodingException
     */
    public String getWriterContent() throws UnsupportedEncodingException {
        if (printWriter == null) {
            return null;
        }
        printWriter.flush();
        return baos.toString("UTF-8");
    }

    /**
     * Specifies if the response object should return a writer object or not. If
     * the value is set to <code>false</code>, <code>null</code> will be
     * returned.
     * 
     * @param enabled
     *            The value to be set.
     */
    public void enableWriter(boolean enabled) {
        this.useWriter = enabled;
    }

    /**
     * Specifies whether to throw an exception when working with the response's
     * writer object or not.
     * 
     * @param throwExceptionWhenUsingWriter
     *            The flag indicating whether to throw an exception or not.
     */
    public void setErrorOnWriterUsage(boolean throwExceptionWhenUsingWriter) {
        this.throwExceptionOnWriterUsage = throwExceptionWhenUsingWriter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#getHeader(java.lang.String)
     */
    @Override
    public String getHeader(String arg0) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#getHeaderNames()
     */
    @Override
    public Collection<String> getHeaderNames() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#getHeaders(java.lang.String)
     */
    @Override
    public Collection<String> getHeaders(String arg0) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletResponse#getStatus()
     */
    @Override
    public int getStatus() {
        return 0;
    }

    @Override
    public void setContentLengthLong(long arg0) {
        // TODO Auto-generated method stub
        
    }
}
