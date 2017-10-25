/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 20.07.15 17:30
 *
 *******************************************************************************/

package org.oscm.converter.api;

import java.util.ArrayList;
import java.util.List;

import org.oscm.converter.strategy.ConversionFactory;
import org.oscm.converter.strategy.ConversionStrategy;
import org.oscm.converter.strategy.api.ToExtPaginationStrategy;
import org.oscm.converter.strategy.api.ToExtParameterDefinitionStrategy;
import org.oscm.converter.strategy.api.ToExtParameterOptionStrategy;
import org.oscm.converter.strategy.api.ToExtParameterStrategy;
import org.oscm.converter.strategy.api.ToExtPriceModelStrategy;
import org.oscm.converter.strategy.api.ToExtPricedEventStrategy;
import org.oscm.converter.strategy.api.ToExtPricedOptionStrategy;
import org.oscm.converter.strategy.api.ToExtPricedParameterStrategy;
import org.oscm.converter.strategy.api.ToExtPricedProductRoleStrategy;
import org.oscm.converter.strategy.api.ToExtProductStrategy;
import org.oscm.converter.strategy.api.ToExtRoleDefinitionStrategy;
import org.oscm.converter.strategy.api.ToExtSteppedPriceStrategy;
import org.oscm.converter.strategy.api.ToExtUnitStrategy;
import org.oscm.converter.strategy.api.ToExtUserStrategy;
import org.oscm.converter.strategy.api.ToPricedRoleStrategy;
import org.oscm.converter.strategy.domain.ToCommonPaginationStrategy;
import org.oscm.converter.strategy.domain.ToDomUnitStrategy;
import org.oscm.converter.strategy.domain.ToDomUserStrategy;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterOption;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.PricedOption;
import org.oscm.domobjects.PricedParameter;
import org.oscm.domobjects.PricedProductRole;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.SteppedPrice;
import org.oscm.domobjects.UserGroup;
import org.oscm.pagination.Pagination;
import org.oscm.vo.VOOrganizationalUnit;
import org.oscm.vo.VOParameter;
import org.oscm.vo.VOParameterDefinition;
import org.oscm.vo.VOParameterOption;
import org.oscm.vo.VOPriceModel;
import org.oscm.vo.VOPricedEvent;
import org.oscm.vo.VOPricedOption;
import org.oscm.vo.VOPricedParameter;
import org.oscm.vo.VOPricedRole;
import org.oscm.vo.VORoleDefinition;
import org.oscm.vo.VOService;
import org.oscm.vo.VOSteppedPrice;
import org.oscm.vo.VOUser;

/**
 * Generic converter used for object conversion between types in order to add
 * new object converter. Register it using
 * {@link ConversionFactory#register(Class, Class, ConversionStrategy)} and add
 * new conversion strategy class that implements {@link ConversionStrategy}
 * interface.
 */
public class Converter {

    /**
     * Register here new object converter with
     * {@link ConversionFactory#register(Class, Class, ConversionStrategy)}
     */
    static {
        ConversionFactory.register(Pagination.class, org.oscm.pagination.Pagination.class, new ToExtPaginationStrategy());
        ConversionFactory.register(org.oscm.pagination.Pagination.class, Pagination.class, new ToCommonPaginationStrategy());
        ConversionFactory.register(ParameterDefinition.class, VOParameterDefinition.class, new ToExtParameterDefinitionStrategy());
        ConversionFactory.register(ParameterOption.class, VOParameterOption.class, new ToExtParameterOptionStrategy());
        ConversionFactory.register(PricedEvent.class, VOPricedEvent.class, new ToExtPricedEventStrategy());
        ConversionFactory.register(PricedParameter.class, VOPricedParameter.class, new ToExtPricedParameterStrategy());
        ConversionFactory.register(PricedProductRole.class, VOPricedRole.class, new ToExtPricedProductRoleStrategy());
        ConversionFactory.register(PriceModel.class, VOPriceModel.class, new ToExtPriceModelStrategy());
        ConversionFactory.register(Product.class, VOService.class, new ToExtProductStrategy());
        ConversionFactory.register(RoleDefinition.class, VORoleDefinition.class, new ToExtRoleDefinitionStrategy());
        ConversionFactory.register(SteppedPrice.class, VOSteppedPrice.class, new ToExtSteppedPriceStrategy());
        ConversionFactory.register(VOOrganizationalUnit.class, UserGroup.class, new ToDomUnitStrategy());
        ConversionFactory.register(UserGroup.class, VOOrganizationalUnit.class, new ToExtUnitStrategy());
        ConversionFactory.register(PlatformUser.class, VOUser.class, new ToExtUserStrategy());
        ConversionFactory.register(VOUser.class, PlatformUser.class, new ToDomUserStrategy());
        ConversionFactory.register(PricedOption.class, VOPricedOption.class, new ToExtPricedOptionStrategy());
        ConversionFactory.register(PricedProductRole.class, VOPricedRole.class, new ToPricedRoleStrategy());
        ConversionFactory.register(Parameter.class, VOParameter.class, new ToExtParameterStrategy());

        ConversionFactory.register(org.oscm.paginator.Pagination.class,
                Pagination.class, new ToExtPaginationStrategy());
        ConversionFactory.register(Pagination.class,
                org.oscm.paginator.Pagination.class,
                new ToCommonPaginationStrategy());
    }

    private Converter() {
    }

    /**
     * If supported conversion strategy is found converts object from one type
     * to another.
     *
     * @param object
     *            Object to convert
     * @param from
     *            - Object class
     * @param to
     *            - Target Object class
     * @param <From>
     * @param <To>
     * @return - Converted object to target class
     */
    public static <From, To> To convert(From object, Class<From> from,
            Class<To> to) {
        ConversionStrategy<From, To> converter = ConversionFactory
                .getConverter(from, to);
        return converter.convert(object);
    }

    /**
     * If supported conversion strategy is found converts object from one type
     * to another.
     *
     * @param object
     *            Object to convert
     * @param from
     *            - Object class
     * @param to
     *            - Target Object class
     * @param <From>
     * @param <To>
     * @return - Converted object to target class
     */
    public static <From, To> To convert(From object, Class<From> from,
            Class<To> to, DataService ds) {
        ConversionStrategy<From, To> converter = ConversionFactory
                .getConverter(from, to, ds);
        converter.setDataService(ds);
        return  converter.convert(object);
    }

    /**
     * Allows conversion of objects in list from list of one type to another
     * {@link #convert(Object, Class, Class)}
     *
     * @param list
     * @param from
     * @param to
     * @param <From>
     * @param <To>
     * @return
     */
    public static <From, To> List<To> convertList(List<From> list,
            Class<From> from, Class<To> to) {
        ConversionStrategy<From, To> converter = ConversionFactory
                .getConverter(from, to);

        List<To> result = new ArrayList<>();
        for (From e : list) {
            result.add(converter.convert(e));
        }

        return result;
    }

    /**
     * Allows conversion of objects in list from list of one type to another
     * {@link #convert(Object, Class, Class)}
     *
     * @param list
     * @param from
     * @param to
     * @param <From>
     * @param <To>
     * @return
     */
    public static <From, To> List<To> convertList(List<From> list,
                                                  Class<From> from, Class<To> to, DataService ds) {
        ConversionStrategy<From, To> converter = ConversionFactory
                .getConverter(from, to, ds);

        List<To> result = new ArrayList<>();
        for (From e : list) {
            result.add(converter.convert(e));
        }

        return result;
    }
}
