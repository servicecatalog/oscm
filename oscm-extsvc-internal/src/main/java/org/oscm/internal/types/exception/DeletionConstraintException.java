/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-02-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import javax.ejb.ApplicationException;

import org.oscm.internal.types.exception.beans.DeletionConstraintExceptionBean;

/**
 * Exception thrown when the deletion of an object fails due to the violation of
 * specific business rules.
 */
@ApplicationException(rollback = true)
public class DeletionConstraintException extends DomainObjectException {

    private static final long serialVersionUID = 2482782574158410011L;

    private DeletionConstraintExceptionBean bean = new DeletionConstraintExceptionBean();

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public DeletionConstraintException() {
        super();
    }

    /**
     * Constructs a new exception with a pre-defined detail message that
     * includes the given parameter values. This constructor is used when an
     * object cannot be deleted because other objects depend on it.
     * 
     * @param classEnum
     *            a <code>classEnum</code> specifying the type of the object
     *            which cannot be deleted
     * @param businessKey
     *            the business key of the object which cannot be deleted
     * @param dependentClassEnum
     *            a <code>classEnum</code> specifying the type of the object
     *            which depends on the object to delete and prevents the
     *            operation
     */
    public DeletionConstraintException(ClassEnum classEnum, String businessKey,
            ClassEnum dependentClassEnum) {
        super(generateMessage(classEnum, dependentClassEnum), classEnum,
                businessKey);
        setDependentDomClass(dependentClassEnum);
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public DeletionConstraintException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and bean for
     * JAX-WS exception serialization.
     * 
     * @param message
     *            the detail message
     * @param bean
     *            the bean for JAX-WS exception serialization
     */
    public DeletionConstraintException(String message,
            DeletionConstraintExceptionBean bean) {
        super(message, bean);
        this.bean = bean;
    }

    /**
     * Constructs a new exception with the specified detail message, cause, and
     * bean for JAX-WS exception serialization.
     * 
     * @param message
     *            the detail message
     * @param bean
     *            the bean for JAX-WS exception serialization
     * @param cause
     *            the cause
     */
    public DeletionConstraintException(String message,
            DeletionConstraintExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
        this.bean = bean;
    }

    private static String generateMessage(ClassEnum domClassEnum,
            ClassEnum dependentDomClassEnum) {
        return "Cannot delete an object of class '" + domClassEnum.toString()
                + "' as long as it uses an object in the class '"
                + dependentDomClassEnum.toString() + "'";
    }

    /**
     * Returns the type of an object that depends on the object to delete and
     * prevents the operation.
     * 
     * @return the type of the dependent object
     */
    public ClassEnum getDependentDomClass() {
        return bean.getDependentClassEnum();
    }

    /**
     * Sets the type of an object that depends on the object to delete and
     * prevents the operation.
     * 
     * @param dependentClassEnum
     *            the type of the dependent object
     */
    public void setDependentDomClass(ClassEnum dependentClassEnum) {
        bean.setDependentClassEnum(dependentClassEnum);
    }

    /* javadoc will be copied from super class */
    @Override
    public DeletionConstraintExceptionBean getFaultInfo() {
        return new DeletionConstraintExceptionBean(super.getFaultInfo(),
                bean.getDependentClassEnum());
    }
}
