/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: July 18, 2011                                                      
 *                                                                              
 *  Completion Time: July 19, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects.index;

import java.io.Serializable;

import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Category;
import org.oscm.domobjects.CategoryToCatalogEntry;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.LocalizedResource;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TechnicalProductTag;
import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.domobjects.enums.ModificationType;

/**
 * Message object representing an indexing request triggered by a modification
 * of the underlying domain object.
 * 
 * @author Dirk Bernsau
 * 
 */
public class IndexRequestMessage implements Serializable {

    private static final long serialVersionUID = 3378077291284192770L;

    // store only the class name (cluster scenario)
    private String objectClass;
    private long objectKey;
    private final ModificationType modType;

    /**
     * Generates a message object in case the given object is required for
     * indexing.
     * 
     * @param object
     *            the object that has been modified and should be send to the
     *            indexer
     * @param modTypes
     *            the type of modification
     * @return the respective message object or <code>null</code> if the object
     *         is of no interest for indexing
     */
    public static IndexRequestMessage get(Object object,
            ModificationType modType) {

        if (object instanceof LocalizedResource) {
            IndexRequestMessage message = new IndexRequestMessage(
                    (LocalizedResource) object, modType);
            if (message.getKey() == -1) {
                return null; // resource not of interest
            }
            return message;
        }
        if (object instanceof CatalogEntry) {
            if (ModificationType.DELETE.equals(modType)) {
                // to avoid problems with lazy proxy objects
                // we set the class directly
                return new IndexRequestMessage(
                        ((CatalogEntry) object).getProduct(), Product.class,
                        modType);
            }
            return new IndexRequestMessage((CatalogEntry) object, modType);
        }
        if (object instanceof TechnicalProductTag) {
            if (ModificationType.DELETE.equals(modType)) {
                // to avoid problems with lazy proxy objects
                // we set the class directly
                return new IndexRequestMessage(
                        ((TechnicalProductTag) object).getTechnicalProduct(),
                        TechnicalProduct.class, modType);
            }
            return new IndexRequestMessage((TechnicalProductTag) object,
                    modType);
        }
        if (object instanceof Category) {
            if (ModificationType.DELETE.equals(modType)) {
                // delete is handled by cascading CategoryToCatalogEntry entries
                return null;
            }
            IndexRequestMessage message = new IndexRequestMessage(
                    (Category) object, modType);
            if (message.getKey() <= 0) {
                return null; // category is not yet assigned to a service
            }
            return message;
        }
        if (object instanceof CategoryToCatalogEntry) {
            IndexRequestMessage message = new IndexRequestMessage(
                    ((CategoryToCatalogEntry) object).getCatalogEntry()
                            .getProduct(), Product.class, modType);
            return message;
        }
        if (object instanceof Product) {
            IndexRequestMessage message = new IndexRequestMessage(
                    (Product) object, Product.class, modType);
            return message;
        }
        if (object instanceof Subscription) {
            IndexRequestMessage message = new IndexRequestMessage(
                    (Subscription) object, Subscription.class, modType);
            return message;
        }
        if (object instanceof Uda) {
            IndexRequestMessage message = new IndexRequestMessage((Uda) object,
                    Uda.class, modType);
            return message;
        }
        if (object instanceof UdaDefinition) {
            IndexRequestMessage message = new IndexRequestMessage(
                    (UdaDefinition) object, UdaDefinition.class, modType);
            return message;
        }
        if (object instanceof Parameter) {
            if (ModificationType.DELETE.equals(modType)) {
                // delete is handled by cascading subscription
                return null;
            }
            IndexRequestMessage message = new IndexRequestMessage(
                    (Parameter) object, Parameter.class, modType);
            return message;
        }
        // default handler returning no message
        return null;
    }

    private IndexRequestMessage(DomainObject<?> object, Class<?> clazz,
            ModificationType modType) {
        objectClass = clazz.getName();
        objectKey = object.getKey();
        this.modType = modType;
    }

    private IndexRequestMessage(DomainObject<?> object, ModificationType modType) {
        this(object, object.getClass(), modType);
    }

    private IndexRequestMessage(LocalizedResource resource,
            ModificationType modType) {
        switch (resource.getObjectType()) {
        case PRODUCT_MARKETING_NAME:
            objectClass = Product.class.getName();
            objectKey = resource.getObjectKey();
            break;
        case PRODUCT_MARKETING_DESC:
            objectClass = Product.class.getName();
            objectKey = resource.getObjectKey();
            break;
        case PRODUCT_SHORT_DESCRIPTION:
            objectClass = Product.class.getName();
            objectKey = resource.getObjectKey();
            break;
        case PRICEMODEL_DESCRIPTION:
            objectClass = PriceModel.class.getName();
            objectKey = resource.getObjectKey();
            break;
        case CATEGORY_NAME:
            objectClass = Category.class.getName();
            objectKey = resource.getObjectKey();
            break;
        default:
            // do not set key to signal unimportant type
            objectClass = resource.getClass().getName();
            objectKey = -1;
        }
        this.modType = modType;
    }

    /**
     * Returns the class of the object the message is representing.
     * 
     * @return the class object
     */
    @SuppressWarnings("unchecked")
    public Class<? extends DomainObject<?>> getObjectClass() {
        try {
            Class<?> c = Class.forName(objectClass);
            if (DomainObject.class.isAssignableFrom(c)) {
                return (Class<? extends DomainObject<?>>) c;
            }
            throw new IllegalStateException("Object class " + objectClass
                    + " is not representing a valid domain object");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot resolve object class - "
                    + e.getMessage());
        }
    }

    /**
     * Returns the modification type.
     * 
     * @return the key
     */
    public ModificationType getType() {
        return modType;
    }

    /**
     * Returns the key identifying the object within its object class.
     * 
     * @return the key
     */
    public long getKey() {
        return objectKey;
    }

    @Override
    public String toString() {
        String name = objectClass;
        if (objectClass != null && objectClass.lastIndexOf(".") > 0) {
            name = objectClass.substring(objectClass.lastIndexOf(".") + 1);
        }
        return name + "[" + objectKey + "]";
    }
}
