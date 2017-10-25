/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2015-01-15                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.pricemodel.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a list of price models described by their metadata.
 */
public class PriceModelList implements Serializable {

    private static final long serialVersionUID = -2680222051153784929L;

    private List<String> metadataCategories = new ArrayList<String>();
    private List<PriceModelMetadata> metadata = new ArrayList<PriceModelMetadata>();

    /**
     * Returns the available metadata categories.
     * 
     * @return the categories
     */
    public List<String> getMetadataCategories() {
        return metadataCategories;
    }

    /**
     * Sets the metadata categories.
     * 
     * @param metadataCategories
     *            the categories
     */
    public void setMetadataCategories(List<String> metadataCategories) {
        this.metadataCategories = metadataCategories;
    }

    /**
     * Returns the metadata.
     * 
     * @return the metadata
     */
    public List<PriceModelMetadata> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata.
     * 
     * @param metadata
     *            the metadata
     */
    public void setMetadata(List<PriceModelMetadata> metadata) {
        this.metadata = metadata;
    }
}
