package com.pears.pass.autofill.utils;

import java.util.Map;

/**
 * Real checks against LoginFillPlan. A silent pass means classified OTP
 * fields get the TOTP code even when username and password already filled.
 */
public final class LoginFillPlanTest {
    private static int failures = 0;

    public static void main(String[] args) {
        fillsOtpAlongsideUsernamePassword();
        skipsOtpWhenCodeMissing();
        otpOnlyPageDoesNotUseFallback();
        sparseFallbackDoesNotUseOtp();
        keyboardChipSkipsOtpEvenWhenCodePresent();

        if (failures > 0) {
            System.err.println(failures + " LoginFillPlan checks failed");
            System.exit(1);
        }
        System.out.println("ok");
    }

    private static void fillsOtpAlongsideUsernamePassword() {
        Map<String, String> got = LoginFillPlan.values(
                true, true, true, 0, "alice", "s3cret", "123456");
        expect("username", got.get(LoginFillPlan.USERNAME), "alice");
        expect("password", got.get(LoginFillPlan.PASSWORD), "s3cret");
        expect("otp with user+pass", got.get(LoginFillPlan.OTP), "123456");
        expect("no fallback when classified", got.containsKey(LoginFillPlan.FALLBACK_0), false);
    }

    private static void skipsOtpWhenCodeMissing() {
        Map<String, String> got = LoginFillPlan.values(
                true, true, true, 0, "alice", "s3cret", null);
        expect("no otp without code", got.containsKey(LoginFillPlan.OTP), false);
        expect("username kept", got.get(LoginFillPlan.USERNAME), "alice");
    }

    private static void otpOnlyPageDoesNotUseFallback() {
        Map<String, String> got = LoginFillPlan.values(
                false, false, true, 2, "alice", "s3cret", "654321");
        expect("otp-only fills otp", got.get(LoginFillPlan.OTP), "654321");
        expect("otp-only skips fallback", got.containsKey(LoginFillPlan.FALLBACK_0), false);
    }

    private static void sparseFallbackDoesNotUseOtp() {
        Map<String, String> got = LoginFillPlan.values(
                false, false, false, 2, "alice", "s3cret", "123456");
        expect("fallback username", got.get(LoginFillPlan.FALLBACK_0), "alice");
        expect("fallback password", got.get(LoginFillPlan.FALLBACK_1), "s3cret");
        expect("fallback never otp", got.containsKey(LoginFillPlan.OTP), false);
    }

    private static void keyboardChipSkipsOtpEvenWhenCodePresent() {
        Map<String, String> got = LoginFillPlan.values(
                true, true, true, 0, "alice", "s3cret", "123456", false);
        expect("chip username", got.get(LoginFillPlan.USERNAME), "alice");
        expect("chip password", got.get(LoginFillPlan.PASSWORD), "s3cret");
        expect("chip never otp", got.containsKey(LoginFillPlan.OTP), false);
    }

    private static void expect(String label, Object got, Object want) {
        if (got == null ? want != null : !got.equals(want)) {
            failures++;
            System.err.println("FAIL " + label + ": got " + got + ", want " + want);
        }
    }
}
