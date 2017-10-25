/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Enes Sejfi                                                    
 *                                                                              
 *  Creation Date: 02.05.2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.persistence.Query;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Tag;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TechnicalProductTag;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.serviceprovisioningservice.assembler.TagAssembler;
import org.oscm.serviceprovisioningservice.local.TagServiceLocal;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validation.ArgumentValidator;
import org.oscm.internal.intf.TagService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOTag;

@Stateless
@Remote(TagService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class TagServiceBean implements TagService, TagServiceLocal {
    private static final Log4jLogger logger = LoggerFactory
            .getLogger(TagServiceBean.class);

    @EJB(beanInterface = DataService.class)
    protected DataService dm;

    @EJB(beanInterface = ConfigurationServiceLocal.class)
    private ConfigurationServiceLocal configService;

    // max. count of tags per service
    private static final int MAX_COUNT_TAGS = 5;

    @Resource
    private SessionContext sessionCtx;

    public TagServiceBean() {
    }

    /**
     * A tag by value comparator.
     */
    private Comparator<VOTag> valueComperator = new Comparator<VOTag>() {

        public int compare(VOTag o1, VOTag o2) {
            String value = o1.getValue();
            String value2 = o2.getValue();
            if (value != null && value2 != null) {
                return value.compareTo(value2);
            }
            return 0;
        }

    };

    /**
     * Returns the list of tags for the <code>maxTags</code> defined tags for
     * the given language, that score exceed the defined <code>minScore</code>.
     * 
     * @param locale
     *            String indication the language as returned by
     *            {@link java.util.Locale#getLanguage()}
     * @return the tag list as described above.
     */
    public List<VOTag> getTagsByLocale(String locale) {
        
        ArgumentValidator.notNull("locale", locale);
        int taggingMaxTags = getTaggingMaxTags();
        int minScore = getTaggingMinScore();
        Query query = dm
                .createNamedQuery("TechnicalProductTag.getAllTagsGreaterMinScore");
        query.setParameter("tagMinScore", Long.valueOf(minScore));
        query.setParameter("locale", locale);

        List<Object[]> queryResult = ParameterizedTypes.list(
                query.getResultList(), Object[].class);

        List<VOTag> result = new LinkedList<VOTag>();
        for (Object[] data : queryResult) {
            String value = (String) data[0];
            Long numberReferences = (Long) data[1];

            VOTag voTag = TagAssembler.toVOTag(locale, value, numberReferences);
            result.add(voTag);
        }
        if (result.size() > taggingMaxTags) {
            result = result.subList(0, taggingMaxTags);
        }
        Collections.sort(result, valueComperator);

        
        return result;
    }

    public List<VOTag> getTagsForMarketplace(String locale, String marketplaceId) {
        
        ArgumentValidator.notNull("locale", locale);
        ArgumentValidator.notNull("marketplaceId", marketplaceId);
        int taggingMaxTags = getTaggingMaxTags();
        int minScore = getTaggingMinScore();

        List<VOTag> tags = getTagsForMarketplaceInternal(locale, marketplaceId,
                minScore);
        if (tags.size() < taggingMaxTags) {

            // hash set for avoiding duplicate values
            Set<String> uniqueList = new HashSet<String>();
            for (VOTag tag : tags) {
                uniqueList.add(tag.getValue());
            }
        }

        if (tags.size() > taggingMaxTags) {
            tags = tags.subList(0, taggingMaxTags);
        }
        Collections.sort(tags, valueComperator);

        
        return tags;
    }

    private List<VOTag> getTagsForMarketplaceInternal(String locale,
            String marketplaceId, int minScore) {
        List<VOTag> tags = new LinkedList<VOTag>();
        if (marketplaceId != null) {
            Query query = dm
                    .createNamedQuery("TechnicalProductTag.getAllVisibleTagsGreaterMinScore");
            query.setParameter("tagMinScore", Long.valueOf(minScore));
            query.setParameter("marketplaceId", marketplaceId);
            query.setParameter("locale", locale);

            List<Object[]> result = ParameterizedTypes.list(
                    query.getResultList(), Object[].class);

            for (Object[] data : result) {
                String value = (String) data[0];
                Long numberReferences = (Long) data[1];
                VOTag voTag = TagAssembler.toVOTag(locale, value,
                        numberReferences);
                tags.add(voTag);
            }
        }
        return tags;
    }

    /**
     * Returns the minimum score value for given tags (threshold). Tag with
     * lower score value are filtered out.
     * 
     * @return the minimum score value
     */
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public int getTaggingMinScore() {
        return getConfiguration(ConfigurationKey.TAGGING_MIN_SCORE);
    }

    /**
     * Returns the number of maximum number of tags that can be displayed within
     * the cloud.
     * 
     * @return the number of maximum number of tags that can be displayed within
     *         the cloud.
     */
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public int getTaggingMaxTags() {
        return getConfiguration(ConfigurationKey.TAGGING_MAX_TAGS);
    }

    private int getConfiguration(ConfigurationKey informationId) {
        String configValue = configService.getConfigurationSetting(
                informationId, Configuration.GLOBAL_CONTEXT).getValue();
        if (configValue == null || configValue.trim().length() == 0) {
            SaaSSystemException se = new SaaSSystemException(
                    "Mandatory configuration setting '" + informationId.name()
                            + "' is not set");
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_MANDATORY_PROPERTY_NOT_SET,
                    informationId.name());
            throw se;
        }
        return Integer.parseInt(configValue);
    }

    /**
     * Returns the sorted list of all tags for the given locale, which matches
     * to the specified pattern.
     * 
     * @param locale
     *            String indication the language as returned by
     *            {@link java.util.Locale#getLanguage()}
     * @param tagPattern
     *            the pattern for the tag value to be matched
     * @param limit
     *            maximum number of tags which should be returned
     * @return the tag values as described above.
     */
    public List<String> getTagsByPattern(String locale, String tagPattern,
            int limit) {
        

        ArgumentValidator.notNull("locale", locale);
        ArgumentValidator.notNull("tagPattern", tagPattern);
        if (limit <= 0) {
            throw new IllegalArgumentException(
                    "Input parameter maxTags must be greater than 0");
        }

        Query query = dm.createNamedQuery("Tag.getAllOfLocaleFiltered");
        query.setParameter("locale", locale);
        query.setParameter("value", tagPattern.toLowerCase(new Locale(locale)));
        query.setMaxResults(limit);

        List<String> result = new ArrayList<String>();
        for (Tag tag : ParameterizedTypes.iterable(query.getResultList(),
                Tag.class)) {
            result.add(tag.getValue());
        }

        

        return result;
    }

    /**
     * Deletes all tag entities which are not longer referenced by any service.
     */
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void deleteOrphanedTags() {
        dm.createNamedQuery("Tag.deleteOrphanedTags").executeUpdate();
    }

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
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void updateTags(TechnicalProduct tp, String locale, List<Tag> tags)
            throws ValidationException {
        // Validate max. number of defined tags
        ArgumentValidator.notNull("tags", tags);

        Map<String, Integer> localeCounter = new HashMap<String, Integer>();

        // Get list of currently defined tag assignments for this service
        List<TechnicalProductTag> existingTags = new ArrayList<TechnicalProductTag>(
                tp.getTags());
        List<TechnicalProductTag> removedTags = new ArrayList<TechnicalProductTag>(
                tp.getTags());

        // Now process all given tags (of the requested locale)
        Iterator<Tag> iter = tags.iterator();
        while (iter.hasNext()) {
            Tag tag = iter.next();
            if (locale == null || tag.getLocale().equals(locale)) {
                // check maximum number of tags per locale
                Integer localeCount = localeCounter.get(tag.getLocale());
                if (localeCount == null)
                    localeCount = new Integer(1);
                else
                    localeCount = new Integer(localeCount.intValue() + 1);
                localeCounter.put(tag.getLocale(), localeCount);
                if (localeCount.intValue() > MAX_COUNT_TAGS) {
                    sessionCtx.setRollbackOnly();
                    throw new ValidationException(ReasonEnum.TAGS_MAX_COUNT,
                            "tags",
                            new Object[] { Integer.valueOf(MAX_COUNT_TAGS) });
                }

                // Check whether this tag is already referenced
                TechnicalProductTag existingTag = getTagAssignment(
                        existingTags, tag);
                if (existingTag == null) {
                    // Not yet assigned! => create assignment

                    try {
                        // 1. Get or create tag
                        Tag persistentTag = (Tag) dm.find(tag);
                        if (persistentTag == null) {
                            // Not yet existing => create new tag
                            dm.persist(tag);
                            persistentTag = tag;
                        }

                        // 2. Create assignment
                        TechnicalProductTag persistentRel = new TechnicalProductTag();
                        persistentRel.setTag(persistentTag);
                        persistentRel.setTechnicalProduct(tp);
                        dm.persist(persistentRel);

                        dm.flush();

                        // And update our lists
                        existingTags.add(persistentRel);
                        tp.getTags().add(persistentRel);

                    } catch (NonUniqueBusinessKeyException e) {
                        // Should never occur...
                        sessionCtx.setRollbackOnly();
                        SaaSSystemException sse = new SaaSSystemException(
                                "The tag '"
                                        + tag.getValue()
                                        + " for technical product '"
                                        + tp.getKey()
                                        + "' cannot be stored, as the business key already exists.",
                                e);
                        throw sse;
                    }

                } else {
                    // Remove tag from list of deleted tags
                    removedTags.remove(existingTag);
                }
            }
        }

        // Now remove all not longer defined assignments of the given locale
        // (and tag object as well if it was the last assignment)
        Iterator<TechnicalProductTag> deliter = removedTags.iterator();
        while (deliter.hasNext()) {
            TechnicalProductTag deltagrel = deliter.next();
            Tag deltag = deltagrel.getTag();
            if (locale == null || deltag.getLocale().equals(locale)) {
                // Delete assignment
                dm.remove(deltagrel);
                tp.getTags().remove(deltagrel);
            }
        }
        dm.flush();

        // Validate/handle all orphaned tags
        deleteOrphanedTags();
    }

    /**
     * Returns a specified tag from a given list of tags.
     * 
     * @param list
     *            the list of all tags which should be used
     * @param tag
     *            the tag to search for
     * @return the found element assignment or NULL if not found
     */
    private TechnicalProductTag getTagAssignment(
            final List<TechnicalProductTag> list, Tag tag) {
        TechnicalProductTag result = null;
        Iterator<TechnicalProductTag> iter = list.iterator();
        while (iter.hasNext()) {
            TechnicalProductTag exTag = iter.next();
            if (exTag.getTag().getLocale().equals(tag.getLocale())
                    && exTag.getTag().getValue().equals(tag.getValue())) {
                result = exTag;
                break;
            }
        }
        return result;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Tag getTag(String tagValue, String locale)
            throws ObjectNotFoundException {
        
        Tag tag = (Tag) dm.getReferenceByBusinessKey(new Tag(locale, tagValue));
        
        return tag;
    }
}
