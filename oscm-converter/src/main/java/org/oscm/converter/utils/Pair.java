/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 27.07.15 11:15
 *
 *******************************************************************************/

package org.oscm.converter.utils;

import org.oscm.validation.ArgumentValidator;

/**
 * Generic simple pair class. It does not allow null values.
 * 
 * @param <E>
 * @param <U>
 */
public class Pair<E, U> {
    private E first;
    private U second;

    public Pair(E first, U second) {
        ArgumentValidator.notNull("first", first);
        ArgumentValidator.notNull("second", second);

        this.first = first;
        this.second = second;
    }
    
    public E first() {
        return first;
    }
    
    public void setFirst(E first) {
        this.first = first;
    }
    
    public U second() {
        return second;
    }
    
    public void setSecond(U second) {
        this.second = second;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Pair<?, ?> pair = (Pair<?, ?>) o;

        return first.equals(pair.first) && second.equals(pair.second);

    }

    @Override
    public int hashCode() {
        int result = first.hashCode();
        result = 31 * result + second.hashCode();
        return result;
    }
}
