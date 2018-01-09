/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2016/10/06                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.oscm.app.openstack.controller.PropertyHandler;
import org.oscm.app.openstack.controller.ServerStatus;
import org.oscm.app.openstack.data.Server;
import org.oscm.app.openstack.exceptions.HeatException;
import org.oscm.app.openstack.exceptions.NovaException;
import org.oscm.app.openstack.exceptions.OpenStackConnectionException;
import org.oscm.app.openstack.i18n.Messages;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.InstanceNotAliveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tateiwamext
 * 
 */
public class NovaProcessor {
    private final Logger logger = LoggerFactory.getLogger(NovaProcessor.class);

    private OpenStackConnection getConnection(PropertyHandler ph)
            throws APPlatformException, NovaException {
        OpenStackConnection connection = new OpenStackConnection(
                ph.getKeystoneUrl());
        KeystoneClient client = new KeystoneClient(connection);
        try {
            client.authenticate(ph.getUserName(), ph.getPassword(),
                    ph.getDomainName(), ph.getTenantId());
        } catch (OpenStackConnectionException ex) {
            throw new NovaException("Failed to connect to Nova: "
                    + ex.getMessage(), ex.getResponseCode());
        }
        return connection;
    }

    /**
     * Start servers which are in Stack. The stack is identified by its name.
     * 
     * @param ph
     * @throws HeatException
     *             , APPlatformException, NovaException
     */
    public HashMap<String, Boolean> startInstances(PropertyHandler ph)
            throws HeatException, APPlatformException, NovaException {
        OpenStackConnection connection = getConnection(ph);
        List<String> skippedServers = new ArrayList<String>();

        List<String> serverIds = new HeatClient(connection).getServerIds(ph
                .getStackName());

        HashMap<String, Boolean> operationStatuses = new HashMap<String, Boolean>();
        if (serverIds.size() == 0) {
            throw new InstanceNotAliveException(
                    Messages.getAll("error_starting_failed_instance_not_found"));
        }

        NovaClient nc = new NovaClient(connection);
        for (String id : serverIds) {
            Boolean result = Boolean.FALSE;
            try {
                if (nc.isNotServerExceptedStatus(ServerStatus.ACTIVE, ph, id)) {
                    result = nc.startServer(ph, id);
                } else {
                    skippedServers.add(id);
                }
            } catch (OpenStackConnectionException ex) {
                if (ex.getResponseCode() == 401) {
                    logger.info(
                            "Could not start server (Server ID:"
                                    + id
                                    + ") in stack (Stack ID: "
                                    + ph.getStackId()
                                    + ") because of unauthorized. Retry once after authorised.",
                            ex);
                    try {
                        connection = getConnection(ph);
                        nc = new NovaClient(connection);

                        if (nc.isNotServerExceptedStatus(ServerStatus.ACTIVE,
                                ph, id)) {
                            result = nc.startServer(ph, id);
                        } else {
                            skippedServers.add(id);
                        }
                    } catch (OpenStackConnectionException e) {
                        logger.info("Could not start server (Server ID:" + id
                                + ") in stack (Stack ID: " + ph.getStackId()
                                + ")", e);
                        result = Boolean.FALSE;
                    }
                } else {

                    logger.info("Could not start server (Server ID:" + id
                            + ") in stack (Stack ID: " + ph.getStackId() + ")",
                            ex);
                    result = Boolean.FALSE;
                }
            }
            operationStatuses.put(id, result);
        }
        if (skippedServers.size() == serverIds.size()) {
            throw new APPlatformException(
                    Messages.getAll("error_all_servers_active"));
        }
        return operationStatuses;
    }

    /**
     * Stop servers which are in Stack. The stack is identified by its name.
     * 
     * @param ph
     * @return The HashMap of server ID and status of execution
     * @throws HeatException
     * @throws APPlatformException
     * @throws NovaException
     */
    public HashMap<String, Boolean> stopInstances(PropertyHandler ph)
            throws HeatException, APPlatformException, NovaException {
        OpenStackConnection connection = getConnection(ph);
        List<String> skippedServers = new ArrayList<String>();

        List<String> serverIds = new HeatClient(connection).getServerIds(ph
                .getStackName());

        HashMap<String, Boolean> operationStatuses = new HashMap<String, Boolean>();
        if (serverIds.size() == 0) {
            throw new InstanceNotAliveException(
                    Messages.getAll("error_stopping_failed_instance_not_found"));
        }

        NovaClient nc = new NovaClient(connection);
        for (String id : serverIds) {
            Boolean result = Boolean.FALSE;
            try {
                if (nc.isNotServerExceptedStatus(ServerStatus.SHUTOFF, ph, id)) {
                    result = nc.stopServer(ph, id);
                } else {
                    skippedServers.add(id);
                }
            } catch (OpenStackConnectionException ex) {
                if (ex.getResponseCode() == 401) {
                    logger.info(
                            "Could not stop server (Server ID:"
                                    + id
                                    + ") in stack (Stack ID: "
                                    + ph.getStackId()
                                    + ") because of unauthorized. Retry once after authorised.",
                            ex);
                    try {
                        connection = getConnection(ph);
                        nc = new NovaClient(connection);
                        if (nc.isNotServerExceptedStatus(ServerStatus.SHUTOFF,
                                ph, id)) {
                            result = nc.stopServer(ph, id);
                        } else {
                            skippedServers.add(id);
                        }
                    } catch (OpenStackConnectionException e) {
                        logger.info("Could not stop server (Server ID:" + id
                                + ") in stack (Stack ID: " + ph.getStackId()
                                + ")", e);
                        result = Boolean.FALSE;
                    }
                } else {

                    logger.info("Could not stop server (Server ID:" + id
                            + ") in stack (Stack ID: " + ph.getStackId() + ")",
                            ex);
                    result = Boolean.FALSE;
                }
            }
            operationStatuses.put(id, result);
        }
        if (skippedServers.size() == serverIds.size()) {
            throw new APPlatformException(
                    Messages.getAll("error_all_servers_stopped"));
        }
        return operationStatuses;
    }

    /**
     * Get servers information which are in Stack. The stack is identified by
     * its name.
     * 
     * @param ph
     * @return The List of server object
     * @throws HeatException
     * @throws APPlatformException
     * @throws NovaException
     */
    public List<Server> getServersDetails(PropertyHandler ph, boolean moreInfo)
            throws HeatException, APPlatformException, NovaException {
        OpenStackConnection connection = getConnection(ph);

        List<String> serverIds = new HeatClient(connection).getServerIds(ph
                .getStackName());

        List<Server> servers = new ArrayList<Server>();
        if (serverIds.size() == 0) {
            throw new InstanceNotAliveException(Messages.getAll(
                    "error_check_servers_status_failed_instance_not_found",
                    ph.getStackName()));
        }

        NovaClient nc = new NovaClient(connection);
        for (String id : serverIds) {
            Server server = new Server(id);
            try {
                server = nc.getServerDetails(ph, id, moreInfo);
            } catch (OpenStackConnectionException ex) {
                if (ex.getResponseCode() == 401) {
                    logger.info(
                            "NovaClient.getServerDetails() Could not get server status (Server ID:"
                                    + id
                                    + ") in stack (Stack ID: "
                                    + ph.getStackId()
                                    + ") because of unauthorized. Retry once after authorised.",
                            ex);
                    try {
                        connection = getConnection(ph);
                        nc = new NovaClient(connection);
                        server = nc.getServerDetails(ph, id, moreInfo);
                    } catch (OpenStackConnectionException e) {
                        logger.error(
                                "NovaClient.getServerDetails() Could not get server status (Server ID:"
                                        + id + ") in stack (Stack ID: "
                                        + ph.getStackId() + ")", e);
                        server.setName("");
                        server.setStatus(ServerStatus.UNKNOWN.toString());
                    }
                } else {
                    logger.error(
                            "NovaClient.getServerDetails() Could not get server status (Server ID:"
                                    + id + ") in stack (Stack ID: "
                                    + ph.getStackId() + ")", ex);
                    server.setName("");
                    server.setStatus(ServerStatus.UNKNOWN.toString());
                }
            }
            servers.add(server);
        }
        return servers;
    }

}
