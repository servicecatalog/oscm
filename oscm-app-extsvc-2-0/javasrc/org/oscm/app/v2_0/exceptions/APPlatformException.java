/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  Creation Date: 2012-08-21                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.exceptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.oscm.app.v2_0.data.LocalizedText;
import org.oscm.app.v2_0.data.Setting;

/**
 * Exception thrown when a problem occurs in accessing APP.
 */
public class APPlatformException extends Exception {

    private static final long serialVersionUID = 4919058103515796142L;

    private static final String DEFAULT_LOCALE = "en";

    private List<LocalizedText> messages;

    private HashMap<String, Setting> parameters;

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public APPlatformException(String message) {
        super(message);
        this.messages = new ArrayList<>();
        this.messages.add(new LocalizedText(DEFAULT_LOCALE, message));
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause
     */
    public APPlatformException(String message, Throwable cause) {
        super(message, cause);
        this.messages = new ArrayList<>();
        this.messages.add(new LocalizedText(DEFAULT_LOCALE, message));
    }

    /**
     * Constructs a new exception with the specified localized text messages.
     * The cause is not initialized.
     * 
     * @param messages
     *            the localized text messages
     */
    public APPlatformException(List<LocalizedText> messages) {
        super(getDefaultOrFirst(messages));
        this.messages = messages;
    }

    /**
     * Constructs a new exception with the specified localized text messages and
     * cause.
     * 
     * @param messages
     *            the localized text messages
     * @param cause
     *            the cause
     */
    public APPlatformException(List<LocalizedText> messages, Throwable cause) {
        super(getDefaultOrFirst(messages), cause);
        this.messages = messages;
    }

    /**
     * Returns the localized text messages of the exception.
     * 
     * @return the localized text messages
     */
    public List<LocalizedText> getLocalizedMessages() {
        return messages;
    }

    /**
     * Returns the error message in the specified language or, if this is not
     * found, in the default language.
     * 
     * @param locale
     *            the language. Specify a language code as returned by
     *            <code>getLanguage()</code> of <code>java.util.Locale</code>.
     * @return the localized message
     */
    public String getLocalizedMessage(String locale) {
        // Search text for requested locale
        for (LocalizedText loc : messages) {
            if (loc.getLocale().equals(locale)) {
                return loc.getText();
            }
        }

        // Not found => try default locale
        if (!DEFAULT_LOCALE.equals(locale))
            return getLocalizedMessage(DEFAULT_LOCALE);

        // Return first one
        return getMessage();
    }

    /**
     * Returns the parameters and settings of the application instance that are
     * to be persisted as instance information by APP.
     * 
     * @return the parameters and settings to store, consisting of a key and a
     *         value each
     */
    public HashMap<String, Setting> getChangedParameters() {
        return parameters;
    }

    /**
     * Sets the parameters and settings of the application instance that are to
     * be persisted as instance information by APP.
     * 
     * @param parameters
     *            the parameters and settings to store, consisting of a key and
     *            a value each
     */
    public void setChangedParameters(HashMap<String, Setting> parameters) {
        this.parameters = parameters;
    }

    private static String getDefaultOrFirst(List<LocalizedText> list) {
        String result = null;
        if (list != null) {
            for (LocalizedText text : list) {
                if (DEFAULT_LOCALE.equals(text.getLocale())) {
                    return text.getText();
                } else if (result == null) {
                    result = text.getText();
                }
            }
        }
        return result;
    }
}
