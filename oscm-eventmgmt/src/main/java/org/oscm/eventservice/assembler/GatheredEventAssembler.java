/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 08.06.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.eventservice.assembler;

import java.math.BigDecimal;

import org.oscm.domobjects.GatheredEvent;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOGatheredEvent;

/**
 * Assembler to convert the event data to be gathered.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class GatheredEventAssembler extends BaseAssembler {

    public static GatheredEvent toGatheredEvent(VOGatheredEvent voEvent)
            throws ValidationException {

        if (voEvent == null) {
            return null;
        }

        validate(voEvent);
        GatheredEvent result = new GatheredEvent();
        result.setActor(voEvent.getActor());
        result.setEventId(voEvent.getEventId());
        result.setOccurrenceTime(voEvent.getOccurrenceTime());
        result.setMultiplier(voEvent.getMultiplier());
        result.setUniqueId(voEvent.getUniqueId());
        return result;
    }

    private static void validate(VOGatheredEvent event)
            throws ValidationException {
        BLValidator.isDescription("actor", event.getActor(), false);
        BLValidator.isDescription("uniqueId", event.getUniqueId(), false);
        BLValidator.isInRange("occurrenceTime",
                new BigDecimal(event.getOccurrenceTime()), new BigDecimal(0),
                new BigDecimal(Long.MAX_VALUE));
        BLValidator.isInRange("multiplier",
                new BigDecimal(event.getMultiplier()), new BigDecimal(1),
                new BigDecimal(Long.MAX_VALUE));
    }
}
