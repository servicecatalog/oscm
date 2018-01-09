/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: May 9, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.rest.common;

import java.util.Collection;

import javax.ws.rs.WebApplicationException;

/**
 * Generic representation class for collections of representation items
 * 
 * @author miethaner
 */
public class RepresentationCollection<T extends Representation> extends
        Representation {

    private Collection<T> items;

    public Collection<T> getItems() {
        return items;
    }

    public void setItems(Collection<T> items) {
        this.items = items;
    }

    /**
     * Creates a new representative collection
     */
    public RepresentationCollection() {
        super(null);
    }

    /**
     * Creates a new representative collection of items
     * 
     * @param items
     *            the representation items
     */
    public RepresentationCollection(Collection<T> items) {
        super(null);
        this.items = items;
    }

    @Override
    public void validateContent() throws WebApplicationException {

        if (items != null) {
            for (T item : items) {
                item.validateContent();
            }
        }
    }

    @Override
    public void setVersion(Integer version) {
        super.setVersion(version);

        for (T item : items) {
            item.setVersion(version);
        }
    }

    @Override
    public void update() {

        for (T item : items) {
            item.update();
        }
    }

    @Override
    public void convert() {

        for (T item : items) {
            item.convert();
        }
    }

}
