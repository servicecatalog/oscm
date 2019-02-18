/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.remote.vmware;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;

import org.oscm.app.vmware.persistence.VMwareCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VimService;
import com.vmware.vim25.VirtualMachineSnapshotInfo;
import com.vmware.vim25.VirtualMachineSnapshotTree;

/**
 * @author Dirk Bernsau
 *
 */
public class VMwareClient implements AutoCloseable {

    private static final Logger LOG = LoggerFactory
            .getLogger(VMwareClient.class);

    private static final String MO_TYPE_VIRTUAL_MACHINE = "VirtualMachine";
    private static final String PROPERTY_SNAPSHOT = "snapshot";
    private static final String PROPERTY_INFO = "info";

    private String url;
    private String user;
    private String password;
    private ServiceConnection connection;

    public VMwareClient() {
    }

    public VMwareClient(VMwareCredentials credentials) {
        this.url = credentials.getURL();
        this.user = credentials.getUserId();
        this.password = credentials.getPassword();
    }

    /**
     * Establish a connection to the vCenter.
     */
    public void connect() throws Exception {
        // FIXME what to do?
        HostnameVerifier hv = new HostnameVerifier() {
            @Override
            public boolean verify(String urlHostName, SSLSession session) {
                return true;
            }
        };

        HttpsURLConnection.setDefaultHostnameVerifier(hv);
        LOG.info("FIXME - Allowing all SSL.");
        HttpsTrustManager.allowAllSSL();
        int numFailedLogins = 0;
        boolean repeatLogin = true;

        while (repeatLogin) {
            try {

                VimService vimService = new VimService();
                VimPortType vimPort = vimService.getVimPort();
                Map<String, Object> ctxt = ((BindingProvider) vimPort)
                        .getRequestContext();

                ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
                ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY,
                        Boolean.TRUE);

                ManagedObjectReference morSvcInstance = new ManagedObjectReference();
                morSvcInstance.setType("ServiceInstance");
                morSvcInstance.setValue("ServiceInstance");
                ServiceContent serviceContent = vimPort
                        .retrieveServiceContent(morSvcInstance);
                vimPort.login(serviceContent.getSessionManager(), user,
                        password, null);
                connection = new ServiceConnection(vimPort, serviceContent);
                LOG.info("Established connection to vSphere. URL: " + url
                        + ", UserId: " + user);

                repeatLogin = false;
            } catch (Exception e) {
                LOG.error("Failed to establish connection to vSphere. URL: "
                        + url + ", UserId: " + user, e);
                if (numFailedLogins > 2) {
                    throw e;
                }
                numFailedLogins++;
                repeatLogin = true;
                try {
                    Thread.sleep(3000);
                } catch (@SuppressWarnings("unused") InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Returns whether the connection has been established
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        if (connection == null) {
            LOG.debug("Not connected to vSphere. URL: " + url + ", UserId: "
                    + user);
            return false;
        }

        try {
            long ref = System.currentTimeMillis();
            new ManagedObjectAccessor(connection).getDecendentMoRef(null,
                    "VirtualMachine", "no-name");
            LOG.debug("vSphere connection is alive. Check took "
                    + (System.currentTimeMillis() - ref) + "ms. URL: " + url
                    + ", UserId: " + user);
            return true;
        } catch (@SuppressWarnings("unused") Exception e) {
            LOG.debug("Current connection is invalid. URL: " + url
                    + ", UserId: " + user);
            return false;
        }
    }

    public VimPortType getService() {
        return getConnection().getService();
    }

    /**
     * Returns the {@link ManagedObjectAccessor} based on the current client
     * connection.
     *
     * @return the managed object accessor
     * @throws IllegalStateException
     *             when client is not connected
     */
    public ManagedObjectAccessor getServiceUtil() {
        return new ManagedObjectAccessor(getConnection());
    }

    /**
     * Returns the {@link ServiceConnection} when client is in connect state.
     *
     * @return the service connection
     */
    public ServiceConnection getConnection() {
        if (connection == null) {
            try {
                connect();
            } catch (Exception e) {
                throw new RuntimeException("Couldn't connect to vSphere", e);
            }
        }
        return connection;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.disconnect();
        }
        connection = null;
    }

    public ManagedObjectReference getVirtualMachine(String vmName)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {

        ManagedObjectReference vm = getServiceUtil().getDecendentMoRef(null,
                MO_TYPE_VIRTUAL_MACHINE, vmName);
        return vm;
    }

    public ManagedObjectReference findSnapshot(String vmName, String snapshotId)
            throws Exception {

        ManagedObjectReference vm = getVirtualMachine(vmName);
        VirtualMachineSnapshotInfo info = (VirtualMachineSnapshotInfo) getServiceUtil()
                .getDynamicProperty(vm, PROPERTY_SNAPSHOT);
        if (info == null) {
            return null;
        }
        return searchSnapshot(info.getRootSnapshotList(), snapshotId);
    }

    private static ManagedObjectReference searchSnapshot(
            List<VirtualMachineSnapshotTree> tree, String id) {

        if (tree == null) {
            return null;
        }

        for (VirtualMachineSnapshotTree snapshot : tree) {
            if (snapshot.getSnapshot().getValue().equals(id)) {
                return snapshot.getSnapshot();
            }
            ManagedObjectReference mor = searchSnapshot(
                    snapshot.getChildSnapshotList(), id);
            if (mor != null) {
                return mor;
            }
        }

        return null;
    }

    public TaskInfo retrieveTaskInfo(ManagedObjectReference task)
            throws Exception {

        return ((TaskInfo) getServiceUtil().getDynamicProperty(task,
                PROPERTY_INFO));
    }

    static class HttpsTrustManager implements X509TrustManager {
        private static TrustManager[] trustManagers;
        java.security.cert.X509Certificate[] _AcceptedIssuers = new java.security.cert.X509Certificate[] {};

        /*
         * (non-Javadoc)
         * 
         * @see
         * javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.
         * X509Certificate[], java.lang.String)
         */
        @Override
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] arg0, String arg1)
                throws CertificateException {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.
         * X509Certificate[], java.lang.String)
         */
        @Override
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] chain, String authType)
                throws CertificateException {
            // TODO Auto-generated method stub

        }

        public boolean isClientTrusted(X509Certificate[] chain) {
            return true;
        }

        public boolean isServerTrusted(X509Certificate[] chain) {
            return true;
        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return _AcceptedIssuers;
        }

        public static void allowAllSSL() {
            HttpsURLConnection
                    .setDefaultHostnameVerifier(new HostnameVerifier() {

                        @Override
                        public boolean verify(String arg0, SSLSession arg1) {
                            return true;
                        }

                    });

            SSLContext context = null;
            if (trustManagers == null) {
                trustManagers = new TrustManager[] { new HttpsTrustManager() };
            }

            try {
                context = SSLContext.getInstance("TLS");
                context.init(null, trustManagers, new SecureRandom());
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                LOG.error("Error allowing SSL", e);
            }

            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context != null ? context.getSocketFactory() : null);
        }

    }

}
