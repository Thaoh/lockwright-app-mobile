package com.pears.pass.autofill.utils;

/**
 * Real checks against AutofillSheetLoad. Unlock-to-fill must not block
 * the sheet on per-login TOTP, and a missing search field must not NPE.
 */
public final class AutofillSheetLoadTest {
    private static int failures = 0;

    public static void main(String[] args) {
        nullSearchIsEmptyQuery();
        typedSearchKeepsText();
        loginChipPreselectMatchesId();

        if (failures > 0) {
            System.err.println(failures + " AutofillSheetLoad checks failed");
            System.exit(1);
        }
        System.out.println("ok");
    }

    private static void nullSearchIsEmptyQuery() {
        expect("null query", AutofillSheetLoad.searchQuery(null), "");
    }

    private static void typedSearchKeepsText() {
        expect("typed query", AutofillSheetLoad.searchQuery("gh"), "gh");
    }

    private static void loginChipPreselectMatchesId() {
        expect("login chip matches",
                AutofillSheetLoad.isPreselect("rec-1", "rec-1"), true);
        expect("other login skipped",
                AutofillSheetLoad.isPreselect("rec-1", "rec-2"), false);
        expect("empty preselect skipped",
                AutofillSheetLoad.isPreselect("", "rec-1"), false);
        expect("null preselect skipped",
                AutofillSheetLoad.isPreselect(null, "rec-1"), false);
    }

    private static void expect(String label, Object got, Object want) {
        if (got == null ? want != null : !got.equals(want)) {
            failures++;
            System.err.println("FAIL " + label + ": got " + got + ", want " + want);
        }
    }
}
