/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Author: Mao
 *
 * <p>Creation Date: 29.08.2013
 *
 * <p>Completion Time: 29.08.2013
 *
 * <p>*****************************************************************************
 */
package org.oscm.ui.common;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/** Validate if the url is accessible. */
public class RequestUrlHandler {
  private static final Log4jLogger logger = LoggerFactory.getLogger(RequestUrlHandler.class);

  public static boolean isUrlAccessible(String url) throws MalformedURLException {
    boolean result = false;
    if (url == null || url.length() <= 0) {
      return false;
    }

    URL requestUrl = new URL(url);
    try {
      int rc = getTrustAllConnection(requestUrl);
      result = (200 <= rc && 300 > rc);

    } catch (Exception e) {
      logger.logError(Log4jLogger.SYSTEM_LOG, e, LogMessageIdentifier.ERROR);
      e.printStackTrace();
    }
    return result;
  }

  static int getTrustAllConnection(URL url)
      throws NoSuchAlgorithmException, KeyManagementException, IOException {
    int rc = 500;
    if ("https".equals(url.getProtocol().toLowerCase())) {
      HostnameVerifier oldHvf = HttpsURLConnection.getDefaultHostnameVerifier();

      try {
        TrustManager[] trustAllCerts =
            new TrustManager[] {
              new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                  return null;
                }

                @Override
                public void checkClientTrusted(
                    java.security.cert.X509Certificate[] arg0, String arg1)
                    throws CertificateException {}

                @Override
                public void checkServerTrusted(
                    java.security.cert.X509Certificate[] arg0, String arg1)
                    throws CertificateException {}
              }
            };

        SSLContext sc = SSLContext.getInstance("TLSv1.2");

        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier allHostsValid =
            new HostnameVerifier() {
              public boolean verify(String hostname, SSLSession session) {
                return true;
              }
            };
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        rc = con.getResponseCode();

      } finally {
        HttpsURLConnection.setDefaultHostnameVerifier(oldHvf);
      }
    }
    return rc;
  }
}
