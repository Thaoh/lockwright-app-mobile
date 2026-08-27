package com.pears.pass.autofill.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.OutcomeReceiver;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.credentials.exceptions.CreateCredentialException;
import androidx.credentials.exceptions.CreateCredentialUnknownException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.GetCredentialUnknownException;
import androidx.credentials.exceptions.NoCredentialException;
import androidx.credentials.provider.BeginCreateCredentialRequest;
import androidx.credentials.provider.BeginCreateCredentialResponse;
import androidx.credentials.provider.BeginCreatePublicKeyCredentialRequest;
import androidx.credentials.provider.BeginGetCredentialRequest;
import androidx.credentials.provider.BeginGetCredentialResponse;
import androidx.credentials.provider.BeginGetPublicKeyCredentialOption;
import androidx.credentials.provider.CreateEntry;
import androidx.credentials.provider.CredentialProviderService;
import androidx.credentials.provider.ProviderClearCredentialStateRequest;
import androidx.credentials.provider.PublicKeyCredentialEntry;

import com.pears.pass.R;
import com.pears.pass.autofill.data.AutofillUnlockSession;
import com.pears.pass.autofill.data.CredentialItem;
import com.pears.pass.autofill.ui.AuthenticationActivity;
import com.pears.pass.autofill.utils.AutofillConstants;
import com.pears.pass.autofill.utils.PasskeyPickerPlan;
import com.pears.pass.autofill.utils.SecureLog;
import com.pears.pass.autofill.ui.PasskeyRegistrationActivity;

import org.json.JSONObject;

import java.util.List;

/**
 * Android CredentialProviderService for passkey operations (Android 14+).
 * Handles both passkey registration (creation) and assertion (authentication).
 * The existing AutofillService remains for password autofill on all Android versions.
 */
@RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
public class PearPassCredentialProviderService extends CredentialProviderService {
    private static final String TAG = "PearPassCredProvService";

    @Override
    public void onBeginCreateCredentialRequest(
            @NonNull BeginCreateCredentialRequest request,
            @NonNull CancellationSignal cancellationSignal,
            @NonNull OutcomeReceiver<BeginCreateCredentialResponse, CreateCredentialException> callback) {

        SecureLog.d(TAG, "onBeginCreateCredentialRequest called");

        if (request instanceof BeginCreatePublicKeyCredentialRequest) {
            SecureLog.d(TAG, "Processing passkey registration request");

            try {
                // Create PendingIntent to launch PasskeyRegistrationActivity
                Intent intent = new Intent(this, PasskeyRegistrationActivity.class);
                intent.setAction("com.pears.pass.PASSKEY_REGISTRATION");

                PendingIntent pendingIntent = PendingIntent.getActivity(
                        this, 0, intent,
                        PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

                CreateEntry createEntry = new CreateEntry.Builder(
                        "Lockwright", pendingIntent)
                        .setDescription("Save passkey in Lockwright")
                        .build();

                BeginCreateCredentialResponse response =
                        new BeginCreateCredentialResponse.Builder()
                                .addCreateEntry(createEntry)
                                .build();

                callback.onResult(response);
            } catch (Exception e) {
                SecureLog.e(TAG, "Error creating registration response: " + e.getMessage());
                callback.onError(new CreateCredentialUnknownException(e.getMessage()));
            }
        } else {
            SecureLog.d(TAG, "Unsupported credential type, ignoring");
            callback.onError(new CreateCredentialUnknownException("Unsupported credential type"));
        }
    }

    @Override
    public void onBeginGetCredentialRequest(
            @NonNull BeginGetCredentialRequest request,
            @NonNull CancellationSignal cancellationSignal,
            @NonNull OutcomeReceiver<BeginGetCredentialResponse, GetCredentialException> callback) {

        SecureLog.d(TAG, "onBeginGetCredentialRequest called");

        BeginGetCredentialResponse.Builder responseBuilder =
                new BeginGetCredentialResponse.Builder();
        boolean hasEntries = false;

        for (int i = 0; i < request.getBeginGetCredentialOptions().size(); i++) {
            if (request.getBeginGetCredentialOptions().get(i) instanceof BeginGetPublicKeyCredentialOption) {
                BeginGetPublicKeyCredentialOption option =
                        (BeginGetPublicKeyCredentialOption) request.getBeginGetCredentialOptions().get(i);

                SecureLog.d(TAG, "Processing passkey assertion request");

                try {
                    String rpId = "";
                    try {
                        JSONObject json = new JSONObject(option.getRequestJson());
                        rpId = json.optString("rpId", "");
                    } catch (Exception parseError) {
                        SecureLog.e(TAG, "Passkey request JSON parse failed: " + parseError.getMessage());
                    }

                    int added = 0;
                    if (AutofillUnlockSession.get().isUnlocked()) {
                        List<CredentialItem> logins = AutofillUnlockSession.get().copyLogins();
                        for (int j = 0; j < logins.size(); j++) {
                            CredentialItem item = logins.get(j);
                            if (!PasskeyPickerPlan.isPasskeyForRp(
                                    item.hasPasskey(), item.getWebsites(), item.getUris(), rpId)) {
                                continue;
                            }
                            Intent intent = new Intent(this, AuthenticationActivity.class);
                            intent.setAction("com.pears.pass.PASSKEY_ASSERTION");
                            intent.putExtra("is_passkey_assertion", true);
                            intent.putExtra(AutofillConstants.EXTRA_PRESELECT_RECORD_ID, item.getId());
                            if (!rpId.isEmpty()) {
                                intent.putExtra(AutofillConstants.EXTRA_WEB_DOMAIN, rpId);
                            }

                            PendingIntent pendingIntent = PendingIntent.getActivity(
                                    this, 100 + i * 50 + j, intent,
                                    PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

                            String display = item.getUsername() != null && !item.getUsername().trim().isEmpty()
                                    ? item.getUsername().trim()
                                    : (item.getTitle() != null ? item.getTitle() : "Passkey");
                            PublicKeyCredentialEntry entry = new PublicKeyCredentialEntry.Builder(
                                    this, display, pendingIntent, option)
                                    .build();
                            responseBuilder.addCredentialEntry(entry);
                            added++;
                        }
                    }

                    if (added == 0) {
                        Intent intent = new Intent(this, AuthenticationActivity.class);
                        intent.setAction("com.pears.pass.PASSKEY_ASSERTION");
                        intent.putExtra("is_passkey_assertion", true);
                        if (!rpId.isEmpty()) {
                            intent.putExtra(AutofillConstants.EXTRA_WEB_DOMAIN, rpId);
                        }

                        PendingIntent pendingIntent = PendingIntent.getActivity(
                                this, 1 + i, intent,
                                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

                        PublicKeyCredentialEntry entry = new PublicKeyCredentialEntry.Builder(
                                this, "Unlock Lockwright",
                                pendingIntent, option)
                                .build();
                        responseBuilder.addCredentialEntry(entry);
                    }
                    hasEntries = true;
                } catch (Exception e) {
                    SecureLog.e(TAG, "Error creating assertion entry: " + e.getMessage());
                }
            }
        }

        if (hasEntries) {
            callback.onResult(responseBuilder.build());
        } else {
            SecureLog.d(TAG, "No passkey options found in request");
            callback.onError(new NoCredentialException());
        }
    }

    @Override
    public void onClearCredentialStateRequest(
            @NonNull ProviderClearCredentialStateRequest request,
            @NonNull CancellationSignal cancellationSignal,
            @NonNull OutcomeReceiver<Void, ClearCredentialException> callback) {
        SecureLog.d(TAG, "onClearCredentialStateRequest - no-op");
        callback.onResult(null);
    }
}
