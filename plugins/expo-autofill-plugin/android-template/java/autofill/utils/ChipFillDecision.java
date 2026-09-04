package com.pears.pass.autofill.utils;

import java.util.Map;

/**
 * Keyboard-chip TOTP policy. Username/password stay on the chip.
 * A detected OTP field plus a stored authenticator opens the app.
 */
public final class ChipFillDecision {
    public static final String SUBTITLE_TOTP = "TOTP";

    private ChipFillDecision() {}

    public static boolean openAppForTotp(boolean hasOtpField, boolean recordHasOtp) {
        return hasOtpField && recordHasOtp;
    }

    public static String subtitle(String title, boolean hasOtpField, boolean recordHasOtp) {
        if (openAppForTotp(hasOtpField, recordHasOtp)) {
            return SUBTITLE_TOTP;
        }
        if (title != null && !title.trim().isEmpty()) {
            return title.trim();
        }
        return "Lockwright";
    }

    public static boolean recordHasOtp(Object otp, String otpCode) {
        return recordHasOtp(otp, otpCode, null);
    }

    public static boolean recordHasOtp(Object otp, String otpCode, Object otpPublic) {
        if (otpCode != null && !otpCode.isEmpty()) {
            return true;
        }
        if (otpPublic instanceof Map) {
            Object type = ((Map<?, ?>) otpPublic).get("type");
            if ("TOTP".equals(type) || "HOTP".equals(type)) {
                return true;
            }
        }
        if (!(otp instanceof Map)) {
            return false;
        }
        Object secret = ((Map<?, ?>) otp).get("secret");
        return secret instanceof String && !((String) secret).isEmpty();
    }
}
