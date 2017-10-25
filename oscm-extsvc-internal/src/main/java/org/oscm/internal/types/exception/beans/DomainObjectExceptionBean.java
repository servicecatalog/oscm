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

import org.oscm.internal.types.exception.DomainObjectException;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;

/**
 * Bean for JAX-WS exception serialization, specific for
 * {@link DomainObjectException}.
 * 
 */
@XmlRootElement(name = "DomainObjectExceptionBean")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class DomainObjectExceptionBean extends ApplicationExceptionBean {

    private static final long serialVersionUID = -6619684098555143733L;

    private ClassEnum classEnum;

    /**
     * Default constructor.
     */
    public DomainObjectExceptionBean() {
        super();
    }

    /**
     * Instantiates a <code>DomainObjectExceptionBean</code> based on the
     * specified <code>ApplicationExceptionBean</code> and sets the given object
     * type.
     * 
     * @param sup
     *            the <code>ApplicationExceptionBean</code> to use as the base
     * @param classEnum
     *            the type of the object the exception is related to
     */
    public DomainObjectExceptionBean(ApplicationExceptionBean sup,
            ClassEnum classEnum) {
        super(sup);
        setClassEnum(classEnum);
    }

    /**
     * Returns the type of the object the exception is related to.
     * 
     * @return the type
     */
    public ClassEnum getClassEnum() {
        return classEnum;
    }

    /**
     * Sets the type of the object the exception is related to.
     * 
     * @param classEnum
     *            the type
     */
    public void setClassEnum(ClassEnum classEnum) {
        this.classEnum = classEnum;
    }

}
