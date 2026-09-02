package com.pears.pass.autofill.utils;

/**
 * BiometricPrompt pauses AuthenticationActivity without finishing it.
 * Releasing the worklet or UI on that pause shows MissingConfiguration
 * on first open and dismisses CombinedItems after fingerprint.
 */
public final class AutofillHostTeardown {
    private AutofillHostTeardown() {}

    public static boolean shouldReleaseWorklet(boolean isFinishing) {
        return isFinishing;
    }
}
