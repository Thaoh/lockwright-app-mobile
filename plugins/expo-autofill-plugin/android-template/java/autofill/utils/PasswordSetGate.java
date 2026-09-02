package com.pears.pass.autofill.utils;

/**
 * Unlock to fill always opens the vaults store locked. Master-encryption
 * blobs are only readable while unlocked, so a finished vault must still
 * count as password-set when the store is initialized.
 */
public final class PasswordSetGate {
    private PasswordSetGate() {}

    public static boolean decide(boolean vaultsInitialized, boolean encryptionMaterialPresent) {
        return vaultsInitialized || encryptionMaterialPresent;
    }
}
