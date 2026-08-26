package com.pears.pass.autofill.utils;

import java.util.List;
import java.util.Map;

/**
 * Unwrap GENERATE_OTP_CODES_BY_IDS replies ({@code { array: [...] }}).
 */
public final class OtpCodeResponse {
    private OtpCodeResponse() {}

    public static String codeFor(Map<String, Object> result, String recordId) {
        if (result == null || recordId == null || recordId.isEmpty()) {
            return null;
        }
        Object arrayObj = result.get("array");
        if (!(arrayObj instanceof List)) {
            return null;
        }
        for (Object rowObj : (List<?>) arrayObj) {
            if (!(rowObj instanceof Map)) {
                continue;
            }
            Map<?, ?> row = (Map<?, ?>) rowObj;
            if (!recordId.equals(row.get("recordId"))) {
                continue;
            }
            Object code = row.get("code");
            if (code instanceof String && !((String) code).isEmpty()) {
                return (String) code;
            }
            return null;
        }
        return null;
    }
}
