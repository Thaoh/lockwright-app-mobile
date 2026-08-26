package com.pears.pass.autofill.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Real checks against OtpCodeResponse. A silent pass means GENERATE_OTP
 * worklet arrays yield the code for the requested login id.
 */
public final class OtpCodeResponseTest {
    private static int failures = 0;

    public static void main(String[] args) {
        picksMatchingRecord();
        missingRecordIsNull();
        emptyResultIsNull();

        if (failures > 0) {
            System.err.println(failures + " OtpCodeResponse checks failed");
            System.exit(1);
        }
        System.out.println("ok");
    }

    private static void picksMatchingRecord() {
        Map<String, Object> result = wrap(
                row("other", "000000"),
                row("login-1", "123456"));
        expect("code for login-1", OtpCodeResponse.codeFor(result, "login-1"), "123456");
    }

    private static void missingRecordIsNull() {
        Map<String, Object> result = wrap(row("login-1", "123456"));
        expect("unknown id", OtpCodeResponse.codeFor(result, "nope"), null);
    }

    private static void emptyResultIsNull() {
        expect("null result", OtpCodeResponse.codeFor(null, "login-1"), null);
        expect("empty map", OtpCodeResponse.codeFor(new HashMap<>(), "login-1"), null);
    }

    private static Map<String, Object> wrap(Map<String, Object> a, Map<String, Object> b) {
        List<Object> array = new ArrayList<>();
        array.add(a);
        if (b != null) {
            array.add(b);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("array", array);
        return result;
    }

    private static Map<String, Object> wrap(Map<String, Object> a) {
        return wrap(a, null);
    }

    private static Map<String, Object> row(String recordId, String code) {
        Map<String, Object> row = new HashMap<>();
        row.put("recordId", recordId);
        row.put("code", code);
        return row;
    }

    private static void expect(String label, Object got, Object want) {
        if (got == null ? want != null : !got.equals(want)) {
            failures++;
            System.err.println("FAIL " + label + ": got " + got + ", want " + want);
        }
    }
}
