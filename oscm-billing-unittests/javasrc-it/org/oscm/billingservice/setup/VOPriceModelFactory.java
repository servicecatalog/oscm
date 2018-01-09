/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 22.04.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.setup;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.vo.VOEventDefinition;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterOption;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOPricedEvent;
import org.oscm.internal.vo.VOPricedOption;
import org.oscm.internal.vo.VOPricedParameter;
import org.oscm.internal.vo.VOPricedRole;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSteppedPrice;
import org.oscm.internal.vo.VOTechnicalService;

/**
 * @author baumann
 */
public class VOPriceModelFactory {

    public enum TestPriceModel {
        EXAMPLE_PERUNIT_MONTH_ROLES, //
        EXAMPLE_PERUNIT_MONTH_ROLES_1, //
        EXAMPLE_PERUNIT_MONTH_ROLES_2, //
        EXAMPLE_PERUNIT_MONTH_ROLES_PARS, //
        EXAMPLE_PERUNIT_MONTH_ROLES_PARS2, //
        EXAMPLE_PERUNIT_MONTH_ROLES_PARS_EVENTS_FREEP, //
        EXAMPLE_PERUNIT_MONTH_ROLES_PARS_FREEP, //
        EXAMPLE_PERUNIT_MONTH_STEPPED_EVENTS, //
        EXAMPLE_PERUNIT_MONTH_USER_STEPPS_ROLES_PARS_FREEP, //
        EXAMPLE_PERUNIT_MONTH, //

        EXAMPLE_PERUNIT_WEEK_FREEP, //
        EXAMPLE_PERUNIT_WEEK_FREEP_1, //
        EXAMPLE_PERUNIT_WEEK_FREEP_2, //
        EXAMPLE_PERUNIT_WEEK_FREEP_3, //
        EXAMPLE_PERUNIT_WEEK_ROLES, //
        EXAMPLE_PERUNIT_WEEK_ROLES_EVENTS_FREEP, //
        EXAMPLE_PERUNIT_WEEK_ROLES_EVENTS_FREEP2, //
        EXAMPLE_PERUNIT_WEEK_ROLES_FREEP, //
        EXAMPLE_PERUNIT_WEEK_ROLES_FREEP_2, //
        EXAMPLE_PERUNIT_WEEK_ROLES_FREEP_3, //
        EXAMPLE_PERUNIT_WEEK_ROLES_PARS, //
        EXAMPLE_PERUNIT_WEEK_ROLES_PARS2, //
        EXAMPLE_PERUNIT_WEEK_ROLES_PARS3, //
        EXAMPLE_PERUNIT_WEEK_ROLES_PARS_FREEP, //
        EXAMPLE_PERUNIT_WEEK_ROLES_PARS_FREEP_2, //
        EXAMPLE_PERUNIT_WEEK_ROLES_PARS_STEPPED, //
        EXAMPLE_PERUNIT_WEEK_ROLES_STEPPED_FREEP, //
        EXAMPLE_PERUNIT_WEEK_EVENTS_PARS_STEPPED, //

        EXAMPLE_PERUNIT_DAY_ROLES, //
        EXAMPLE_FP_PUDAY_ROLES_PAR_EVENTS, //
        EXAMPLE_FP_PUDAY_ROLES_PAR_EVENTS2, //
        EXAMPLE_PERUNIT_DAY_EVENTS_PARS_STEPPED, //

        EXAMPLE_PERUNIT_HOUR_ROLES, //

        EXAMPLE_PRORATA_MONTH_ROLES, //
        EXAMPLE_PRORATA_MONTH_ROLES_2, //
        EXAMPLE_PRORATA_MONTH_ROLES_3, //

        EXAMPLE_RATA_WEEK_ROLES, //
        EXAMPLE_RATA_WEEK_ROLES_PARS, //
        EXAMPLE_RATA_WEEK_ROLES_PARS_FREEP, //
        EXAMPLE_RATA_WEEK_ROLES_EVENTS_FREEP, //
        EXAMPLE_RATA_WEEK_ROLES_PAR_B, //
        EXAMPLE_RATA_WEEK_ROLES_PAR_I, //
        EXAMPLE_RATA_WEEK_32, //
        EXAMPLE_RATA_WEEK_EVENTS_PARS_STEPPED, //

        EXAMPLE_RATA_DAY_ROLES, //
        EXAMPLE_RATA_DAY_EVENTS_PARS_STEPPED, //

        EXAMPLE_PICT01_RATA_DAY, //
        EXAMPLE_PICT02_RATA_WEEK, //
        EXAMPLE_PICT02_RATA_WEEK2, //
        EXAMPLE_PICT03_5_UNIT_HOUR, //
        EXAMPLE_PICT03_UNIT_HOUR, //
        EXAMPLE_PICT04_PERUNIT_DAY, //
        EXAMPLE_PICT05_UNIT_WEEK, //
        EXAMPLE_PICT05_RATA_WEEK, //
        EXAMPLE_PICT05_RATA_WEEK2, //
        EXAMPLE_PICT05_1_UNIT_WEEK, //
        EXAMPLE_PICT05_1_RATA_WEEK, //
        EXAMPLE_PICT06_UNIT_HOUR, //
        EXAMPLE_PICT06_RATA_HOUR, //
        EXAMPLE_PICT07_RATA_MONTH, //
        EXAMPLE_PICT08_RATA_WEEK, //
        EXAMPLE_PICT09_UNIT_HOUR, //
        EXAMPLE_PICT10_UNIT_MONTH, //
        EXAMPLE_PICT10_2_UNIT_MONTH, //
        EXAMPLE_PICT13_UNIT_HOUR, //
        EXAMPLE_PICT12_RATA_MONTH, //
        EXAMPLE_PICT11_UNIT_WEEK, //
        EXAMPLE_PICT11_2_UNIT_WEEK, //
        EXAMPLE_PICT13_UNIT_DAY, //
        EXAMPLE_PICT14_RATA_HOURS, //
        EXAMPLE_PICT15_UNIT_HOURS, //
        EXAMPLE_PICT15_RATA_HOURS, //
        EXAMPLE_PICT16_RATA_DAYS, //
        EXAMPLE_PICT16_1_RATA_DAYS, //
        EXAMPLE_PICT16_2_RATA_DAYS, //
        EXAMPLE_PICT17_RATA_WEEKS, //
        EXAMPLE_PICT19_UNIT_HOUR, //
        EXAMPLE_PICT20_UNIT_MONTH, //
        EXAMPLE_PICT21_RATA_WEEK, //
        EXAMPLE_PICT22_RATA_HOUR, //
        EXAMPLE_PICT24_RATA_MONTH, //
        EXAMPLE_PICT24_UNIT_HOUR, //
        EXAMPLE_PICT26_UNIT_WEEKS, //
        EXAMPLE_PICT25_RATA_WEEKS, //
        EXAMPLE_PICT25_3_RATA_WEEKS, //
        EXAMPLE_PICT26_RATA_WEEKS, //
        EXAMPLE_PICT28_UNIT_WEEKS, //
        EXAMPLE_PICT28_RATA_WEEKS, //
        EXAMPLE_PICT27_RATA_WEEKS, //
        EXAMPLE_PICT28_1_UNIT_WEEKS, //
        EXAMPLE_PICT28_1_RATA_WEEKS, //
        EXAMPLE_PICT29_UNIT_WEEKS, //
        EXAMPLE_PICT30, //
        EXAMPLE_PICT32_UNIT_WEEKS, //
        EXAMPLE_PICT32_UNIT_WEEKS_2, //
        EXAMPLE_PICT33_UNIT_WEEKS, //
        EXAMPLE_PICT34_UNIT_WEEKS, //
        EXAMPLE_PICT35, //
        EXAMPLE_PICT36_RATA_WEEKS, //
        EXAMPLE_PICT36_UNIT_WEEKS, //
        EXAMPLE_PICT37_RATA_MONTH, //
        EXAMPLE_PICT38_RATA_HOUR, //
        EXAMPLE_PICT38_UNIT_DAYS, //
        EXAMPLE_PICT40_UNIT_WEEKS, //
        EXAMPLE_PICT41_RATA_HOUR, //
        EXAMPLE_PICT42_UNIT_MONTH, //
        EXAMPLE_PICT44_UNIT_MONTH, //
        EXAMPLE_PICT46_UNIT_WEEKS, //
        EXAMPLE_PICT47_UNIT_WEEKS, //
        EXAMPLE_PICT48_RATA_WEEKS, //
        EXAMPLE_PICT49_1_HOUR_ROLES, //
        EXAMPLE_PICT49_5, //
        EXAMPLE_PICT49_HOUR_ROLES, //
        EXAMPLE_PICT50_RATA_HOUR, //
        EXAMPLE_RATA_WEEK_PARAM, //
        EXAMPLE_UNIT_WEEK_PARAM, //
        EXAMPLE_UNIT_MONTH_PARAM, //
        EXAMPLE_RATA_MONTH_PARAM, //
        EXAMPLE_TEST,

        FREE//

    }

    public static VOPriceModel createVOPriceModel(
            TestPriceModel testPriceModel, VOServiceDetails voServiceDetails) {
        switch (testPriceModel) {
        case EXAMPLE_PICT49_5:
            return createPriceModelExamplePICT49_5(voServiceDetails);
        case EXAMPLE_PERUNIT_MONTH_ROLES:
            return createPriceModelExamplePerUnitMonthRoles(voServiceDetails);
        case EXAMPLE_PERUNIT_MONTH_ROLES_1:
            return createPriceModelExamplePerUnitMonthRoles_1(voServiceDetails);
        case EXAMPLE_PERUNIT_MONTH_ROLES_2:
            return createPriceModelExamplePerUnitMonthRoles2(voServiceDetails);
        case EXAMPLE_PERUNIT_MONTH_ROLES_PARS:
            return createPriceModelExamplePerUnitMonthRolesPars(voServiceDetails);
        case EXAMPLE_PERUNIT_MONTH_ROLES_PARS2:
            return createPriceModelExamplePerUnitMonthRolesPars2(voServiceDetails);
        case EXAMPLE_PERUNIT_MONTH_ROLES_PARS_FREEP:
            return createPriceModelExamplePerUnitMonthRolesParsFreeP(voServiceDetails);
        case EXAMPLE_PERUNIT_MONTH_USER_STEPPS_ROLES_PARS_FREEP:
            return createPriceModelExamplePerUnitMonthUserSteppsRolesParsFreeP(voServiceDetails);
        case EXAMPLE_PERUNIT_MONTH_ROLES_PARS_EVENTS_FREEP:
            return createPriceModelExamplePerUnitMonthRolesParsEventsFreeP(voServiceDetails);
        case EXAMPLE_PERUNIT_MONTH_STEPPED_EVENTS:
            return createPriceModelExamplePerUnitMonthSteppedEvents(voServiceDetails);
        case EXAMPLE_PERUNIT_MONTH:
            return createPriceModelExamplePerUnitMonth(voServiceDetails);
        case EXAMPLE_PRORATA_MONTH_ROLES:
            return createPriceModelExampleProRataMonthRoles(voServiceDetails);
        case EXAMPLE_PRORATA_MONTH_ROLES_2:
            return createPriceModelExampleProRataMonthRoles_2();
        case EXAMPLE_PRORATA_MONTH_ROLES_3:
            return createPriceModelExampleProRataMonthRoles_3();
        case EXAMPLE_PERUNIT_WEEK_ROLES_EVENTS_FREEP:
            return createPriceModelExamplePerUnitWeekRolesEventsFreeP(voServiceDetails);
        case EXAMPLE_PERUNIT_WEEK_ROLES_EVENTS_FREEP2:
            return createPriceModelExamplePerUnitWeekRolesEventsFreeP2(voServiceDetails);
        case EXAMPLE_PERUNIT_WEEK_FREEP:
            return createPriceModelExamplePerUnitWeekFreeP();
        case EXAMPLE_PERUNIT_WEEK_FREEP_2:
            return createPriceModelExamplePerUnitWeekFreeP_2(voServiceDetails);
        case EXAMPLE_PERUNIT_WEEK_FREEP_1:
            return createPriceModelExamplePerUnitWeekFreeP_1(voServiceDetails);
        case EXAMPLE_PERUNIT_WEEK_FREEP_3:
            return createPriceModelExamplePerUnitWeekFreeP_3(voServiceDetails);
        case EXAMPLE_PERUNIT_WEEK_ROLES_FREEP:
            return createPriceModelExamplePerUnitWeekRolesFreeP(voServiceDetails);
        case EXAMPLE_PERUNIT_WEEK_ROLES_FREEP_2:
            return createPriceModelExamplePerUnitWeekRolesFreeP2(voServiceDetails);
        case EXAMPLE_PERUNIT_WEEK_ROLES_FREEP_3:
            return createPriceModelExamplePerUnitWeekRolesFreeP3(voServiceDetails);
        case EXAMPLE_PERUNIT_WEEK_ROLES_PARS:
            return createPriceModelExamplePerUnitWeekRolesPars(voServiceDetails);
        case EXAMPLE_PERUNIT_WEEK_ROLES_PARS2:
            return createPriceModelExamplePerUnitWeekRolesPars2(voServiceDetails);
        case EXAMPLE_PERUNIT_WEEK_ROLES_PARS3:
            return createPriceModelExamplePerUnitWeekRolesPars3(voServiceDetails);
        case EXAMPLE_PERUNIT_WEEK_ROLES_PARS_FREEP:
            return createPriceModelExamplePerUnitWeekRolesParsFreeP(voServiceDetails);
        case EXAMPLE_PERUNIT_WEEK_ROLES_PARS_FREEP_2:
            return createPriceModelExamplePerUnitWeekRolesParsFreeP2(voServiceDetails);
        case EXAMPLE_PERUNIT_WEEK_ROLES_PARS_STEPPED:
            return createPriceModelExamplePerUnitWeekRolesParsStepped(voServiceDetails);
        case EXAMPLE_PERUNIT_WEEK_ROLES:
            return createPriceModelExamplePerUnitWeekRoles(voServiceDetails);
        case EXAMPLE_PERUNIT_WEEK_ROLES_STEPPED_FREEP:
            return createPriceModelExamplePerUnitWeekRolesStepped(voServiceDetails);
        case EXAMPLE_PERUNIT_WEEK_EVENTS_PARS_STEPPED:
            return createPriceModelExamplePerUnitWeekEventsParsStepped(voServiceDetails);
        case EXAMPLE_RATA_WEEK_ROLES_EVENTS_FREEP:
            return createPriceModelExampleRataWeekRolesEventsFreeP(voServiceDetails);
        case EXAMPLE_RATA_WEEK_ROLES:
            return createPriceModelExamplePerRataWeekRolesNoPar(voServiceDetails);
        case EXAMPLE_RATA_WEEK_32:
            return createPriceModelExamplePerRataWeek32(voServiceDetails);
        case EXAMPLE_RATA_WEEK_ROLES_PARS:
            return createPriceModelExampleRataWeekRolesPars(voServiceDetails);
        case EXAMPLE_RATA_WEEK_ROLES_PAR_B:
            return createPriceModelExampleRataWeekRolesPar_B(voServiceDetails);
        case EXAMPLE_RATA_WEEK_ROLES_PAR_I:
            return createPriceModelExampleRataWeekRolesPar_I(voServiceDetails);
        case EXAMPLE_RATA_WEEK_EVENTS_PARS_STEPPED:
            return createPriceModelExampleProRataWeekEventsParsStepped(voServiceDetails);
        case EXAMPLE_PICT50_RATA_HOUR:
            return createPriceModelPict50(voServiceDetails);
        case EXAMPLE_PICT36_RATA_WEEKS:
            return createPriceModelPict36(voServiceDetails);
        case EXAMPLE_PICT36_UNIT_WEEKS:
            return createPriceModelExamplePICT36_Unit_Weeks(voServiceDetails);
        case EXAMPLE_PICT34_UNIT_WEEKS:
            return createPriceModelExamplePICT34_Unit_Weeks(voServiceDetails);
        case EXAMPLE_PICT33_UNIT_WEEKS:
            return createPriceModelExamplePICT33_Unit_Weeks(voServiceDetails);
        case EXAMPLE_PICT32_UNIT_WEEKS:
            return createPriceModelExamplePICT32_Unit_Weeks(voServiceDetails);
        case EXAMPLE_PICT32_UNIT_WEEKS_2:
            return createPriceModelExamplePICT32_Unit_Weeks_2(voServiceDetails);
        case EXAMPLE_PICT30:
            return createPriceModelExamplePICT30(voServiceDetails);
        case EXAMPLE_PICT29_UNIT_WEEKS:
            return createPriceModelExamplePICT29_Unit_Weeks(voServiceDetails);
        case EXAMPLE_PICT26_UNIT_WEEKS:
            return createPriceModelExamplePICT26_Unit_Weeks(voServiceDetails);
        case EXAMPLE_PICT14_RATA_HOURS:
            return createPriceModelExamplePICT14(voServiceDetails);
        case EXAMPLE_PICT15_UNIT_HOURS:
            return createPriceModelExamplePICT15_2(voServiceDetails);
        case EXAMPLE_PICT15_RATA_HOURS:
            return createPriceModelExamplePICT15(voServiceDetails);
        case EXAMPLE_PICT16_RATA_DAYS:
            return createPriceModelExamplePICT16(voServiceDetails);
        case EXAMPLE_PICT16_1_RATA_DAYS:
            return createPriceModelExamplePICT16_1(voServiceDetails);
        case EXAMPLE_PICT16_2_RATA_DAYS:
            return createPriceModelExamplePICT16_2(voServiceDetails);
        case EXAMPLE_PICT17_RATA_WEEKS:
            return createPriceModelExamplePICT17(voServiceDetails);
        case EXAMPLE_PICT19_UNIT_HOUR:
            return createPriceModelExamplePICT19(voServiceDetails);
        case EXAMPLE_PICT20_UNIT_MONTH:
            return createPriceModelExamplePICT20(voServiceDetails);
        case EXAMPLE_PICT21_RATA_WEEK:
            return createPriceModelExamplePICT21(voServiceDetails);
        case EXAMPLE_PICT22_RATA_HOUR:
            return createPriceModelExamplePICT22(voServiceDetails);
        case EXAMPLE_PICT24_RATA_MONTH:
            return createPriceModelExamplePICT24(voServiceDetails);
        case EXAMPLE_PICT24_UNIT_HOUR:
            return createPriceModelExamplePICT24_2(voServiceDetails);
        case EXAMPLE_PICT28_UNIT_WEEKS:
            return createPriceModelExamplePICT28_Unit_Weeks(voServiceDetails);
        case EXAMPLE_PICT25_RATA_WEEKS:
            return createPriceModelExamplePICT25_Rata_Weeks(voServiceDetails);
        case EXAMPLE_PICT25_3_RATA_WEEKS:
            return createPriceModelExamplePICT25_3_Rata_Weeks(voServiceDetails);
        case EXAMPLE_PICT26_RATA_WEEKS:
            return createPriceModelExamplePICT26_Rata_Weeks(voServiceDetails);
        case EXAMPLE_PICT28_RATA_WEEKS:
            return createPriceModelExamplePICT28_Rata_Weeks(voServiceDetails);
        case EXAMPLE_PICT27_RATA_WEEKS:
            return createPriceModelExamplePICT27_Rata_Weeks(voServiceDetails);
        case EXAMPLE_PICT28_1_UNIT_WEEKS:
            return createPriceModelExamplePICT28_1_Unit_Weeks(voServiceDetails);
        case EXAMPLE_PICT28_1_RATA_WEEKS:
            return createPriceModelExamplePICT28_1_Rata_Weeks(voServiceDetails);
        case EXAMPLE_PICT41_RATA_HOUR:
            return createPriceModelPict41(voServiceDetails);
        case EXAMPLE_PICT38_RATA_HOUR:
            return createPriceModelPict38(voServiceDetails);
        case EXAMPLE_PICT37_RATA_MONTH:
            return createPriceModelPict37(voServiceDetails);
        case EXAMPLE_RATA_WEEK_ROLES_PARS_FREEP:
            return createPriceModelExamplePerUnitWeekRolesFreePRata(voServiceDetails);
        case EXAMPLE_PERUNIT_DAY_ROLES:
            return createPriceModelExamplePerUnitDayRoles(voServiceDetails);
        case EXAMPLE_PERUNIT_DAY_EVENTS_PARS_STEPPED:
            return createPriceModelExamplePerUnitDayEventsParsStepped(voServiceDetails);
        case EXAMPLE_RATA_DAY_ROLES:
            return createPriceModelExampleProRataDayRoles(voServiceDetails);
        case EXAMPLE_RATA_DAY_EVENTS_PARS_STEPPED:
            return createPriceModelExampleProRataDayEventsParsStepped(voServiceDetails);
        case EXAMPLE_FP_PUDAY_ROLES_PAR_EVENTS:
            return createPriceModelExample_FP_PUDayRolesParEvents(voServiceDetails);
        case EXAMPLE_FP_PUDAY_ROLES_PAR_EVENTS2:
            return createPriceModelExample_FP_PUDayRolesParEvents2(voServiceDetails);
        case EXAMPLE_PERUNIT_HOUR_ROLES:
            return createPriceModelExamplePerUnitHourRoles(voServiceDetails);
        case EXAMPLE_PICT49_HOUR_ROLES:
            return createPriceModelExamplePICT49PerUnitHourRoles(voServiceDetails);
        case EXAMPLE_PICT49_1_HOUR_ROLES:
            return createPriceModelExamplePICT49_1PerUnitHourRoles(voServiceDetails);
        case EXAMPLE_PICT48_RATA_WEEKS:
            return createPriceModelExamplePICT48_Rata_Weeks(voServiceDetails);
        case EXAMPLE_PICT40_UNIT_WEEKS:
            return createPriceModelExamplePICT40_Unit_Weeks(voServiceDetails);
        case EXAMPLE_PICT35:
            return createPriceModelExamplePICT35_Rata_Days(voServiceDetails);
        case EXAMPLE_PICT38_UNIT_DAYS:
            return createPriceModelExamplePICT38_Unit_Days(voServiceDetails);
        case EXAMPLE_PICT47_UNIT_WEEKS:
            return createPriceModelExamplePICT47_Unit_Weeks(voServiceDetails);
        case EXAMPLE_PICT46_UNIT_WEEKS:
            return createPriceModelExamplePICT46_Unit_Weeks(voServiceDetails);
        case EXAMPLE_PICT44_UNIT_MONTH:
            return createPriceModelExamplePICT44_Unit_Month(voServiceDetails);
        case EXAMPLE_PICT42_UNIT_MONTH:
            return createPriceModelExamplePICT42_Unit_Month(voServiceDetails);
        case EXAMPLE_PICT13_UNIT_DAY:
            return createPriceModelExamplePICT13_PerUnitDay(voServiceDetails);
        case EXAMPLE_PICT13_UNIT_HOUR:
            return createPriceModelExamplePICT13_PerUnitHour(voServiceDetails);
        case EXAMPLE_PICT12_RATA_MONTH:
            return createPriceModelExamplePICT12_ProRataMonth(voServiceDetails);
        case EXAMPLE_PICT11_UNIT_WEEK:
            return createPriceModelExamplePICT11_PerUnitWeek(voServiceDetails);
        case EXAMPLE_PICT11_2_UNIT_WEEK:
            return createPriceModelExamplePICT11_2_PerUnitWeek(voServiceDetails);
        case EXAMPLE_PICT10_UNIT_MONTH:
            return createPriceModelExamplePICT10_PerUnitMonth(voServiceDetails);
        case EXAMPLE_PICT10_2_UNIT_MONTH:
            return createPriceModelExamplePICT10_2_PerUnitMonth(voServiceDetails);
        case EXAMPLE_PICT09_UNIT_HOUR:
            return createPriceModelExamplePICT09_PerUnitHour(voServiceDetails);
        case EXAMPLE_PICT08_RATA_WEEK:
            return createPriceModelExamplePICT08_Rata_Weeks(voServiceDetails);
        case EXAMPLE_PICT07_RATA_MONTH:
            return createPriceModelExamplePICT07_ProRataMonth(voServiceDetails);
        case EXAMPLE_PICT06_UNIT_HOUR:
            return createPriceModelExamplePICT06_PerUnitHour(voServiceDetails);
        case EXAMPLE_PICT06_RATA_HOUR:
            return createPriceModelExamplePICT06_ProRataHour(voServiceDetails);
        case EXAMPLE_PICT05_UNIT_WEEK:
            return createPriceModelExamplePICT05_Unit_Weeks(voServiceDetails);
        case EXAMPLE_PICT05_RATA_WEEK:
            return createPriceModelExamplePICT05_Rata_Weeks(voServiceDetails);
        case EXAMPLE_PICT05_RATA_WEEK2:
            return createPriceModelExamplePICT05_Rata_Weeks2(voServiceDetails);
        case EXAMPLE_PICT05_1_UNIT_WEEK:
            return createPriceModelExamplePICT05_1_Unit_Weeks(voServiceDetails);
        case EXAMPLE_PICT05_1_RATA_WEEK:
            return createPriceModelExamplePICT05_1_Rata_Weeks(voServiceDetails);
        case EXAMPLE_PICT04_PERUNIT_DAY:
            return createPriceModelExamplePict04PerUnitDay(voServiceDetails);
        case EXAMPLE_PICT03_UNIT_HOUR:
            return createPriceModelExamplePICT03_PerUnitHourStepRolesEvents(voServiceDetails);
        case EXAMPLE_PICT03_5_UNIT_HOUR:
            return createPriceModelExamplePICT03_5_PerUnitHourStepRolesEvents(voServiceDetails);
        case EXAMPLE_PICT02_RATA_WEEK:
            return createPriceModelExamplePICT02_Rata_Week(voServiceDetails);
        case EXAMPLE_PICT02_RATA_WEEK2:
            return createPriceModelExamplePICT02_Rata_Week2(voServiceDetails);
        case EXAMPLE_PICT01_RATA_DAY:
            return createPriceModelExamplePict01RataDay();
        case EXAMPLE_RATA_WEEK_PARAM:
            return createPriceModelExamplePR_WeekPar(voServiceDetails);
        case EXAMPLE_UNIT_WEEK_PARAM:
            return createPriceModelExampleTU_WeekPar(voServiceDetails);
        case EXAMPLE_RATA_MONTH_PARAM:
            return createPriceModelExamplePR_MonthPar(voServiceDetails);
        case EXAMPLE_UNIT_MONTH_PARAM:
            return createPriceModelExampleTU_MonthPar(voServiceDetails);
        case EXAMPLE_TEST:
            return createPriceModelExampleTest();
        case FREE:
            return createFreePriceModel();
        default:
            return null;
        }
    }

    private static VOPriceModel createPriceModelExamplePerUnitMonth(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.MONTH, "EUR", new BigDecimal("10.00"),
                new BigDecimal("20.00"), new BigDecimal("30.00"), 0);
        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitMonthRoles(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.MONTH, "EUR", new BigDecimal("123.00"),
                new BigDecimal("678.00"), new BigDecimal("345.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitMonthRoles_1(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.MONTH, "EUR", new BigDecimal("0.00"),
                new BigDecimal("678.00"), new BigDecimal("345.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitMonthRoles2(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.MONTH, "EUR", new BigDecimal("254.00"),
                new BigDecimal("734.00"), new BigDecimal("434.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitMonthRolesPars(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.MONTH, "EUR", new BigDecimal("123.00"),
                new BigDecimal("678.00"), new BigDecimal("345.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(15),
                new BigDecimal(8), new BigDecimal(5));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays.asList(
                newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "HAS_OPTIONS"),
                        new BigDecimal("2.00"), new BigDecimal("1.10"),
                        pricedRoles),
                newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "2.00"), new BigDecimal("20.00"), pricedRoles),
                newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "BOOLEAN_PARAMETER"), new BigDecimal(
                        "3.00"), new BigDecimal("30.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitMonthRolesPars2(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.MONTH, "EUR", new BigDecimal("123.00"),
                new BigDecimal("678.00"), new BigDecimal("345.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(15),
                new BigDecimal(8), new BigDecimal(5));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays.asList(
                newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "LONG_NUMBER"),
                        new BigDecimal("0.80"), new BigDecimal("0.30"),
                        pricedRoles),
                newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "PERIOD"), new BigDecimal("1.40"),
                        new BigDecimal("3.80"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitMonthUserSteppsRolesParsFreeP(
            VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.MONTH, "EUR", new BigDecimal("25.00"),
                new BigDecimal("85.00"), //
                Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(1), new BigDecimal(
                                "150.00")),
                        newVOSteppedPrice(Long.valueOf(2), new BigDecimal(
                                "120.00")),
                        newVOSteppedPrice(Long.valueOf(3), new BigDecimal(
                                "110.00")),
                        newVOSteppedPrice(null, new BigDecimal("100.00")) }), //
                3);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "BOOLEAN_PARAMETER"), new BigDecimal(
                        "3.00"), new BigDecimal("30.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitMonthRolesParsFreeP(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.MONTH, "EUR", new BigDecimal("123.00"),
                new BigDecimal("678.00"), new BigDecimal("345.00"), 3);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays.asList(
                newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "HAS_OPTIONS"),
                        new BigDecimal("2.00"), new BigDecimal("1.10"),
                        pricedRoles),
                newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "2.00"), new BigDecimal("20.00"), pricedRoles),
                newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "BOOLEAN_PARAMETER"), new BigDecimal(
                        "3.00"), new BigDecimal("30.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitMonthRolesParsEventsFreeP(
            VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.MONTH, "EUR", new BigDecimal("123.00"),
                new BigDecimal("678.00"), new BigDecimal("345.00"), 3);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(voTechService, "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays.asList(
                newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "HAS_OPTIONS"),
                        new BigDecimal("2.00"), new BigDecimal("1.10"),
                        pricedRoles),
                newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "2.00"), new BigDecimal("20.00"), pricedRoles),
                newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "BOOLEAN_PARAMETER"), new BigDecimal(
                        "3.00"), new BigDecimal("30.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("10")));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitWeekRolesEventsFreeP(
            VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("25.00"),
                new BigDecimal("85.00"), new BigDecimal("10.00"), 5);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(voTechService, "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("10")));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitWeekRolesEventsFreeP2(
            VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("25.00"),
                new BigDecimal("85.00"), new BigDecimal("10.00"), 2);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(voTechService, "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("10")));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExampleRataWeekRolesEventsFreeP(
            VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("25.00"),
                new BigDecimal("85.00"), new BigDecimal("10.00"), 2);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(voTechService, "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("10")));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitMonthSteppedEvents(
            VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.MONTH, "EUR", new BigDecimal("123.00"),
                new BigDecimal("678.00"), new BigDecimal("345.00"), 0);

        List<VOPricedEvent> consideredEvents = Arrays
                .asList(newVOPricedEvent(VOTechServiceFactory
                        .getEventDefinition(voTechService,
                                "USER_LOGIN_TO_SERVICE"), new BigDecimal("13")),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "USER_LOGOUT_FROM_SERVICE"),
                                new BigDecimal("12")),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "FILE_DOWNLOAD"), //
                                Arrays.asList(new VOSteppedPrice[] {
                                        newVOSteppedPrice(Long.valueOf(1),
                                                new BigDecimal("10.00")),
                                        newVOSteppedPrice(Long.valueOf(13),
                                                new BigDecimal("5.00")),
                                        newVOSteppedPrice(Long.valueOf(80),
                                                new BigDecimal("2.00")),
                                        newVOSteppedPrice(Long.valueOf(200),
                                                new BigDecimal("1.00")),
                                        newVOSteppedPrice(null, //
                                                new BigDecimal("0.50")) })),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "FILE_UPLOAD"), new BigDecimal("9")),
                        newVOPricedEvent(
                                VOTechServiceFactory.getEventDefinition(
                                        voTechService, "FOLDER_NEW"),
                                new BigDecimal("110.25")));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExampleProRataMonthRoles(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.MONTH, "EUR", new BigDecimal("254.00"),
                new BigDecimal("734.00"), new BigDecimal("434.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExampleProRataMonthRoles_2() {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.MONTH, "EUR", new BigDecimal("0.00"),
                new BigDecimal("734.00"), new BigDecimal("434.00"), 0);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExampleProRataMonthRoles_3() {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.MONTH, "EUR", new BigDecimal("10.00"),
                new BigDecimal("734.00"), new BigDecimal("434.00"), 0);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitWeekRolesFreeP(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("25.00"),
                new BigDecimal("85.00"), new BigDecimal("150.00"), 5);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitWeekRolesFreeP3(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("25.00"),
                new BigDecimal("85.00"), new BigDecimal("150.00"), 2);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitWeekRolesFreeP2(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("25.00"),
                new BigDecimal("85.00"), new BigDecimal("150.00"), 2);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitWeekRolesPars(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("25.00"),
                new BigDecimal("85.00"), new BigDecimal("150.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays.asList(
                newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "HAS_OPTIONS"),
                        new BigDecimal("2.00"), new BigDecimal("1.10"),
                        pricedRoles),
                newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "2.00"), new BigDecimal("20.00"), pricedRoles),
                newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "BOOLEAN_PARAMETER"), new BigDecimal(
                        "3.00"), new BigDecimal("30.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitWeekRolesPars2(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("2.00"),
                new BigDecimal("5.00"), new BigDecimal("10.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(3), new BigDecimal(2),
                new BigDecimal(1));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "1.00"), new BigDecimal("10.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitWeekRolesPars3(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("4.00"),
                new BigDecimal("10.00"), new BigDecimal("20.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(4),
                new BigDecimal(2));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "2.00"), new BigDecimal("20.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitWeekRolesParsFreeP(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("25.00"),
                new BigDecimal("85.00"), new BigDecimal("150.00"), 2);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays.asList(
                newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "HAS_OPTIONS"),
                        new BigDecimal("2.00"), new BigDecimal("1.10"),
                        pricedRoles),
                newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "2.00"), new BigDecimal("20.00"), pricedRoles),
                newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "BOOLEAN_PARAMETER"), new BigDecimal(
                        "3.00"), new BigDecimal("30.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitWeekRolesParsFreeP2(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("25.00"),
                new BigDecimal("85.00"), new BigDecimal("150.00"), 1);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays.asList(
                newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "HAS_OPTIONS"),
                        new BigDecimal("2.00"), new BigDecimal("1.10"),
                        pricedRoles),
                newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "2.00"), new BigDecimal("20.00"), pricedRoles),
                newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "BOOLEAN_PARAMETER"), new BigDecimal(
                        "3.00"), new BigDecimal("30.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitWeekRolesParsStepped(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("25.00"),
                new BigDecimal("85.00"), //
                Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(1), new BigDecimal(
                                "150.00")),
                        newVOSteppedPrice(Long.valueOf(2), new BigDecimal(
                                "120.00")),
                        newVOSteppedPrice(Long.valueOf(3), new BigDecimal(
                                "110.00")),
                        newVOSteppedPrice(null, new BigDecimal("100.00")) }), //
                0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(10),
                new BigDecimal(7), new BigDecimal(5));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "PERIOD"), new BigDecimal("1.40"),
                        new BigDecimal("3.80"), pricedRoles),
                        newVOPricedParameter(VOServiceFactory.getParameter(
                                voServiceDetails, "LONG_NUMBER"), //
                                Arrays.asList(new VOSteppedPrice[] {
                                        newVOSteppedPrice(Long.valueOf(100),
                                                new BigDecimal("1.20")),
                                        newVOSteppedPrice(Long.valueOf(500),
                                                new BigDecimal("1.00")),
                                        newVOSteppedPrice(Long.valueOf(900),
                                                new BigDecimal("0.80")),
                                        newVOSteppedPrice(null, new BigDecimal(
                                                "0.50")) }), //
                                new BigDecimal("0.30"), pricedRoles),
                        newVOPricedParameter(VOServiceFactory.getParameter(
                                voServiceDetails, "BOOLEAN_PARAMETER"),
                                new BigDecimal("3.00"),
                                new BigDecimal("30.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitWeekEventsParsStepped(
            VOServiceDetails voServiceDetails) {

        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("25.00"),
                new BigDecimal("85.00"), //
                Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(1), new BigDecimal(
                                "150.00")),
                        newVOSteppedPrice(Long.valueOf(3), new BigDecimal(
                                "120.00")),
                        newVOSteppedPrice(null, new BigDecimal("100.00")) }), //
                0);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(
                        VOServiceFactory.getParameter(voServiceDetails,
                                "LONG_NUMBER"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(100),
                                        new BigDecimal("5.00")),
                                newVOSteppedPrice(Long.valueOf(300),
                                        new BigDecimal("3.00")),
                                newVOSteppedPrice(Long.valueOf(800),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(null, new BigDecimal("1.00")) }), //
                        new BigDecimal("0.30"), new ArrayList<VOPricedRole>()));
        priceModel.setSelectedParameters(selectedParameters);

        List<VOPricedEvent> consideredEvents = Arrays.asList(newVOPricedEvent(
                VOTechServiceFactory.getEventDefinition(voTechService,
                        "FILE_DOWNLOAD"), //
                Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(10), new BigDecimal(
                                "10.00")),
                        newVOSteppedPrice(Long.valueOf(40), new BigDecimal(
                                "5.00")),
                        newVOSteppedPrice(Long.valueOf(80), new BigDecimal(
                                "2.00")), newVOSteppedPrice(null, //
                                new BigDecimal("1.00")) })));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExampleProRataWeekEventsParsStepped(
            VOServiceDetails voServiceDetails) {

        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("25.00"),
                new BigDecimal("85.00"), //
                Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(1), new BigDecimal(
                                "150.00")),
                        newVOSteppedPrice(Long.valueOf(3), new BigDecimal(
                                "120.00")),
                        newVOSteppedPrice(null, new BigDecimal("100.00")) }), //
                0);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(
                        VOServiceFactory.getParameter(voServiceDetails,
                                "LONG_NUMBER"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(100),
                                        new BigDecimal("5.00")),
                                newVOSteppedPrice(Long.valueOf(300),
                                        new BigDecimal("3.00")),
                                newVOSteppedPrice(Long.valueOf(800),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(null, new BigDecimal("1.00")) }), //
                        new BigDecimal("0.30"), new ArrayList<VOPricedRole>()));
        priceModel.setSelectedParameters(selectedParameters);

        List<VOPricedEvent> consideredEvents = Arrays.asList(newVOPricedEvent(
                VOTechServiceFactory.getEventDefinition(voTechService,
                        "FILE_DOWNLOAD"), //
                Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(10), new BigDecimal(
                                "10.00")),
                        newVOSteppedPrice(Long.valueOf(40), new BigDecimal(
                                "5.00")),
                        newVOSteppedPrice(Long.valueOf(80), new BigDecimal(
                                "2.00")), newVOSteppedPrice(null, //
                                new BigDecimal("1.00")) })));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitDayEventsParsStepped(
            VOServiceDetails voServiceDetails) {

        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.DAY, "EUR", new BigDecimal("25.00"),
                new BigDecimal("85.00"), //
                Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(1), new BigDecimal(
                                "150.00")),
                        newVOSteppedPrice(Long.valueOf(3), new BigDecimal(
                                "120.00")),
                        newVOSteppedPrice(null, new BigDecimal("100.00")) }), //
                0);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(
                        VOServiceFactory.getParameter(voServiceDetails,
                                "LONG_NUMBER"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(100),
                                        new BigDecimal("5.00")),
                                newVOSteppedPrice(Long.valueOf(300),
                                        new BigDecimal("3.00")),
                                newVOSteppedPrice(Long.valueOf(800),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(null, new BigDecimal("1.00")) }), //
                        new BigDecimal("0.30"), new ArrayList<VOPricedRole>()));
        priceModel.setSelectedParameters(selectedParameters);

        List<VOPricedEvent> consideredEvents = Arrays.asList(newVOPricedEvent(
                VOTechServiceFactory.getEventDefinition(voTechService,
                        "FILE_DOWNLOAD"), //
                Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(10), new BigDecimal(
                                "10.00")),
                        newVOSteppedPrice(Long.valueOf(40), new BigDecimal(
                                "5.00")),
                        newVOSteppedPrice(Long.valueOf(80), new BigDecimal(
                                "2.00")), newVOSteppedPrice(null, //
                                new BigDecimal("1.00")) })));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExampleProRataDayRoles(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.DAY, "EUR", new BigDecimal("20.00"),
                new BigDecimal("120.00"), new BigDecimal("60.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExampleProRataDayEventsParsStepped(
            VOServiceDetails voServiceDetails) {

        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.DAY, "EUR", new BigDecimal("25.00"),
                new BigDecimal("85.00"), //
                Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(1), new BigDecimal(
                                "150.00")),
                        newVOSteppedPrice(Long.valueOf(3), new BigDecimal(
                                "120.00")),
                        newVOSteppedPrice(null, new BigDecimal("100.00")) }), //
                0);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(
                        VOServiceFactory.getParameter(voServiceDetails,
                                "LONG_NUMBER"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(100),
                                        new BigDecimal("5.00")),
                                newVOSteppedPrice(Long.valueOf(300),
                                        new BigDecimal("3.00")),
                                newVOSteppedPrice(Long.valueOf(800),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(null, new BigDecimal("1.00")) }), //
                        new BigDecimal("0.30"), new ArrayList<VOPricedRole>()));
        priceModel.setSelectedParameters(selectedParameters);

        List<VOPricedEvent> consideredEvents = Arrays.asList(newVOPricedEvent(
                VOTechServiceFactory.getEventDefinition(voTechService,
                        "FILE_DOWNLOAD"), //
                Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(10), new BigDecimal(
                                "10.00")),
                        newVOSteppedPrice(Long.valueOf(40), new BigDecimal(
                                "5.00")),
                        newVOSteppedPrice(Long.valueOf(80), new BigDecimal(
                                "2.00")), newVOSteppedPrice(null, //
                                new BigDecimal("1.00")) })));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitWeekRolesStepped(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("25.00"),
                new BigDecimal("85.00"), //
                Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(1), new BigDecimal(
                                "150.00")),
                        newVOSteppedPrice(Long.valueOf(2), new BigDecimal(
                                "120.00")),
                        newVOSteppedPrice(Long.valueOf(3), new BigDecimal(
                                "110.00")),
                        newVOSteppedPrice(null, new BigDecimal("100.00")) }), //
                3);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(10),
                new BigDecimal(7), new BigDecimal(5));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitWeekFreeP() {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("25.00"),
                new BigDecimal("85.00"), new BigDecimal("150.00"), 5);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitWeekRoles(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("254.00"),
                new BigDecimal("734.00"), new BigDecimal("434.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitWeekFreeP_2(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("254.00"),
                new BigDecimal("734.00"), new BigDecimal("434.00"), 6);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitWeekFreeP_1(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("254.00"),
                new BigDecimal("734.00"), new BigDecimal("434.00"), 1);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitWeekFreeP_3(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("254.00"),
                new BigDecimal("734.00"), new BigDecimal("434.00"), 9);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerRataWeekRolesNoPar(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("254.00"),
                new BigDecimal("734.00"), new BigDecimal("434.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerRataWeek32(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("0.00"),
                new BigDecimal("734.00"), new BigDecimal("434.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitWeekRolesFreePRata(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("25.00"),
                new BigDecimal("85.00"), new BigDecimal("150.00"), 5);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExampleRataWeekRolesPars(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("25.00"),
                new BigDecimal("85.00"), new BigDecimal("150.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays.asList(
                newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "HAS_OPTIONS"),
                        new BigDecimal("2.00"), new BigDecimal("1.10"),
                        pricedRoles),
                newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "2.00"), new BigDecimal("20.00"), pricedRoles),
                newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "BOOLEAN_PARAMETER"), new BigDecimal(
                        "3.00"), new BigDecimal("30.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePR_WeekPar(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("0.00"),
                new BigDecimal("10.00"), new BigDecimal("0.00"), 0);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "2.00"), new BigDecimal("20.00"),
                        new ArrayList<VOPricedRole>()));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExampleTU_WeekPar(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("0.00"),
                new BigDecimal("10.00"), new BigDecimal("0.00"), 0);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "2.00"), new BigDecimal("20.00"),
                        new ArrayList<VOPricedRole>()));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExampleTU_MonthPar(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.MONTH, "EUR", new BigDecimal("0.00"),
                new BigDecimal("10.00"), new BigDecimal("0.00"), 0);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "2.00"), new BigDecimal("20.00"),
                        new ArrayList<VOPricedRole>()));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePR_MonthPar(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.MONTH, "EUR", new BigDecimal("0.00"),
                new BigDecimal("10.00"), new BigDecimal("0.00"), 0);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "2.00"), new BigDecimal("20.00"),
                        new ArrayList<VOPricedRole>()));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExampleRataWeekRolesPar_B(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("25.00"),
                new BigDecimal("85.00"), new BigDecimal("150.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "BOOLEAN_PARAMETER"), new BigDecimal(
                        "3.00"), new BigDecimal("30.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExampleRataWeekRolesPar_I(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("2.00"),
                new BigDecimal("4.00"), new BigDecimal("8.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(10),
                new BigDecimal(8), new BigDecimal(6));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "2.00"), new BigDecimal("20.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelPict50(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.HOUR, "EUR", new BigDecimal("0.00"),
                new BigDecimal("0.00"), Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(1), new BigDecimal(
                                "10.00")),
                        newVOSteppedPrice(Long.valueOf(2), new BigDecimal(
                                "5.00")),
                        newVOSteppedPrice(Long.valueOf(3), new BigDecimal(
                                "2.00")),
                        newVOSteppedPrice(null, new BigDecimal("1.00")) }), 0);
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("10")));

        priceModel.setConsideredEvents(consideredEvents);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "0.00"), new BigDecimal("0.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelPict36(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("25.00"),
                new BigDecimal("0.00"), new BigDecimal("0.00"), 0);
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("10")));

        priceModel.setConsideredEvents(consideredEvents);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(2),
                new BigDecimal(3));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "BOOLEAN_PARAMETER"), new BigDecimal(
                        "3.00"), new BigDecimal("4.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelPict41(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.HOUR, "EUR", new BigDecimal("25.00"),
                new BigDecimal("10.00"), new BigDecimal("20.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(2), new BigDecimal(3),
                new BigDecimal(4));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "LONG_NUMBER"), new BigDecimal(5),
                        new BigDecimal(10), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelPict38(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.HOUR, "EUR", new BigDecimal("25.00"),
                new BigDecimal("0.01"), new BigDecimal("0.02"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal("0.02"),
                new BigDecimal("0.03"), new BigDecimal("0.04"));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "LONG_NUMBER"),
                        new BigDecimal("0.01"), new BigDecimal("0.01"),
                        pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelPict37(
            VOServiceDetails voServiceDetails) {

        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.MONTH, "EUR", new BigDecimal("25.00"),
                new BigDecimal("0.00"), new BigDecimal("2.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal("1.00"),
                new BigDecimal("2.00"), new BigDecimal("3.00"));

        priceModel.setRoleSpecificUserPrices(pricedRoles);
        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("10")));
        priceModel.setConsideredEvents(consideredEvents);

        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal("0.00"),
                new BigDecimal("0.00"), new BigDecimal("0.00"));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(
                        VOServiceFactory.getParameter(voServiceDetails,
                                "MAX_FOLDER_NUMBER"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(50),
                                        new BigDecimal("1.20")),
                                newVOSteppedPrice(Long.valueOf(100),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(Long.valueOf(400),
                                        new BigDecimal("0.80")),
                                newVOSteppedPrice(null, new BigDecimal("0.50")) }), //
                        new BigDecimal("0.30"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT03_PerUnitHourStepRolesEvents(
            VOServiceDetails voServiceDetails) {

        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.HOUR, "EUR", new BigDecimal("10.00"),
                new BigDecimal("1.00"), //
                Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(1), new BigDecimal(
                                "3.00")),
                        newVOSteppedPrice(Long.valueOf(2), new BigDecimal(
                                "2.00")),
                        newVOSteppedPrice(null, new BigDecimal("1.00")) }), //
                20);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(2), new BigDecimal(1),
                new BigDecimal(0));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedRole> pricedRolesPeriod = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));

        // price for the PERIOD parameter (days subscription deactivated)
        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "PERIOD"), new BigDecimal("0.00"),
                        new BigDecimal("0.00"), pricedRolesPeriod));

        priceModel.setSelectedParameters(selectedParameters);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("10")));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT13_PerUnitHour(
            VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.HOUR, "EUR", //
                new BigDecimal("10.00"), // one time fee
                new BigDecimal("0.00"), // price per period
                Arrays.asList(new VOSteppedPrice[] { // stepped price per user
                        newVOSteppedPrice(Long.valueOf(1), new BigDecimal(
                                "3.00")),
                        newVOSteppedPrice(Long.valueOf(2), new BigDecimal(
                                "2.00")),
                        newVOSteppedPrice(null, new BigDecimal("0.00")) }), //
                0); // free period

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(2),
                new BigDecimal(0));
        // priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), //
                        new BigDecimal("0.00"), // sub price
                        new BigDecimal("4.00"), // user price
                        pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT13_PerUnitDay(
            VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.DAY, "EUR", //
                new BigDecimal("10.00"), // one time fee
                new BigDecimal("0.00"), // price per period
                Arrays.asList(new VOSteppedPrice[] { // stepped price per user
                        newVOSteppedPrice(Long.valueOf(1), new BigDecimal(
                                "3.00")),
                        newVOSteppedPrice(Long.valueOf(2), new BigDecimal(
                                "2.00")),
                        newVOSteppedPrice(null, new BigDecimal("0.00")) }), //
                0); // free period

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(2),
                new BigDecimal(0));
        // priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), //
                        new BigDecimal("0.00"), // sub price
                        new BigDecimal("4.00"), // user price
                        pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT12_ProRataMonth(
            VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.MONTH, "EUR", //
                new BigDecimal("0.00"), // one time free
                new BigDecimal("0.00"), // per period
                new BigDecimal("1.00"), // per user assignment
                0); // free period

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "BOOLEAN_PARAMETER"), //
                        new BigDecimal("0.00"), // price per sub
                        new BigDecimal("0.00"), // price per user
                        pricedRoles));

        priceModel.setSelectedParameters(selectedParameters);
        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT11_PerUnitWeek(
            VOServiceDetails voServiceDetails) {

        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("0.00"), // one time
                                                                   // fee
                new BigDecimal("1.00"), // price per period
                Arrays.asList(new VOSteppedPrice[] { // stepped price per user
                        newVOSteppedPrice(Long.valueOf(1), new BigDecimal(
                                "3.00")),
                        newVOSteppedPrice(Long.valueOf(2), new BigDecimal(
                                "2.00")),
                        newVOSteppedPrice(null, new BigDecimal("1.00")) }), //
                10); // free period

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "BOOLEAN_PARAMETER"), //
                        new BigDecimal("4.00"), // price per sub
                        new BigDecimal("2.00"), // price per user
                        pricedRoles));

        priceModel.setSelectedParameters(selectedParameters);
        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(1),
                                        new BigDecimal("10.00")),
                                newVOSteppedPrice(null, //
                                        new BigDecimal("0.00")) })),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), Arrays
                        .asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(1),
                                        new BigDecimal("20.00")),
                                newVOSteppedPrice(null, //
                                        new BigDecimal("0.00")) })));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT11_2_PerUnitWeek(
            VOServiceDetails voServiceDetails) {

        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("0.00"), // one time
                                                                   // fee
                new BigDecimal("10.00"), // price per period
                Arrays.asList(new VOSteppedPrice[] { // stepped price per user
                        newVOSteppedPrice(Long.valueOf(1), new BigDecimal(
                                "30.00")),
                        newVOSteppedPrice(Long.valueOf(2), new BigDecimal(
                                "20.00")),
                        newVOSteppedPrice(null, new BigDecimal("10.00")) }), //
                100); // free period

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "BOOLEAN_PARAMETER"), //
                        new BigDecimal("40.00"), // price per sub
                        new BigDecimal("20.00"), // price per user
                        pricedRoles));

        priceModel.setSelectedParameters(selectedParameters);
        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(1),
                                        new BigDecimal("100.00")),
                                newVOSteppedPrice(null, //
                                        new BigDecimal("0.00")) })),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), Arrays
                        .asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(1),
                                        new BigDecimal("200.00")),
                                newVOSteppedPrice(null, //
                                        new BigDecimal("0.00")) })));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT10_PerUnitMonth(
            VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.MONTH, "EUR", //
                new BigDecimal("10.00"), // one-time fee
                new BigDecimal("1.00"), // period fee
                new BigDecimal("5.00"), // price per user
                0); // free period

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(0),
                new BigDecimal(0));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "BOOLEAN_PARAMETER"), //
                        new BigDecimal("4.00"), // price per sub
                        new BigDecimal("2.00"), // price per user
                        pricedRoles));

        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT10_2_PerUnitMonth(
            VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.MONTH, "EUR", //
                new BigDecimal("100.00"), // one-time fee
                new BigDecimal("10.00"), // period fee
                new BigDecimal("50.00"), // price per user
                0); // free period

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(10),
                new BigDecimal(0), new BigDecimal(0));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "BOOLEAN_PARAMETER"), //
                        new BigDecimal("40.00"), // price per sub
                        new BigDecimal("20.00"), // price per user
                        pricedRoles));

        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT09_PerUnitHour(
            VOServiceDetails voServiceDetails) {

        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.HOUR, "EUR", new BigDecimal("0.00"), // one time
                                                                   // fee
                new BigDecimal("1.00"), // price per period
                Arrays.asList(new VOSteppedPrice[] { // stepped price per user
                        newVOSteppedPrice(Long.valueOf(1), new BigDecimal(
                                "3.00")),
                        newVOSteppedPrice(Long.valueOf(2), new BigDecimal(
                                "2.00")),
                        newVOSteppedPrice(null, new BigDecimal("1.00")) }), 20); // free
                                                                                 // period

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(2), new BigDecimal(1),
                new BigDecimal(0));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedRole> pricedRolesEnum = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));

        // parameter type enumeration
        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "HAS_OPTIONS"),
                        new BigDecimal("0.00"), // per subscription
                        new BigDecimal("1.00"), // per user
                        pricedRolesEnum));
        priceModel.setSelectedParameters(selectedParameters);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(1),
                                        new BigDecimal("10.00")),
                                newVOSteppedPrice(null, //
                                        new BigDecimal("0.00")) })),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), Arrays
                        .asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(1),
                                        new BigDecimal("20.00")),
                                newVOSteppedPrice(null, //
                                        new BigDecimal("0.00")) })));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT08_Rata_Weeks(
            VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("0.00"), // one time
                                                                   // fee
                new BigDecimal("10.00"), // price per period
                new BigDecimal("0.00"), // price per user
                0); // no free period

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        // parameter type enumeration
        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "HAS_OPTIONS"),
                        new BigDecimal("0.00"), new BigDecimal("0.00"),
                        pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT07_ProRataMonth(
            VOServiceDetails voServiceDetails) {

        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.MONTH, "EUR", //
                new BigDecimal("10.00"), // one-time fee
                new BigDecimal("0.00"), // period fee
                new BigDecimal("1.00"), // price per user assignment
                31); // free period

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(2),
                new BigDecimal(0));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(
                        VOServiceFactory.getParameter(voServiceDetails,
                                "LONG_NUMBER"), //
                        Arrays.asList(new VOSteppedPrice[] { // price per sub
                                newVOSteppedPrice(Long.valueOf(100),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(null, new BigDecimal("0.00")) }),
                        new BigDecimal("2.00"), // price per user
                        pricedRoles));

        priceModel.setSelectedParameters(selectedParameters);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(1),
                                        new BigDecimal("10.00")),
                                newVOSteppedPrice(null, //
                                        new BigDecimal("0.00")) })),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), Arrays
                        .asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(1),
                                        new BigDecimal("20.00")),
                                newVOSteppedPrice(null, //
                                        new BigDecimal("0.00")) })));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT06_PerUnitHour(
            VOServiceDetails voServiceDetails) {

        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.HOUR,
                "EUR", //
                new BigDecimal("10.00"), // one-time fee
                new BigDecimal("1.00"), // period fee
                Arrays.asList(new VOSteppedPrice[] { // user-assignment
                newVOSteppedPrice(Long.valueOf(1), new BigDecimal("1.00")),
                        newVOSteppedPrice(null, new BigDecimal("0.00")) }), //
                0); // free period

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(0),
                new BigDecimal(0));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "BOOLEAN_PARAMETER"), //
                        new BigDecimal("4.00"), // price per sub
                        new BigDecimal("2.00"), // price per user
                        pricedRoles));

        priceModel.setSelectedParameters(selectedParameters);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("10")));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT06_ProRataHour(
            VOServiceDetails voServiceDetails) {

        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.HOUR,
                "EUR", //
                new BigDecimal("10.00"), // one-time fee
                new BigDecimal("1.00"), // period fee
                Arrays.asList(new VOSteppedPrice[] { // user-assignment
                newVOSteppedPrice(Long.valueOf(1), new BigDecimal("1.00")),
                        newVOSteppedPrice(null, new BigDecimal("0.00")) }), //
                0); // free period

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "BOOLEAN_PARAMETER"), new BigDecimal(
                        "4.00"), new BigDecimal("2.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        priceModel.setSelectedParameters(selectedParameters);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("10")));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT03_5_PerUnitHourStepRolesEvents(
            VOServiceDetails voServiceDetails) {

        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.HOUR, "EUR", new BigDecimal("10.00"),
                new BigDecimal("1.00"), //
                Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(1), new BigDecimal(
                                "3.00")),
                        newVOSteppedPrice(Long.valueOf(2), new BigDecimal(
                                "2.00")),
                        newVOSteppedPrice(null, new BigDecimal("1.00")) }), //
                20);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(2), new BigDecimal(1),
                new BigDecimal(0));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedRole> pricedRolesPeriod = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));

        // price for the PERIOD parameter (days subscription deactivated)
        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "PERIOD"), new BigDecimal("1.00"),
                        new BigDecimal("1.00"), pricedRolesPeriod));

        priceModel.setSelectedParameters(selectedParameters);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("10")));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT02_Rata_Week(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("0.00"),
                new BigDecimal("100.00"), new BigDecimal("100.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "2.00"), new BigDecimal("4.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT02_Rata_Week2(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("0.00"),
                new BigDecimal("200.00"), new BigDecimal("200.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "4.00"), new BigDecimal("8.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePict01RataDay() {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.DAY, "EUR", new BigDecimal("0.00"),
                new BigDecimal("0.00"), new BigDecimal("0.00"), 0);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExampleTest() {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("10.00"),
                new BigDecimal("1.00"), new BigDecimal("1.00"), 0);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitDayRoles(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.DAY, "EUR", new BigDecimal("20.00"),
                new BigDecimal("120.00"), new BigDecimal("60.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePerUnitHourRoles(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.HOUR, "EUR", new BigDecimal("10.00"),
                new BigDecimal("60.00"), new BigDecimal("30.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(6), new BigDecimal(7),
                new BigDecimal(8));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        return priceModel;
    }

    private static VOPriceModel createFreePriceModel() {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.FREE_OF_CHARGE);
        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT49PerUnitHourRoles(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.HOUR, "EUR", new BigDecimal("10.00"),
                new BigDecimal("0.00"), new BigDecimal("0.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(5), new BigDecimal(4),
                new BigDecimal(3));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "10.00"), new BigDecimal("20.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT49_1PerUnitHourRoles(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.HOUR, "EUR", new BigDecimal("10.00"),
                new BigDecimal("0.00"), new BigDecimal("0.00"), 1);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(5), new BigDecimal(4),
                new BigDecimal(3));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "10.00"), new BigDecimal("20.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT48_Rata_Weeks(
            VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("5.00"),
                new BigDecimal("4.00"), //
                Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(1), new BigDecimal(
                                "15.00")),
                        newVOSteppedPrice(Long.valueOf(2), new BigDecimal(
                                "12.00")),
                        newVOSteppedPrice(Long.valueOf(3), new BigDecimal(
                                "11.00")),
                        newVOSteppedPrice(null, new BigDecimal("100.00")) }), //
                0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "0.00"), new BigDecimal("1.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT40_Unit_Weeks(
            VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("0.00"),
                new BigDecimal("0.00"), //
                Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(1), new BigDecimal(
                                "1.00")),
                        newVOSteppedPrice(Long.valueOf(2), new BigDecimal(
                                "2.00")),
                        newVOSteppedPrice(Long.valueOf(3), new BigDecimal(
                                "3.00")),
                        newVOSteppedPrice(null, new BigDecimal("4.00")) }), //
                5);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(2), new BigDecimal(3),
                new BigDecimal(4));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "LONG_NUMBER"),
                        new BigDecimal("1.00"), new BigDecimal("0.00"),
                        pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT34_Unit_Weeks(
            VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("5.00"),
                new BigDecimal("1.00"), //
                Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(1), new BigDecimal(
                                "1.00")),
                        newVOSteppedPrice(Long.valueOf(2), new BigDecimal(
                                "2.00")),
                        newVOSteppedPrice(Long.valueOf(3), new BigDecimal(
                                "3.00")),
                        newVOSteppedPrice(null, new BigDecimal("4.00")) }), //
                5);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(2),
                new BigDecimal(3));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "LONG_NUMBER"),
                        new BigDecimal("1.00"), new BigDecimal("2.00"),
                        pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT32_Unit_Weeks(
            VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("5.00"),
                new BigDecimal("0.00"), //
                Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(10), new BigDecimal(
                                "1.00")),
                        newVOSteppedPrice(Long.valueOf(20), new BigDecimal(
                                "2.00")),
                        newVOSteppedPrice(Long.valueOf(30), new BigDecimal(
                                "3.00")),
                        newVOSteppedPrice(null, new BigDecimal("4.00")) }), //
                15);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        // for parameter
        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(
                        VOServiceFactory.getParameter(voServiceDetails,
                                "LONG_NUMBER"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(100),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(Long.valueOf(200),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(Long.valueOf(400),
                                        new BigDecimal("3.00")),
                                newVOSteppedPrice(null, new BigDecimal("0.50")) }), //
                        new BigDecimal("1.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT32_Unit_Weeks_2(
            VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("0.00"),
                new BigDecimal("0.00"), //
                Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(10), new BigDecimal(
                                "1.00")),
                        newVOSteppedPrice(Long.valueOf(20), new BigDecimal(
                                "2.00")),
                        newVOSteppedPrice(Long.valueOf(30), new BigDecimal(
                                "3.00")),
                        newVOSteppedPrice(null, new BigDecimal("4.00")) }), //
                0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        // for parameter
        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(
                        VOServiceFactory.getParameter(voServiceDetails,
                                "LONG_NUMBER"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(100),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(Long.valueOf(200),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(Long.valueOf(400),
                                        new BigDecimal("3.00")),
                                newVOSteppedPrice(null, new BigDecimal("0.50")) }), //
                        new BigDecimal("1.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT30(
            VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("0.00"),
                new BigDecimal("1.00"), //
                Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(2), new BigDecimal(
                                "1.00")),
                        newVOSteppedPrice(Long.valueOf(4), new BigDecimal(
                                "2.00")),
                        newVOSteppedPrice(Long.valueOf(6), new BigDecimal(
                                "3.00")),
                        newVOSteppedPrice(null, new BigDecimal("4.00")) }), //
                0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));
        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("13")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("12")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), //
                        new BigDecimal("8")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("9")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("2.00")));

        priceModel.setConsideredEvents(consideredEvents);

        // for parameter
        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(
                        VOServiceFactory.getParameter(voServiceDetails,
                                "MAX_FOLDER_NUMBER"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(100),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(Long.valueOf(200),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(Long.valueOf(400),
                                        new BigDecimal("3.00")),
                                newVOSteppedPrice(null, new BigDecimal("0.50")) }), //
                        new BigDecimal("0.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT29_Unit_Weeks(
            VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("1.00"),
                new BigDecimal("1.00"), new BigDecimal("1.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));
        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("13")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("12")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), //
                        new BigDecimal("8")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("9")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("2.00")));

        priceModel.setConsideredEvents(consideredEvents);

        // for parameter
        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "1.00"), new BigDecimal("0.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT28_Unit_Weeks(
            VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("0.00"),
                new BigDecimal("1.00"), new BigDecimal("1.00"), 21);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("13")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("12")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(1),
                                        new BigDecimal("10.00")),
                                newVOSteppedPrice(Long.valueOf(14),
                                        new BigDecimal("5.00")),
                                newVOSteppedPrice(Long.valueOf(80),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(Long.valueOf(200),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(null, //
                                        new BigDecimal("0.50")) })),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("9")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("2.00")));

        priceModel.setConsideredEvents(consideredEvents);

        // for parameter
        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(
                        VOServiceFactory.getParameter(voServiceDetails,
                                "LONG_NUMBER"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(100),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(Long.valueOf(200),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(Long.valueOf(400),
                                        new BigDecimal("3.00")),
                                newVOSteppedPrice(null, new BigDecimal("0.50")) }), //
                        new BigDecimal("1.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT24(
            VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.MONTH, "EUR", new BigDecimal("0.00"),
                new BigDecimal("1.00"), new BigDecimal("0.00"), 21);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("13")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("12")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(1),
                                        new BigDecimal("10.00")),
                                newVOSteppedPrice(Long.valueOf(14),
                                        new BigDecimal("5.00")),
                                newVOSteppedPrice(Long.valueOf(80),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(Long.valueOf(200),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(null, //
                                        new BigDecimal("0.50")) })),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("9")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("2.00")));

        priceModel.setConsideredEvents(consideredEvents);

        // for parameter
        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(
                        VOServiceFactory.getParameter(voServiceDetails,
                                "LONG_NUMBER"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(100),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(Long.valueOf(200),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(Long.valueOf(400),
                                        new BigDecimal("3.00")),
                                newVOSteppedPrice(null, new BigDecimal("0.50")) }), //
                        new BigDecimal("1.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT24_2(
            VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.HOUR, "EUR", new BigDecimal("10.00"),
                new BigDecimal("0.10"), new BigDecimal("0.10"), 20);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("13")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("12")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(1),
                                        new BigDecimal("10.00")),
                                newVOSteppedPrice(Long.valueOf(14),
                                        new BigDecimal("5.00")),
                                newVOSteppedPrice(Long.valueOf(80),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(Long.valueOf(200),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(null, //
                                        new BigDecimal("0.50")) })),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("9")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("2.00")));

        priceModel.setConsideredEvents(consideredEvents);

        // for parameter
        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "1.00"), new BigDecimal("1.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT28_1_Unit_Weeks(
            VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("10.00"),
                new BigDecimal("1.00"), new BigDecimal("1.00"), 1);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("13")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("12")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(1),
                                        new BigDecimal("10.00")),
                                newVOSteppedPrice(Long.valueOf(14),
                                        new BigDecimal("5.00")),
                                newVOSteppedPrice(Long.valueOf(80),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(Long.valueOf(200),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(null, //
                                        new BigDecimal("0.50")) })),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("9")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("2.00")));

        priceModel.setConsideredEvents(consideredEvents);

        // for parameter
        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(
                        VOServiceFactory.getParameter(voServiceDetails,
                                "LONG_NUMBER"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(100),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(Long.valueOf(200),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(Long.valueOf(400),
                                        new BigDecimal("3.00")),
                                newVOSteppedPrice(null, new BigDecimal("0.50")) }), //
                        new BigDecimal("1.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT28_Rata_Weeks(
            VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("0.00"),
                new BigDecimal("1.00"), new BigDecimal("1.00"), 21);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("13")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("12")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(1),
                                        new BigDecimal("10.00")),
                                newVOSteppedPrice(Long.valueOf(14),
                                        new BigDecimal("5.00")),
                                newVOSteppedPrice(Long.valueOf(80),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(Long.valueOf(200),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(null, //
                                        new BigDecimal("0.50")) })),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("9")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("2.00")));

        priceModel.setConsideredEvents(consideredEvents);

        // for parameter
        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(
                        VOServiceFactory.getParameter(voServiceDetails,
                                "LONG_NUMBER"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(100),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(Long.valueOf(200),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(Long.valueOf(400),
                                        new BigDecimal("3.00")),
                                newVOSteppedPrice(null, new BigDecimal("0.50")) }), //
                        new BigDecimal("1.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT26_Rata_Weeks(
            VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("10.00"),
                new BigDecimal("1.00"), new BigDecimal("1.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        // for parameter
        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "1.00"), new BigDecimal("1.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);
        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT25_Rata_Weeks(
            VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("10.00"),
                new BigDecimal("0.00"), new BigDecimal("1.00"), 30);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        // for parameter
        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "1.00"), new BigDecimal("1.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);
        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT25_3_Rata_Weeks(
            VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("10.00"),
                new BigDecimal("0.00"), new BigDecimal("1.00"), 40);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        // for parameter
        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "1.00"), new BigDecimal("1.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);
        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT27_Rata_Weeks(
            VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("0.00"),
                new BigDecimal("1.00"), new BigDecimal("1.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        // for parameter
        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "1.00"), new BigDecimal("1.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);
        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT28_1_Rata_Weeks(
            VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("5.00"),
                new BigDecimal("1.00"), new BigDecimal("1.00"), 1);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("13")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("12")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(1),
                                        new BigDecimal("10.00")),
                                newVOSteppedPrice(Long.valueOf(14),
                                        new BigDecimal("5.00")),
                                newVOSteppedPrice(Long.valueOf(80),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(Long.valueOf(200),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(null, //
                                        new BigDecimal("0.50")) })),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("9")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("2.00")));

        priceModel.setConsideredEvents(consideredEvents);

        // for parameter
        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(
                        VOServiceFactory.getParameter(voServiceDetails,
                                "LONG_NUMBER"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(100),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(Long.valueOf(200),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(Long.valueOf(400),
                                        new BigDecimal("3.00")),
                                newVOSteppedPrice(null, new BigDecimal("0.50")) }), //
                        new BigDecimal("1.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT26_Unit_Weeks(
            VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("0.00"),
                new BigDecimal("0.00"), new BigDecimal("1.00"), 11);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("13")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("12")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(1),
                                        new BigDecimal("10.00")),
                                newVOSteppedPrice(Long.valueOf(14),
                                        new BigDecimal("5.00")),
                                newVOSteppedPrice(Long.valueOf(80),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(Long.valueOf(200),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(null, //
                                        new BigDecimal("0.50")) })),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("9")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("2.00")));

        priceModel.setConsideredEvents(consideredEvents);

        // for parameter
        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "0.00"), new BigDecimal("1.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);
        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT33_Unit_Weeks(
            VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("0.00"),
                new BigDecimal("0.00"), //
                Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(10), new BigDecimal(
                                "1.00")),
                        newVOSteppedPrice(Long.valueOf(20), new BigDecimal(
                                "2.00")),
                        newVOSteppedPrice(Long.valueOf(30), new BigDecimal(
                                "3.00")),
                        newVOSteppedPrice(null, new BigDecimal("4.00")) }), //
                5);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));
        priceModel.setRoleSpecificUserPrices(pricedRoles);
        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("13")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("12")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(1),
                                        new BigDecimal("10.00")),
                                newVOSteppedPrice(Long.valueOf(14),
                                        new BigDecimal("5.00")),
                                newVOSteppedPrice(Long.valueOf(80),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(Long.valueOf(200),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(null, //
                                        new BigDecimal("0.50")) })),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("9")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("2.00")));

        priceModel.setConsideredEvents(consideredEvents);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(
                        VOServiceFactory.getParameter(voServiceDetails,
                                "MAX_FOLDER_NUMBER"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(50),
                                        new BigDecimal("1.20")),
                                newVOSteppedPrice(Long.valueOf(100),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(Long.valueOf(400),
                                        new BigDecimal("0.80")),
                                newVOSteppedPrice(null, new BigDecimal("0.50")) }), //
                        new BigDecimal("0.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT35_Rata_Days(
            VOServiceDetails voServiceDetails) {

        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.DAY, "EUR", new BigDecimal("1.00"),
                new BigDecimal("1.00"), new BigDecimal("0.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));
        priceModel.setRoleSpecificUserPrices(pricedRoles);
        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("10")));
        priceModel.setConsideredEvents(consideredEvents);

        List<VOPricedRole> pricedRolesPar = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(
                        VOServiceFactory.getParameter(voServiceDetails,
                                "LONG_NUMBER"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(100),
                                        new BigDecimal("3.00")),
                                newVOSteppedPrice(Long.valueOf(200),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(Long.valueOf(300),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(null, new BigDecimal("0.50")) }), //
                        new BigDecimal("0.00"), pricedRolesPar));
        priceModel.setSelectedParameters(selectedParameters);
        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT36_Unit_Weeks(
            VOServiceDetails voServiceDetails) {

        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("0.00"),
                new BigDecimal("4.00"), //
                Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(1), new BigDecimal(
                                "1.00")),
                        newVOSteppedPrice(Long.valueOf(2), new BigDecimal(
                                "2.00")),
                        newVOSteppedPrice(Long.valueOf(3), new BigDecimal(
                                "3.00")),
                        newVOSteppedPrice(null, new BigDecimal("4.00")) }), //
                5);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("10")));
        priceModel.setConsideredEvents(consideredEvents);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(4), new BigDecimal(5),
                new BigDecimal(6));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "0.10"), new BigDecimal("0.20"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT38_Unit_Days(
            VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.DAY, "EUR", new BigDecimal("0.00"),
                new BigDecimal("10.00"), //
                Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(1), new BigDecimal(
                                "1.00")),
                        newVOSteppedPrice(Long.valueOf(2), new BigDecimal(
                                "2.00")),
                        newVOSteppedPrice(Long.valueOf(3), new BigDecimal(
                                "3.00")),
                        newVOSteppedPrice(null, new BigDecimal("4.00")) }), //
                5);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("10")));
        priceModel.setConsideredEvents(consideredEvents);

        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(2), new BigDecimal(3),
                new BigDecimal(4));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "HAS_OPTIONS"),
                        new BigDecimal("1.00"), new BigDecimal("0.00"),
                        pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT47_Unit_Weeks(
            VOServiceDetails voServiceDetails) {

        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("5.00"),
                new BigDecimal("0.00"), new BigDecimal("12.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(2),
                new BigDecimal(3));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "7.00"), new BigDecimal("0.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        List<VOPricedEvent> consideredEvents = Arrays
                .asList(newVOPricedEvent(VOTechServiceFactory
                        .getEventDefinition(voTechService,
                                "USER_LOGIN_TO_SERVICE"), new BigDecimal("13")),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "USER_LOGOUT_FROM_SERVICE"),
                                new BigDecimal("12")),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "FILE_DOWNLOAD"), //
                                Arrays.asList(new VOSteppedPrice[] {
                                        newVOSteppedPrice(Long.valueOf(1),
                                                new BigDecimal("10.00")),
                                        newVOSteppedPrice(Long.valueOf(13),
                                                new BigDecimal("5.00")),
                                        newVOSteppedPrice(Long.valueOf(80),
                                                new BigDecimal("2.00")),
                                        newVOSteppedPrice(Long.valueOf(200),
                                                new BigDecimal("1.00")),
                                        newVOSteppedPrice(null, //
                                                new BigDecimal("0.50")) })),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "FILE_UPLOAD"), new BigDecimal("9")),
                        newVOPricedEvent(
                                VOTechServiceFactory.getEventDefinition(
                                        voTechService, "FOLDER_NEW"),
                                new BigDecimal("110.25")));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT46_Unit_Weeks(
            VOServiceDetails voServiceDetails) {

        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("5.00"),
                new BigDecimal("10.00"), new BigDecimal("0.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(2),
                new BigDecimal(3));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "0.00"), new BigDecimal("0.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        List<VOPricedEvent> consideredEvents = Arrays
                .asList(newVOPricedEvent(VOTechServiceFactory
                        .getEventDefinition(voTechService,
                                "USER_LOGIN_TO_SERVICE"), new BigDecimal("13")),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "USER_LOGOUT_FROM_SERVICE"),
                                new BigDecimal("12")),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "FILE_DOWNLOAD"), //
                                Arrays.asList(new VOSteppedPrice[] {
                                        newVOSteppedPrice(Long.valueOf(1),
                                                new BigDecimal("10.00")),
                                        newVOSteppedPrice(Long.valueOf(13),
                                                new BigDecimal("5.00")),
                                        newVOSteppedPrice(Long.valueOf(80),
                                                new BigDecimal("2.00")),
                                        newVOSteppedPrice(Long.valueOf(200),
                                                new BigDecimal("1.00")),
                                        newVOSteppedPrice(null, //
                                                new BigDecimal("0.50")) })),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "FILE_UPLOAD"), new BigDecimal("9")),
                        newVOPricedEvent(
                                VOTechServiceFactory.getEventDefinition(
                                        voTechService, "FOLDER_NEW"),
                                new BigDecimal("110.25")));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT44_Unit_Month(
            VOServiceDetails voServiceDetails) {

        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.MONTH, "EUR", new BigDecimal("5.00"),
                new BigDecimal("10.00"), new BigDecimal("7.00"), 1);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "HAS_OPTIONS"),
                        new BigDecimal("8.00"), new BigDecimal("0.00"),
                        pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        List<VOPricedEvent> consideredEvents = Arrays
                .asList(newVOPricedEvent(VOTechServiceFactory
                        .getEventDefinition(voTechService,
                                "USER_LOGIN_TO_SERVICE"), new BigDecimal("13")),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "USER_LOGOUT_FROM_SERVICE"),
                                new BigDecimal("12")),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "FILE_DOWNLOAD"), //
                                Arrays.asList(new VOSteppedPrice[] {
                                        newVOSteppedPrice(Long.valueOf(1),
                                                new BigDecimal("10.00")),
                                        newVOSteppedPrice(Long.valueOf(13),
                                                new BigDecimal("5.00")),
                                        newVOSteppedPrice(Long.valueOf(80),
                                                new BigDecimal("2.00")),
                                        newVOSteppedPrice(Long.valueOf(200),
                                                new BigDecimal("1.00")),
                                        newVOSteppedPrice(null, //
                                                new BigDecimal("0.50")) })),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "FILE_UPLOAD"), new BigDecimal("9")),
                        newVOPricedEvent(
                                VOTechServiceFactory.getEventDefinition(
                                        voTechService, "FOLDER_NEW"),
                                new BigDecimal("110.25")));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT42_Unit_Month(
            VOServiceDetails voServiceDetails) {

        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.MONTH, "EUR", new BigDecimal("25.00"),
                new BigDecimal("85.00"), //
                Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(1), new BigDecimal(
                                "150.00")),
                        newVOSteppedPrice(Long.valueOf(2), new BigDecimal(
                                "120.00")),
                        newVOSteppedPrice(Long.valueOf(3), new BigDecimal(
                                "110.00")),
                        newVOSteppedPrice(null, new BigDecimal("100.00")) }), //
                0);
        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(20),
                new BigDecimal(10), new BigDecimal(30));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(
                        VOServiceFactory.getParameter(voServiceDetails,
                                "MAX_FOLDER_NUMBER"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(50),
                                        new BigDecimal("1.20")),
                                newVOSteppedPrice(Long.valueOf(100),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(Long.valueOf(400),
                                        new BigDecimal("0.80")),
                                newVOSteppedPrice(null, new BigDecimal("0.50")) }), //
                        new BigDecimal("0.30"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        List<VOPricedEvent> consideredEvents = Arrays
                .asList(newVOPricedEvent(VOTechServiceFactory
                        .getEventDefinition(voTechService,
                                "USER_LOGIN_TO_SERVICE"), new BigDecimal("13")),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "USER_LOGOUT_FROM_SERVICE"),
                                new BigDecimal("12")),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "FILE_DOWNLOAD"), //
                                Arrays.asList(new VOSteppedPrice[] {
                                        newVOSteppedPrice(Long.valueOf(1),
                                                new BigDecimal("10.00")),
                                        newVOSteppedPrice(Long.valueOf(13),
                                                new BigDecimal("5.00")),
                                        newVOSteppedPrice(Long.valueOf(80),
                                                new BigDecimal("2.00")),
                                        newVOSteppedPrice(Long.valueOf(200),
                                                new BigDecimal("1.00")),
                                        newVOSteppedPrice(null, //
                                                new BigDecimal("0.50")) })),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "FILE_UPLOAD"), new BigDecimal("9")),
                        newVOPricedEvent(
                                VOTechServiceFactory.getEventDefinition(
                                        voTechService, "FOLDER_NEW"),
                                new BigDecimal("110.25")));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT49_5(
            VOServiceDetails voServiceDetails) {
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.MONTH, "EUR", new BigDecimal("123.00"),
                new BigDecimal("678.00"), new BigDecimal("345.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(15),
                new BigDecimal(8), new BigDecimal(5));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "2.00"), new BigDecimal("20.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT05_Unit_Weeks(
            VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("5.00"), // one time
                                                                   // fee
                new BigDecimal("0.00"), // price per period
                Arrays.asList(new VOSteppedPrice[] { // stepped price per user
                        // assignment
                        newVOSteppedPrice(Long.valueOf(1), new BigDecimal(
                                "1.00")),
                        newVOSteppedPrice(null, new BigDecimal("2.00")), }), //
                0); // no free period

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(2),
                new BigDecimal(3));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        // parameter type enumeration
        List<VOPricedRole> pricedRolesPar = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));
        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "HAS_OPTIONS"),
                        new BigDecimal("0.00"), new BigDecimal("0.00"),
                        pricedRolesPar));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT22(
            VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.HOUR, "EUR", new BigDecimal("5.00"),
                new BigDecimal("0.00"), Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(1), new BigDecimal(
                                "1.00")),
                        newVOSteppedPrice(null, new BigDecimal("2.00")), }), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(2),
                new BigDecimal(3));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        // parameter type enumeration
        List<VOPricedRole> pricedRolesPar = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));
        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "HAS_OPTIONS"),
                        new BigDecimal("0.00"), new BigDecimal("0.00"),
                        pricedRolesPar));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT21(

    VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("5.00"),
                new BigDecimal("1.00"), new BigDecimal("1.00"), 7);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        // parameter type enumeration
        List<VOPricedRole> pricedRolesPar = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));
        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "HAS_OPTIONS"),
                        new BigDecimal("1.00"), new BigDecimal("0.00"),
                        pricedRolesPar));
        priceModel.setSelectedParameters(selectedParameters);
        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("13")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("12")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(1),
                                        new BigDecimal("10.00")),
                                newVOSteppedPrice(Long.valueOf(13),
                                        new BigDecimal("5.00")),
                                newVOSteppedPrice(Long.valueOf(80),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(Long.valueOf(200),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(null, //
                                        new BigDecimal("0.50")) })),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("1")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("1.00")));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT20(

    VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.MONTH, "EUR", new BigDecimal("5.00"),
                new BigDecimal("1.00"), new BigDecimal("1.00"), 7);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        // parameter type enumeration
        List<VOPricedRole> pricedRolesPar = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));
        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "HAS_OPTIONS"),
                        new BigDecimal("1.00"), new BigDecimal("0.00"),
                        pricedRolesPar));
        priceModel.setSelectedParameters(selectedParameters);
        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("13")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("12")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(1),
                                        new BigDecimal("10.00")),
                                newVOSteppedPrice(Long.valueOf(13),
                                        new BigDecimal("5.00")),
                                newVOSteppedPrice(Long.valueOf(80),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(Long.valueOf(200),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(null, //
                                        new BigDecimal("0.50")) })),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("1")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("1.00")));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT17(

    VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("0.00"),
                new BigDecimal("1.00"), new BigDecimal("1.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "1.00"), new BigDecimal("1.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("13")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("12")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(1),
                                        new BigDecimal("10.00")),
                                newVOSteppedPrice(Long.valueOf(13),
                                        new BigDecimal("5.00")),
                                newVOSteppedPrice(Long.valueOf(80),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(Long.valueOf(200),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(null, //
                                        new BigDecimal("0.50")) })),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("1")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("1.00")));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT15(

    VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.HOUR, "EUR", new BigDecimal("0.00"),
                new BigDecimal("1.00"), Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(100), new BigDecimal(
                                "1.00")),
                        newVOSteppedPrice(Long.valueOf(200), new BigDecimal(
                                "2.00")),
                        newVOSteppedPrice(Long.valueOf(300), new BigDecimal(
                                "3.00")),
                        newVOSteppedPrice(null, new BigDecimal("4.00")) }), //
                0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(
                        VOServiceFactory.getParameter(voServiceDetails,
                                "LONG_NUMBER"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(1000),
                                        new BigDecimal("0.10")),
                                newVOSteppedPrice(Long.valueOf(5000),
                                        new BigDecimal("0.20")),
                                newVOSteppedPrice(Long.valueOf(9000),
                                        new BigDecimal("0.30")),
                                newVOSteppedPrice(null, new BigDecimal("0.50")) }), //
                        new BigDecimal("0.10"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        List<VOPricedEvent> consideredEvents = Arrays
                .asList(newVOPricedEvent(VOTechServiceFactory
                        .getEventDefinition(voTechService,
                                "USER_LOGIN_TO_SERVICE"), new BigDecimal("13")),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "USER_LOGOUT_FROM_SERVICE"),
                                new BigDecimal("12")),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "FILE_DOWNLOAD"), //
                                Arrays.asList(new VOSteppedPrice[] {
                                        newVOSteppedPrice(Long.valueOf(1),
                                                new BigDecimal("10.00")),
                                        newVOSteppedPrice(Long.valueOf(13),
                                                new BigDecimal("5.00")),
                                        newVOSteppedPrice(Long.valueOf(80),
                                                new BigDecimal("2.00")),
                                        newVOSteppedPrice(Long.valueOf(200),
                                                new BigDecimal("1.00")),
                                        newVOSteppedPrice(null, //
                                                new BigDecimal("0.50")) })),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "FILE_UPLOAD"), new BigDecimal("9")),
                        newVOPricedEvent(
                                VOTechServiceFactory.getEventDefinition(
                                        voTechService, "FOLDER_NEW"),
                                new BigDecimal("110.25")));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT14(

    VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.HOUR, "EUR", new BigDecimal("1.00"),
                new BigDecimal("1.00"), new BigDecimal("1.00"), //
                1);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "HAS_OPTIONS"),
                        new BigDecimal("1.00"), new BigDecimal("0.00"),
                        pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        List<VOPricedEvent> consideredEvents = Arrays
                .asList(newVOPricedEvent(VOTechServiceFactory
                        .getEventDefinition(voTechService,
                                "USER_LOGIN_TO_SERVICE"), new BigDecimal("13")),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "USER_LOGOUT_FROM_SERVICE"),
                                new BigDecimal("12")),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "FILE_DOWNLOAD"), //
                                Arrays.asList(new VOSteppedPrice[] {
                                        newVOSteppedPrice(Long.valueOf(1),
                                                new BigDecimal("10.00")),
                                        newVOSteppedPrice(Long.valueOf(13),
                                                new BigDecimal("5.00")),
                                        newVOSteppedPrice(Long.valueOf(80),
                                                new BigDecimal("2.00")),
                                        newVOSteppedPrice(Long.valueOf(200),
                                                new BigDecimal("1.00")),
                                        newVOSteppedPrice(null, //
                                                new BigDecimal("0.50")) })),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "FILE_UPLOAD"), new BigDecimal("9")),
                        newVOPricedEvent(
                                VOTechServiceFactory.getEventDefinition(
                                        voTechService, "FOLDER_NEW"),
                                new BigDecimal("110.25")));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT15_2(

    VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.HOUR, "EUR", new BigDecimal("0.00"),
                new BigDecimal("1.00"), Arrays.asList(new VOSteppedPrice[] {
                        newVOSteppedPrice(Long.valueOf(100), new BigDecimal(
                                "1.00")),
                        newVOSteppedPrice(Long.valueOf(200), new BigDecimal(
                                "2.00")),
                        newVOSteppedPrice(Long.valueOf(300), new BigDecimal(
                                "3.00")),
                        newVOSteppedPrice(null, new BigDecimal("4.00")) }), //
                0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(
                        VOServiceFactory.getParameter(voServiceDetails,
                                "LONG_NUMBER"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(1000),
                                        new BigDecimal("0.10")),
                                newVOSteppedPrice(Long.valueOf(5000),
                                        new BigDecimal("0.20")),
                                newVOSteppedPrice(Long.valueOf(9000),
                                        new BigDecimal("0.30")),
                                newVOSteppedPrice(null, new BigDecimal("0.50")) }), //
                        new BigDecimal("0.10"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        List<VOPricedEvent> consideredEvents = Arrays
                .asList(newVOPricedEvent(VOTechServiceFactory
                        .getEventDefinition(voTechService,
                                "USER_LOGIN_TO_SERVICE"), new BigDecimal("13")),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "USER_LOGOUT_FROM_SERVICE"),
                                new BigDecimal("12")),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "FILE_DOWNLOAD"), //
                                Arrays.asList(new VOSteppedPrice[] {
                                        newVOSteppedPrice(Long.valueOf(1),
                                                new BigDecimal("10.00")),
                                        newVOSteppedPrice(Long.valueOf(13),
                                                new BigDecimal("5.00")),
                                        newVOSteppedPrice(Long.valueOf(80),
                                                new BigDecimal("2.00")),
                                        newVOSteppedPrice(Long.valueOf(200),
                                                new BigDecimal("1.00")),
                                        newVOSteppedPrice(null, //
                                                new BigDecimal("0.50")) })),
                        newVOPricedEvent(VOTechServiceFactory
                                .getEventDefinition(voTechService,
                                        "FILE_UPLOAD"), new BigDecimal("9")),
                        newVOPricedEvent(
                                VOTechServiceFactory.getEventDefinition(
                                        voTechService, "FOLDER_NEW"),
                                new BigDecimal("110.25")));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT16(

    VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.DAY, "EUR", new BigDecimal("0.00"),
                new BigDecimal("1.00"), new BigDecimal("1.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "BOOLEAN_PARAMETER"), new BigDecimal(
                        "1.00"), new BigDecimal("1.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT16_1(

    VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.DAY, "EUR", new BigDecimal("0.00"),
                new BigDecimal("1.00"), new BigDecimal("1.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT16_2(

    VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.DAY, "EUR", new BigDecimal("0.00"),
                new BigDecimal("1.00"), new BigDecimal("1.00"), 1);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT19(

    VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();
        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.HOUR, "EUR", new BigDecimal("5.00"),
                new BigDecimal("1.00"), new BigDecimal("1.00"), 1);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(1),
                new BigDecimal(1));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "BOOLEAN_PARAMETER"), new BigDecimal(
                        "1.00"), new BigDecimal("1.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("13")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("12")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(1),
                                        new BigDecimal("10.00")),
                                newVOSteppedPrice(Long.valueOf(13),
                                        new BigDecimal("5.00")),
                                newVOSteppedPrice(Long.valueOf(80),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(Long.valueOf(200),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(null, //
                                        new BigDecimal("0.50")) })),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("1")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("1.00")));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT05_Rata_Weeks(
            VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("0.00"), // one time
                                                                   // fee
                new BigDecimal("10.00"), // price per period
                new BigDecimal("0.00"), // price per user
                0); // no free period

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        // parameter type enumeration
        List<VOPricedRole> pricedRolesPar = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(2),
                new BigDecimal(3));
        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "HAS_OPTIONS"),
                        new BigDecimal("0.00"), new BigDecimal("0.00"),
                        pricedRolesPar));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT05_Rata_Weeks2(
            VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("100.00"), // one time
                                                                     // fee
                new BigDecimal("0.00"), // price per period
                new BigDecimal("0.00"), // price per user
                0); // no free period

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        // parameter type enumeration
        List<VOPricedRole> pricedRolesPar = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(2),
                new BigDecimal(3));
        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "HAS_OPTIONS"),
                        new BigDecimal("0.00"), new BigDecimal("0.00"),
                        pricedRolesPar));
        priceModel.setSelectedParameters(selectedParameters);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT05_1_Rata_Weeks(
            VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PRO_RATA,
                PricingPeriod.WEEK, "EUR", new BigDecimal("0.00"), // one time
                                                                   // fee
                new BigDecimal("0.00"), // price per period
                new BigDecimal("0.00"), // price per user
                0); // no free period

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(0), new BigDecimal(0),
                new BigDecimal(0));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePICT05_1_Unit_Weeks(
            VOServiceDetails voServiceDetails) {

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.WEEK, "EUR", new BigDecimal("5.00"), // one time
                                                                   // fee
                new BigDecimal("0.00"), // price per period
                new BigDecimal("0.00"), // price per user
                0); // no free period

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(1), new BigDecimal(2),
                new BigDecimal(3));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExamplePict04PerUnitDay(
            VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.DAY, "EUR", new BigDecimal("0.00"),
                new BigDecimal("2.00"), new BigDecimal("3.00"), 4);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(
                        voServiceDetails.getTechnicalService(), "ADMIN",
                        "USER", "GUEST"), new BigDecimal(5), new BigDecimal(4),
                new BigDecimal(3));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "PERIOD"),
                        new BigDecimal("1.20"),
                        new BigDecimal("3.80"), //
                        createPricedRoles(VOTechServiceFactory
                                .getRoleDefinitions(
                                        voServiceDetails.getTechnicalService(),
                                        "ADMIN", "USER", "GUEST"),
                                new BigDecimal(8.50), new BigDecimal(4.50),
                                new BigDecimal(1.50))));
        priceModel.setSelectedParameters(selectedParameters);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGIN_TO_SERVICE"),
                        new BigDecimal("2.50")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "USER_LOGOUT_FROM_SERVICE"),
                        new BigDecimal("3.50")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), //
                        Arrays.asList(new VOSteppedPrice[] {
                                newVOSteppedPrice(Long.valueOf(1),
                                        new BigDecimal("10.00")),
                                newVOSteppedPrice(Long.valueOf(3),
                                        new BigDecimal("5.00")),
                                newVOSteppedPrice(Long.valueOf(7),
                                        new BigDecimal("2.00")),
                                newVOSteppedPrice(Long.valueOf(10),
                                        new BigDecimal("1.00")),
                                newVOSteppedPrice(null, //
                                        new BigDecimal("0.50")) })),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("3.10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("8.25")));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExample_FP_PUDayRolesParEvents(
            VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.DAY, "EUR", new BigDecimal("3.00"),
                new BigDecimal("6.00"), new BigDecimal("5.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(voTechService, "ADMIN",
                        "USER", "GUEST"), new BigDecimal(3), new BigDecimal(2),
                new BigDecimal(1));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "2.00"), new BigDecimal("8.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("20")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("30")));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel createPriceModelExample_FP_PUDayRolesParEvents2(
            VOServiceDetails voServiceDetails) {
        VOTechnicalService voTechService = voServiceDetails
                .getTechnicalService();

        VOPriceModel priceModel = newVOPriceModel(PriceModelType.PER_UNIT,
                PricingPeriod.DAY, "EUR", new BigDecimal("1.00"),
                new BigDecimal("2.00"), new BigDecimal("3.00"), 0);

        List<VOPricedRole> pricedRoles = createPricedRoles(
                VOTechServiceFactory.getRoleDefinitions(voTechService, "ADMIN",
                        "USER", "GUEST"), new BigDecimal(5), new BigDecimal(4),
                new BigDecimal(3));
        priceModel.setRoleSpecificUserPrices(pricedRoles);

        List<VOPricedParameter> selectedParameters = Arrays
                .asList(newVOPricedParameter(VOServiceFactory.getParameter(
                        voServiceDetails, "MAX_FOLDER_NUMBER"), new BigDecimal(
                        "1.00"), new BigDecimal("4.00"), pricedRoles));
        priceModel.setSelectedParameters(selectedParameters);

        List<VOPricedEvent> consideredEvents = Arrays.asList(
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_DOWNLOAD"), new BigDecimal("5")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FILE_UPLOAD"), new BigDecimal("10")),
                newVOPricedEvent(VOTechServiceFactory.getEventDefinition(
                        voTechService, "FOLDER_NEW"), new BigDecimal("20")));

        priceModel.setConsideredEvents(consideredEvents);

        return priceModel;
    }

    private static VOPriceModel newVOPriceModel(PriceModelType pmType) {
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(pmType);
        return priceModel;
    }

    private static VOPriceModel newVOPriceModel(PriceModelType pmType,
            PricingPeriod period, String currencyISOCode,
            BigDecimal oneTimeFee, BigDecimal pricePerPeriod,
            BigDecimal pricePerUserAssignment, int freePeriod) {
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(pmType);
        priceModel.setPeriod(period);
        priceModel.setCurrencyISOCode(currencyISOCode);
        priceModel.setOneTimeFee(oneTimeFee);
        priceModel.setPricePerPeriod(pricePerPeriod);
        priceModel.setPricePerUserAssignment(pricePerUserAssignment);
        priceModel.setFreePeriod(freePeriod);

        return priceModel;
    }

    private static VOPriceModel newVOPriceModel(PriceModelType pmType,
            PricingPeriod period, String currencyISOCode,
            BigDecimal oneTimeFee, BigDecimal pricePerPeriod,
            List<VOSteppedPrice> steppedPrices, int freePeriod) {
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(pmType);
        priceModel.setPeriod(period);
        priceModel.setCurrencyISOCode(currencyISOCode);
        priceModel.setOneTimeFee(oneTimeFee);
        priceModel.setPricePerPeriod(pricePerPeriod);
        priceModel.setSteppedPrices(steppedPrices);
        priceModel.setFreePeriod(freePeriod);

        return priceModel;
    }

    public static VOPriceModel modifyPriceModel(VOPriceModel priceModel,
            PriceModelType pmType, PricingPeriod period,
            String currencyISOCode, BigDecimal oneTimeFee,
            BigDecimal pricePerPeriod, BigDecimal pricePerUserAssignment,
            int freePeriod) {
        priceModel.setType(pmType);
        priceModel.setPeriod(period);
        priceModel.setCurrencyISOCode(currencyISOCode);
        priceModel.setOneTimeFee(oneTimeFee);
        priceModel.setPricePerPeriod(pricePerPeriod);
        priceModel.setPricePerUserAssignment(pricePerUserAssignment);
        priceModel.setFreePeriod(freePeriod);

        return priceModel;
    }

    public static VOPriceModel modifyPriceModelPeriodFee(
            VOPriceModel priceModel, BigDecimal pricePerPeriod) {

        priceModel.setPricePerPeriod(pricePerPeriod);

        return priceModel;
    }

    private static List<VOPricedRole> createPricedRoles(
            List<VORoleDefinition> roleDefinitions,
            BigDecimal... roleUserPrices) {
        Assert.assertEquals("There must be a price for every role definition",
                roleDefinitions.size(), roleUserPrices.length);

        List<VOPricedRole> listVOPricedRole = new ArrayList<VOPricedRole>();
        for (int i = 0; i < roleDefinitions.size(); i++) {
            VOPricedRole voPricedRole = new VOPricedRole();
            voPricedRole.setPricePerUser(roleUserPrices[i]);
            voPricedRole.setRole(roleDefinitions.get(i));
            listVOPricedRole.add(voPricedRole);
        }

        return listVOPricedRole;
    }

    private static VOPricedParameter newVOPricedParameter(VOParameter param,
            BigDecimal pricePerSub, BigDecimal pricePerUser,
            List<VOPricedRole> listPricedRole) {
        VOPricedParameter pricedParam = new VOPricedParameter();
        pricedParam.setParameterKey(param.getKey());
        pricedParam.setVoParameterDef(param.getParameterDefinition());

        if (param.getParameterDefinition().getParameterOptions().size() > 0) {
            for (VOParameterOption parOption : param.getParameterDefinition()
                    .getParameterOptions()) {
                pricedParam.getPricedOptions().add(
                        newVOPricedOption(parOption, pricePerSub, pricePerUser,
                                listPricedRole));
            }
        } else {
            pricedParam.setPricePerSubscription(pricePerSub);
            pricedParam.setPricePerUser(pricePerUser);
            pricedParam.setRoleSpecificUserPrices(listPricedRole);
        }

        return pricedParam;
    }

    /**
     * Create VOPriceParameter with stepped prices. Only usable for numeric
     * parameters ({@link ParameterValueType#INTEGER ParameterValueType#LONG
     * ParameterValueType#DURATION})
     */
    private static VOPricedParameter newVOPricedParameter(VOParameter param,
            List<VOSteppedPrice> steppedPrices, BigDecimal pricePerUser,
            List<VOPricedRole> listPricedRole) {
        VOPricedParameter pricedParam = new VOPricedParameter();
        pricedParam.setParameterKey(param.getKey());
        pricedParam.setVoParameterDef(param.getParameterDefinition());
        pricedParam.setSteppedPrices(steppedPrices);
        pricedParam.setPricePerUser(pricePerUser);
        pricedParam.setRoleSpecificUserPrices(listPricedRole);

        return pricedParam;
    }

    private static VOPricedOption newVOPricedOption(
            VOParameterOption parOption, BigDecimal pricePerSub,
            BigDecimal pricePerUser, List<VOPricedRole> listPricedRole) {
        VOPricedOption pricedOption = new VOPricedOption();
        pricedOption.setOptionId(parOption.getOptionId());
        pricedOption.setParameterOptionKey(parOption.getKey());
        pricedOption.setPricePerSubscription(pricePerSub);
        pricedOption.setPricePerUser(pricePerUser);
        pricedOption.setRoleSpecificUserPrices(listPricedRole);
        return pricedOption;
    }

    private static VOPricedEvent newVOPricedEvent(
            VOEventDefinition eventDefinition, BigDecimal eventPrice) {
        VOPricedEvent pricedEvent = new VOPricedEvent(eventDefinition);
        pricedEvent.setEventPrice(eventPrice);
        return pricedEvent;
    }

    private static VOPricedEvent newVOPricedEvent(
            VOEventDefinition eventDefinition,
            List<VOSteppedPrice> steppedPrices) {
        VOPricedEvent pricedEvent = new VOPricedEvent(eventDefinition);
        pricedEvent.setSteppedPrices(steppedPrices);
        return pricedEvent;
    }

    private static VOSteppedPrice newVOSteppedPrice(Long limit, BigDecimal price) {
        VOSteppedPrice steppedPrice = new VOSteppedPrice();
        steppedPrice.setLimit(limit);
        steppedPrice.setPrice(price);
        return steppedPrice;
    }

}
