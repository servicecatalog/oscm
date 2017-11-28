/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.oscm.domobjects.Tag;
import org.oscm.domobjects.TechnicalProductTag;
import org.oscm.validator.ADMValidator;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOTag;

public class TagAssemblerTest {
    @Test
    public void testToVOTag_NullInput() throws Exception {
        VOTag voTag = TagAssembler.toVOTag(null, null, null);
        assertNull(voTag);
    }

    @Test
    public void testToVOTag() {
        long numberReferences = 5;
        VOTag voTag = TagAssembler.toVOTag("it", "Enterprise",
                Long.valueOf(numberReferences));
        assertEquals("it", voTag.getLocale());
        assertEquals("Enterprise", voTag.getValue());
        assertEquals(5, voTag.getNumberReferences());
        assertTrue(voTag.toString().contains("it"));
        assertTrue(voTag.toString().contains("Enterprise"));
    }

    @Test
    public void testToTags() throws Exception {
        List<String> tags = new ArrayList<String>();
        tags.add("enterprise");
        tags.add("company");

        List<Tag> result = TagAssembler.toTags(tags, "en");
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("enterprise", result.get(0).getValue());
        assertEquals("en", result.get(0).getLocale());
        assertEquals("company", result.get(1).getValue());
        assertEquals("en", result.get(1).getLocale());
    }

    @Test
    public void testToStrings() throws Exception {
        Tag tag1 = new Tag("it", "enterprise");
        tag1.setKey(3);

        Tag tag2 = new Tag("en", "company");
        tag2.setKey(4);

        TechnicalProductTag tpt1 = new TechnicalProductTag();
        tpt1.setTag(tag1);
        TechnicalProductTag tpt2 = new TechnicalProductTag();
        tpt2.setTag(tag2);

        List<TechnicalProductTag> tags = new ArrayList<TechnicalProductTag>();
        tags.add(tpt1);
        tags.add(tpt2);

        List<String> result = TagAssembler.toStrings(tags, "en");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("company", result.get(0));

        result = TagAssembler.toStrings(tags, "it");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("enterprise", result.get(0));

        result = TagAssembler.toStrings(tags, "fr");
        assertNotNull(result);
        assertEquals(0, result.size());

        result = TagAssembler.toStrings(null, "fr");
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    @Test
    public void testToTagsNullValues() throws Exception {
        // Check whether empty values are checked
         List<Tag> result = TagAssembler.toTags(null, "de");
         assertNotNull(result);
         assertEquals(0, result.size());
    }

    @Test(expected = ValidationException.class)
    public void testToTagsMaxLength() throws Exception {
        List<String> tags = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ADMValidator.LENGTH_TAG + 1; i++)
            sb.append('x');
        tags.add(sb.toString());
        try {
            TagAssembler.toTags(tags, "en");
        } catch (ValidationException e) {
            assertEquals(ReasonEnum.LENGTH, e.getReason());
            assertEquals("value", e.getMember());
            assertEquals(Integer.valueOf(ADMValidator.LENGTH_TAG).toString(),
                    e.getMessageParams()[1]);
            throw e;
        }
    }

    @Test(expected = ValidationException.class)
    public void testToTagsEmpty() throws Exception {
        // Check whether empty values are checked
        List<String> tags = new ArrayList<String>();
        tags.add("enterprise");
        tags.add("");
        tags.add("one more");
        try {
            TagAssembler.toTags(tags, "de");
        } catch (ValidationException e) {
            assertEquals(ReasonEnum.REQUIRED, e.getReason());
            assertEquals("value", e.getMember());
            throw e;
        }
    }

    @Test(expected = ValidationException.class)
    public void testToTagsNull() throws Exception {
        // Check whether empty values are checked
        List<String> tags = new ArrayList<String>();
        tags.add("enterprise");
        tags.add(null);
        tags.add("one more");
        try {
            TagAssembler.toTags(tags, "de");
        } catch (ValidationException e) {
            assertEquals(ReasonEnum.REQUIRED, e.getReason());
            assertEquals("value", e.getMember());
            throw e;
        }
    }

    @Test(expected = ValidationException.class)
    public void testToTagsInvalidChar() throws Exception {
        // Check whether "," values are checked
        List<String> tags = new ArrayList<String>();
        tags.add("enterprise");
        tags.add("one,two");
        try {
            TagAssembler.toTags(tags, "de");
        } catch (ValidationException e) {
            assertEquals(ReasonEnum.INVALID_CHAR, e.getReason());
            assertEquals("value", e.getMember());
            assertEquals(",", e.getMessageParams()[0]);
            throw e;
        }
    }

    @Test
    public void testToTagsLowercase() throws Exception {
        // Check whether every value is correctly converted to lowercase
        List<String> tags = new ArrayList<String>();
        tags.add("enTERprise");
        tags.add("CompanY  "); // check trimmed as well
        tags.add("Same Again");
        tags.add("already-lower");

        List<Tag> result = TagAssembler.toTags(tags, "es");
        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals("enterprise", result.get(0).getValue());
        assertEquals("es", result.get(0).getLocale());
        assertEquals("company", result.get(1).getValue());
        assertEquals("es", result.get(1).getLocale());
        assertEquals("same again", result.get(2).getValue());
        assertEquals("es", result.get(2).getLocale());
        assertEquals("already-lower", result.get(3).getValue());
        assertEquals("es", result.get(3).getLocale());
    }

}
