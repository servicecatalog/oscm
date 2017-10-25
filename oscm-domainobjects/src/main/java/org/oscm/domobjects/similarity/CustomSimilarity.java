/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: groch                                                     
 *                                                                              
 *  Creation Date: Aug 24, 2011                                                      
 *                                                                              
 *  Completion Time: Aug 25, 2011                                           
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects.similarity;

import org.apache.lucene.search.similarities.BM25Similarity;

/**
 * @author groch
 *
 *         Custom similarity implementation to adjust the scoring formula used
 *         by Lucene for our full text search implementation. The basic
 *         requirement for the ranking of returned documents is defined only by
 *         the weight of the searchable fields, i.e. their boost factors. The
 *         frequency of the search term within a document, the rareness of a
 *         search term compared to all documents or the number of words that
 *         make up the field do not have an impact on the ranking though. For
 *         more details on the underlying formula of the score calculation, see
 *         org/apache/lucene/search/similar/package-summary.html.
 *
 */
public class CustomSimilarity extends BM25Similarity {

    private static final long serialVersionUID = 8995778679501627820L;

    @Override
    public float idf(long docFreq, long numDocs) {
        // simply return 1 to eradicate the effect that rarer terms receive a
        // higher score
        return 1;
    }

}
