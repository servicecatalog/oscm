/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Enes Sejfi                                                   
 *                                                                              
 *  Creation Date: 02.05.2011
 *                                                                              
 *  Completion Time: <date>                                           
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.oscm.domobjects.Tag;
import org.oscm.domobjects.TechnicalProductTag;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOTag;

/**
 * @author Enes Sejfi
 * 
 */
public class TagAssembler extends BaseAssembler {
    public static final String FIELD_NAME_TAG_VALUE = "value";

    /**
     * Converts the provided parameter to a {@link VOTag} setting locale, value
     * and number of references.
     * 
     * @param locale
     *            locale
     * @param value
     *            tag value
     * @param numberReferences
     *            Number of objects which references the tag
     * @return VOTag instance
     */
    public static VOTag toVOTag(String locale, String value,
            Long numberReferences) {
        if (locale == null || value == null || numberReferences == null) {
            return null;
        }

        VOTag voTag = new VOTag();
        voTag.setLocale(locale);
        voTag.setValue(value);
        voTag.setNumberReferences(numberReferences.longValue());

        return voTag;
    }

    /**
     * Converts the provided list of tag value to a list of {@link VOTag}s for
     * the specified locale.
     * 
     * @param tags
     *            the list of values to convert
     * @param locale
     *            the locale for which the tags are defined
     * @return the list of {@link VOTag}s
     */
    public static List<Tag> toTags(List<String> values, String locale)
            throws ValidationException {
        if (values == null) {
            return new ArrayList<Tag>();
        }
        List<Tag> result = new ArrayList<Tag>();
        for (String value : values) {
            result.add(toTag(locale, value));
        }
        return result;
    }

    /**
     * Converts the provided parameter to a {@link Tag} setting locale and
     * value.
     * 
     * @param locale
     *            locale
     * @param value
     *            tag value
     * @return Tag instance
     */
    public static Tag toTag(String locale, String value)
            throws ValidationException {
        // Validate locale
        BLValidator.isLocale("locale", locale, true);

        // Convert to trimmed lowercase
        if (value != null)
            value = value.trim().toLowerCase(new Locale(locale));

        // Validate value
        BLValidator.isTag(FIELD_NAME_TAG_VALUE, value);

        // return newly created tag
        return new Tag(locale, value);
    }

    /**
     * Converts the provided list of {@link TechnicalProductTag} to a list of
     * values for the given locale.
     * 
     * @param tags
     *            the {@link TechnicalProductTag}s to convert
     * @param locale
     *            the locale for which the tags are requested
     * @return the list of string values
     */
    public static List<String> toStrings(List<TechnicalProductTag> tags,
            String locale) {
        if (tags == null) {
            return new ArrayList<String>();
        }
        List<String> result = new ArrayList<String>();
        for (TechnicalProductTag def : tags) {
            if (def.getTag().getLocale().equals(locale))
                result.add(def.getTag().getValue());
        }
        return result;
    }

}
