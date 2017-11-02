/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.filter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

/**
 * Wrapper Class for a ServletInputStream which implements the mark(int)/reset()
 * reset methods
 * 
 */
public class BufferedServletInputStream extends ServletInputStream {

    BufferedInputStream bufferedInputStream;

    public int available() throws IOException {
        return bufferedInputStream.available();
    }

    public void close() throws IOException {
        bufferedInputStream.close();
    }

    public boolean equals(Object obj) {
        return bufferedInputStream.equals(obj);
    }

    public int hashCode() {
        return bufferedInputStream.hashCode();
    }

    public void mark(int readlimit) {
        bufferedInputStream.mark(readlimit);
    }

    public boolean markSupported() {
        return bufferedInputStream.markSupported();
    }

    public int read() throws IOException {
        return bufferedInputStream.read();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return bufferedInputStream.read(b, off, len);
    }

    public int read(byte[] b) throws IOException {
        return bufferedInputStream.read(b);
    }

    public void reset() throws IOException {
        bufferedInputStream.reset();
    }

    public long skip(long n) throws IOException {
        return bufferedInputStream.skip(n);
    }

    public BufferedServletInputStream(InputStream inputStream) {
        super();
        bufferedInputStream = new BufferedInputStream(inputStream);
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setReadListener(ReadListener readListener) {

    }
}
