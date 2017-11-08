/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.oscm.encrypter.AESEncrypter;

@SuppressWarnings("unchecked")
public class XMLSerializerTest {
    @Test
    public void toXml() throws Exception {

        AESEncrypter.generateKey();

        // given
        Map<String, String> map = new HashMap<>();
        map.put("a", "a1");
        map.put("b", "b1");

        // when
        String xml = XMLSerializer.toXml(map);
        xml = AESEncrypter.decrypt(xml);

        // then
        assertTrue(xml.contains("a"));
        assertTrue(xml.contains("a1"));
        assertTrue(xml.contains("b"));
        assertTrue(xml.contains("b1"));
    }

    @Test
    public void toObject() throws Exception {

        AESEncrypter.generateKey();

        // given
        Map<String, String> map = new HashMap<>();
        map.put("a", "a1");
        map.put("b", "b1");
        String xml = XMLSerializer.toXml(map);
        
        // when
        Map<String, String> map2 = (Map<String, String>) XMLSerializer
                .toObject(xml);

        // then
        assertEquals(map.size(), map2.size());
        assertTrue(map2.containsKey("a"));
        assertTrue(map2.containsKey("b"));
        assertEquals("a1", map2.get("a"));
        assertEquals("b1", map2.get("b"));
    }
    
    @Test
    public void toObjectUnencrypted() throws Exception {

        AESEncrypter.generateKey();

        // given
        Map<String, String> map = new HashMap<>();
        map.put("a", "a1");
        map.put("b", "b1");
        String xml = XMLSerializer.toXml(map);
        
        String decrypted = AESEncrypter.decrypt(xml);
        
        // when
        Map<String, String> map2 = (Map<String, String>) XMLSerializer
                .toObject(decrypted);

        // then
        assertEquals(map.size(), map2.size());
        assertTrue(map2.containsKey("a"));
        assertTrue(map2.containsKey("b"));
        assertEquals("a1", map2.get("a"));
        assertEquals("b1", map2.get("b"));
    }
}
