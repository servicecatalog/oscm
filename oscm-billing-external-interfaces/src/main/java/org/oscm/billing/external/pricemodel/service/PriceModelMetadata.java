/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2015-01-08                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.pricemodel.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Represents a price model described by its metadata. Metadata are represented
 * by categories and their values.
 * 
 */
public class PriceModelMetadata implements Serializable {

    private static final long serialVersionUID = -6166495584165597917L;

    private UUID id;

    private Map<String, String> metadata = new HashMap<String, String>();

    /**
     * Constructs a metadata object for the price model with the given ID.
     * 
     * @param id
     *            the unique identifier of the price model
     */
    public PriceModelMetadata(UUID id) {
        this.id = id;
    }

    /**
     * Returns the ID of the price model described by the metadata object.
     * 
     * @return the identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Sets the ID of the price model described by the metadata object.
     * 
     * @param id
     *            the unique identifier of the price model
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Adds a category and its value to the metadata object.
     * 
     * @param metadataCategory
     *            the category
     * @param metadataValue
     *            the value
     */
    public void put(String metadataCategory, String metadataValue) {
        metadata.put(metadataCategory,
                StringEscapeUtils.unescapeJava(metadataValue));
    }

    /**
     * Returns the value for the given category set in the metadata object.
     * 
     * @param metadataCategory
     *            the category
     * @return the value
     */
    public String get(String metadataCategory) {
        return metadata.get(metadataCategory);
    }

    /**
     * Returns all categories and values set in the metadata object.
     * 
     * @return the metadata
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }
}
