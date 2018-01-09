/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: July 12, 2011                                                      
 *                                                                              
 *  Completion Time: September 23, 2011                                           
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;

import org.oscm.converter.WhiteSpaceConverter;
import org.oscm.domobjects.bridge.ProductClassBridge;

/**
 * @author Dirk Bernsau
 * 
 */
public class LuceneQueryBuilder {

    /**
     * Builds the search query for searching the given search phrase on a
     * certain marketplace.
     * 
     * @param searchPhrase
     *            the search phrase
     * @param locale
     *            the user locale
     * @param defaultLocale
     *            if <code>false</code>, check if search term is found in fields
     *            for current locale, otherwise check if fields in current
     *            locale are not specified but in default locale instead
     * @return a Lucene search query
     */
    public static BooleanQuery getServiceQuery(String searchPhrase,
            String locale, String defaultLocale,
            boolean isDefaultLocaleHandling) {

        String phrase = WhiteSpaceConverter.replace(searchPhrase);
        phrase = phrase.trim();

        final List<String> fieldNames = Arrays.asList(
                ProductClassBridge.SERVICE_NAME,
                ProductClassBridge.SERVICE_DESCRIPTION,
                ProductClassBridge.SERVICE_SHORT_DESC, ProductClassBridge.TAGS,
                ProductClassBridge.PRICEMODEL_DESCRIPTION,
                ProductClassBridge.CATEGORY_NAME);

        BooleanQuery booleanQuery = constructWildcardQuery(phrase, fieldNames,
                locale, defaultLocale, isDefaultLocaleHandling);

        return booleanQuery;
    }

    private static BooleanQuery constructWildcardQuery(String searchPhrase,
            List<String> fieldNames, String locale, String defaultLocale,
            boolean isDefaultLocaleHandling) {

        String[] splitStr = searchPhrase.split("\\s+");

        BooleanQuery booleanQuery = new BooleanQuery();

        for (String token : splitStr) {
            booleanQuery.add(
                    prepareWildcardQueryForSingleToken(token, fieldNames,
                            locale, defaultLocale, isDefaultLocaleHandling),
                    Occur.MUST);
        }

        return booleanQuery;
    }

    private static BooleanQuery prepareWildcardQueryForSingleToken(String token,
            List<String> fieldNames, String locale, String defaultLocale,
            boolean isDefaultLocaleHandling) {

        BooleanQuery queryPart = new BooleanQuery();

        for (String fieldName : fieldNames) {
            if (isDefaultLocaleHandling) {
                if (locale.equals(defaultLocale)) {
                    throw new IllegalArgumentException(
                            "For default locale handling, locale and default locale must be different");
                }
                BooleanQuery localeHandlingQuery = constructDefaultLocaleHandlingQuery(
                        fieldName, locale, defaultLocale, token);
                queryPart.add(localeHandlingQuery, Occur.SHOULD);
            } else {
                WildcardQuery wildcardQuery = new WildcardQuery(new Term(
                        fieldName + locale, "*" + token.toLowerCase() + "*"));
                queryPart.add(wildcardQuery, Occur.SHOULD);
            }

        }
        return queryPart;
    }

    private static BooleanQuery constructDefaultLocaleHandlingQuery(
            String fieldName, String locale, String defaultLocale,
            String searchPhrase) {
        BooleanQuery bq1 = new BooleanQuery();
        TermQuery tq1 = new TermQuery(
                new Term(fieldName + ProductClassBridge.DEFINED_LOCALES_SUFFIX,
                        defaultLocale));
        TermQuery tq2 = new TermQuery(new Term(
                fieldName + ProductClassBridge.DEFINED_LOCALES_SUFFIX, locale));
        bq1.add(tq1, Occur.MUST);
        bq1.add(tq2, Occur.MUST_NOT);
        BooleanQuery bq2 = new BooleanQuery();
        WildcardQuery wq1 = new WildcardQuery(
                new Term(fieldName + defaultLocale,
                        "*" + searchPhrase.toLowerCase() + "*"));
        bq2.add(wq1, Occur.SHOULD);
        BooleanQuery finalQuery = new BooleanQuery();
        finalQuery.add(bq1, Occur.MUST);
        finalQuery.add(bq2, Occur.MUST);

        return finalQuery;
    }

}
