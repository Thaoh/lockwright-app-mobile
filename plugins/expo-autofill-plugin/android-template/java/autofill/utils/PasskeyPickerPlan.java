package com.pears.pass.autofill.utils;

import java.util.List;

/**
 * Which unlocked passkeys the Android system picker should list for an rpId.
 */
public final class PasskeyPickerPlan {
    private PasskeyPickerPlan() {}

    public static boolean isPasskeyForRp(
            boolean hasPasskey,
            List<String> websites,
            List<UriMatchHelper.UriEntry> uris,
            String rpId
    ) {
        if (!hasPasskey) {
            return false;
        }
        if (rpId == null || rpId.isEmpty()) {
            return true;
        }
        return UriMatchHelper.recordMatchesPage(
                websites, uris, UriMatchHelper.pageUrlFromWebDomain(rpId));
    }
}
