/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 14.09.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import java.util.ArrayList;
import java.util.List;

import org.oscm.domobjects.Event;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOEventDefinition;
import org.oscm.internal.vo.VOPricedEvent;
import org.oscm.internal.vo.VOSteppedPrice;

/**
 * Assembler to handle event definitions and concrete priced events.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class EventAssembler extends BaseAssembler {

    public static final String FIELD_NAME_EVENT_PRICE = "eventPrice";

    /**
     * Converts the provided pricing related events to value objects, also using
     * the description in the user's locale.
     * 
     * @param consideredEvents
     *            The events to be converted, must not be <code>null</code>.
     * @return A list of price event value objects.
     */
    public static List<VOPricedEvent> toVOPricedEvent(
            List<PricedEvent> consideredEvents, LocalizerFacade facade) {
        List<VOPricedEvent> result = new ArrayList<VOPricedEvent>();
        for (PricedEvent currentEvent : consideredEvents) {
            result.add(toVOPricedEvent(currentEvent, facade));
        }
        return result;
    }

    /**
     * Converts a priced event domain object to a priced event value object.
     * 
     * @param currentEvent
     *            The event to be converted. Must not be <code>null</code>
     * @return The converted price event as value object.
     */
    public static VOPricedEvent toVOPricedEvent(PricedEvent currentEvent,
            LocalizerFacade facade) {
        VOEventDefinition evtDef = toVOEventDefinition(currentEvent.getEvent(),
                facade);
        VOPricedEvent result = new VOPricedEvent(evtDef);
        result.setEventPrice(currentEvent.getEventPrice());
        result.setSteppedPrices(SteppedPriceAssembler
                .toVOSteppedPrices(currentEvent.getSteppedPrices()));
        updateValueObject(result, currentEvent);
        return result;
    }

    /**
     * Converts an event domain object to a even definition value object, also
     * considering the user's locale corresponding event description.
     * 
     * @param event
     *            The event definition to be converted.
     * @param facade
     *            The localizer facade object.
     * @return The converted event definition.
     */
    public static VOEventDefinition toVOEventDefinition(Event event,
            LocalizerFacade facade) {
        VOEventDefinition result = new VOEventDefinition();
        String eventDescription = facade.getText(event.getKey(),
                LocalizedObjectTypes.EVENT_DESC);
        result.setEventDescription(eventDescription);
        result.setEventId(event.getEventIdentifier());
        result.setEventType(event.getEventType());
        updateValueObject(result, event);
        return result;
    }

    /**
     * Converts a set of event domain objects to event definition value objects,
     * also considering the user's locale corresponding event description.
     * 
     * @param events
     *            The events to be converted.
     * @param facade
     *            The localizer facade.
     * @return A list of converted event definitions.
     */
    public static List<VOEventDefinition> toVOEventDefinitions(
            List<Event> platformEvents, List<Event> events,
            LocalizerFacade facade) {
        List<VOEventDefinition> result = new ArrayList<VOEventDefinition>();
        for (Event event : platformEvents) {
            result.add(toVOEventDefinition(event, facade));
        }
        for (Event event : events) {
            result.add(toVOEventDefinition(event, facade));
        }
        return result;
    }

    /**
     * Creates a priced event from the value object. In the result object, the
     * reference to the event and the price model must still be set!
     * 
     * @param voPE
     *            The value object serving as template.
     * @return A domain object representation of the value object.
     * @throws ValidationException
     *             Thrown in case the priced event has a negative price.
     */
    public static PricedEvent toPricedEvent(VOPricedEvent voPE)
            throws ValidationException {
        validatePricedEvent(voPE);
        PricedEvent result = new PricedEvent();
        result.setEventPrice(voPE.getEventPrice());
        return result;
    }

    /**
     * Creates a priced event from the value object. In the result object, the
     * reference to the event and the price model must still be set!
     * 
     * @param voPE
     *            The value object serving as template.
     * @return A domain object representation of the value object.
     * @throws ValidationException
     *             Thrown in case the priced event has a negative price.
     * @throws ConcurrentModificationException
     *             Thrown in case the value object's version does not match the
     *             current domain object's.
     */
    public static PricedEvent updatePricedEvent(VOPricedEvent voPE,
            PricedEvent pEvt) throws ValidationException,
            ConcurrentModificationException {
        verifyVersionAndKey(pEvt, voPE);
        validatePricedEvent(voPE);
        pEvt.setEventPrice(voPE.getEventPrice());
        return pEvt;
    }

    static void validatePricedEvent(VOPricedEvent pricedEvent)
            throws ValidationException {
        BLValidator.isNonNegativeNumber(FIELD_NAME_EVENT_PRICE,
                pricedEvent.getEventPrice());
        BLValidator.isValidPriceScale(FIELD_NAME_EVENT_PRICE,
                pricedEvent.getEventPrice());
        List<VOSteppedPrice> steppedPriceList = pricedEvent.getSteppedPrices();
        SteppedPriceAssembler.validateSteppedPrice(steppedPriceList);
    }
}
