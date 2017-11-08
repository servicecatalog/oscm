/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test;

import java.math.BigDecimal;

import org.oscm.converter.PriceConverter;

/**
 * Constants for numbers as wrapper types used in tests.
 * 
 * @author hoffmann
 */
public class Numbers {

    public static final Long L_MIN = Long.valueOf(Long.MIN_VALUE);

    public static final Long Lm100 = Long.valueOf(-100);
    public static final Long Lm1 = Long.valueOf(-1);
    public static final Long L0 = Long.valueOf(0);
    public static final Long L1 = Long.valueOf(1);
    public static final Long L2 = Long.valueOf(2);
    public static final Long L3 = Long.valueOf(3);
    public static final Long L4 = Long.valueOf(4);
    public static final Long L5 = Long.valueOf(5);
    public static final Long L15 = Long.valueOf(15);
    public static final Long L10 = Long.valueOf(10);
    public static final Long L20 = Long.valueOf(20);
    public static final Long L40 = Long.valueOf(40);
    public static final Long L50 = Long.valueOf(50);
    public static final Long L100 = Long.valueOf(100);
    public static final Long L123 = Long.valueOf(123);
    public static final Long L150 = Long.valueOf(150);
    public static final Long L200 = Long.valueOf(200);
    public static final Long L300 = Long.valueOf(300);
    public static final Long L1000 = Long.valueOf(1000);
    public static final Long L2000 = Long.valueOf(2000);
    public static final Long L3000 = Long.valueOf(3000);

    public static final Long L_MAX = Long.valueOf(Long.MAX_VALUE);

    public static final int BIGDECIMAL_SCALE = PriceConverter.NUMBER_OF_DECIMAL_PLACES;
    public static final BigDecimal BD3000 = BigDecimal.valueOf(3000).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD2000 = BigDecimal.valueOf(2000).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD1900 = BigDecimal.valueOf(1900).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD1800 = BigDecimal.valueOf(1800).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD1700 = BigDecimal.valueOf(1700).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD1600 = BigDecimal.valueOf(1600).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD1500 = BigDecimal.valueOf(1500).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD1400 = BigDecimal.valueOf(1400).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD1300 = BigDecimal.valueOf(1300).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD1200 = BigDecimal.valueOf(1200).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD1100 = BigDecimal.valueOf(1100).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD1000 = BigDecimal.valueOf(1000).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD900 = BigDecimal.valueOf(900).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD700 = BigDecimal.valueOf(700).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD600 = BigDecimal.valueOf(600).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD500 = BigDecimal.valueOf(500).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD400 = BigDecimal.valueOf(400).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD300 = BigDecimal.valueOf(300).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD200 = BigDecimal.valueOf(200).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD150 = BigDecimal.valueOf(150).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD140 = BigDecimal.valueOf(140).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD130 = BigDecimal.valueOf(130).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD120 = BigDecimal.valueOf(120).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD110 = BigDecimal.valueOf(110).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD100 = BigDecimal.valueOf(100).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BDm100 = BigDecimal.valueOf(-100).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD90 = BigDecimal.valueOf(90).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD80 = BigDecimal.valueOf(80).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD70 = BigDecimal.valueOf(70).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD60 = BigDecimal.valueOf(60).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD50 = BigDecimal.valueOf(50).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD40 = BigDecimal.valueOf(40).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD30 = BigDecimal.valueOf(30).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD25 = BigDecimal.valueOf(25).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD20 = BigDecimal.valueOf(20).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD19 = BigDecimal.valueOf(19).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD18 = BigDecimal.valueOf(18).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD17 = BigDecimal.valueOf(17).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD16 = BigDecimal.valueOf(16).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD15 = BigDecimal.valueOf(15).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD14 = BigDecimal.valueOf(14).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD13 = BigDecimal.valueOf(13).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD12 = BigDecimal.valueOf(12).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD11 = BigDecimal.valueOf(11).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD10 = BigDecimal.valueOf(10).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD9 = BigDecimal.valueOf(9).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD8 = BigDecimal.valueOf(8).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD7 = BigDecimal.valueOf(7).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD6 = BigDecimal.valueOf(6).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD5 = BigDecimal.valueOf(5).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD4 = BigDecimal.valueOf(4).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD3 = BigDecimal.valueOf(3).setScale(
            BIGDECIMAL_SCALE);
    public static final BigDecimal BD2 = BigDecimal.valueOf(2).setScale(
            BIGDECIMAL_SCALE);

    /**
     * Constant timestamp for unit testing. The timestamp is Thu Aug 26 12:00:00
     * CEST 2010 (a sunny summer day in munich).
     */
    public static final long TIMESTAMP = 1282816800000L;

    public static final Long L_TIMESTAMP = Long.valueOf(TIMESTAMP);

}
