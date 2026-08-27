package com.pears.pass.autofill.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Real checks against PasskeyPickerPlan. System picker should only list
 * passkeys that match the relying party, not every login in the session.
 */
public final class PasskeyPickerPlanTest {
    private static int failures = 0;

    public static void main(String[] args) {
        matchesPasskeyOnSameRp();
        skipsLoginWithoutPasskey();
        skipsPasskeyOnOtherRp();
        emptyRpKeepsAnyPasskey();

        if (failures > 0) {
            System.err.println(failures + " PasskeyPickerPlan checks failed");
            System.exit(1);
        }
        System.out.println("ok");
    }

    private static void matchesPasskeyOnSameRp() {
        List<String> websites = listOf("https://github.com");
        List<UriMatchHelper.UriEntry> uris = new ArrayList<>();
        uris.add(new UriMatchHelper.UriEntry("https://github.com", "baseDomain"));
        expect("github passkey matches github rp",
                PasskeyPickerPlan.isPasskeyForRp(true, websites, uris, "github.com"),
                true);
    }

    private static void skipsLoginWithoutPasskey() {
        List<String> websites = listOf("https://github.com");
        expect("password-only login is not a picker entry",
                PasskeyPickerPlan.isPasskeyForRp(false, websites, new ArrayList<>(), "github.com"),
                false);
    }

    private static void skipsPasskeyOnOtherRp() {
        List<String> websites = listOf("https://twitter.com");
        expect("twitter passkey does not match github rp",
                PasskeyPickerPlan.isPasskeyForRp(true, websites, new ArrayList<>(), "github.com"),
                false);
    }

    private static void emptyRpKeepsAnyPasskey() {
        List<String> websites = listOf("https://github.com");
        expect("missing rp still lists passkeys",
                PasskeyPickerPlan.isPasskeyForRp(true, websites, new ArrayList<>(), ""),
                true);
    }

    private static List<String> listOf(String value) {
        List<String> out = new ArrayList<>();
        out.add(value);
        return out;
    }

    private static void expect(String label, Object got, Object want) {
        if (got == null ? want != null : !got.equals(want)) {
            failures++;
            System.err.println("FAIL " + label + ": got " + got + ", want " + want);
        }
    }
}
