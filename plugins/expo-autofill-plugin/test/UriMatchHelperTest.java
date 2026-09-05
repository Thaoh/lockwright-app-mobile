package com.pears.pass.autofill.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Real checks against UriMatchHelper. Android app fills must match
 * androidapp:// package URIs, not only a guessed https host.
 */
public final class UriMatchHelperTest {
    private static int failures = 0;

    public static void main(String[] args) {
        keepsAndroidAppUri();
        unwrapsHttpsPrefixedAndroidAppUri();
        androidAppRecordMatchesAndroidAppPage();
        guessedDomainStillMatchesHttpsWebsite();
        hostWithPortStillGetsHttps();
        prefixedAndroidAppRecordMatchesPackageFill();
        searchMatchesWebsiteWhenTitleDoesNot();

        if (failures > 0) {
            System.err.println(failures + " UriMatchHelper checks failed");
            System.exit(1);
        }
        System.out.println("ok");
    }

    private static void keepsAndroidAppUri() {
        expect(
                "androidapp URI is not prefixed with https",
                UriMatchHelper.normalizeUrl("androidapp://com.twitter.android"),
                "androidapp://com.twitter.android");
    }

    private static void unwrapsHttpsPrefixedAndroidAppUri() {
        expect(
                "glued https prefix unwraps",
                UriMatchHelper.normalizeUrl("https://androidapp://com.twitter.android"),
                "androidapp://com.twitter.android");
    }

    private static void androidAppRecordMatchesAndroidAppPage() {
        List<String> websites = listOf("androidapp://com.twitter.android");
        List<UriMatchHelper.UriEntry> uris = new ArrayList<>();
        uris.add(new UriMatchHelper.UriEntry(
                "androidapp://com.twitter.android", "host"));
        expect(
                "native app fill matches stored androidapp URI",
                UriMatchHelper.recordMatchesPage(
                        websites, uris, "androidapp://com.twitter.android"),
                true);
        expect(
                "package fill queries include androidapp URI",
                UriMatchHelper.bestRecordSiteMatchRank(
                        websites,
                        uris,
                        UriMatchHelper.pageUrlsForAutofill(null, "com.twitter.android")) > 0,
                true);
    }

    private static void guessedDomainStillMatchesHttpsWebsite() {
        List<String> websites = listOf("https://twitter.com");
        String page = UriMatchHelper.pageUrlFromWebDomain(
                UriMatchHelper.packageNameToDomain("com.twitter.android"));
        expect(
                "https website still matches guessed package domain",
                UriMatchHelper.recordMatchesPage(websites, new ArrayList<>(), page),
                true);
    }

    private static void hostWithPortStillGetsHttps() {
        expect(
                "schemeless host:port still gets https",
                UriMatchHelper.normalizeUrl("example.com:8080"),
                "https://example.com:8080");
    }

    private static void prefixedAndroidAppRecordMatchesPackageFill() {
        List<String> websites = listOf("https://androidapp://com.twitter.android");
        List<UriMatchHelper.UriEntry> uris = new ArrayList<>();
        uris.add(new UriMatchHelper.UriEntry(
                "https://androidapp://com.twitter.android", "host"));
        expect(
                "prefixed androidapp record matches package fill",
                UriMatchHelper.bestRecordSiteMatchRank(
                        websites,
                        uris,
                        UriMatchHelper.pageUrlsForAutofill(null, "com.twitter.android")) > 0,
                true);
    }

    private static void searchMatchesWebsiteWhenTitleDoesNot() {
        List<String> websites = listOf("https://twitter.com");
        expect(
                "search finds URI when title is unrelated",
                UriMatchHelper.credentialMatchesSearch(
                        "X", "user", websites, new ArrayList<>(), "twitter"),
                true);
        expect(
                "search misses unrelated query",
                UriMatchHelper.credentialMatchesSearch(
                        "X", "user", websites, new ArrayList<>(), "nomatch"),
                false);
        expect(
                "search finds androidapp package",
                UriMatchHelper.credentialMatchesSearch(
                        "X",
                        "user",
                        listOf("androidapp://com.twitter.android"),
                        new ArrayList<>(),
                        "twitter.android"),
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

    private static void expect(String label, boolean got, boolean want) {
        expect(label, Boolean.valueOf(got), Boolean.valueOf(want));
    }
}
