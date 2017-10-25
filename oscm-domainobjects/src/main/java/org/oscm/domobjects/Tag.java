/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Oliver Soehnges                                                      
 *                                                                              
 *  Creation Date: 29.04.2011                                                      
 *                                                                              
 *  Completion Time: n/a                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;

/**
 * The class <code>Tag</code> defines a single localized tag.
 * 
 * @author soehnges
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "LOCALE",
        "VALUE" }) })
@NamedQueries({
        @NamedQuery(name = "Tag.findByBusinessKey", query = "select tg from Tag tg where tg.dataContainer.locale = :locale AND tg.dataContainer.value = :value"),
        @NamedQuery(name = "Tag.deleteOrphanedTags", query = "DELETE FROM Tag tg WHERE NOT EXISTS (SELECT rel FROM TechnicalProductTag rel WHERE rel.tag.key = tg.key)"),
        @NamedQuery(name = "Tag.getAllOfLocaleFiltered", query = "SELECT tg FROM Tag tg WHERE tg.dataContainer.locale = :locale AND tg.dataContainer.value like :value order by tg.dataContainer.value") })
@BusinessKey(attributes = { "locale", "value" })
public class Tag extends DomainObjectWithVersioning<TagData> {

    private static final long serialVersionUID = -8769023228715417739L;

    public Tag() {
        super();
        dataContainer = new TagData();
    }

    public Tag(String locale, String value) {
        this();
        setLocale(locale);
        setValue(value);
    }

    /**
     * Refer to {@link TagData#locale}
     */
    public String getLocale() {
        return dataContainer.getLocale();
    }

    /**
     * Refer to {@link TagData#locale}
     */
    public void setLocale(String locale) {
        dataContainer.setLocale(locale);
    }

    /**
     * Refer to {@link TagData#value}
     */
    public String getValue() {
        return dataContainer.getValue();
    }

    /**
     * Refer to {@link TagData#value}
     */
    public void setValue(String value) {
        dataContainer.setValue(value);
    }

}
