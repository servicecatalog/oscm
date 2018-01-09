/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: Nov 29, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack.data;

import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

/**
 * Build a JSON structure for sending a request to the Heat API to create a
 * stack.
 */
public abstract class AbstractStackRequest {

    private JSONObject request = new JSONObject();

    protected void put(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException(
                    "JSON object key must not be null!");
        }
        if (!(value instanceof String) && !(value instanceof JSONObject)) {
            throw new IllegalArgumentException("Object type "
                    + (value == null ? "NULL" : value.getClass().getName())
                    + " not allowed as JSON value.");
        }
        try {
            request.put(key, value);
        } catch (JSONException e) {
            // this can basically not happen
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public AbstractStackRequest withTemplateUrl(String templateUrl) {
        put("template_url", templateUrl);
        return this;
    }

    public AbstractStackRequest withTemplate(String template) {
        put("template", template);
        return this;
    }

    public AbstractStackRequest withParameters(JSONObject parameters) {
        put("parameters", parameters);
        return this;
    }

    public AbstractStackRequest withParameter(String parameterName,
            String parameterValue) {
        JSONObject parameters = request.optJSONObject("parameters");
        if (parameters == null) {
            parameters = new JSONObject();
            put("parameters", parameters);
        }
        try {
            parameters.put(parameterName, parameterValue);
        } catch (JSONException e) {
            // this can basically not happen
            throw new IllegalArgumentException(e.getMessage());
        }
        return this;
    }

    /**
     * Get the JSON text of this request. For compactness, no whitespace is
     * added. If this would not result in a syntactically correct JSON text,
     * then null will be returned instead.
     * 
     * @return JSON text or null if syntax is not correct
     */
    public String getJSON() {
        return request.toString();
    }
}
