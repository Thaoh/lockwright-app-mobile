package com.pears.pass.autofill.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SDK-free identity fill mapping. Vault zip maps to classified postal.
 */
public final class IdentityFillPlan {
    public static final String NAME = "name";
    public static final String PHONE = "phone";
    public static final String ADDRESS = "address";
    public static final String POSTAL = "postal";
    public static final String CITY = "city";
    public static final String REGION = "region";
    public static final String COUNTRY = "country";

    private IdentityFillPlan() {}

    public static Map<String, String> values(
            boolean hasNameId,
            boolean hasPhoneId,
            boolean hasAddressId,
            boolean hasPostalId,
            boolean hasCityId,
            boolean hasRegionId,
            boolean hasCountryId,
            String fullName,
            String phoneNumber,
            String address,
            String zip,
            String city,
            String region,
            String country
    ) {
        Map<String, String> out = new LinkedHashMap<>();
        putIf(out, NAME, hasNameId, fullName);
        putIf(out, PHONE, hasPhoneId, phoneNumber);
        putIf(out, ADDRESS, hasAddressId, address);
        putIf(out, POSTAL, hasPostalId, zip);
        putIf(out, CITY, hasCityId, city);
        putIf(out, REGION, hasRegionId, region);
        putIf(out, COUNTRY, hasCountryId, country);
        return out;
    }

    private static void putIf(Map<String, String> out, String key, boolean present, String value) {
        if (!present || value == null) {
            return;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        out.put(key, trimmed);
    }
}
