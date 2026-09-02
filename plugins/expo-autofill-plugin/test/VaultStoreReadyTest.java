package com.pears.pass.autofill.utils;

/**
 * Real check: a cold worklet on a set-up phone must wait, not show
 * MissingConfiguration. A truly empty install still times out.
 */
public final class VaultStoreReadyTest {
    private static int failures = 0;

    public static void main(String[] args) {
        waitWhileStoreSilent();
        stopWhenInitialized();
        stopWhenBlobsPresent();
        giveUpAfterWait();

        if (failures > 0) {
            System.err.println(failures + " VaultStoreReady checks failed");
            System.exit(1);
        }
        System.out.println("ok");
    }

    private static void waitWhileStoreSilent() {
        expect("silent store still starting", VaultStoreReady.keepWaiting(false, false, 0), true);
    }

    private static void stopWhenInitialized() {
        expect("initialized is ready", VaultStoreReady.keepWaiting(true, false, 0), false);
    }

    private static void stopWhenBlobsPresent() {
        expect("blobs are ready", VaultStoreReady.keepWaiting(false, true, 0), false);
    }

    private static void giveUpAfterWait() {
        expect(
            "empty install after wait is missing config",
            VaultStoreReady.keepWaiting(false, false, VaultStoreReady.WAIT_MS),
            false);
    }

    private static void expect(String label, boolean got, boolean want) {
        if (got != want) {
            failures++;
            System.err.println("FAIL " + label + ": got " + got + ", want " + want);
        }
    }
}
