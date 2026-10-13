package com.moh.moh_backend.util;

import com.moh.moh_backend.Enum.Gender;

import com.moh.moh_backend.model.Baby;
import com.moh.moh_backend.Enum.GrowthStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * WHO Child Growth Standards, weight-for-age (0-5 years), sex-specific, by age in days.
 * Z-scores use the WHO LMS method including the WHO restricted computation beyond +/-3 SD.
 * Classification follows the Sri Lanka CHDR weight-for-age zones:
 *   z < -3 severe underweight, -3 <= z < -2 moderate underweight, -2..+2 normal, z > +2 above normal.
 * This is the single source used for stored growth status and for the charts in the UI.
 */
public final class WhoWeightForAge {

    public static final int MAX_AGE_DAYS = 1826;
    /** Average days per month used by WHO for month-based tables. */
    public static final double DAYS_PER_MONTH = 30.4375;

    public enum Zone { SEVERE_UNDERWEIGHT, MODERATE_UNDERWEIGHT, NORMAL, ABOVE_NORMAL }

    public record Lms(double l, double m, double s) {
        /** Weight (kg) at a given z-score. */
        public double weightAt(double z) {
            return m * Math.pow(1 + l * s * z, 1 / l);
        }
    }

    public record CurvePoint(int month, int ageDays, double sd3neg, double sd2neg, double median, double sd2pos, double sd3pos) {}

    private static final Lms[][] TABLE = load();

    private WhoWeightForAge() {}

    public static Lms lms(Gender sex, int ageDays) {
        if (sex == null || ageDays < 0 || ageDays > MAX_AGE_DAYS) return null;
        return TABLE[sex == Gender.MALE ? 0 : 1][ageDays];
    }

    /** Weight-for-age z-score, or null when sex/age/weight are missing or age is outside 0-5 years. */
    public static Double zScore(Gender sex, int ageDays, double weightKg) {
        Lms p = lms(sex, ageDays);
        if (p == null || weightKg <= 0) return null;
        double z = (Math.pow(weightKg / p.m(), p.l()) - 1) / (p.l() * p.s());
        if (z > 3) {
            double sd3 = p.weightAt(3);
            double sd23 = sd3 - p.weightAt(2);
            z = 3 + (weightKg - sd3) / sd23;
        } else if (z < -3) {
            double sd3 = p.weightAt(-3);
            double sd23 = p.weightAt(-2) - sd3;
            z = -3 + (weightKg - sd3) / sd23;
        }
        return Math.round(z * 100) / 100.0;
    }

    public static Double zScore(Gender sex, LocalDate dateOfBirth, LocalDate measuredOn, Number weightKg) {
        if (dateOfBirth == null || measuredOn == null || weightKg == null) return null;
        long days = ChronoUnit.DAYS.between(dateOfBirth, measuredOn);
        if (days < 0 || days > MAX_AGE_DAYS) return null;
        return zScore(sex, (int) days, weightKg.doubleValue());
    }

    public static Zone zone(Double z) {
        if (z == null) return null;
        if (z < -3) return Zone.SEVERE_UNDERWEIGHT;
        if (z < -2) return Zone.MODERATE_UNDERWEIGHT;
        if (z > 2) return Zone.ABOVE_NORMAL;
        return Zone.NORMAL;
    }

    /** Maps a zone to the stored GrowthStatus value (DB enum unchanged). */
    public static GrowthStatus toGrowthStatus(Zone zone) {
        if (zone == null) return null;
        return switch (zone) {
            case SEVERE_UNDERWEIGHT, MODERATE_UNDERWEIGHT -> GrowthStatus.UNDERWEIGHT;
            case ABOVE_NORMAL -> GrowthStatus.OVERWEIGHT;
            case NORMAL -> GrowthStatus.NORMAL;
        };
    }

    public static String label(Zone zone) {
        if (zone == null) return null;
        return switch (zone) {
            case SEVERE_UNDERWEIGHT -> "Severe underweight (below -3 SD)";
            case MODERATE_UNDERWEIGHT -> "Moderate underweight (-3 to -2 SD)";
            case NORMAL -> "Normal (-2 to +2 SD)";
            case ABOVE_NORMAL -> "Above +2 SD (check weight-for-length)";
        };
    }

    /** Monthly reference curves 0-60 months for charts. */
    public static List<CurvePoint> monthlyCurve(Gender sex) {
        List<CurvePoint> points = new ArrayList<>();
        for (int month = 0; month <= 60; month++) {
            int day = (int) Math.min(MAX_AGE_DAYS, Math.round(month * DAYS_PER_MONTH));
            Lms p = lms(sex, day);
            points.add(new CurvePoint(month, day,
                    round2(p.weightAt(-3)), round2(p.weightAt(-2)), round2(p.m()),
                    round2(p.weightAt(2)), round2(p.weightAt(3))));
        }
        return points;
    }

    private static double round2(double v) {
        return Math.round(v * 100) / 100.0;
    }

    private static Lms[][] load() {
        Lms[][] table = new Lms[2][MAX_AGE_DAYS + 1];
        try (InputStream in = WhoWeightForAge.class.getResourceAsStream("/who/weight_for_age_lms.tsv")) {
            if (in == null) {
                throw new IllegalStateException("Missing resource /who/weight_for_age_lms.tsv");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#") || line.startsWith("sex")) continue;
                String[] c = line.trim().split("\t");
                int sex = Integer.parseInt(c[0]);
                int age = Integer.parseInt(c[1]);
                if (age > MAX_AGE_DAYS) continue;
                table[sex - 1][age] = new Lms(Double.parseDouble(c[2]), Double.parseDouble(c[3]), Double.parseDouble(c[4]));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        for (Lms[] bySex : table) {
            for (int d = 0; d <= MAX_AGE_DAYS; d++) {
                if (bySex[d] == null) {
                    throw new IllegalStateException("WHO weight-for-age table is incomplete at day " + d);
                }
            }
        }
        return table;
    }
}
