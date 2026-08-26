package com.pears.pass.autofill.utils;

import java.util.Map;

/**
 * Schema-2 migrate gate for the autofill process. Same ready rule as
 * useVaultSchemaBoot: ready === true and migratedToSchema >= 2.
 */
public final class VaultMigrationGate {
    public static final int SCHEMA_V2 = 2;
    public static final long POLL_MS = 100L;
    public static final long TIMEOUT_MS = 60_000L;

    public enum Decision {
        READY,
        RETRY,
        FAILED
    }

    private VaultMigrationGate() {}

    public static Decision decide(Map<String, Object> status) {
        if (status == null) {
            return Decision.RETRY;
        }
        boolean ready = Boolean.TRUE.equals(status.get("ready"));
        int schema = toInt(status.get("migratedToSchema"));
        if (ready && schema >= SCHEMA_V2) {
            return Decision.READY;
        }
        boolean inProgress = Boolean.TRUE.equals(status.get("inProgress"));
        String error = errorMessage(status.get("error"));
        if (error != null && !inProgress) {
            return Decision.FAILED;
        }
        return Decision.RETRY;
    }

    public static boolean timedOut(long elapsedMs) {
        return elapsedMs > TIMEOUT_MS;
    }

    public static String errorMessage(Object error) {
        if (!(error instanceof String)) {
            return null;
        }
        String text = ((String) error).trim();
        return text.isEmpty() ? null : text;
    }

    private static int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return (int) Double.parseDouble(((String) value).trim());
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }
}
