package com.pears.pass.autofill.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.service.autofill.AutofillService;
import android.service.autofill.Dataset;
import android.service.autofill.FillCallback;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.InlinePresentation;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveRequest;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.widget.RemoteViews;
import android.widget.inline.InlinePresentationSpec;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.autofill.inline.UiVersions;
import androidx.autofill.inline.v1.InlineSuggestionUi;

import com.pears.pass.autofill.ui.AuthenticationActivity;
import com.pears.pass.autofill.utils.AutofillConstants;
import com.pears.pass.autofill.utils.AutofillHelper;
import com.pears.pass.autofill.utils.SecureLog;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class PearPassAutofillService extends AutofillService {
    private static final String TAG = "PearPassAutofill";

    @Override
    public void onFillRequest(@NonNull FillRequest request, @NonNull CancellationSignal cancellationSignal, @NonNull FillCallback callback) {
        try {
            AutofillHelper.ParsedFields parsedFields = AutofillHelper.parseFillRequest(request);
            if (parsedFields == null) {
                callback.onSuccess(null);
                return;
            }

            boolean hasSpecificFields = parsedFields.hasUsernameField()
                    || parsedFields.hasPasswordField()
                    || parsedFields.hasOtpField();
            boolean hasCardFields = parsedFields.hasCardField();
            boolean hasFallbackFields = parsedFields.hasAnyFallbackField();

            if (!hasSpecificFields && !hasCardFields && !hasFallbackFields) {
                callback.onSuccess(null);
                return;
            }

            List<AutofillId> targetIds = parsedFields.getFillTargetIds();
            if (targetIds.isEmpty()) {
                callback.onSuccess(null);
                return;
            }

            Intent authIntent = new Intent(this, AuthenticationActivity.class);
            putFieldExtras(authIntent, parsedFields);

            int requestCode = requestCodeFor(parsedFields);
            authIntent.setData(Uri.parse("pearpass://autofill/" + requestCode));

            int flags = PendingIntent.FLAG_CANCEL_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags |= PendingIntent.FLAG_MUTABLE;
            }

            IntentSender sender = PendingIntent.getActivity(this, requestCode, authIntent, flags).getIntentSender();

            RemoteViews presentation = new RemoteViews(getPackageName(), android.R.layout.simple_list_item_1);
            presentation.setTextViewText(android.R.id.text1, "PearPass — Unlock to fill");

            InlinePresentation inlinePresentation = buildInlinePresentation(
                    request, authIntent, requestCode, flags, hasSpecificFields || hasCardFields);

            Dataset.Builder datasetBuilder = new Dataset.Builder();
            for (AutofillId targetId : targetIds) {
                if (inlinePresentation != null) {
                    datasetBuilder.setValue(
                            targetId,
                            AutofillValue.forText(AutofillConstants.PLACEHOLDER_PASSWORD),
                            presentation,
                            inlinePresentation
                    );
                } else {
                    datasetBuilder.setValue(
                            targetId,
                            AutofillValue.forText(AutofillConstants.PLACEHOLDER_PASSWORD),
                            presentation
                    );
                }
            }
            datasetBuilder.setAuthentication(sender);

            FillResponse response = new FillResponse.Builder()
                    .addDataset(datasetBuilder.build())
                    .build();
            callback.onSuccess(response);
        } catch (Exception e) {
            SecureLog.e(TAG, "onFillRequest failed", e);
            try {
                callback.onSuccess(null);
            } catch (Exception ignored) {
                // Callback may already have been invoked.
            }
        }
    }

    private static void putFieldExtras(Intent authIntent, AutofillHelper.ParsedFields parsedFields) {
        authIntent.putExtra(AutofillConstants.EXTRA_USERNAME_ID, parsedFields.getUsernameId());
        authIntent.putExtra(AutofillConstants.EXTRA_PASSWORD_ID, parsedFields.getPasswordId());
        authIntent.putExtra(AutofillConstants.EXTRA_OTP_ID, parsedFields.getOtpId());
        authIntent.putExtra(AutofillConstants.EXTRA_CARD_NUMBER_ID, parsedFields.getCardNumberId());
        authIntent.putExtra(AutofillConstants.EXTRA_CARD_EXPIRY_DATE_ID, parsedFields.getCardExpiryDateId());
        authIntent.putExtra(AutofillConstants.EXTRA_CARD_EXPIRY_MONTH_ID, parsedFields.getCardExpiryMonthId());
        authIntent.putExtra(AutofillConstants.EXTRA_CARD_EXPIRY_YEAR_ID, parsedFields.getCardExpiryYearId());
        authIntent.putExtra(AutofillConstants.EXTRA_CARD_SECURITY_CODE_ID, parsedFields.getCardSecurityCodeId());
        authIntent.putExtra(AutofillConstants.EXTRA_CARDHOLDER_NAME_ID, parsedFields.getCardholderNameId());
        authIntent.putParcelableArrayListExtra(
                AutofillConstants.EXTRA_FALLBACK_IDS,
                new ArrayList<>(parsedFields.getFallbackFieldIds())
        );

        String webDomain = parsedFields.getWebDomain();
        String packageName = parsedFields.getPackageName();
        if (webDomain != null && !webDomain.isEmpty()) {
            authIntent.putExtra(AutofillConstants.EXTRA_WEB_DOMAIN, webDomain);
        }
        if (packageName != null && !packageName.isEmpty()) {
            authIntent.putExtra(AutofillConstants.EXTRA_PACKAGE_NAME, packageName);
        }
    }

    private static int requestCodeFor(AutofillHelper.ParsedFields fields) {
        int hash = 17;
        hash = 31 * hash + (fields.getUsernameId() != null ? fields.getUsernameId().hashCode() : 0);
        hash = 31 * hash + (fields.getPasswordId() != null ? fields.getPasswordId().hashCode() : 0);
        hash = 31 * hash + (fields.getOtpId() != null ? fields.getOtpId().hashCode() : 0);
        hash = 31 * hash + (fields.getWebDomain() != null ? fields.getWebDomain().hashCode() : 0);
        hash = 31 * hash + (fields.getPackageName() != null ? fields.getPackageName().hashCode() : 0);
        if (hash == Integer.MIN_VALUE) return 1;
        return Math.abs(hash);
    }

    private InlinePresentation buildInlinePresentation(
            FillRequest request,
            Intent authIntent,
            int requestCode,
            int flags,
            boolean pinChip
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || request.getInlineSuggestionsRequest() == null) {
            return null;
        }
        if (request.getInlineSuggestionsRequest().getMaxSuggestionCount() <= 0) {
            return null;
        }
        List<InlinePresentationSpec> specs = request.getInlineSuggestionsRequest().getInlinePresentationSpecs();
        if (specs == null || specs.isEmpty()) {
            return null;
        }

        InlinePresentationSpec spec = specs.get(0);
        if (!UiVersions.getVersions(spec.getStyle()).contains(UiVersions.INLINE_UI_VERSION_1)) {
            return null;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode + 1, authIntent, flags);
        Icon icon = Icon.createWithResource(this, android.R.drawable.ic_lock_lock);
        android.app.slice.Slice slice = InlineSuggestionUi.newContentBuilder(pendingIntent)
                .setTitle("PearPass")
                .setSubtitle("Unlock to fill")
                .setStartIcon(icon)
                .setContentDescription("PearPass autofill suggestion")
                .build()
                .getSlice();
        return new InlinePresentation(slice, spec, pinChip);
    }

    @Override
    public void onSaveRequest(@NonNull SaveRequest request, @NonNull SaveCallback callback) {
        callback.onSuccess();
    }

    @Override
    public void onConnected() {
        super.onConnected();
    }

    @Override
    public void onDisconnected() {
        super.onDisconnected();
    }
}
