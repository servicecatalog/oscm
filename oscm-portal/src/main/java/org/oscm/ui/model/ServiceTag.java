/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: goebel                                                      
 *                                                                              
 *  Creation Date: 2.5.2011                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.model;

/**
 * This class represents a service tag in the GUI model. *
 * 
 * @author goebel
 * 
 */
public class ServiceTag {
    private String name = "";
    private String language;
    private String link = "";
    private double score = 1.0;
    private double weight = 0.0;

    /**
     * Constructor with localized name and language.
     */
    public ServiceTag(String tagName, String tagLanguage, long tagScore) {
        language = tagLanguage;
        name = tagName;
        score = tagScore;
    }

    /**
     * Returns the localized name of the tag.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the language of the given tag.
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Return the computed service link associated with the given tag.
     * 
     * @return the computed link.
     */
    public String getLink() {
        return link;
    }

    /**
     * Update the service link for the given tag.
     */
    public void updateLink(String url) {
        link = url;
    }

    /**
     * Set score value for the given tag.
     */
    public void setScore(double val) {
        score = val;
    }

    /**
     * Get score value of the given tag.
     */
    public double getScore() {
        return score;
    }

    /**
     * Get computed weight of the given tag.
     * 
     * @return weight of the given tag.
     */
    public int getWeight() {
        return (int) Math.ceil(weight);
    }

    /**
     *      
     */
    public void setWeight(double weightVal) {
        weight = weightVal;
    }
}
