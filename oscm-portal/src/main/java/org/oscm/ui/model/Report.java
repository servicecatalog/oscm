/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: menk                                                      
 *                                                                              
 *  Creation Date: 18.02.2009                                                      
 *                                                                                                                     
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.oscm.internal.vo.VOReport;

/**
 * Wrapper Class for VOReport which holds additional view attributes.
 * 
 */
public class Report {

    private static final String KEY_SESSIONID = "${sessionid}";
    private final VOReport voReport;
    private final String sessionId;

    public Report(VOReport voReport, String sessionId) {
        this.voReport = voReport;
        this.sessionId = sessionId;
    }

    public String getReportName() {
        return voReport.getReportName();
    }

    public String getLocalizedReportName() {
        return voReport.getLocalizedReportName();
    }

    public String getExternalReportURL() {
        String url = voReport.getReportUrlTemplate();
        try {
            url = url.replace(KEY_SESSIONID, URLEncoder.encode(sessionId,
                    "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // Must never happen for UTF-8
            throw new RuntimeException(e);
        }
        return url;
    }

}
