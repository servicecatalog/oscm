/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: goebel                                                      
 *                                                                              
 *  Creation Date: 29.4.2011                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.beans.marketplace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;

import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.model.ServiceTag;
import org.oscm.internal.vo.VOTag;

/**
 * This class implements the tag could bean. It provides the list of defined
 * tags their computed weights.
 * 
 * @author goebel
 */
@SessionScoped
@ManagedBean(name="tagCloudBean")
public class TagCloudBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 7531333557248727218L;

    private double maxWeightVal = 21.0;
    private double minWeightVal = 10.0;
    private double minScoreVal = 1.0;
    private int maxTags = 50;

    List<ServiceTag> tagsForPreview;
    List<ServiceTag> tagsForMarketplace;

    private String lastUsedLanguage;
    private String lastUsedMarketplace;

    /**
     * Set the minimum score file.
     * 
     * @param minScore
     *            - the threshold.
     */
    public void setMinumumScore(double minScore) {
        minScoreVal = minScore;
    }

    /**
     * Set lower weight border.
     * 
     * @param minWeight
     *            - lower weight border.
     */
    public void setMinumumWeight(double minWeight) {
        minWeightVal = minWeight;
    }

    /**
     * Set upper weight border.
     * 
     * @param maxWeight
     *            - upper weight border.
     */
    public void setMaxWeight(double maxWeight) {
        maxWeightVal = maxWeight;
    }

    /**
     * Set max number of tags managed by the cloud.
     * 
     * @param maxSize
     *            - max number of tags to manage.
     */
    public void setMaxSize(int maxSize) {
        maxTags = maxSize;
    }

    /**
     * Returns a sorted list of maximum <cod>max</code> weighted tags, which
     * score exceeds the given threshold value (including tags of the default
     * locale).
     * 
     * @return sorted list of of tags.
     * @see ServiceTag
     */
    public List<ServiceTag> getTags() {
        if (languageHasChanged() || marketplaceHasChanged()) {
            tagsForMarketplace = null;
        }

        if (tagsForMarketplace == null) {
            lastUsedLanguage = getUserLanguage();
            lastUsedMarketplace = getMarketplaceId();
            tagsForMarketplace = createTagList(readTags(getUserLanguage(),
                    getMarketplaceId()));
        }
        return tagsForMarketplace;
    }

    private boolean languageHasChanged() {
        if (lastUsedLanguage == null
                || !(lastUsedLanguage.equals(getUserLanguage())))
            return true;
        else
            return false;
    }

    private boolean marketplaceHasChanged() {
        if (lastUsedMarketplace == null
                || !(lastUsedMarketplace.equals(getMarketplaceId())))
            return true;
        else
            return false;
    }

    public void resetTagsForMarketplace() {
        tagsForMarketplace = null;
    }

    /**
     * Returns a sorted list of maximum <cod>max</code> weighted tags, which
     * score exceeds the given threshold value (only tags of the currently
     * defined locale). The tagsForPreview list is lazy loaded by the action
     * event listener which is invoked when the link to open the modal panel is
     * clicked.
     * 
     * @return sorted list of of tags.
     * @see ServiceTag
     */
    public List<ServiceTag> getTagsPreview() {
        if (tagsForPreview == null) {
            return Collections.emptyList();
        } else {
            // tagsForPreview list which was loaded by action listener
            return tagsForPreview;
        }
    }

    /**
     * action event invoked by the click link loads the tags into the
     * tagsForPreview list.
     * 
     * @param ae
     */
    public void reloadTagsForPreview(ActionEvent ae) {
        tagsForPreview = createTagList(readTags(getUserLanguage()));
    }

    /**
     * Returns a sorted list of maximum <cod>max</code> weighted tags, which
     * score exceeds the given threshold value.
     */
    private List<ServiceTag> createTagList(Map<String, ServiceTag> tags) {

        List<ServiceTag> list = new ArrayList<ServiceTag>();
        int cnt = 0;
        for (Iterator<ServiceTag> iter = tags.values().iterator(); iter
                .hasNext();) {
            ServiceTag tag = iter.next();

            // Threshold filter
            if (tag.getScore() < minScoreVal)
                continue;
            list.add(tag);
            if (cnt++ >= maxTags)
                break;
        }

        // Find service tag with highest score
        double biggest = 0.0;
        for (Iterator<ServiceTag> iter = list.iterator(); iter.hasNext();) {
            ServiceTag st = iter.next();
            if (st.getScore() > biggest) {
                biggest = st.getScore();
            }
        }

        // Compute weights
        Iterator<ServiceTag> iter = list.iterator();
        while (iter.hasNext()) {
            ServiceTag st = iter.next();

            // Compute the tag weight from normalized score
            st.setWeight(minWeightVal
                    + ((st.getScore() / biggest) * (maxWeightVal - minWeightVal)));
        }

        return list;
    }

    private Map<String, ServiceTag> readTags(String locale) {
        List<VOTag> voTagList = getTagService().getTagsByLocale(locale);
        Map<String, ServiceTag> tags = mapVoListToModelList(voTagList);
        return tags;
    }

    private Map<String, ServiceTag> readTags(String locale, String marketplaceId) {
        List<VOTag> voTagList = getTagService().getTagsForMarketplace(locale,
                marketplaceId);
        Map<String, ServiceTag> tags = mapVoListToModelList(voTagList);
        return tags;
    }

    private Map<String, ServiceTag> mapVoListToModelList(List<VOTag> voTagList) {
        Map<String, ServiceTag> tags = new LinkedHashMap<String, ServiceTag>();
        // Add all tags to the cloud.
        for (VOTag voTag : voTagList) {
            ServiceTag serviceTag = DEFAULT_VOSERVICE_MAPPER.createModel(voTag);
            tags.put(serviceTag.getName(), serviceTag);
        }
        return tags;
    }

    private static final Vo2ModelMapper<VOTag, ServiceTag> DEFAULT_VOSERVICE_MAPPER = new Vo2ModelMapper<VOTag, ServiceTag>() {
        @Override
        public ServiceTag createModel(final VOTag vo) {
            return new ServiceTag(vo.getValue(), vo.getLocale(),
                    vo.getNumberReferences());
        }
    };
}
