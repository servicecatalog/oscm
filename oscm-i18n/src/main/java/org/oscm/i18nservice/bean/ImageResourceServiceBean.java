/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                    
 *                                                                              
 *  Creation Date: 06.11.2009                                                      
 *                                                                              
 *  Completion Time: 06.11.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.i18nservice.bean;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ImageResource;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * Bean implementation to retrieve an image from the database.
 * 
 * @author pock
 * 
 */
@Stateless
@Local(ImageResourceServiceLocal.class)
public class ImageResourceServiceBean implements ImageResourceServiceLocal {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(ImageResourceServiceBean.class);

    @EJB(beanInterface = DataService.class)
    private DataService ds;

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void delete(long objectKey, ImageType imageType) {
        
        if (imageType != null) {
            ImageResource template = new ImageResource(objectKey, imageType);
            ImageResource imageResource = (ImageResource) ds.find(template);
            if (imageResource != null) {
                ds.remove(imageResource);
            }
        }
        
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public ImageResource read(long objectKey, ImageType imageType) {
        

        ImageResource template = new ImageResource(objectKey, imageType);
        ImageResource imageResource = (ImageResource) ds.find(template);

        
        return imageResource;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void save(ImageResource imageResource) {
        

        ImageResource template = new ImageResource(
                imageResource.getObjectKey(), imageResource.getImageType());
        ImageResource storedImageResource = (ImageResource) ds.find(template);
        boolean persistFlag = false;
        if (storedImageResource == null) {
            storedImageResource = new ImageResource();
            persistFlag = true;
        }
        storedImageResource.setContentType(imageResource.getContentType());
        storedImageResource.setBuffer(imageResource.getBuffer());
        if (persistFlag) {
            try {
                ds.persist(imageResource);
                ds.flush();
            } catch (NonUniqueBusinessKeyException e) {
                SaaSSystemException sse = new SaaSSystemException(
                        "Image Resource could not be persisted although prior check was performed, "
                                + imageResource, e);
                logger.logError(
                        Log4jLogger.SYSTEM_LOG,
                        sse,
                        LogMessageIdentifier.ERROR_PERSIST_IMAGE_RESOURCE_FAILED_PRIOR_CHECK_PERFORMED,
                        String.valueOf(imageResource));
                throw sse;
            }
        }

        
    }

}
