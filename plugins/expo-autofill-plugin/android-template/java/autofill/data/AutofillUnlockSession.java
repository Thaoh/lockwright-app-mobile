package com.pears.pass.autofill.data;

import com.pears.pass.autofill.utils.AutofillConstants;
import com.pears.pass.autofill.utils.UriMatchHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Process-memory unlock session for keyboard suggestions.
 * Survives AuthenticationActivity teardown (which must close the Bare
 * worklet so the main app can open the DB). Never written to disk.
 */
public final class AutofillUnlockSession {
    private static final AutofillUnlockSession INSTANCE = new AutofillUnlockSession();

    private final Object lock = new Object();
    private List<CredentialItem> credentials = new ArrayList<>();
    private long unlockedUntilMs;

    private AutofillUnlockSession() {}

    public static AutofillUnlockSession get() {
        return INSTANCE;
    }

    public boolean isUnlocked() {
        synchronized (lock) {
            expireLocked();
            return unlockedUntilMs > 0;
        }
    }

    public void unlock(List<CredentialItem> items, long ttlMs) {
        long ttl = ttlMs > 0 ? ttlMs : AutofillConstants.UNLOCK_SESSION_TTL_MS;
        synchronized (lock) {
            credentials = items != null ? new ArrayList<>(items) : new ArrayList<>();
            unlockedUntilMs = System.currentTimeMillis() + ttl;
        }
    }

    public void touch() {
        synchronized (lock) {
            expireLocked();
            if (unlockedUntilMs > 0) {
                unlockedUntilMs = System.currentTimeMillis() + AutofillConstants.UNLOCK_SESSION_TTL_MS;
            }
        }
    }

    public void lock() {
        synchronized (lock) {
            credentials = new ArrayList<>();
            unlockedUntilMs = 0;
        }
    }

    public List<CredentialItem> copyLogins() {
        synchronized (lock) {
            expireLocked();
            List<CredentialItem> out = new ArrayList<>();
            for (CredentialItem item : credentials) {
                if (item != null && !item.isCreditCard() && !item.isIdentity()) out.add(item);
            }
            return out;
        }
    }

    /**
     * Site matches, most specific first. Empty when nothing matches the page
     * (do not dump the whole vault onto the keyboard).
     */
    public List<CredentialItem> matchingLogins(String webDomain, String packageName, int limit) {
        if (limit <= 0) return Collections.emptyList();
        synchronized (lock) {
            expireLocked();
            if (unlockedUntilMs <= 0) return Collections.emptyList();

            String pageUrl = UriMatchHelper.pageUrlFromWebDomain(webDomain);
            String pkgPageUrl = packageName != null
                    ? UriMatchHelper.pageUrlFromWebDomain(UriMatchHelper.packageNameToDomain(packageName))
                    : null;

            List<CredentialItem> matches = new ArrayList<>();
            List<Integer> ranks = new ArrayList<>();
            for (CredentialItem item : credentials) {
                if (item == null || item.isCreditCard() || item.isIdentity()) continue;
                int rank = 0;
                if (pageUrl != null) {
                    rank = Math.max(rank, UriMatchHelper.getRecordSiteMatchRank(
                            item.getWebsites(), item.getUris(), pageUrl));
                }
                if (rank == 0 && pkgPageUrl != null) {
                    rank = UriMatchHelper.getRecordSiteMatchRank(
                            item.getWebsites(), item.getUris(), pkgPageUrl);
                }
                if (rank > 0) {
                    matches.add(item);
                    ranks.add(rank);
                }
            }

            List<Integer> order = new ArrayList<>();
            for (int i = 0; i < matches.size(); i++) order.add(i);
            order.sort((a, b) -> Integer.compare(ranks.get(b), ranks.get(a)));

            List<CredentialItem> out = new ArrayList<>();
            for (int i = 0; i < order.size() && out.size() < limit; i++) {
                out.add(matches.get(order.get(i)));
            }
            return out;
        }
    }

    private void expireLocked() {
        if (unlockedUntilMs > 0 && System.currentTimeMillis() >= unlockedUntilMs) {
            credentials = new ArrayList<>();
            unlockedUntilMs = 0;
        }
    }
}
