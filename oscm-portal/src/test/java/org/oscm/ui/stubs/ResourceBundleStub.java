/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.stubs;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.oscm.ui.common.Constants;

public class ResourceBundleStub extends ResourceBundle {

    private Properties props;

    @Override
    public Locale getLocale() {
        return Locale.ENGLISH;
    }

    @Override
    protected Object handleGetObject(String key) {
        if (key.equals(Constants.BUNDLE_DATE_PATTERN_KEY)) {
            return "ddMMyyyy";
        }
        if (props != null) {
            return props.getProperty(key, "TestComponentLabel");
        }
        return "TestComponentLabel";
    }

    @Override
    public Enumeration<String> getKeys() {
        throw new UnsupportedOperationException();
    }

    public void addResource(String key, String value) {
        if (props == null) {
            props = new Properties();
        }
        props.put(key, value);
    }
}
