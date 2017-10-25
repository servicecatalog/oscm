/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.local;

import java.util.List;

import javax.ejb.Local;

import org.oscm.domobjects.Tag;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.ValidationException;

@Local
public interface TagServiceLocal {
    /**
     * Returns the minimum score value for given tags (threshold). Tag with
     * lower score value are filtered out.
     * 
     * @return the minimum score value
     */
    public int getTaggingMinScore();

    /**
     * Returns the number of maximum number of tags that can be displayed within
     * the cloud.
     * 
     * @return the number of maximum number of tags that can be displayed within
     *         the cloud.
     */
    public int getTaggingMaxTags();

    /**
     * Stores the given tags of the specified locale for the given technical
     * product.
     * <p>
     * All no longer defined tags of the given locale will be removed from the
     * list of defined tags. If a tag is not longer referenced by any service it
     * will be removed completely.
     * <p>
     * If no locale has been specified all tags of the service will be handled.
     * 
     * @param tp
     *            The technical product for which the tags should be stored.
     * @param locale
     *            The locale for which the tags should be updated or
     *            <code>null</code> if all tags should be processed.
     * @param tags
     *            The list of tags which should be updated. If a locale is
     *            given, only tags of the specified locale will be processed.
     * @throws ValidationException
     *             Thrown in case the number of provided tags exceeds the
     *             defined maximum number of tags per service.
     */
    public void updateTags(TechnicalProduct tp, String locale, List<Tag> tags)
            throws ValidationException;

    /**
     * Deletes all tag entities which are not longer referenced by any service.
     */
    public void deleteOrphanedTags();

    /**
     * Tries to read the {@link Tag} for the specified tag value and locale.
     * 
     * @param tagValue
     *            the tag value
     * @param locale
     *            the locale the tag is defined for
     * @return the {@link Tag} read from the database
     * @throws ObjectNotFoundException
     *             in case the tag wasn't found for the specified locale
     */
    public Tag getTag(String tagValue, String locale)
            throws ObjectNotFoundException;

}
