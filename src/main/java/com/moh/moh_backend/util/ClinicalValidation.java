package com.moh.moh_backend.util;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared input checks for clinical data. Every method throws IllegalArgumentException
 * with a user-readable message, which GlobalExceptionHandler turns into HTTP 400.
 * Null values are accepted (fields are optional unless a caller checks presence).
 */
public final class ClinicalValidation {

    /** Old NIC: 9 digits + V/X. New NIC: 12 digits. */
    private static final Pattern NIC = Pattern.compile("^([0-9]{9}[VvXx]|[0-9]{12})$");
    /** Sri Lankan numbers: 0XXXXXXXXX or +94XXXXXXXXX (spaces/dashes ignored). */
    private static final Pattern PHONE = Pattern.compile("^(\\+94|0)[0-9]{9}$");
    private static final Pattern BLOOD_PRESSURE = Pattern.compile("^\\s*(\\d{2,3})\\s*/\\s*(\\d{2,3})\\s*$");

    private ClinicalValidation() {}

    public static void nic(String value, String label) {
        if (value == null || value.isBlank()) return;
        if (!NIC.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException(label + " must be 9 digits followed by V/X, or 12 digits");
        }
    }

    public static void phone(String value, String label) {
        if (value == null || value.isBlank()) return;
        String compact = value.replaceAll("[\\s-]", "");
        if (!PHONE.matcher(compact).matches()) {
            throw new IllegalArgumentException(label + " must be a valid Sri Lankan number (e.g. 0771234567 or +94771234567)");
        }
    }

    public static void range(Number value, double min, double max, String label) {
        if (value == null) return;
        double v = value.doubleValue();
        if (Double.isNaN(v) || v < min || v > max) {
            throw new IllegalArgumentException(label + " must be between " + fmt(min) + " and " + fmt(max));
        }
    }

    public static void notInFuture(LocalDate date, String label) {
        if (date != null && date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(label + " cannot be in the future");
        }
    }

    public static void notBefore(LocalDate date, LocalDate earliest, String label, String earliestLabel) {
        if (date != null && earliest != null && date.isBefore(earliest)) {
            throw new IllegalArgumentException(label + " cannot be before " + earliestLabel);
        }
    }

    /** Accepts "120/80" style readings with plausible systolic/diastolic values. */
    public static void bloodPressure(String value) {
        if (value == null || value.isBlank()) return;
        Matcher m = BLOOD_PRESSURE.matcher(value);
        if (!m.matches()) {
            throw new IllegalArgumentException("Blood pressure must be in the form systolic/diastolic, e.g. 120/80");
        }
        int systolic = Integer.parseInt(m.group(1));
        int diastolic = Integer.parseInt(m.group(2));
        if (systolic < 60 || systolic > 260 || diastolic < 30 || diastolic > 160 || diastolic >= systolic) {
            throw new IllegalArgumentException("Blood pressure " + value.trim() + " is outside the plausible range");
        }
    }

    /** Temperature is stored as text on child visits; validate when it is numeric. */
    public static void temperature(String value) {
        if (value == null || value.isBlank()) return;
        double t;
        try {
            t = Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Temperature must be a number in °C, e.g. 37.2");
        }
        range(t, 30, 45, "Temperature (°C)");
    }

    private static String fmt(double d) {
        return d == Math.rint(d) ? String.valueOf((long) d) : String.valueOf(d);
    }
}
