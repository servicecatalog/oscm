/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Florian Walker 
 *                                                                              
 *  Creation Date: 13.09.2011                                                      
 *                                                                              
 *  Completion Time: 13.09.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;

import org.oscm.converter.LocaleHandler;
import org.oscm.converter.ResourceLoader;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * This class enables the access to the default UI message properties stored in
 * Messages_XX.properties files in the resources folder and the exception
 * messages stored in ExceptionMessages_XX.properties. Both files are merged.
 * The UI messages overwrite the exception messages. The implementation is
 * similar to the DbMessages class which is used as default resource bundle for
 * messages. A difference in compare to the DbMessages is the missing
 * "default handling". So if there is no resource available for a key
 * <code>null</code> will be returned instead of the value in the default
 * language.
 * 
 * @author Florian Walker
 * 
 */
public class DefaultMessages extends ResourceBundle {
    private static final Locale FALLBACK_LOCALE = Locale.ENGLISH;
    private PropertyResourceBundle uiProps;
    private PropertyResourceBundle exceptionProps;

    DefaultMessages(Locale locale) throws IOException {
        super();
        loadUiMessages(locale);
        loadExceptionMessages(locale);
    }

    void loadUiMessages(Locale locale) throws IOException {
        InputStream in = null;
        try {
            in = openUiMessages(locale);
            uiProps = new PropertyResourceBundle(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Open input stream for the UI messages properties file
     */
    static InputStream openUiMessages(Locale locale) {
        if (LocaleHandler.isStandardLanguage(locale)) {
            InputStream in = openResource(uiMessagesPath(locale));
            if (in != null) {
                return in;
            }
        }
        return openResource(uiMessagesPath(FALLBACK_LOCALE));
    }

    /**
     * Returns the path to the UI properties for the given locale
     */
    static String uiMessagesPath(Locale locale) {
        return DbMessages.class.getPackage().getName().replaceAll("\\.", "/")
                + "/Messages_" + locale.toString() + ".properties";
    }

    void loadExceptionMessages(Locale locale) throws IOException {
        InputStream in = null;
        try {
            in = openExceptionMessages(locale);
            exceptionProps = new PropertyResourceBundle(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Open input stream for the exception messages properties file
     */
    static InputStream openExceptionMessages(Locale locale) {
        if (LocaleHandler.isStandardLanguage(locale)) {
            InputStream in = openResource(exceptionMessagesPath(locale));
            if (in != null) {
                return in;
            }
        }
        return openResource(exceptionMessagesPath(FALLBACK_LOCALE));
    }

    /**
     * Returns the path to the exception messages for the given locale
     */
    static String exceptionMessagesPath(Locale locale) {
        return "ExceptionMessages_" + locale.toString() + ".properties";
    }

    static InputStream openResource(String resource) {
        try {
            return ResourceLoader.getResourceAsStream(DbMessages.class,
                    resource);
        } catch (SaaSSystemException e) {
            return null;
        }
    }

    /**
     * Implementation of ResourceBundle.getKeys.
     */
    @Override
    public Enumeration<String> getKeys() {
        Set<String> keys = uiProps.keySet();
        keys.addAll(exceptionProps.keySet());
        final Iterator<String> i = keys.iterator();
        return new Enumeration<String>() {

            @Override
            public boolean hasMoreElements() {
                return i.hasNext();
            }

            @Override
            public String nextElement() {
                return i.next();
            }
        };
    }

    @Override
    public Object handleGetObject(String key) {
        Object value = uiProps.handleGetObject(key);
        if (value != null) {
            return value;
        }
        return exceptionProps.handleGetObject(key);
    }

}
