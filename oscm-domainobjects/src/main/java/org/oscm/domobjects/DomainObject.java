/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: schmid                                 
 *                                                                              
 *  Creation Date: 21.01.2009                                                      
 *                                                                              
 *  Completion Time:                                     
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.persistence.*;

import org.hibernate.proxy.HibernateProxyHelper;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.oscm.domobjects.enums.LocalizedObjectTypes;

/**
 * DomainObject is the base class for all domain objects stored in the database.
 * It defines an artificial primary key named "key", a version attribute to be
 * used for optimistic locking. oryIn addition the abstract methods
 * "getDataContainer" and "setDataContainer" are introduced: each domain object
 * class has to implement these interfaces, by adding a specialized
 * DomainDataContainer-Object in the class. This DomainDataContainer holds all
 * data fields of the domain object.
 * 
 * @author schmid
 * 
 */
@MappedSuperclass
public abstract class DomainObject<D extends DomainDataContainer> implements
        Serializable {

    private static final long serialVersionUID = 1L;

    private static final List<LocalizedObjectTypes> LOCALIZATION_TYPES = Collections
            .emptyList();

    public DomainObject() {
    }

    /**
     * Domain object's attributes are held in separate data classes
     * ("data containers"), which are embedded into the domain object class.
     * This allows reuse of these attributes in History objects.
     */
    @IndexedEmbedded
    @Embedded
    protected D dataContainer;

    /**
     * Primary Key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "do_seq")
    @TableGenerator(table = "hibernate_sequences", name = "do_seq", allocationSize = 1000, valueColumnName = "sequence_next_hi_value")
    @Column(name = "TKEY")
    // Required to access key without resolving proxy
    private long key;

    /**
     * Time to set for the corresponding history entry as modification date. If
     * not set, the current time will be used instead.
     */
    @Transient
    private Long historyModificationTime;

    public long getKey() {
        return key;
    }

    public void setKey(long id) {
        this.key = id;
    }

    /**
     * Overrides equals-method by comparing the primary key
     */
    @Override
    public final boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (DomainObject.getDomainClass(other) == DomainObject
                .getDomainClass(this)) {
            if (getKey() != 0) {
                return (getKey() == ((DomainObject<?>) other).getKey());
            } else {
                // if the object has not been persisted yet (so the key is 0),
                // we cannot tell if the object is identical to the current one,
                // especially since same values in all fields needn't mean the
                // same object. So return equality of the super class
                return super.equals(other);
            }
        }
        return false;
    }

    /**
     * Returns the domain class of this entity (e.g. Subscription.class).
     * WARNING: Never use the method entity.getClass(), because JPA might create
     * dynamic proxy classes during runtime (byte code injection). Always, use
     * this method instead.
     */
    public static Class<?> getDomainClass(Object entityOrProxy) {
        return HibernateProxyHelper
                .getClassWithoutInitializingProxy(entityOrProxy);
    }

    @Override
    public final int hashCode() {
        final long k = getKey();
        return (int) (k ^ (k >>> 32));
    }

    /**
     * Returns the version for the domain object.
     * 
     * @return
     */
    public abstract int getVersion();

    /**
     * Helper method to obtain the name of the query to check this objects
     * version number. Each DomainObject-class has to provide such a query.
     * 
     * @return name of the query to lookup version number
     */
    public String getVersionCheckQueryString() {
        String className = this.getClass().getName();
        String queryName = className.substring(className.lastIndexOf(".") + 1)
                + ".lookupVersion";
        return queryName;
    }

    /**
     * No history
     */
    public boolean hasHistory() {
        return false;
    }

    /**
     * Get the data container.
     * 
     * @return the data container.
     */
    public D getDataContainer() {
        return dataContainer;
    }

    /**
     * Set the data container.
     * 
     * @param dataContainer
     *            The data container to set.
     */
    @SuppressWarnings("unchecked")
    public void setDataContainer(DomainDataContainer dataContainer) {
        this.dataContainer = (D) dataContainer;
    }

    public Long getHistoryModificationTime() {
        return historyModificationTime;
    }

    public void setHistoryModificationTime(Long historyModificationTime) {
        this.historyModificationTime = historyModificationTime;
    }

    @Override
    public final String toString() {
        String result = String.format("%s [key='%s', version='%s'%s]",
                getDomainClass(this).getSimpleName(), getKey(),
            (long) getVersion(), toStringAttributes());
        return result;
    }

    String toStringAttributes() {
        return "";
    }

    public List<LocalizedObjectTypes> getLocalizedObjectTypes() {
        return LOCALIZATION_TYPES;
    }
}
