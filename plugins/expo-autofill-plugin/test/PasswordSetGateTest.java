package com.pears.pass.autofill.utils;

/**
 * Real check: Unlock to fill opens the vaults store locked. Encryption
 * blobs are only readable while unlocked, so a set-up phone must still
 * count as password-set when isInitialized is true.
 */
public final class PasswordSetGateTest {
    private static int failures = 0;

    public static void main(String[] args) {
        setUpPhoneLocked();
        freshInstall();
        unlockedWithBlobs();
        blobsWithoutInitFlag();

        if (failures > 0) {
            System.err.println(failures + " PasswordSetGate checks failed");
            System.exit(1);
        }
        System.out.println("ok");
    }

    private static void setUpPhoneLocked() {
        expect(
            "initialized+locked is password set",
            PasswordSetGate.decide(true, false),
            true);
    }

    private static void freshInstall() {
        expect(
            "not initialized and no blobs is missing config",
            PasswordSetGate.decide(false, false),
            false);
    }

    private static void unlockedWithBlobs() {
        expect(
            "initialized with encryption blobs is password set",
            PasswordSetGate.decide(true, true),
            true);
    }

    private static void blobsWithoutInitFlag() {
        expect(
            "encryption blobs without init flag still count",
            PasswordSetGate.decide(false, true),
            true);
    }

    private static void expect(String label, boolean got, boolean want) {
        if (got != want) {
            failures++;
            System.err.println("FAIL " + label + ": got " + got + ", want " + want);
        }
    }
}
