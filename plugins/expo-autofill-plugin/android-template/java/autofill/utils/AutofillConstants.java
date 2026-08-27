package com.pears.pass.autofill.utils;

/**
 * Constants used throughout the autofill extension.
 * Centralizes hardcoded values for easier maintenance.
 */
public final class AutofillConstants {

    private AutofillConstants() {
        // Prevent instantiation
    }

    /**
     * Placeholder value shown for password fields in autofill suggestions
     */
    public static final String PLACEHOLDER_PASSWORD = "●●●●●●●●";

    /**
     * Default unknown credential name when no name is available
     */
    public static final String UNKNOWN_CREDENTIAL = "Unknown";

    /**
     * Date format pattern used for displaying vault dates
     */
    public static final String DATE_FORMAT_PATTERN = "dd/MM/yyyy";
    
    /**
     * The autofill service class name as declared in AndroidManifest.xml
     */
    public static final String AUTOFILL_SERVICE_CLASS = ".autofill.service.PearPassAutofillService";

    public static final String EXTRA_USERNAME_ID = "username_id";
    public static final String EXTRA_PASSWORD_ID = "password_id";
    public static final String EXTRA_OTP_ID = "otp_id";
    public static final String EXTRA_CARD_NUMBER_ID = "card_number_id";
    public static final String EXTRA_CARD_EXPIRY_DATE_ID = "card_expiry_date_id";
    public static final String EXTRA_CARD_EXPIRY_MONTH_ID = "card_expiry_month_id";
    public static final String EXTRA_CARD_EXPIRY_YEAR_ID = "card_expiry_year_id";
    public static final String EXTRA_CARD_SECURITY_CODE_ID = "card_security_code_id";
    public static final String EXTRA_CARDHOLDER_NAME_ID = "cardholder_name_id";
    public static final String EXTRA_IDENTITY_NAME_ID = "identity_name_id";
    public static final String EXTRA_IDENTITY_PHONE_ID = "identity_phone_id";
    public static final String EXTRA_IDENTITY_ADDRESS_ID = "identity_address_id";
    public static final String EXTRA_IDENTITY_POSTAL_ID = "identity_postal_id";
    public static final String EXTRA_IDENTITY_CITY_ID = "identity_city_id";
    public static final String EXTRA_IDENTITY_REGION_ID = "identity_region_id";
    public static final String EXTRA_IDENTITY_COUNTRY_ID = "identity_country_id";
    public static final String EXTRA_FALLBACK_IDS = "fallback_ids";
    public static final String EXTRA_WEB_DOMAIN = "web_domain";
    public static final String EXTRA_PACKAGE_NAME = "package_name";
    public static final String EXTRA_PRESELECT_RECORD_ID = "preselect_record_id";

    /** In-memory autofill session; matches the app default (15 minutes). */
    public static final long UNLOCK_SESSION_TTL_MS = 15 * 60 * 1000L;

    public static final String INLINE_CLICK_ACTION = "com.pears.pass.autofill.INLINE_CLICK";
}
