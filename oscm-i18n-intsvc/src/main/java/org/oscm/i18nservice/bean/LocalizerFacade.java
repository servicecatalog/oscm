/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                     
 *                                                                              
 *  Creation Date: 30.04.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.i18nservice.bean;

import java.util.List;
import java.util.UUID;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.domobjects.LocalizedBillingResource;
import org.oscm.domobjects.enums.LocalizedBillingResourceType;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.LocalizedObjectTypes.InformationSource;
import org.oscm.i18nservice.local.LocalizedDomainObject;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.string.Strings;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * @author Mike J&auml;ger
 * 
 */
public class LocalizerFacade {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(LocalizerFacade.class);

    private LocalizerServiceLocal localizer;
    private String locale;
    private LocalizationCache cache = new LocalizationCache();
    private PerformanceTracker performanceTracker;

    // remember the last used tracker to access it from test code
    private static ThreadLocal<PerformanceTracker> currentTracker;

    public LocalizerFacade(LocalizerServiceLocal localizer, String locale) {
        this.locale = locale;
        this.localizer = localizer;
        this.performanceTracker = PerformanceTracker.newInstance();
        currentTracker = new ThreadLocal<PerformanceTracker>();
        currentTracker.set(performanceTracker);
    }

    public String getText(long objectKey, LocalizedObjectTypes objectType) {

        String result = cache.getText(objectKey, objectType);
        performanceTracker.addStat(objectKey, objectType, result);

        if (result != null) {
            return result;
        }

        if (objectType.getSource() == InformationSource.DATABASE
                || objectType.getSource() == InformationSource.DATABASE_AND_RESOURCE_BUNDLE) {
            result = localizer.getLocalizedTextFromDatabase(getLocale(),
                    objectKey, objectType);
            if (Strings.isEmpty(result)
                    && objectType.getSource() == InformationSource.DATABASE_AND_RESOURCE_BUNDLE) {
                result = localizer.getLocalizedTextFromBundle(objectType, null,
                        getLocale(), objectType.toString() + "." + objectKey);
            }
            cache.put(objectKey, objectType, result);
        } else {
            logger.logWarn(Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.WARN_NON_SUPPORTED_LOCALE,
                    String.valueOf(objectType.getSource()));
        }

        
        return result;
    }

    public String getLocale() {
        return locale;
    }



    /**
     * Preloads all resources for the given domain objects and localization
     * types. The resources are cached and will be returned instantly in
     * subsequent requests.
     * 
     * @param keys
     *            the domain object keys
     * @param types
     *            localization types to be loaded
     */
    public void prefetch(List<Long> keys, List<LocalizedObjectTypes> types) {

        // load
        List<LocalizedDomainObject> localizedResources = localizer
                .getLocalizedTextFromDatabase(locale, keys, types);

        // add to cache
        cache.put(localizedResources);
    }

    /**
     * Returns the last created performance tracker. Intended for test code
     * only.
     * 
     * @return PerformanceTracker
     */
    public static PerformanceTracker getLatestPerformanceStats() {
        return currentTracker.get();
    }

    /**
     * Utility class to identify source code locations with performance
     * problems. A performance problem is assumed if more then 10 SQL queries
     * are executed with the same localizer facade. The performance problem
     * should be removed by prefetching the data in one request.
     * 
     * WARNING: THE CLASS WORKS ONLY SINGLE THREADED. OTHERWISE THE LOGGING
     * SHOULD BE IGNORED.
     * 
     * @author cheld
     * 
     */
    public abstract static class PerformanceTracker {

        public static volatile boolean DEBUG = false;


        abstract void addStat(long objectKey, LocalizedObjectTypes objectType,
                String localizedText);

        public abstract int getCacheMisses();

        public static PerformanceTracker newInstance() {
            if (logger.isDebugLoggingEnabled() || DEBUG) {
                return new DebugTracker();
            } else {
                return NullTracker.INSTANCE;
            }
        }

        private static class DebugTracker extends PerformanceTracker {

            private static final Log4jLogger logger = LoggerFactory
                    .getLogger(PerformanceTracker.class);
            private static final int MISS_THRESHOLD = 10;
            private String traceForLogging;
            private int cacheMisses;
            private String messageForLogging;

            public DebugTracker() {
                StringBuffer sb = createTrace();
                traceForLogging = sb.toString();
                cacheMisses = 0;
                messageForLogging = "";
                System.gc();
            }

            private StringBuffer createTrace() {
                StackTraceElement elements[] = Thread.currentThread()
                        .getStackTrace();
                StringBuffer sb = new StringBuffer();
                sb.append("\nWarning! The localizer is called very often without prefetching. This causes many small SQL queries that might result in slow performance. The performance can be tuned by using the prefetcher from the LocalizerFacade.\n");
                sb.append("\nCaller Stack (this is no exception):\n");
                for (int i = 5; i < elements.length && i < 30; i++) {
                    sb.append(elements[i] + "\n");
                }
                return sb;
            }

            @Override
            protected void finalize() throws Throwable {
                try {
                    if (cacheMisses > MISS_THRESHOLD) {
                        String logging = toString();
                        logger.logDebug(logging);
                        if (DEBUG) {
                            System.out.println(logging);
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                super.finalize();
            }

            public String toString() {
                if (cacheMisses == 0) {
                    return "No cache misses";
                }
                return traceForLogging + "\n\nNumber of cache misses: "
                        + cacheMisses + "\n\n" + messageForLogging;

            }

            void addStat(long objectKey, LocalizedObjectTypes objectType,
                    String localizedText) {
                if (localizedText == null) {
                    cacheMisses++;
                    messageForLogging = messageForLogging
                            + "Cache miss for type " + objectType
                            + " and objectKey " + objectKey + "\n";
                }
            }

            public int getCacheMisses() {
                return cacheMisses;
            }

        }

        private static class NullTracker extends PerformanceTracker {

            public static final NullTracker INSTANCE = new NullTracker();

            void addStat(long objectKey, LocalizedObjectTypes objectType,
                    String localizedText) {

            }

            public int getCacheMisses() {
                return -1;
            }

            public String toString() {
                return "No performance stats tracked";
            }
        }

    }
    
    public LocalizedBillingResource getLocalizedBillingResource(UUID objectID,
            LocalizedBillingResourceType resourceType) {
        return localizer.getLocalizedBillingResource(getLocale(), objectID,
                resourceType);
    }
    
    public LocalizedBillingResource getLocalizedPriceModelResource(UUID objectID) {
        return localizer.getLocalizedPriceModelResource(getLocale(), objectID);
    }

}
