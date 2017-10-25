/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-05-05                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.intf;

import java.util.List;

import javax.ejb.Remote;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.oscm.vo.VOTag;

/**
 * Remote interface for handling service tags.
 * 
 */
@Remote
@WebService(targetNamespace = "http://oscm.org/xsd")
public interface TagService {

    /**
     * Returns the tags of the tag cloud for the given language. The maximum
     * number of tags returned is determined by the
     * <code>TAGGING_MAX_TAGS</code> configuration parameter. For a tag to
     * appear in the list, the number of times it is used in services must be
     * equal to or greater than the value defined in the
     * <code>TAGGING_MIN_SCORE</code> configuration parameter.
     * <p>
     * The result is independent of any service activation states and
     * marketplaces.
     * <p>
     * Required role: none
     * 
     * @param locale
     *            the language for which to return the tags. Specify a language
     *            code as returned by <code>getLanguage()</code> of
     *            <code>java.util.Locale</code>.
     * @return the list of tags
     */
    public List<VOTag> getTagsByLocale(@WebParam(name = "locale") String locale);

    /**
     * Returns the tags of the tag cloud for the specified marketplace and
     * language. The maximum number of tags returned is determined by the
     * <code>TAGGING_MAX_TAGS</code> configuration parameter. For a tag to
     * appear in the list, the number of times it is used in services must be
     * equal to or greater than the value defined in the
     * <code>TAGGING_MIN_SCORE</code> configuration parameter.
     * <p>
     * If the number of matching tags in the given language is less than
     * <code>TAGGING_MAX_TAGS</code>, the list is filled up with tags of the
     * default language.
     * <p>
     * Required role: none
     * 
     * @param locale
     *            the language for which to return the tags. Specify a language
     *            code as returned by <code>getLanguage()</code> of
     *            <code>java.util.Locale</code>.
     * @param marketplaceId
     *            the ID of the marketplace for which to return the tags
     * @return the list of tags
     */
    public List<VOTag> getTagsForMarketplace(
            @WebParam(name = "locale") String locale,
            @WebParam(name = "marketplaceId") String marketplaceId);

    /**
     * Returns the tags of the given language which match the specified pattern.
     * <p>
     * Required role: none
     * 
     * @param locale
     *            the language for which to return the tags. Specify a language
     *            code as returned by <code>getLanguage()</code> of
     *            <code>java.util.Locale</code>.
     * @param tagPattern
     *            the search pattern for the tags to be returned; you can
     *            specify whole words or use wildcards: an underscore (_)
     *            represents one character, a percent sign (%) any number of
     *            characters
     * @param limit
     *            the maximum number of tags to return
     * @return the list of tags
     */
    public List<String> getTagsByPattern(
            @WebParam(name = "locale") String locale,
            @WebParam(name = "tagPattern") String tagPattern,
            @WebParam(name = "limit") int limit);

}
