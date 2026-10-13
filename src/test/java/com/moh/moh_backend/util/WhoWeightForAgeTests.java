package com.moh.moh_backend.util;

import com.moh.moh_backend.Enum.Gender;

import com.moh.moh_backend.model.Baby;
import com.moh.moh_backend.Enum.GrowthStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Checks against values published in the WHO weight-for-age z-score tables (kg, 1 decimal). */
class WhoWeightForAgeTests {

    private static void assertCurve(WhoWeightForAge.CurvePoint p, double sd3neg, double sd2neg, double median, double sd2pos, double sd3pos) {
        assertEquals(sd3neg, p.sd3neg(), 0.051, "-3SD at month " + p.month());
        assertEquals(sd2neg, p.sd2neg(), 0.051, "-2SD at month " + p.month());
        assertEquals(median, p.median(), 0.051, "median at month " + p.month());
        assertEquals(sd2pos, p.sd2pos(), 0.051, "+2SD at month " + p.month());
        assertEquals(sd3pos, p.sd3pos(), 0.051, "+3SD at month " + p.month());
    }

    @Test
    void boysCurveMatchesWhoTable() {
        List<WhoWeightForAge.CurvePoint> boys = WhoWeightForAge.monthlyCurve(Gender.MALE);
        assertEquals(61, boys.size());
        assertCurve(boys.get(0), 2.1, 2.5, 3.3, 4.4, 5.0);
        assertCurve(boys.get(12), 6.9, 7.7, 9.6, 12.0, 13.3);
        assertCurve(boys.get(60), 12.4, 14.1, 18.3, 24.2, 27.9);
    }

    @Test
    void girlsCurveMatchesWhoTable() {
        List<WhoWeightForAge.CurvePoint> girls = WhoWeightForAge.monthlyCurve(Gender.FEMALE);
        assertCurve(girls.get(0), 2.0, 2.4, 3.2, 4.2, 4.8);
        assertCurve(girls.get(12), 6.3, 7.0, 8.9, 11.5, 13.1);
        assertCurve(girls.get(60), 12.1, 13.7, 18.2, 24.9, 29.5);
    }

    @Test
    void zScoreAtMedianIsZeroAndSexMatters() {
        assertEquals(0.0, WhoWeightForAge.zScore(Gender.MALE, 0, 3.3464), 0.01);
        // Same weight is lower for a boy than for a girl at 12 months
        double boy = WhoWeightForAge.zScore(Gender.MALE, 365, 8.0);
        double girl = WhoWeightForAge.zScore(Gender.FEMALE, 365, 8.0);
        assertTrue(boy < girl);
    }

    @Test
    void zonesFollowChdrCutOffs() {
        assertEquals(WhoWeightForAge.Zone.SEVERE_UNDERWEIGHT, WhoWeightForAge.zone(-3.01));
        assertEquals(WhoWeightForAge.Zone.MODERATE_UNDERWEIGHT, WhoWeightForAge.zone(-3.0));
        assertEquals(WhoWeightForAge.Zone.MODERATE_UNDERWEIGHT, WhoWeightForAge.zone(-2.01));
        assertEquals(WhoWeightForAge.Zone.NORMAL, WhoWeightForAge.zone(-2.0));
        assertEquals(WhoWeightForAge.Zone.NORMAL, WhoWeightForAge.zone(2.0));
        assertEquals(WhoWeightForAge.Zone.ABOVE_NORMAL, WhoWeightForAge.zone(2.01));
        assertEquals(GrowthStatus.UNDERWEIGHT, WhoWeightForAge.toGrowthStatus(WhoWeightForAge.Zone.SEVERE_UNDERWEIGHT));
        assertEquals(GrowthStatus.OVERWEIGHT, WhoWeightForAge.toGrowthStatus(WhoWeightForAge.Zone.ABOVE_NORMAL));
    }

    @Test
    void classifiesRealisticCases() {
        // 6-month-old boy (day 183): -3SD is about 5.7 kg and the median about 7.9 kg
        assertEquals(WhoWeightForAge.Zone.SEVERE_UNDERWEIGHT,
                WhoWeightForAge.zone(WhoWeightForAge.zScore(Gender.MALE, 183, 5.5)));
        assertEquals(WhoWeightForAge.Zone.NORMAL,
                WhoWeightForAge.zone(WhoWeightForAge.zScore(Gender.MALE, 183, 7.9)));
        // Restricted computation beyond -3 SD stays continuous at the boundary
        double atSd3 = WhoWeightForAge.lms(Gender.MALE, 183).weightAt(-3);
        assertEquals(-3.0, WhoWeightForAge.zScore(Gender.MALE, 183, atSd3), 0.01);
    }

    @Test
    void outsideRangeReturnsNull() {
        assertNull(WhoWeightForAge.zScore(Gender.MALE, -1, 3.0));
        assertNull(WhoWeightForAge.zScore(Gender.MALE, 1827, 18.0));
        assertNull(WhoWeightForAge.zScore(null, 100, 6.0));
    }
}
