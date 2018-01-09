/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2014-11-20                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.vmware.ui;

import java.io.InputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.oscm.converter.PropertiesLoader;

/**
 * Managed bean which provides some field settings to the view elements
 * 
 */
public class ApplicationBean implements Serializable {

    private static final Logger logger = LoggerFactory
            .getLogger(ApplicationBean.class);

    private static final long serialVersionUID = -4479522469761297L;
    private String buildId = null;
    private String buildDate = null;

    /**
     * The interval in milliseconds between the previous response and the next
     * request of <a4j:poll> component.
     */
    private Long interval = null;

    /**
     * Read the build id and date from the ear manifest.
     */
    private void initBuildIdAndDate() {
        if (buildId != null) {
            return;
        }
        buildId = "-1";
        buildDate = "";

        // read the implementation version property from the war manifest
        final InputStream in = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getResourceAsStream("/META-INF/MANIFEST.MF");
        String str = null;
        if (in != null) {
            final Properties prop = PropertiesLoader.loadProperties(in);
            str = prop.getProperty("Implementation-Version");
        }

        if (str == null) {
            return;
        }

        // parse the implementation version
        final int sep = str.lastIndexOf("-");
        buildId = str.substring(0, sep);

        SimpleDateFormat inFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat outFormat = new SimpleDateFormat("yyyy/MM/dd");
        try {
            buildDate = outFormat
                    .format(inFormat.parse(str.substring(sep + 1)));
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }

    }

    /**
     * @return the interval of keepAlive tag
     */
    public Long getInterval() {
        if (interval == null) {
            FacesContext ctx = getFacesContext();
            HttpSession httpSession = (HttpSession) ctx.getExternalContext()
                    .getSession(false);
            int maxInactiveInterval = httpSession.getMaxInactiveInterval();
            // To keep session alive, the interval value is 1 minute less than
            // session timeout.
            long intervalValue = (long) maxInactiveInterval * 1000 - 60000L;
            interval = Long.valueOf(intervalValue);
        }
        return interval;
    }

    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    public String getBuildId() {
        initBuildIdAndDate();
        return buildId;
    }

    public String getBuildDate() {
        initBuildIdAndDate();
        return buildDate;
    }

}
