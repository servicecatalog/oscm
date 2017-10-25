/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                          
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.components.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Response object for service calls.
 * 
 * @author cheld
 * 
 */
public class Response implements Serializable {

    private static final long serialVersionUID = 6343376119642180141L;

    List<Object> results = new ArrayList<Object>();

    List<ReturnCode> returnCodes = new ArrayList<ReturnCode>();

    public Response(Object... result) {
        if (result != null) {
            for (Object o : result) {
                results.add(o);
            }
        }
    }

    /**
     * Returns all stored results.
     * 
     * @return List
     */
    public List<Object> getResults() {
        return results;
    }

    /**
     * Sets all results to be stored.
     * 
     * @param results
     */
    public void setResults(List<Object> results) {
        this.results = results;
    }

    /**
     * Returns the first result for the given type.
     * 
     * @param typeOfResult
     *            The type of the searched result
     * @return result
     */
    @SuppressWarnings("unchecked")
    public <T> T getResult(Class<T> typeOfResult) {
        for (Object result : results) {
            if (typeOfResult.isInstance(result)) {
                return (T) result;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getResultList(Class<T> typeOfResult) {
        for (Object result : results) {
            if (result instanceof List) {
                List<?> listResult = (List<?>) result;
                if (listResult.size() > 0) {
                    if (listResult.get(0).getClass() == typeOfResult) {
                        return (List<T>) listResult;
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Returns all stored return codes.
     * 
     * @return list
     */
    public List<ReturnCode> getReturnCodes() {
        return returnCodes;
    }

    /**
     * Sets all return codes to be stored.
     * 
     * @param returnCodes
     */
    public void setReturnCodes(List<ReturnCode> returnCodes) {
        this.returnCodes = returnCodes;
    }

    /**
     * Returns a sublist of all info out of all stored return codes.
     * 
     * @return list
     */
    public List<ReturnCode> getInfos() {
        return filterErrorsCodes(ReturnType.INFO);
    }

    /**
     * Returns a sublist of all warnings out of all stored return codes.
     * 
     * @return list
     */
    public List<ReturnCode> getWarnings() {
        return filterErrorsCodes(ReturnType.WARNING);
    }

    List<ReturnCode> filterErrorsCodes(ReturnType type) {
        List<ReturnCode> filteredList = new ArrayList<ReturnCode>();
        for (ReturnCode returnCode : returnCodes) {
            if (returnCode.getType().equals(type)) {
                filteredList.add(returnCode);
            }
        }
        return Collections.unmodifiableList(filteredList);
    }

    /**
     * Returns a return code that is most severe. E.g. in case this object
     * contains an error and a warning, then the error is returned.
     * 
     * @return ReturnCode
     */
    public ReturnCode getMostSevereReturnCode() {
        if (getWarnings().size() > 0) {
            return getWarnings().get(0);
        }
        if (getInfos().size() > 0) {
            return getInfos().get(0);
        }
        return null;

    }

}
