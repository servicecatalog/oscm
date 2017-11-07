/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 15.03.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.i18n;

import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ImageResource;
import org.oscm.i18nservice.bean.ImageResourceServiceBean;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

public class ImageResMgmtServiceIT extends EJBTestBase {

    private ImageResourceServiceLocal imageMgmt;
    private DataService ds;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.login("1");
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new ImageResourceServiceBean());

        imageMgmt = container.get(ImageResourceServiceLocal.class);
        ds = container.get(DataService.class);
    }

    @Test
    public void testSave() throws Exception {
        final ImageResource res = initImageResource();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                imageMgmt.save(res);
                return null;
            }
        });
        ImageResource ir = runTX(new Callable<ImageResource>() {
            @Override
            public ImageResource call() throws Exception {
                Query query = ds.createQuery("SELECT ir FROM ImageResource ir");
                return (ImageResource) query.getSingleResult();
            }
        });

        Assert.assertEquals("Wrong content type", "de", ir.getContentType());
        Assert.assertEquals("Wrong image type", ImageType.SERVICE_IMAGE,
                ir.getImageType());
        Assert.assertEquals("Wrong buffer content", "value",
                new String(ir.getBuffer()));
        Assert.assertEquals("Wrong object key", 12, ir.getObjectKey());
    }

    @Test
    public void testDeleteNullArgument() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                imageMgmt.delete(0, null);
                return null;
            }
        });
    }

    @Test
    public void testDeleteNonExisting() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                imageMgmt.delete(0, ImageType.SERVICE_IMAGE);
                return null;
            }
        });
    }

    @Test
    public void testDelete() throws Exception {
        final ImageResource ir = initImageResource();

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                imageMgmt.save(ir);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                imageMgmt.delete(ir.getObjectKey(), ir.getImageType());
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = ds.createQuery("SELECT ir FROM ImageResource ir");
                List<ImageResource> list = ParameterizedTypes
                        .list(query.getResultList(), ImageResource.class);
                Assert.assertEquals("No object must exist anymore", 0,
                        list.size());
                return null;
            }
        });

    }

    @Test
    public void testRead() throws Exception {
        final ImageResource ir = initImageResource();

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                imageMgmt.save(ir);
                return null;
            }
        });
        ImageResource storedObject = runTX(new Callable<ImageResource>() {
            @Override
            public ImageResource call() throws Exception {
                return imageMgmt.read(ir.getObjectKey(), ir.getImageType());
            }
        });

        Assert.assertNotNull(storedObject);
        Assert.assertEquals("Wrong content type", ir.getContentType(),
                storedObject.getContentType());
        Assert.assertEquals("Wrong image type", ir.getImageType(),
                storedObject.getImageType());
        Assert.assertEquals("Wrong buffer content", new String(ir.getBuffer()),
                new String(storedObject.getBuffer()));
        Assert.assertEquals("Wrong object key", ir.getObjectKey(),
                storedObject.getObjectKey());
    }

    @Test
    public void testReadNullInput() throws Exception {
        ImageResource res = runTX(new Callable<ImageResource>() {
            @Override
            public ImageResource call() throws Exception {
                return imageMgmt.read(0, null);
            }
        });

        Assert.assertNull(res);
    }

    /**
     * Initializes and returns an ImageResource object.
     * 
     * @return
     */
    private ImageResource initImageResource() {
        ImageResource res = new ImageResource();
        res.setContentType("de");
        res.setImageType(ImageType.SERVICE_IMAGE);
        res.setBuffer("value".getBytes());
        res.setObjectKey(12L);
        return res;
    }

}
