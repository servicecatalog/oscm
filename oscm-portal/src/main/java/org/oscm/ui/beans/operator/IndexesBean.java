/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                     
 *                                                                              
 *  Creation Date: 31.01.2011                                                      
 *                                                                              
 *  Completion Time: <date>                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.beans.operator;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.oscm.internal.intf.SearchService;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.JSFUtils;

/**
 * Bean for managing currencies settings.
 * 
 * @author tokoda
 * 
 */
@ViewScoped
@ManagedBean(name = "indexesBean")
public class IndexesBean extends BaseOperatorBean implements Serializable {

    private static final long serialVersionUID = 4160936328150199908L;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(OperatorOrgBean.class);

    @EJB
    SearchService search;

    public String recreate() {

        try {
            search.initIndexForFulltextSearch(true);

            JSFUtils.addMessage(null, FacesMessage.SEVERITY_INFO,
                    BaseBean.INFO_RECREATE_INDEXES, null);

        } catch (Exception e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_INVALID_JSON);
        }

        return BaseBean.OUTCOME_SUCCESS;
    }
}
