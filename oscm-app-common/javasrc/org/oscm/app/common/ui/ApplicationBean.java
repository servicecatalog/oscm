/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2014-11-20                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.common.ui;

import java.io.InputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.oscm.converter.PropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managed bean which provides some field settings to the view elements
 * 
 */
@ManagedBean(name = "appBean")
@SessionScoped
public class ApplicationBean implements Serializable {

    private static final Logger logger = LoggerFactory
            .getLogger(ApplicationBean.class);

    private static final long serialVersionUID = -4479522469761297L;
    private String buildId = null;
    private String buildDate = null;

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

    public String getBuildId() {
        initBuildIdAndDate();
        return buildId;
    }

    public String getBuildDate() {
        initBuildIdAndDate();
        return buildDate;
    }

}
