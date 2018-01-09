/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 05.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.domain;

import java.io.*;
import java.util.Properties;

import javax.persistence.*;

import org.oscm.app.v2_0.intf.APPlatformController;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.string.Strings;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * Represents an operation for a service instance.
 * 
 * @author Suzana Stavreva
 * 
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "Operation.getFirstOperationFromQueue", query = "SELECT op FROM Operation op WHERE op.serviceInstance.instanceId = :id AND op.forQueue = true"),
        @NamedQuery(name = "Operation.removeAll", query = "DELETE FROM Operation op WHERE op.forQueue = true"),
        @NamedQuery(name = "Operation.getOperationByInstanceId", query = "SELECT op FROM Operation op WHERE op.serviceInstance.instanceId = :id AND op.forQueue = false"),
        @NamedQuery(name = "Operation.removeForKey", query = "DELETE FROM Operation op WHERE op.tkey =:key") })
public class Operation {
    private static final Log4jLogger LOGGER = LoggerFactory
            .getLogger(Operation.class);

    /**
     * The technical key of the entity.
     */
    @Column(nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "do_seq")
    @TableGenerator(table = "hibernate_sequences", name = "do_seq", allocationSize = 1000, valueColumnName = "sequence_next_hi_value")
    private long tkey;

    /**
     * Reference to the service instance containing this operation.
     */
    @ManyToOne(optional = false)
    private ServiceInstance serviceInstance;

    /**
     * The id of the operation.
     */
    @Column(nullable = false)
    private String operationId;

    /**
     * The parameters of the operation.
     */
    @Column(nullable = true)
    private String parameters;

    /**
     * The calling user of the operation.
     */
    @Column(nullable = false)
    private String userId;

    /**
     * The transaction id of the operation.
     */
    @Column(nullable = true)
    private String transactionId;

    /**
     * Is the operation is for queue
     */
    @Column(nullable = false)
    private boolean forQueue;

    /**
     * @return the forQueue
     */
    public boolean isForQueue() {
        return forQueue;
    }

    /**
     * @param forQueue
     *            the forQueue to set
     */
    public void setForQueue(boolean forQueue) {
        this.forQueue = forQueue;
    }

    public long getTkey() {
        return tkey;
    }

    public void setTkey(long tkey) {
        this.tkey = tkey;
    }

    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setFromProperties(Properties properties) {
        if (!properties.isEmpty()) {
            setOperationId((String) properties
                    .get(APPlatformController.KEY_OPERATION_ID));
            properties.remove(APPlatformController.KEY_OPERATION_ID);
            setUserId((String) properties
                    .get(APPlatformController.KEY_OPERATION_USER_ID));
            properties.remove(APPlatformController.KEY_OPERATION_USER_ID);
            if (!properties.isEmpty()) {
                setParameters(convertPropertiesToXML(properties));
            }
        }
    }

    public Properties getParametersAsProperties() {
        String params = getParameters();
        if (Strings.isEmpty(params)) {
            return new Properties();
        }
        return convertXMLToProperties(params);
    }

    String convertPropertiesToXML(Properties properties) {
        String xmlString = null;
        try (OutputStream out = new ByteArrayOutputStream()) {
            properties.storeToXML(out, null, "UTF-8");
            xmlString = out.toString();
        } catch (IOException e) {
            LOGGER.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR);
        }
        return xmlString;
    }

    Properties convertXMLToProperties(String xmlString) {
        Properties properties = new Properties();
        try (InputStream in = new ByteArrayInputStream(xmlString.getBytes())) {
            properties.loadFromXML(in);
        } catch (IOException e) {
            LOGGER.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR);
        }
        return properties;
    }
}
