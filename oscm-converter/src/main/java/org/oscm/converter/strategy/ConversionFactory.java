/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 20.07.15 16:42
 *
 *******************************************************************************/

package org.oscm.converter.strategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.oscm.converter.utils.Pair;
import org.oscm.dataservice.local.DataService;

/**
 * Factory used to return conversion strategy to convert object from one type to
 * other type.
 */
@SuppressWarnings("unchecked")
public class ConversionFactory {

    /**
     * Registry with mapping of supported class mapping pair to conversion
     * strategy
     */
    private static final Map<Pair<Class<?>, Class<?>>, ConversionStrategy<?, ?>> REGISTRY = new HashMap<>();

    /**
     * Default constructor set to private
     */
    private ConversionFactory() {
    }

    /**
     * 
     * @param from
     *            - Class that should be converted
     * @param to
     *            - Class that should be result of conversion
     * @param strategy
     *            - Converter assigned to this From -> To mapping
     */
    public static void register(Class<?> from, Class<?> to,
            ConversionStrategy<?, ?> strategy) {
        REGISTRY.put(new Pair<Class<?>, Class<?>>(from, to), strategy);
    }

    /**
     * 
     * @param from
     *            - Class that should be removed from conversion registry
     */
    public static void deregister(Class<?> from, Class<?> to) {
        REGISTRY.remove(new Pair<Class<?>, Class<?>>(from, to));
    }

    /**
     * @return Returns all registered converters.
     */
    public static Collection<ConversionStrategy<?, ?>> getRegisteredConverters() {
        return REGISTRY.values();
    }

    /**
     * Checks if given class mapping is registered.
     * 
     * @param from
     * @param to
     * @return - true is class to class mapping is registered, false otherwise
     */
    private static boolean isRegistered(Class<?> from, Class<?> to) {
        return REGISTRY.get(new Pair<Class<?>, Class<?>>(from, to)) != null;
    }

    /**
     * Returns converter assigned to class to class mapping If not converter is
     * found throws {@link UnsupportedOperationException}
     *
     * @param from
     *            - Class that should be converter
     * @param to
     *            - Class that should be result of conversion
     * @param <From>
     * @param <To>
     * @return - ConversionStrategy if supported class to class mapping is
     *         found.
     */
    public static <From, To> ConversionStrategy<From, To> getConverter(
            Class<From> from, Class<To> to) {

        if (!isRegistered(from, to)) {
            throw new UnsupportedOperationException("Class mapping "
                    + from.getName() + " -> " + to.getName()
                    + " not supported.");
        }

        return (ConversionStrategy<From, To>) REGISTRY
                .get(new Pair<Class<?>, Class<?>>(from, to));
    }

    /**
     * Returns converter assigned to class to class mapping If not converter is
     * found throws {@link UnsupportedOperationException}
     *
     * @param from
     *            - Class that should be converter
     * @param to
     *            - Class that should be result of conversion
     * @param <From>
     * @param <To>
     * @return - ConversionStrategy if supported class to class mapping is
     *         found.
     */
    public static <From, To> ConversionStrategy<From, To> getConverter(
            Class<From> from, Class<To> to, DataService dataService) {

        if (!isRegistered(from, to)) {
            throw new UnsupportedOperationException("Class mapping "
                    + from.getName() + " -> " + to.getName()
                    + " not supported.");
        }

        final ConversionStrategy<From, To> strategy = (ConversionStrategy<From, To>) REGISTRY
                .get(new Pair<Class<?>, Class<?>>(from, to));
        strategy.setDataService(dataService);
        return strategy;
    }

}
