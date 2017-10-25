/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-12-16                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.oscm.internal.types.exception.DeletionConstraintException;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;

/**
 * Bean for JAX-WS exception serialization, specific for
 * {@link DeletionConstraintException}.
 * 
 */
@XmlRootElement(name = "DeletionConstraintExceptionBean")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class DeletionConstraintExceptionBean extends DomainObjectExceptionBean {

    private static final long serialVersionUID = -6619684098555143734L;

    private ClassEnum dependentClassEnum;

    /**
     * Default constructor.
     */
    public DeletionConstraintExceptionBean() {
        super();
    }

    /**
     * Instantiates a <code>DeletionConstraintExceptionBean</code> based on the
     * specified <code>DomainObjectExceptionBean</code> and sets the given
     * object type.
     * 
     * @param sup
     *            the <code>DomainObjectExceptionBean</code> to use as the base
     * @param depClassEnum
     *            the type of an object that depends on the object to delete and
     *            prevents the deletion
     */
    public DeletionConstraintExceptionBean(DomainObjectExceptionBean sup,
            ClassEnum depClassEnum) {
        super(sup, sup.getClassEnum());
        setDependentClassEnum(depClassEnum);
    }

    /**
     * Returns the type of an object that depends on the object to delete and
     * prevents the operation.
     * 
     * @return the type of the dependent object
     */
    public ClassEnum getDependentClassEnum() {
        return dependentClassEnum;
    }

    /**
     * Sets the type of an object that depends on the object to delete and
     * prevents the operation.
     * 
     * @param dependentClassEnum
     *            the type of the dependent object
     */
    public void setDependentClassEnum(ClassEnum dependentClassEnum) {
        this.dependentClassEnum = dependentClassEnum;
    }

}
