package com.pears.pass.autofill.utils;

/**
 * Unlock to fill starts a new worklet. vaultsGetStatus can read
 * isInitialized=false for a moment on a finished phone. That is not
 * MissingConfiguration.
 */
public final class VaultStoreReady {
    public static final long WAIT_MS = 2_500L;

    private VaultStoreReady() {}

    public static boolean keepWaiting(
            boolean vaultsInitialized,
            boolean encryptionMaterialPresent,
            long elapsedMs) {
        if (vaultsInitialized || encryptionMaterialPresent) {
            return false;
        }
        return elapsedMs < WAIT_MS;
    }
}
