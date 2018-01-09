/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: July 20, 2011                                                      
 *                                                                              
 *  Completion Time: July 20, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects.index;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.LocalizedResource;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.TechnicalProductTag;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.ModificationType;

/**
 * @author Dirk Bernsau
 * 
 */
public class IndexRequestMessageTest {

    @Test
    public void testCatalogEntry() throws Throwable {

        long key = 12;
        CatalogEntry ce = new CatalogEntry();
        ce.setKey(key);

        IndexRequestMessage message = IndexRequestMessage.get(ce,
                ModificationType.MODIFY);
        Assert.assertNotNull("Message expected", message);
        assertEquals(ce.getClass(), message.getObjectClass());
        assertEquals(key, message.getKey());
    }

    @Test
    public void testTechnicalProductTag() throws Throwable {

        long key = 12;
        TechnicalProductTag tag = new TechnicalProductTag();
        tag.setKey(key);

        IndexRequestMessage message = IndexRequestMessage.get(tag,
                ModificationType.MODIFY);
        Assert.assertNotNull("Message expected", message);
        assertEquals(tag.getClass(), message.getObjectClass());
        assertEquals(key, message.getKey());
    }

    @Test
    public void testUdaDefinitionMessage() throws Throwable {

        long key = 12;
        UdaDefinition tag = new UdaDefinition();
        tag.setKey(key);

        IndexRequestMessage message = IndexRequestMessage.get(tag,
                ModificationType.MODIFY);
        Assert.assertNotNull("Message expected", message);
        assertEquals(tag.getClass(), message.getObjectClass());
        assertEquals(key, message.getKey());
    }

    @Test
    public void testLocalizedResource() throws Throwable {

        long key = 124324;
        LocalizedResource r = new LocalizedResource("de", key,
                LocalizedObjectTypes.PRODUCT_MARKETING_NAME);

        IndexRequestMessage message = IndexRequestMessage.get(r,
                ModificationType.MODIFY);
        Assert.assertNotNull("Message expected", message);
        assertEquals(Product.class, message.getObjectClass());
        assertEquals(key, message.getKey());

        r = new LocalizedResource("de", key,
                LocalizedObjectTypes.PRODUCT_MARKETING_DESC);
        message = IndexRequestMessage.get(r, ModificationType.MODIFY);
        Assert.assertNotNull("Message expected", message);
        assertEquals(Product.class, message.getObjectClass());
        assertEquals(key, message.getKey());

        r = new LocalizedResource("en", key,
                LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION);
        message = IndexRequestMessage.get(r, ModificationType.MODIFY);
        Assert.assertNotNull("Message expected", message);
        assertEquals(Product.class, message.getObjectClass());
        assertEquals(key, message.getKey());

        r = new LocalizedResource("ja", key,
                LocalizedObjectTypes.PRICEMODEL_DESCRIPTION);
        message = IndexRequestMessage.get(r, ModificationType.MODIFY);
        Assert.assertNotNull("Message expected", message);
        assertEquals(PriceModel.class, message.getObjectClass());
        assertEquals(key, message.getKey());

        r = new LocalizedResource("es", key,
                LocalizedObjectTypes.PRICEMODEL_LICENSE);
        message = IndexRequestMessage.get(r, ModificationType.MODIFY);
        Assert.assertNull("No message expected", message);

        r = new LocalizedResource("en", key,
                LocalizedObjectTypes.PRODUCT_CUSTOM_TAB_NAME);
        message = IndexRequestMessage.get(r, ModificationType.MODIFY);
        Assert.assertNull("No message expected", message);
    }

    @Test
    public void testNull() throws Throwable {
        IndexRequestMessage message = IndexRequestMessage.get(null,
                ModificationType.MODIFY);
        Assert.assertNull("No message expected", message);
    }

}
