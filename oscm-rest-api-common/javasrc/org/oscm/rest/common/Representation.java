/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: May 12, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.rest.common;

import javax.ws.rs.WebApplicationException;

import org.oscm.internal.vo.BaseVO;

/**
 * Base class for all representations
 * 
 * @author miethaner
 */
public class Representation {

    private transient Integer version;
    private Long etag;
    private Long id;

    /**
     * Creates new representation
     */
    public Representation() {
    }

    public Representation(BaseVO vo) {
        if (vo == null) {
            return;
        }

        this.id = new Long(vo.getKey());
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Long getETag() {
        return etag;
    }

    public void setETag(Long tag) {
        this.etag = tag;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Validates the content and format of the fields to be legitimate. Throws
     * BadRequestException if not valid.
     * 
     * @throws WebApplicationException
     */
    public void validateContent() throws WebApplicationException {}

    /**
     * Updates the fields and format of the internal version to the current one
     */
    public void update() {}

    /**
     * Converts the format and fields of the current version to the internal old
     * one
     */
    public void convert() {}
}
