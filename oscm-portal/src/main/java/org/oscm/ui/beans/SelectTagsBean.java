/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Author: soehnges                                                      
 *
 *  Creation Date: 13.03.2010                                                      
 *
 *******************************************************************************/
package org.oscm.ui.beans;

import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.delegates.ServiceLocator;
import org.oscm.internal.intf.TagService;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOUserDetails;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.util.List;

/**
 * Backing bean to select an user to perform operator tasks on it.
 * <p/>
 * <p>
 * <b>Changelog</b>
 * <ol>
 * <li>replaced {@link javax.faces.bean.RequestScoped} with {@link javax.faces.bean.ViewScoped} to prevent
 * recreating bean for each request, event the smallest</li>
 * <li>fix invalid <b>Managedbean</b> annotation</li>
 * <li>reduced bean size by making it single-level class without extending {@link org.oscm.ui.beans.BaseBean}</li>
 * </ol>
 * </p>
 *
 * @author soehnges
 * @author trebskit
 */
@ViewScoped
@ManagedBean(name = "selectTagsBean")
public class SelectTagsBean {
    private final static int MAX_SUGGESTIONS = 100;
    private static final String SEPARATOR = ",";
    private static final String DEFAULT_LOCALE = "en";
    private TagService tagService;

    /**
     * Provides suggestion list for en logged in.
     */
    @SuppressWarnings(value = "unused")
    public List<String> suggest(FacesContext context, UIComponent component, String tag) {
        if (StringUtils.isEmpty(tag) || tag.equals(SEPARATOR)) {
            // no suggestion for empty data (or if separator "," is typed)
            return null;
        }
        try {
            // Get all tags which starts with given input
            final String pattern = tag + "%";
            return this.getTagService()
                    .getTagsByPattern(this.getUserLanguage(), pattern, MAX_SUGGESTIONS);
        } catch (Exception e) {
            ExceptionHandler.execute(new SaaSApplicationException(e));
        }

        return null;
    }

    @EJB
    public void setTagService(final TagService service) {
        this.tagService = service;
    }

    public TagService getTagService() {
        if (this.tagService == null) {
            this.tagService = new ServiceLocator().findService(TagService.class);
        }
        return this.tagService;
    }

    /**
     * Retrieves current language. If {@link javax.faces.context.FacesContext} is not presented
     * default locale {@link #DEFAULT_LOCALE} is returned. Otherwise {@link BaseBean#getUserFromSessionWithoutException(javax.faces.context.FacesContext)}
     * is used to retrieve details of the logged in user and therefore its locale.
     *
     * @return current user language as {@link java.lang.String}
     */
    protected String getUserLanguage() {
        final FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) {
            return DEFAULT_LOCALE;
        }
        final VOUserDetails voUserDetails = BaseBean.getUserFromSessionWithoutException(fc);
        if (voUserDetails == null) {
            return fc.getViewRoot().getLocale().getLanguage();
        }
        return voUserDetails.getLocale();
    }

}
