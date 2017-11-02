/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-05-13                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.filter;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * Request wrapper for handling illegal parameter and parameter values.
 * <p/>
 * 
 * @author goebel
 * 
 */
class RequestWithCleanParameters extends HttpServletRequestWrapper {

    private Map<String, String[]> parameters = null;
    private String queryString = null;
    private Pattern[] ignorePatterns;

    static Log4jLogger logger = LoggerFactory
            .getLogger(RequestWithCleanParameters.class);

    /**
     * Constructor.
     */
    RequestWithCleanParameters(HttpServletRequest request,
            Pattern[] ignorePatterns) {
        super(request);
        this.ignorePatterns = ignorePatterns;
        initParameters();
    }

    void initParameters() {

        parameters = new TreeMap<String, String[]>();
        queryString = super.getQueryString();

        final boolean hasNoParameters = super.getParameterMap().isEmpty();
        final boolean hasQueryString = queryString != null
                && queryString.trim().length() > 0;

        if (hasNoParameters) {
            if (hasQueryString) {
                NameValuePair[] pairs = parseParamsFromQueryString(queryString);
                parameters = createNewParameters(pairs);
                queryString = createNewQueryString(pairs);
            }

        } else {
            parameters.putAll(super.getParameterMap());
        }
    }

    Map<String, String[]> createNewParameters(NameValuePair[] params) {
        parameters.clear();
        for (NameValuePair param : params) {
            parameters.put(param.name, new String[] { param.value });
        }
        return parameters;
    }

    String createNewQueryString(NameValuePair[] params) {
        StringBuffer qs = new StringBuffer();
        for (int i = 0; i < params.length; i++) {
            qs.append(params[i].name);
            qs.append("=");
            qs.append(params[i].value);
            if (i < params.length - 1)
                qs.append("&");
        }
        return qs.toString();
    }

    NameValuePair[] parseParamsFromQueryString(String qs) {

        List<NameValuePair> nameValues = new ArrayList<NameValuePair>();
        qs = new QuotedDelimitors().escapeDelimitorChars(qs);
        String[] nameValuePairs = qs.split("&");

        for (int i = 0; i < nameValuePairs.length; i++) {
            String[] pair = nameValuePairs[i].split("=");

            if (pair.length < 1)
                continue;

            NameValuePair nvp;
            if (pair.length < 2) {
                nvp = new NameValuePair(pair[0], "");
            } else {
                nvp = new NameValuePair(pair[0], pair[1]);
            }
            NameValuePair checked = checkParameter(nvp);
            if (checked != null)
                nameValues.add(checked);

        }
        return nameValues.toArray(new NameValuePair[nameValues.size()]);
    }

    NameValuePair checkParameter(NameValuePair pair) {

        try {
            URLDecoder.decode(pair.name, "UTF-8");
        } catch (Exception e) {
            warningIllegalPrameterName(pair.name);
            return null;
        }
        NameValuePair nvp = new NameValuePair(pair.name, pair.value);
        try {
            URLDecoder.decode(pair.value, "UTF-8");
        } catch (Exception e) {
            warningIllegalPrameterValue(pair.name);
            nvp.value = "";
        }
        return nvp;
    }

    private boolean isIgnored(String param) {
        for (Pattern p : ignorePatterns) {
            if (p.matcher(param).matches()) {
                return true;
            }
        }
        return false;
    }

    void escapeAll(Map<String, String> forbiddenToReplacement) {
        Map<String, String[]> paramMap = getModifiableParameterMap();

        for (String forbidden : forbiddenToReplacement.keySet()) {
            Pattern pattern = Pattern.compile(forbidden);

            String[] parameterNames = paramMap.keySet().toArray(
                    new String[paramMap.size()]);

            for (String parmeterName : parameterNames) {

                if (isIgnored(parmeterName)) {
                    continue;
                }
                String[] values = getParameterValues(parmeterName);
                Matcher matcher = pattern.matcher(parmeterName);
                if (matcher.find()) {
                    String newParameterName = matcher
                            .replaceAll(forbiddenToReplacement.get(forbidden));
                    paramMap.remove(parmeterName);
                    paramMap.put(newParameterName, values);

                    warningIllegalPrameterName(newParameterName);
                }

                if (values != null) {
                    for (int i = 0; i < values.length; i++) {
                        matcher = pattern.matcher(values[i]);
                        if (matcher.find()) {
                            values[i] = matcher
                                    .replaceAll(forbiddenToReplacement
                                            .get(forbidden));

                            warningIllegalPrameterValue(values[i]);
                        }
                    }
                }
            }
        }

    }

    private void warningIllegalPrameterName(String parameter) {
        logger.logWarn(Log4jLogger.ACCESS_LOG,
                LogMessageIdentifier.WARN_ILLEGAL_REQUEST_PARAMETER_NAME,
                getRemoteAddr(), parameter);

    }

    private void warningIllegalPrameterValue(String value) {
        logger.logWarn(Log4jLogger.ACCESS_LOG,
                LogMessageIdentifier.WARN_ILLEGAL_REQUEST_PARAMETER_VALUE,
                getRemoteAddr(), value);

    }

    @Override
    public String getParameter(final String name) {
        String[] values = parameters.get(name);
        return values != null ? values[0] : null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(parameters);
    }

    protected Map<String, String[]> getModifiableParameterMap() {
        return parameters;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Enumeration getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(final String name) {
        return parameters.get(name);
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    static class QuotedDelimitors {
        private boolean isQuoted = false;
        String buf ="";
        private int addedChars;

        private String replace(int i, String replacement) {
            int targetIdx = i + addedChars;
            buf = buf.substring(0, targetIdx) + replacement
                    + buf.substring(targetIdx + 1);
            addedChars += (replacement.length() - 1);
            return buf;
        }

        public String escapeDelimitorChars(String str) {
            buf = new String(str);

            for (int i = 0; i < str.length(); i++) {
                buf = visit(str.charAt(i), i);
            }
            return buf;
        }

        String visit(char c, int i) {
            switch (c) {
            case '"':
                isQuoted = !isQuoted;
                break;
            default:
                if (isQuoted) {
                    accept(c, i);
                }
            }
            return buf;
        }

        private void accept(char c, int i) {
            switch (c) {
            case '&':
                buf = replace(i, "%26");
                break;
            case '=':
                buf = replace(i, "%3D");
                break;

            }
        }

    }

    static class NameValuePair {
        NameValuePair(String name, String value) {
            this.name = name;
            this.value = value;
        }

        String value;
        String name;
    }

}
