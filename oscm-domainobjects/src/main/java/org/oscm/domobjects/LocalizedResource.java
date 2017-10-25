/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 30.04.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.*;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.converters.LOTConverter;

/**
 * Represents one concrete localized string within the product.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "LocalizedResource.findByBusinessKey", query = "SELECT lr FROM LocalizedResource lr WHERE lr.objectKey = :objectKey AND lr.locale = :locale AND lr.objectType = :objectType"),
        @NamedQuery(name = "LocalizedResource.getAllForCurrKey", query = "SELECT lr FROM LocalizedResource lr WHERE locale = :locale AND objectType IN (:objectTypes) AND objectKey = :objectKey"),
        @NamedQuery(name = "LocalizedResource.getAllForCurrAndParentKey", query = "SELECT lr FROM LocalizedResource lr WHERE locale = :locale AND objectType IN (:objectTypes) AND objectKey IN (:objectKey, :objectKeyParent)"),
        @NamedQuery(name = "LocalizedResource.getForCurrAndParentKey", query = "SELECT lr FROM LocalizedResource lr WHERE locale = :locale AND objectType = :objectType AND objectKey IN (:objectKeyChild, :objectKeyParent)"),
        @NamedQuery(name = "LocalizedResource.getAllTextsWithLocale", query = "SELECT lr FROM LocalizedResource lr WHERE objectKey = :objectKey AND  objectType = :objectType"),
        @NamedQuery(name = "LocalizedResource.deleteForObjectAndType", query = "DELETE FROM LocalizedResource WHERE objectKey = :objectKey AND objectType = :objectType"),
        @NamedQuery(name = "LocalizedResource.deleteForObjectAndTypeAndLocale", query = "DELETE FROM LocalizedResource WHERE objectKey = :objectKey AND objectType = :objectType AND locale = :locale"),
        @NamedQuery(name = "LocalizedResource.getAll", query = "SELECT lr FROM LocalizedResource lr WHERE locale IN (:locales) AND objectType IN (:objectTypes) AND objectKey IN (:objectKeys)") })
@BusinessKey(attributes = { "objectKey", "locale", "objectType" })
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "objectKey",
        "locale", "objectType" }))
public class LocalizedResource extends
        DomainObjectWithVersioning<EmptyDataContainer> {

    private static final long serialVersionUID = 8046872629898264735L;

    private String locale;
    private long objectKey;
    @Enumerated(EnumType.STRING)
    private LocalizedObjectTypes objectType;

    @Column(nullable = false)
    private String value;

    public LocalizedResource() {
        super();
    }

    public LocalizedResource(String locale, long objectKey,
            LocalizedObjectTypes objectType) {
        super();
        this.locale = locale;
        this.objectKey = objectKey;
        this.objectType = objectType;
    }

    public String getLocale() {
        return locale;
    }

    @Column(nullable = false)
    public String getValue() {
        return value;
    }

    public long getObjectKey() {
        return objectKey;
    }

    public LocalizedObjectTypes getObjectType() {
        return objectType;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setObjectKey(long objectKey) {
        this.objectKey = objectKey;
    }

    public void setObjectType(LocalizedObjectTypes objectType) {
        this.objectType = objectType;
    }

}
