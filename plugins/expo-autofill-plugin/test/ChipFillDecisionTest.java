package com.pears.pass.autofill.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Real checks against ChipFillDecision. Keyboard chips open the app for TOTP
 * instead of stuffing a cached code into the OTP field.
 */
public final class ChipFillDecisionTest {
    private static int failures = 0;

    public static void main(String[] args) {
        opensAppOnlyWhenOtpFieldAndRecordHaveTotp();
        subtitleShowsTotpWhenChipWillOpenApp();
        recordHasOtpFromSecretOrCachedCode();
        recordHasOtpFromListedOtpPublic();

        if (failures > 0) {
            System.err.println(failures + " ChipFillDecision checks failed");
            System.exit(1);
        }
        System.out.println("ok");
    }

    private static void opensAppOnlyWhenOtpFieldAndRecordHaveTotp() {
        expect("otp field + totp opens app",
                ChipFillDecision.openAppForTotp(true, true), true);
        expect("otp field without totp stays on chip",
                ChipFillDecision.openAppForTotp(true, false), false);
        expect("no otp field stays on chip",
                ChipFillDecision.openAppForTotp(false, true), false);
    }

    private static void subtitleShowsTotpWhenChipWillOpenApp() {
        expect("totp subtitle",
                ChipFillDecision.subtitle("GitHub", true, true),
                ChipFillDecision.SUBTITLE_TOTP);
        expect("title when no totp path",
                ChipFillDecision.subtitle("GitHub", true, false),
                "GitHub");
        expect("fallback brand",
                ChipFillDecision.subtitle("  ", false, false),
                "Lockwright");
    }

    private static void recordHasOtpFromSecretOrCachedCode() {
        Map<String, Object> otp = new HashMap<>();
        otp.put("secret", "JBSWY3DPEHPK3PXP");
        expect("secret counts", ChipFillDecision.recordHasOtp(otp, null), true);
        expect("cached code counts", ChipFillDecision.recordHasOtp(null, "123456"), true);
        expect("empty otp does not", ChipFillDecision.recordHasOtp(new HashMap<>(), ""), false);
        expect("null does not", ChipFillDecision.recordHasOtp(null, null), false);
    }

    private static void recordHasOtpFromListedOtpPublic() {
        Map<String, Object> otpPublic = new HashMap<>();
        otpPublic.put("type", "TOTP");
        expect("listed otpPublic counts",
                ChipFillDecision.recordHasOtp(null, null, otpPublic), true);
        Map<String, Object> emptyPublic = new HashMap<>();
        expect("empty otpPublic does not",
                ChipFillDecision.recordHasOtp(null, null, emptyPublic), false);
    }

    private static void expect(String label, Object got, Object want) {
        if (got == null ? want != null : !got.equals(want)) {
            failures++;
            System.err.println("FAIL " + label + ": got " + got + ", want " + want);
        }
    }
}
