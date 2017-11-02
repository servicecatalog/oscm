/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 18.02.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.j2ep;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import net.sf.j2ep.rules.BaseRule;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.ui.common.Constants;

/**
 * J2EP Rule which store the structure of a processed URI in a local variable
 * and uses this structure to perform the revert operation e.g.:
 * 
 * Rewriting URI: /opt/ff00/example.do >> /example-context/example.do
 * 
 * Reverting URI: /example-context/img/logo.gif >> /opt/ff00/img/logo.gif
 * 
 */
public class AdmRule extends BaseRule {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(AdmRule.class);

    private static final Pattern matchPattern = Pattern
            .compile("^/opt(/[^/]*)(/.*)?");
    private String rewriteTo = "/example-service$2";

    private Pattern revertPattern = Pattern.compile("^/example-service(/.*)?");
    private static final String revertToTemplate = "/opt$1";

    private String revertTo;

    public static Pattern getMatchPattern() {
        return matchPattern;
    }

    /**
     * (non-Javadoc)
     * 
     * @see net.sf.j2ep.model.Rule#matches(javax.servlet.http.HttpServletRequest)
     */
    public boolean matches(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String uri = request.getRequestURI().substring(contextPath.length());
        Matcher matcher = matchPattern.matcher(uri);
        return matcher.matches();
    }

    /**
     * @see net.sf.j2ep.model.Rule#process(java.lang.String)
     */
    public String process(String uri) {
        Matcher matcher = matchPattern.matcher(uri);
        String rewritten = matcher.replaceAll(rewriteTo);
        revertTo = matcher.replaceAll(revertToTemplate) + "$1";
        if (logger.isDebugLoggingEnabled()) {
            logger.logDebug("Rewriting URI: " + uri + " >> " + rewritten);
        }
        return rewritten;
    }

    /**
     * @see net.sf.j2ep.model.Rule#revert(java.lang.String)
     */
    public String revert(String uri) {

        String reverted = uri;
        if (revertTo != null && !Constants.SERVICE_LOGOUT_URI.equals(uri)) {
            Matcher matcher = revertPattern.matcher(uri);
            reverted = matcher.replaceAll(revertTo);
            if (logger.isDebugLoggingEnabled()) {
                logger.logDebug("Reverting URI: " + uri + " >> " + reverted);
            }
        }
        return reverted;
    }

    /**
     * Called from the server after the instantiation of a new rule.
     * 
     * @param rewriteTo
     *            the rewrite string containing the target context path of the
     *            subscription.
     */
    public void setRewriteTo(String rewriteTo) {
        this.rewriteTo = rewriteTo;
    }

    /**
     * Called from the server after the instantiation of a new rule.
     * 
     * @param revertPattern
     *            the revert pattern containing the target context path of the
     *            subscription.
     */
    public void setRevertPattern(Pattern revertPattern) {
        this.revertPattern = revertPattern;
    }

}
