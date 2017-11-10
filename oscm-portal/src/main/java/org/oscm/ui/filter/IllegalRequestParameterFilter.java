/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-05-13                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;

/**
 * HTTP request filter implementation escaping illegal strings in request
 * parameters and parameter values.
 * <p/>
 * 
 * @author goebel
 */
public class IllegalRequestParameterFilter implements Filter {

    protected Pattern[] ignorePatterns;
    protected Map<String, String> replaceMap;
    private String excludeUrlPattern;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(IllegalRequestParameterFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        excludeUrlPattern = filterConfig
                .getInitParameter("exclude-url-pattern");

        String[] ignoredPatterns = toStrings(filterConfig
                .getInitParameter("ignore-patterns"));

        ignorePatterns = toPatterns(ignoredPatterns);

        final String[] forbidden = toStrings(filterConfig
                .getInitParameter("forbidden-patterns"));

        this.replaceMap = getReplacementMap(forbidden);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (hasToBeFiltered(httpRequest)) {

            RequestWithCleanParameters cleanRequest = new RequestWithCleanParameters(
                    httpRequest, ignorePatterns);

            cleanRequest.escapeAll(replaceMap);

            chain.doFilter(cleanRequest, httpResponse);

        } else {
            chain.doFilter(httpRequest, httpResponse);
        }

    }

    boolean hasToBeFiltered(HttpServletRequest request) {

        boolean toExclude = request.getServletPath().matches(excludeUrlPattern);
        return (request.getQueryString() != null
                && request.getQueryString().length() > 0 && !toExclude);
    }

    Pattern[] toPatterns(String[] regexStrings) {
        if (regexStrings == null)
            return new Pattern[0];
        List<Pattern> patterns = new ArrayList<Pattern>();
        for (String pattern : regexStrings) {
            try {
                patterns.add(Pattern.compile(pattern));
            } catch (PatternSyntaxException pse) {
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(bs);
                try {
                    pse.printStackTrace(ps);
                    logger.logDebug(String.format("%s: %s", this.getClass()
                            .getName(), bs.toString()));
                } finally {
                    ps.close();
                }
            }
        }
        return patterns.toArray(new Pattern[patterns.size()]);
    }

    private String[] toStrings(String strings) {
        if (strings == null)
            return new String[0];

        return strings.split(",");
    }

    private Map<String, String> getReplacementMap(String[] forbidden) {

        Map<String, String> replacements = new LinkedHashMap<>();
        for (String pattern : forbidden) {
            replacements.put(pattern, "");
        }
        return replacements;
    }

    @Override
    public void destroy() {

    }

}
