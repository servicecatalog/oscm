/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.reportingservice.bean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import javax.persistence.Query;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ReportResultCache;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

/**
 * @author weiser
 */
public class ReportingResultCache {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(ReportingResultCache.class);

    private static final long DIFFERENCE = 120 * 1000;

    private ReportingResultCache() {

    }

    /**
     * Puts the result for the provided session id to the cache with its
     * creation time.
     * 
     * @param sessionId
     *            the session id
     * @param timeStamp
     *            the time stamp (creation time of the result)
     * @param report
     *            the result to cache
     */

    public static void put(DataService dm, String cacheKey, long timestamp,
            Object report) {
        removeOldEntries(dm);

        byte[] serializedReport = serializeObject(report);
        if (serializedReport == null) {
            return;
        }
        if (serializedReport.length == 0) {
            return;
        }
        ReportResultCache result = new ReportResultCache();
        result.setReport(serializedReport);
        result.setCachekey(cacheKey);
        result.setTimestamp(new Date(timestamp));
        try {
            dm.persist(result);
        } catch (NonUniqueBusinessKeyException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_REPORT_RESULT_CACHE);
        }
    }

    /**
     * Serializes the object to byte array to prepare it for writing in
     * database.
     * 
     * @param report
     * @return byte array (serialized object)
     */
    public static byte[] serializeObject(Object report) {

        if (report == null) {
            return null;
        }

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = null;

        try {
            objOut = new ObjectOutputStream(byteOut);
            objOut.writeObject(report);
        } catch (IOException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_REPORT_RESULT_CACHE);
            byteOut = new ByteArrayOutputStream();
        } finally {
            try {
                if (objOut != null) {
                    objOut.close();
                }
            } catch (IOException e) {
                logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_REPORT_RESULT_CACHE);
                byteOut = new ByteArrayOutputStream();
            }
        }

        return byteOut.toByteArray();

    }

    /**
     * Gets the cached result or <code>null</code> for the provided session id
     * after removing to old entries comparing to the passed time stamp.
     */
    public static Object get(DataService dm, String cacheKey) {

        ReportResultCache result = new ReportResultCache();
        result.setCachekey(cacheKey);
        result = (ReportResultCache) dm.find(result);
        if (result == null) {
            return null;
        }
        Date timestamp = result.getTimestamp();
        long currentTime = System.currentTimeMillis();
        long value = timestamp.getTime();
        if (currentTime - value > DIFFERENCE) {
            return null;
        }

        return deserializeObject(result.getReport());
    }

    /**
     * De-serializes the object read from the database.
     * 
     * @param bytes
     * @return RDO object
     */
    private static Object deserializeObject(byte[] bytes) {

        if (bytes == null) {
            return null;
        }
        if (bytes.length == 0) {
            return null;
        }

        Object resultObject = null;
        ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
        ObjectInputStream objIn = null;
        try {
            objIn = new ObjectInputStream(byteIn);
            try {
                resultObject = objIn.readObject();
            } catch (ClassNotFoundException e) {
                logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_REPORT_RESULT_CACHE);
            }
        } catch (IOException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_REPORT_RESULT_CACHE);
        } finally {
            try {
                if (objIn != null) {
                    objIn.close();
                }
            } catch (IOException e) {
                logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_REPORT_RESULT_CACHE);
                resultObject = null;
            }
        }

        return resultObject;
    }

    /**
     * Removes entries that are older than {@link #DIFFERENCE}.
     */
    protected static void removeOldEntries(DataService dm) {
        long timeStamp = System.currentTimeMillis();
        Query query = dm.createNamedQuery("ReportResultCache.removeOldEntries");
        long value = timeStamp - DIFFERENCE;
        query.setParameter("timestamp", new Date(value));
        query.executeUpdate();
    }
}
