/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 04.08.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Before;
import org.junit.Test;
import org.oscm.app.openstack.exceptions.OpenStackConnectionException;

import sun.net.www.protocol.http.HttpURLConnection;

/**
 * @author afschar
 *
 */
public class OpenStackConnectionTest {

    private static final String HTTPS_PROXY_HOST = "https.proxyHost";
    private static final String HTTPS_PROXY_PORT = "https.proxyPort";
    private static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";

    private static final String HTTPS_PROXY_USER = "https.proxyUser";
    private static final String HTTPS_PROXY_PASSWORD = "https.proxyPassword";

    @Before
    public void setUp() {
        OpenStackConnection.setURLStreamHandler(new MockURLStreamHandler());
    }

    @Test
    public void processRequest_wrongURL() {
        // given
        String url = "more test";

        // when
        try {
            givenOpenStackConnetion().processRequest(url, "POST");
            fail("Test must fail with HeatException!");
        } catch (OpenStackConnectionException ex) {
            // then
            assertTrue(ex.getMessage().contains("invalid URL"));
            assertTrue(ex.getMessage().contains(url));
            assertEquals(-1, ex.getResponseCode());
        }
    }

    @Test
    public void processRequest_noHTTPConnection() {
        // given
        String url = "http://openservicecatalogmanager.org";
        OpenStackConnection.setURLStreamHandler(new MockURLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u, Proxy p)
                    throws IOException {
                return new URLConnection(u) {
                    @Override
                    public void connect() throws IOException {
                    }
                };
            }
        });

        // when
        try {
            givenOpenStackConnetion().processRequest(url, "POST");
            fail("Test must fail with HeatException!");
        } catch (OpenStackConnectionException ex) {
            // then
            assertTrue(ex.getMessage().contains("Expected http(s) connection for URL"));
            assertTrue(ex.getMessage().contains(url));
        }
    }

    @Test
    public void processRequest_IOException401() {

        // given
        OpenStackConnection.setURLStreamHandler(new MockURLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u, Proxy p)
                    throws IOException {
                return new HttpURLConnection(u, (Proxy) null) {
                    @Override
                    public void connect() throws IOException {
                        throw new IOException();
                    }

                    @Override
                    public int getResponseCode() {
                        return 401;
                    }

                    @Override
                    public synchronized InputStream getErrorStream() {
                        return new ByteArrayInputStream(
                                "401 error occurred".getBytes());
                    }
                };
            }
        });
        OpenStackConnection oc = givenOpenStackConnetion();

        // when
        try {

            oc.processRequest("http://openservicecatalogmanager.org", "POST");

            fail("Test must fail with HeatException!");
        } catch (OpenStackConnectionException ex) {
            // then
            assertTrue(ex.getMessage().contains("unauthorized"));
            assertTrue(ex.getMessage().contains("HTTP 401"));
            assertTrue(ex.getMessage().contains("http://openservicecatalogmanager.org"));
            assertTrue(ex.getMessage().contains("401 error occurred"));
        }

    }

    @Test
    public void processRequest_IOException400() {
        // given
        OpenStackConnection.setURLStreamHandler(new MockURLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u, Proxy p)
                    throws IOException {
                return new HttpURLConnection(u, (Proxy) null) {
                    @Override
                    public void connect() throws IOException {
                        throw new IOException();
                    }

                    @Override
                    public int getResponseCode() {
                        return 400;
                    }

                    @Override
                    public synchronized InputStream getErrorStream() {
                        return new ByteArrayInputStream(
                                "400 error occurred".getBytes());
                    }
                };
            }
        });
        OpenStackConnection oc = givenOpenStackConnetion();

        // when
        try {
            oc.processRequest("http://openservicecatalogmanager.org", "POST");
            fail("Test must fail with HeatException!");
        } catch (OpenStackConnectionException ex) {
            // then
            assertTrue(ex.getMessage().contains("either input parameter format error"));
            assertTrue(ex.getMessage().contains("HTTP 400"));
            assertTrue(ex.getMessage().contains("http://openservicecatalogmanager.org"));
            assertTrue(ex.getMessage().contains("400 error occurred"));
        }
    }

    @Test
    public void processRequest_IOException404() {
        // given
        OpenStackConnection.setURLStreamHandler(new MockURLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u, Proxy p)
                    throws IOException {
                return new HttpURLConnection(u, (Proxy) null) {
                    @Override
                    public void connect() throws IOException {
                        throw new IOException();
                    }

                    @Override
                    public int getResponseCode() {
                        return 404;
                    }

                    @Override
                    public synchronized InputStream getErrorStream() {
                        return new ByteArrayInputStream(
                                "404 error occurred".getBytes());
                    }
                };
            }
        });
        OpenStackConnection oc = givenOpenStackConnetion();

        // when
        try {
            oc.processRequest("http://openservicecatalogmanager.org", "POST");
            fail("Test must fail with HeatException!");
        } catch (OpenStackConnectionException ex) {
            // then
            assertTrue(ex.getMessage().contains("resource not found"));
            assertTrue(ex.getMessage().contains("HTTP 404"));
            assertTrue(ex.getMessage().contains("http://openservicecatalogmanager.org"));
            assertTrue(ex.getMessage().contains("404 error occurred"));
        }
    }

    @Test
    public void processRequest_IOException406() {
        // given
        final String msg = "more of this test!";
        OpenStackConnection.setURLStreamHandler(new MockURLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u, Proxy p)
                    throws IOException {
                return new HttpURLConnection(u, (Proxy) null) {
                    @Override
                    public void connect() throws IOException {
                        throw new IOException(msg);
                    }

                    @Override
                    public int getResponseCode() {
                        return 406;
                    }

                    @Override
                    public synchronized InputStream getInputStream() {
                        return new ByteArrayInputStream(
                                "exception test".getBytes());
                    }

                    @Override
                    public synchronized InputStream getErrorStream() {
                        return new ByteArrayInputStream(
                                "406 error occurred".getBytes());
                    }
                };
            }
        });
        OpenStackConnection oc = givenOpenStackConnetion();

        // when
        try {
            oc.processRequest("http://openservicecatalogmanager.org", "POST");
            fail("Test must fail with HeatException!");
        } catch (OpenStackConnectionException ex) {
            // then
            assertTrue(ex.getMessage().contains("send failed"));
            assertTrue(ex.getMessage().contains("HTTP 406"));
            assertTrue(ex.getMessage().contains("http://openservicecatalogmanager.org"));
            assertTrue(ex.getMessage().contains("406 error occurred"));
            assertTrue(ex.getMessage().contains(msg));
        }
    }

    @Test
    public void processRequest_IOException504() {
        // given
        OpenStackConnection.setURLStreamHandler(new MockURLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u, Proxy p)
                    throws IOException {
                return new HttpURLConnection(u, (Proxy) null) {
                    @Override
                    public void connect() throws IOException {
                        throw new IOException();
                    }

                    @Override
                    public int getResponseCode() {
                        return 504;
                    }

                    @Override
                    public synchronized InputStream getErrorStream() {
                        return new ByteArrayInputStream(
                                "504 error occurred".getBytes());
                    }
                };
            }
        });
        OpenStackConnection oc = givenOpenStackConnetion();

        // when
        try {
            oc.processRequest("http://openservicecatalogmanager.org", "POST");
            fail("Test must fail with HeatException!");
        } catch (OpenStackConnectionException ex) {
            // then
            assertTrue(ex.getMessage().contains("Gateway/proxy timeout"));
            assertTrue(ex.getMessage().contains("HTTP 504"));
            assertTrue(ex.getMessage().contains("http://openservicecatalogmanager.org"));
            assertTrue(ex.getMessage().contains("504 error occurred"));
        }
    }

    @Test
    public void processRequest_IOException() {
        // given
        final String msg = "get out of my way!";
        OpenStackConnection.setURLStreamHandler(new MockURLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u, Proxy p)
                    throws IOException {
                return new HttpURLConnection(u, (Proxy) null) {
                    @Override
                    public void connect() throws IOException {
                        throw new IOException(msg);
                    }
                };
            }
        });

        // when
        try {
            givenOpenStackConnetion().processRequest(
                    "http://openservicecatalogmanager.org", "POST");
            fail("Test must fail with HeatException!");
        } catch (OpenStackConnectionException ex) {
            // then
            assertTrue(ex.getMessage().contains("send failed"));
            assertTrue(ex.getMessage().contains(msg));
        }
    }

    @Test
    public void processRequest_usingProxy()
            throws OpenStackConnectionException {
        // given
        System.setProperty(HTTPS_PROXY_HOST, "host.fqdn");
        System.setProperty(HTTPS_PROXY_PORT, "9876");
        System.setProperty(HTTP_NON_PROXY_HOSTS,
                "localhost|127.0.0.1|testHost*");
        System.setProperty(HTTPS_PROXY_USER, "testuser");
        System.setProperty(HTTPS_PROXY_PASSWORD, "testpassword");
        MockURLStreamHandler st = new MockURLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u, Proxy p)
                    throws IOException {
                return new HttpURLConnection(u, p) {
                    @Override
                    public void connect() {
                        assertTrue("Connect successful", true);
                    }

                    @Override
                    public int getResponseCode() {
                        return 200;
                    }

                    @Override
                    public InputStream getInputStream() {
                        connect();
                        return new ByteArrayInputStream(
                                "connect success".getBytes());
                    }
                };
            }
        };
        OpenStackConnection.setURLStreamHandler(st);

        // when
        givenOpenStackConnetion()
                .processRequest("http://processUsingProxy.de/hoge", "POST");

        // then
        assertTrue("finish connection", true);
    }

    @Test
    public void processRequest_setProxyAndConnectNonProxy()
            throws OpenStackConnectionException {
        // given
        System.setProperty(HTTPS_PROXY_HOST, "host.fqdn");
        System.setProperty(HTTPS_PROXY_PORT, "9876");
        System.setProperty(HTTP_NON_PROXY_HOSTS,
                "localhost|127.0.0.1|testHost*");
        System.setProperty(HTTPS_PROXY_USER, "testuser");
        System.setProperty(HTTPS_PROXY_PASSWORD, "testpassword");
        MockURLStreamHandler st = new MockURLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u, Proxy p)
                    throws IOException {
                if (p == Proxy.NO_PROXY) {
                    assertTrue("Connect successful", true);

                    return new HttpURLConnection(u, p) {
                        @Override
                        public void connect() throws IOException {
                        }

                        @Override
                        public int getResponseCode() throws IOException {
                            return 200;
                        }

                        @Override
                        public InputStream getInputStream() throws IOException {
                            connect();
                            return new ByteArrayInputStream(
                                    "connect success".getBytes());
                        }
                    };
                } else {
                    throw new IOException("Please not use proxy!!");
                }
            }
        };
        OpenStackConnection.setURLStreamHandler(st);

        OpenStackConnection os = givenOpenStackConnetion();

        // when
        os.processRequest("http://testHost/hoge", "POST");

        // then
        assertTrue("finish connection", true);
    }

    @Test
    public void processRequest_nonProxyFirstStar()
            throws OpenStackConnectionException {
        // given
        System.setProperty(HTTPS_PROXY_HOST, "host.fqdn");
        System.setProperty(HTTPS_PROXY_PORT, "9876");
        System.setProperty(HTTP_NON_PROXY_HOSTS,
                "localhost|127.0.0.1|*testHost");
        System.setProperty(HTTPS_PROXY_USER, "testuser");
        System.setProperty(HTTPS_PROXY_PASSWORD, "testpassword");
        MockURLStreamHandler st = new MockURLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u, Proxy p)
                    throws IOException {
                if (p == Proxy.NO_PROXY) {
                    assertTrue("Connect successful", true);

                    return new HttpURLConnection(u, p) {
                        @Override
                        public void connect() throws IOException {
                        }

                        @Override
                        public int getResponseCode() throws IOException {
                            return 200;
                        }

                        @Override
                        public InputStream getInputStream() throws IOException {
                            connect();
                            return new ByteArrayInputStream(
                                    "connect success".getBytes());
                        }
                    };
                } else {
                    throw new IOException("Please not use proxy!!");
                }
            }
        };
        OpenStackConnection.setURLStreamHandler(st);

        // when
        givenOpenStackConnetion().processRequest("http://testHost/hoge",
                "POST");

        // then
        assertTrue("finish connection", true);
    }

    @Test
    public void processRequest_nonProxyFirstAndLastStar()
            throws OpenStackConnectionException {
        // given
        System.setProperty(HTTPS_PROXY_HOST, "host.fqdn");
        System.setProperty(HTTPS_PROXY_PORT, "9876");
        System.setProperty(HTTP_NON_PROXY_HOSTS,
                "localhost|127.0.0.1|*testHost*");
        System.setProperty(HTTPS_PROXY_USER, "testuser");
        System.setProperty(HTTPS_PROXY_PASSWORD, "testpassword");
        MockURLStreamHandler st = new MockURLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u, Proxy p)
                    throws IOException {
                if (p == Proxy.NO_PROXY) {
                    assertTrue("Connect successful", true);

                    return new HttpURLConnection(u, p) {
                        @Override
                        public void connect() throws IOException {
                        }

                        @Override
                        public int getResponseCode() throws IOException {
                            return 200;
                        }

                        @Override
                        public InputStream getInputStream() throws IOException {
                            connect();
                            return new ByteArrayInputStream(
                                    "connect success".getBytes());
                        }
                    };
                } else {
                    throw new IOException("Please not use proxy!!");
                }
            }
        };
        OpenStackConnection.setURLStreamHandler(st);

        // when
        givenOpenStackConnetion().processRequest("http://testHost/hoge",
                "POST");

        // then
        assertTrue("finish connection", true);
    }

    @Test
    public void processRequest_nonProxyNoStar()
            throws OpenStackConnectionException {
        // given
        System.setProperty(HTTPS_PROXY_HOST, "host.fqdn");
        System.setProperty(HTTPS_PROXY_PORT, "9876");
        System.setProperty(HTTP_NON_PROXY_HOSTS,
                "localhost|127.0.0.1|testHost");
        System.setProperty(HTTPS_PROXY_USER, "testuser");
        System.setProperty(HTTPS_PROXY_PASSWORD, "testpassword");
        MockURLStreamHandler st = new MockURLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u, Proxy p)
                    throws IOException {
                if (p == Proxy.NO_PROXY) {
                    assertTrue("Connect successful", true);

                    return new HttpURLConnection(u, p) {
                        @Override
                        public void connect() throws IOException {
                        }

                        @Override
                        public int getResponseCode() throws IOException {
                            return 200;
                        }

                        @Override
                        public InputStream getInputStream() throws IOException {
                            connect();
                            return new ByteArrayInputStream(
                                    "connect success".getBytes());
                        }
                    };
                } else {
                    throw new IOException("Please not use proxy!!");
                }
            }
        };
        OpenStackConnection.setURLStreamHandler(st);

        // when
        givenOpenStackConnetion().processRequest("http://testHost/hoge",
                "POST");

        // then
        assertTrue("finish connection", true);
    }

    @Test
    public void processRequest_connectionFailure()
            throws OpenStackConnectionException {
        // given
        System.setProperty(HTTPS_PROXY_HOST, "host.fqdn");
        System.setProperty(HTTPS_PROXY_PORT, "9876");
        System.setProperty(HTTP_NON_PROXY_HOSTS,
                "localhost|127.0.0.1|testHost");
        System.setProperty(HTTPS_PROXY_USER, "testuser");
        System.setProperty(HTTPS_PROXY_PASSWORD, "testpassword");

        MockURLStreamHandler st = new MockURLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u, Proxy p)
                    throws IOException {

                return new HttpURLConnection(u, p) {
                    @Override
                    public void connect() throws IOException {
                        throw new IOException("Cannot connect!");
                    }

                    @Override
                    public int getResponseCode() throws IOException {
                        return -1;
                    }
                };
            }
        };
        OpenStackConnection.setURLStreamHandler(st);

        // when
        try {
            givenOpenStackConnetion().processRequest("http://testHost/hoge",
                    "POST");
            fail("Test must fail with Exception!");
        } catch (OpenStackConnectionException ex) {
            // then
            assertTrue(ex.getMessage(), ex.getMessage().contains("send failed"));
        }
    }

    private OpenStackConnection givenOpenStackConnetion() {
        return new OpenStackConnection("some test") {

            @Override
            protected Proxy resolveProxy(String proxyHost, int proxyPortInt) {
                return Proxy.NO_PROXY;
            }
        };
    }
}
