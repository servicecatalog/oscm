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
import java.util.Date;

import javax.persistence.*;

import org.oscm.domobjects.converters.DHOConverter;
import org.oscm.domobjects.enums.ModificationType;

/**
 * DomainHistoryObject is the base class of all history objects. It holds the
 * <ul>
 * <li>artificial primary key of the history entity</li>
 * <li>version number of the domain object version it represents</li>
 * <li>modificationType (ADD,MODIFY,DELETE) which caused its creation</li>
 * <li>user who initiated the change</li>
 * <li>modDate timestamp of the change</li>
 * <li>invocationDate timestamp of the change occurring in the invocation date</li>
 * </ul>
 * Note that the DomainHistoryObject does not hold a JPA-relation to the domain
 * object, but directly stores the primary key of the corresponding object.
 * 
 * @author schmid
 */
@MappedSuperclass
public abstract class DomainHistoryObject<D extends DomainDataContainer>
        implements Serializable {

    private static final long serialVersionUID = 1L;

    public DomainHistoryObject() {
    }

    /**
     * Primary key value of the DomainObjectWithHistory this history object
     * belongs to.
     */
    @Column(nullable = false)
    private long objKey;

    /**
     * Constructs a DomainHistoryObject from a corresponding domain object. The
     * data container of the domain object is referenced, which means that
     * changes later of the domain objects will also be visible inside the
     * history object. Thus the domain object and the history object will
     * contain the same values during transaction commit, regardless when the
     * history object was created.
     * 
     * @param domobj
     *            corresponding domain object (must not be <code>null</code>)
     */
    public DomainHistoryObject(DomainObjectWithHistory<D> domobj) {
        setObjKey(domobj.getKey());
        setDataContainer(domobj.getDataContainer());
        setObjVersion(domobj.getVersion());
    }

    /**
     * Constructs a history object for a domain object without versioning. The
     * version information will not be considered.
     * 
     * @param domobj
     *            The domain object to create the history object or. Must not be
     *            <code>null</code>.
     */
    public DomainHistoryObject(DomainObjectWithoutVersioning<D> domobj) {
        setObjKey(domobj.getKey());
        setDataContainer(domobj.getDataContainer());
        setObjVersion(domobj.getVersion());
    }

    /**
     * Artificial primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "doh_seq")
    @TableGenerator(table = "hibernate_sequences", name = "doh_seq", allocationSize = 1000, valueColumnName = "sequence_next_hi_value")
    @Column(name = "TKEY")
    private long key;

    /**
     * Version number of the referenced domain object
     */
    @Column(nullable = false)
    private long objVersion;

    /**
     * Reason for change (ADD, MODIFY or DELETE of domain object)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModificationType modType;

    /**
     * User that initiated the change
     */
    @Column(nullable = false)
    private String modUser;

    /**
     * Timestamp of the change occurring in the specified invocation date
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date invocationDate;

    /**
     * Timestamp of the change
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date modDate;

    /**
     * Domain object's attributes are held in separate data classes
     * ("data containers"), which are embedded into the domain object class.
     * This allows reuse of these attributes in History objects.
     */
    @Embedded
    protected D dataContainer;

    public void setObjKey(long objid) {
        this.objKey = objid;
    }

    public long getObjKey() {
        return objKey;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long id) {
        this.key = id;
    }

    public void setObjVersion(long objVersion) {
        this.objVersion = objVersion;
    }

    public long getObjVersion() {
        return objVersion;
    }

    public ModificationType getModtype() {
        return modType;
    }

    public void setModtype(ModificationType modtype) {
        this.modType = modtype;
    }

    public String getModuser() {
        return modUser;
    }

    public void setModuser(String moduser) {
        this.modUser = moduser;
    }

    public Date getModdate() {
        return modDate;
    }

    public void setModdate(Date moddate) {
        this.modDate = moddate;
    }

    /**
     * Get the invocation date of a change.
     * 
     * @return the invocation date.
     */
    public Date getInvocationDate() {
        return invocationDate;
    }

    /**
     * Sets the invocationDate field.
     * 
     * @param invocationDate
     *            The invocation date to set.
     */
    public void setInvocationDate(Date invocDate) {
        this.invocationDate = invocDate;
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

}
