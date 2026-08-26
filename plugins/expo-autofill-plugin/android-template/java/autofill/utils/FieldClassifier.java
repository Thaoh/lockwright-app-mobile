package com.pears.pass.autofill.utils;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Firstline field classification shared with AutofillHelper.
 * No android.* imports so the plugin test harness can javac this on a laptop.
 */
public final class FieldClassifier {
    private FieldClassifier() {}

    public static final int TYPE_MASK_CLASS = 0x0000000f;
    public static final int TYPE_MASK_VARIATION = 0x00000ff0;
    public static final int TYPE_CLASS_TEXT = 0x00000001;
    public static final int TYPE_CLASS_NUMBER = 0x00000002;
    public static final int TYPE_CLASS_PHONE = 0x00000003;
    public static final int TYPE_TEXT_VARIATION_PASSWORD = 0x00000080;
    public static final int TYPE_TEXT_VARIATION_VISIBLE_PASSWORD = 0x00000090;
    public static final int TYPE_TEXT_VARIATION_WEB_PASSWORD = 0x000000e0;
    public static final int TYPE_TEXT_VARIATION_EMAIL_ADDRESS = 0x00000020;
    public static final int TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS = 0x000000d0;
    public static final int TYPE_TEXT_FLAG_MULTI_LINE = 0x00020000;

    private static final String[] USERNAME_HINTS = {
            "email", "phone", "username", "user", "mobile", "login", "tel", "account"
    };
    private static final String[] PASSWORD_HINTS = {"password", "pswd", "pwd", "passwd"};
    private static final String[] IGNORED_HINTS = {"search", "find", "recipient", "edit"};
    private static final String[] OTP_HINTS = {
            "otp", "totp", "2fa", "mfa", "one-time", "onetime", "sms-code", "smscode",
            "verification", "one_time"
    };
    private static final String[] CARD_SECURITY_KEYWORDS = {
            "cvc", "cvv", "csc", "securitycode", "security-code", "cardcode"
    };
    private static final Pattern NEXTCLOUD_PASSWORD_CLASS =
            Pattern.compile("input-field__input|password-field");
    private static final Pattern PASSWORD_LABEL = Pattern.compile("\\bpassword\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern RECOVERY_OR_BACKUP = Pattern.compile(
            "recovery[\\s_-]?code|backup[\\s_-]?code|backup[\\s_-]?token",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern STRONG_OTP = Pattern.compile(
            "one[\\s_-]?time[\\s_-]?code|\\btotp\\b|otp[\\s_-]?code|verification[\\s_-]?code|two[\\s_-]?factor|2[\\s_-]?factor|\\bmfa[\\s_-]?code\\b|\\b2fa[\\s_-]?code\\b|onetimecode|authenticator",
            Pattern.CASE_INSENSITIVE
    );

    public static final class OrderedField {
        public final String id;
        public final boolean usernameCandidate;
        public final boolean password;

        public OrderedField(String id, boolean usernameCandidate, boolean password) {
            this.id = id;
            this.usernameCandidate = usernameCandidate;
            this.password = password;
        }
    }

    public static boolean isIgnored(FieldSignals s) {
        return containsAnyTerm(joinHaystack(s), IGNORED_HINTS);
    }

    public static boolean isPassword(FieldSignals s) {
        if (s == null) return false;
        if (isUsername(s)) return false;

        if (equalsIgnoreCase(s.htmlType, "password")) return true;

        int variation = s.inputType & TYPE_MASK_VARIATION;
        boolean hasPasswordVariation =
                variation == TYPE_TEXT_VARIATION_PASSWORD
                        || variation == TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        || variation == TYPE_TEXT_VARIATION_WEB_PASSWORD;
        if (hasPasswordVariation && (s.inputType & TYPE_TEXT_FLAG_MULTI_LINE) == 0) {
            return true;
        }

        if (isIgnored(s)) return false;

        if (containsAnyTerm(joinHints(s.autofillHints), PASSWORD_HINTS)) return true;
        if (containsAnyTerm(lower(s.hintText), PASSWORD_HINTS)) return true;
        if (containsAnyTerm(lower(s.idEntry), PASSWORD_HINTS)) return true;
        if (containsAnyTerm(lower(s.htmlAutocomplete), PASSWORD_HINTS)) return true;
        if (containsAnyTerm(lower(s.htmlName), PASSWORD_HINTS)) return true;
        if (containsToken(s.htmlAutocomplete, "current-password")
                || containsToken(s.htmlAutocomplete, "new-password")) {
            return true;
        }

        return isNextcloudPassword(s);
    }

    public static boolean isUsername(FieldSignals s) {
        if (s == null) return false;
        if (isIgnored(s)) return false;

        if (equalsIgnoreCase(s.htmlType, "email")) return true;

        int variation = s.inputType & TYPE_MASK_VARIATION;
        int inputClass = s.inputType & TYPE_MASK_CLASS;
        if (inputClass == TYPE_CLASS_PHONE || equalsIgnoreCase(s.htmlType, "tel")) {
            return true;
        }
        if (variation == TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
                || variation == TYPE_TEXT_VARIATION_EMAIL_ADDRESS) {
            return true;
        }

        return hasUsernameHints(s);
    }

    public static boolean isUsernameCandidate(FieldSignals s) {
        if (s == null) return false;
        if (isIgnored(s) || isPassword(s) || isOtp(s) || isCardSecurity(s) || isIdentity(s)) return false;
        if (isUsername(s)) return true;

        if (equalsIgnoreCase(s.htmlType, "text")
                || equalsIgnoreCase(s.htmlType, "email")
                || equalsIgnoreCase(s.htmlType, "tel")) {
            return true;
        }

        int inputClass = s.inputType & TYPE_MASK_CLASS;
        return inputClass == TYPE_CLASS_TEXT
                || inputClass == TYPE_CLASS_PHONE
                || inputClass == TYPE_CLASS_NUMBER;
    }

    public static boolean isOtp(FieldSignals s) {
        if (s == null) return false;
        if (isIgnored(s)) return false;
        if (isPassword(s) || isUsername(s)) return false;
        if (isCardSecurity(s)) return false;

        String excludeHay = lower(s.htmlName) + " " + lower(s.idEntry) + " " + lower(s.htmlPlaceholder);
        if (RECOVERY_OR_BACKUP.matcher(excludeHay).find()) return false;

        if (containsToken(s.htmlAutocomplete, "one-time-code")
                || containsToken(s.htmlAutocomplete, "one_time_code")
                || equalsIgnoreCase(s.htmlType, "one-time-code")) {
            return true;
        }

        if (hintsMatch(s.autofillHints, "smsOTPCode")
                || hintsMatch(s.autofillHints, "sms_otp")
                || containsAnyTerm(joinHints(s.autofillHints), OTP_HINTS)) {
            return true;
        }

        String hay = joinHaystack(s);
        return STRONG_OTP.matcher(hay).find() || containsAnyTerm(hay, OTP_HINTS);
    }

    public static boolean isCardSecurity(FieldSignals s) {
        if (s == null) return false;
        if (containsToken(s.htmlAutocomplete, "one-time-code")) return false;
        String hay = joinHaystack(s);
        if (hay.matches("(?s).*\\botp\\b.*")) return false;
        return containsAnyTerm(hay, CARD_SECURITY_KEYWORDS)
                || containsToken(s.htmlAutocomplete, "cc-csc");
    }

    public static boolean isIdentity(FieldSignals s) {
        return identityKind(s) != null;
    }

    /**
     * @return postal, address, city, region, country, phone, name, or null
     */
    public static String identityKind(FieldSignals s) {
        if (s == null) return null;
        if (isIgnored(s) || isPassword(s) || isUsername(s) || isOtp(s) || isCardSecurity(s)) {
            return null;
        }

        String hay = joinHaystack(s);
        String autocomplete = lower(s.htmlAutocomplete);

        if (containsToken(autocomplete, "postal-code")
                || hay.contains("zip")
                || hay.contains("postal")) {
            return "postal";
        }
        if (containsToken(autocomplete, "street-address")
                || containsToken(autocomplete, "address-line1")
                || hay.contains("address")) {
            return "address";
        }
        if (containsToken(autocomplete, "address-level2") || hay.contains("city")) {
            return "city";
        }
        if (containsToken(autocomplete, "address-level1")
                || hay.contains("region")
                || hay.contains("state")) {
            return "region";
        }
        if (containsToken(autocomplete, "country") || hay.contains("country")) {
            return "country";
        }
        if (equalsIgnoreCase(s.htmlType, "tel")
                || (s.inputType & TYPE_MASK_CLASS) == TYPE_CLASS_PHONE
                || hay.contains("phone")
                || hay.contains("tel")) {
            return "phone";
        }
        if (containsToken(autocomplete, "name")
                || containsToken(autocomplete, "given-name")
                || containsToken(autocomplete, "family-name")
                || hay.contains("name")) {
            return "name";
        }
        return null;
    }

    public static String pickPrecedingUsername(List<OrderedField> order) {
        if (order == null) return null;
        int passwordIndex = -1;
        for (int i = 0; i < order.size(); i++) {
            if (order.get(i).password) {
                passwordIndex = i;
                break;
            }
        }
        if (passwordIndex < 0) return null;
        for (int i = passwordIndex - 1; i >= 0; i--) {
            if (order.get(i).usernameCandidate) {
                return order.get(i).id;
            }
        }
        return null;
    }

    private static boolean isNextcloudPassword(FieldSignals s) {
        boolean nextcloudClass = s.htmlClass != null
                && NEXTCLOUD_PASSWORD_CLASS.matcher(s.htmlClass).find();
        if (!s.htmlVisibleAttr && !nextcloudClass) return false;

        String label = lower(s.htmlAriaLabel) + " " + lower(s.hintText) + " " + lower(s.contentDescription);
        return PASSWORD_LABEL.matcher(label).find();
    }

    private static boolean hasUsernameHints(FieldSignals s) {
        return containsAnyTerm(joinHints(s.autofillHints), USERNAME_HINTS)
                || containsAnyTerm(lower(s.hintText), USERNAME_HINTS)
                || containsAnyTerm(lower(s.idEntry), USERNAME_HINTS)
                || containsAnyTerm(lower(s.htmlAutocomplete), USERNAME_HINTS)
                || containsAnyTerm(lower(s.htmlName), USERNAME_HINTS)
                || containsAnyTerm(lower(s.htmlPlaceholder), USERNAME_HINTS)
                || containsAnyTerm(lower(s.htmlAriaLabel), USERNAME_HINTS);
    }

    private static String joinHaystack(FieldSignals s) {
        return (joinHints(s.autofillHints) + " "
                + lower(s.hintText) + " "
                + lower(s.idEntry) + " "
                + lower(s.contentDescription) + " "
                + lower(s.htmlAutocomplete) + " "
                + lower(s.htmlName) + " "
                + lower(s.htmlPlaceholder) + " "
                + lower(s.htmlAriaLabel)).trim();
    }

    private static String joinHints(String[] hints) {
        if (hints == null || hints.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (String h : hints) {
            if (h != null) sb.append(lower(h)).append(' ');
        }
        return sb.toString();
    }

    private static boolean hintsMatch(String[] hints, String want) {
        if (hints == null) return false;
        for (String h : hints) {
            if (h != null && h.equalsIgnoreCase(want)) return true;
        }
        return false;
    }

    private static boolean containsToken(String value, String token) {
        if (value == null || value.isEmpty()) return false;
        String lower = value.toLowerCase(Locale.ROOT);
        for (String part : lower.split("[\\s]+")) {
            if (part.equals(token) || part.endsWith(" " + token)) return true;
        }
        return lower.equals(token) || lower.contains(token);
    }

    private static boolean containsAnyTerm(String text, String[] terms) {
        if (text == null || text.isEmpty()) return false;
        for (String term : terms) {
            if (text.contains(term)) return true;
        }
        return false;
    }

    private static boolean equalsIgnoreCase(String value, String want) {
        return value != null && value.equalsIgnoreCase(want);
    }

    private static String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
