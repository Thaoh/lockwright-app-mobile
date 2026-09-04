package com.pears.pass.autofill.utils;

/**
 * Unlock-to-fill sheet load. OTP codes are generated on credential select,
 * not while listing. Search text is read after the host may have gone.
 */
public final class AutofillSheetLoad {
    private AutofillSheetLoad() {}

    public static String searchQuery(CharSequence text) {
        return text == null ? "" : text.toString();
    }

    public static boolean isPreselect(String preselectId, String itemId) {
        return preselectId != null && !preselectId.isEmpty() && preselectId.equals(itemId);
    }
}
