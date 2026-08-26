package com.pears.pass.autofill.utils;

import java.util.Map;

/**
 * Real checks against IdentityFillPlan. A silent pass means classified
 * identity fields get vault identity values, and empty values are skipped.
 */
public final class IdentityFillPlanTest {
    private static int failures = 0;

    public static void main(String[] args) {
        fillsClassifiedFields();
        skipsEmptyAndMissing();
        mapsZipToPostal();

        if (failures > 0) {
            System.err.println(failures + " IdentityFillPlan checks failed");
            System.exit(1);
        }
        System.out.println("ok");
    }

    private static void fillsClassifiedFields() {
        Map<String, String> got = IdentityFillPlan.values(
                true, true, true, true, true, true, true,
                "Jane Doe", "+15551212", "1 Main St", "94107",
                "San Francisco", "CA", "US");
        expect("name", got.get(IdentityFillPlan.NAME), "Jane Doe");
        expect("phone", got.get(IdentityFillPlan.PHONE), "+15551212");
        expect("address", got.get(IdentityFillPlan.ADDRESS), "1 Main St");
        expect("postal", got.get(IdentityFillPlan.POSTAL), "94107");
        expect("city", got.get(IdentityFillPlan.CITY), "San Francisco");
        expect("region", got.get(IdentityFillPlan.REGION), "CA");
        expect("country", got.get(IdentityFillPlan.COUNTRY), "US");
    }

    private static void skipsEmptyAndMissing() {
        Map<String, String> got = IdentityFillPlan.values(
                true, false, false, true, false, false, false,
                "Jane Doe", "+15551212", "1 Main St", "",
                "San Francisco", "CA", "US");
        expect("name kept", got.get(IdentityFillPlan.NAME), "Jane Doe");
        expect("no phone field", got.containsKey(IdentityFillPlan.PHONE), false);
        expect("empty zip skipped", got.containsKey(IdentityFillPlan.POSTAL), false);
        expect("no address field", got.containsKey(IdentityFillPlan.ADDRESS), false);
    }

    private static void mapsZipToPostal() {
        Map<String, String> got = IdentityFillPlan.values(
                false, false, false, true, false, false, false,
                null, null, null, "94107", null, null, null);
        expect("zip → postal", got.get(IdentityFillPlan.POSTAL), "94107");
    }

    private static void expect(String label, Object got, Object want) {
        if (got == null ? want != null : !got.equals(want)) {
            failures++;
            System.err.println("FAIL " + label + ": got " + got + ", want " + want);
        }
    }
}
