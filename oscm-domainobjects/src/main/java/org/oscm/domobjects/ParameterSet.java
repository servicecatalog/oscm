/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Peter Pock                                                      
 *                                                                              
 *  Creation Date: 29.06.2009                                                      
 *                                                                              
 *  Completion Time: 30.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;

/**
 * A parameter set is the connection of a product or a subscription with a set
 * of parameters. Per default a subscription points to the same parameter set as
 * the product to which the subscription belongs. Only if special parameters are
 * necessary a new parameter set is created and connected to the subscription
 * (see price model).
 * 
 * @author Peter Pock
 * 
 */
@Entity
public class ParameterSet extends DomainObjectWithEmptyDataContainer {

    private static final long serialVersionUID = 273606238278847037L;

    /**
     * The parameters of the parameter set, 1:n relation.
     */
    @OneToMany(cascade = { CascadeType.REMOVE, CascadeType.PERSIST }, mappedBy = "parameterSet", fetch = FetchType.LAZY)
    @OrderBy
    private List<Parameter> parameters = new ArrayList<Parameter>();

    @OneToOne(mappedBy = "parameterSet", optional = false, fetch = FetchType.LAZY)
    private Product product;

    public ParameterSet() {
        super();
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    /**
     * Returns a copy of this parameter set, containing a copy of all related
     * parameters.
     * 
     * @return A copy of the parameter set.
     */
    public ParameterSet copy() {
        ParameterSet copy = new ParameterSet();
        List<Parameter> paramList = new ArrayList<Parameter>();

        for (Parameter param : parameters) {
            paramList.add(param.copy(copy));
        }

        copy.setParameters(paramList);
        return copy;
    }

}
