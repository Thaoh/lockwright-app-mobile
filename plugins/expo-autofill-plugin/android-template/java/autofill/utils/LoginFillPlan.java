package com.pears.pass.autofill.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SDK-free login fill mapping. OTP is a classified field, not a fallback.
 */
public final class LoginFillPlan {
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String OTP = "otp";
    public static final String FALLBACK_0 = "fallback0";
    public static final String FALLBACK_1 = "fallback1";

    private LoginFillPlan() {}

    public static Map<String, String> values(
            boolean hasUsernameId,
            boolean hasPasswordId,
            boolean hasOtpId,
            int fallbackCount,
            String username,
            String password,
            String otpCode
    ) {
        Map<String, String> out = new LinkedHashMap<>();
        boolean specific = false;
        if (hasUsernameId) {
            out.put(USERNAME, username != null ? username : "");
            specific = true;
        }
        if (hasPasswordId) {
            out.put(PASSWORD, password != null ? password : "");
            specific = true;
        }
        if (hasOtpId && otpCode != null && !otpCode.isEmpty()) {
            out.put(OTP, otpCode);
            specific = true;
        }
        if (specific) {
            return out;
        }
        if (fallbackCount >= 1) {
            out.put(FALLBACK_0, username != null ? username : "");
        }
        if (fallbackCount >= 2) {
            out.put(FALLBACK_1, password != null ? password : "");
        }
        return out;
    }
}
