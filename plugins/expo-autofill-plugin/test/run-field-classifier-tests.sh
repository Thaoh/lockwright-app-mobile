#!/usr/bin/env bash
# Compile production FieldClassifier with the real test harness and run it.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

javac -d "$TMP" \
  "$ROOT/android-template/java/autofill/utils/FieldSignals.java" \
  "$ROOT/android-template/java/autofill/utils/FieldClassifier.java" \
  "$ROOT/test/FieldClassifierTest.java"

java -cp "$TMP" com.pears.pass.autofill.utils.FieldClassifierTest

javac -d "$TMP" \
  "$ROOT/android-template/java/autofill/utils/RecordStoreKeys.java" \
  "$ROOT/test/RecordStoreKeysTest.java"
java -cp "$TMP" com.pears.pass.autofill.utils.RecordStoreKeysTest

javac -d "$TMP" \
  "$ROOT/android-template/java/autofill/utils/VaultMigrationGate.java" \
  "$ROOT/test/VaultMigrationGateTest.java"
java -cp "$TMP" com.pears.pass.autofill.utils.VaultMigrationGateTest

javac -d "$TMP" \
  "$ROOT/android-template/java/autofill/utils/PasswordSetGate.java" \
  "$ROOT/test/PasswordSetGateTest.java"
java -cp "$TMP" com.pears.pass.autofill.utils.PasswordSetGateTest

javac -d "$TMP" \
  "$ROOT/android-template/java/autofill/utils/AutofillHostTeardown.java" \
  "$ROOT/test/AutofillHostTeardownTest.java"
java -cp "$TMP" com.pears.pass.autofill.utils.AutofillHostTeardownTest

javac -d "$TMP" \
  "$ROOT/android-template/java/autofill/utils/VaultStoreReady.java" \
  "$ROOT/test/VaultStoreReadyTest.java"
java -cp "$TMP" com.pears.pass.autofill.utils.VaultStoreReadyTest

javac -d "$TMP" \
  "$ROOT/android-template/java/autofill/utils/LoginFillPlan.java" \
  "$ROOT/test/LoginFillPlanTest.java"
java -cp "$TMP" com.pears.pass.autofill.utils.LoginFillPlanTest

javac -d "$TMP" \
  "$ROOT/android-template/java/autofill/utils/ChipFillDecision.java" \
  "$ROOT/test/ChipFillDecisionTest.java"
java -cp "$TMP" com.pears.pass.autofill.utils.ChipFillDecisionTest

javac -d "$TMP" \
  "$ROOT/android-template/java/autofill/utils/UriMatchHelper.java" \
  "$ROOT/android-template/java/autofill/utils/PasskeyPickerPlan.java" \
  "$ROOT/test/PasskeyPickerPlanTest.java"
java -cp "$TMP" com.pears.pass.autofill.utils.PasskeyPickerPlanTest

javac -d "$TMP" \
  "$ROOT/android-template/java/autofill/utils/OtpCodeResponse.java" \
  "$ROOT/test/OtpCodeResponseTest.java"
java -cp "$TMP" com.pears.pass.autofill.utils.OtpCodeResponseTest

javac -d "$TMP" \
  "$ROOT/android-template/java/autofill/utils/IdentityFillPlan.java" \
  "$ROOT/test/IdentityFillPlanTest.java"
java -cp "$TMP" com.pears.pass.autofill.utils.IdentityFillPlanTest

INIT="$ROOT/android-template/java/autofill/utils/VaultInitializer.java"
grep -q 'PasswordSetGate.decide' "$INIT"
grep -q 'VaultStoreReady.keepWaiting' "$INIT"

CLIENT="$ROOT/android-template/java/autofill/data/PearPassVaultClient.java"
grep -q 'RecordStoreKeys.recordKeyV2' "$CLIENT"
grep -q 'writeRecordDualStore' "$CLIENT"
grep -q 'RecordStoreKeys.fileKeyV2' "$CLIENT"
grep -q 'GET_VAULT_MIGRATION_STATUS(82)' "$CLIENT"
grep -q 'waitForVaultMigration' "$CLIENT"
grep -A20 'listCanonicalRecords()' "$CLIENT" | grep -q 'waitForVaultMigration'
grep -q 'GENERATE_OTP_CODES_BY_IDS(56)' "$CLIENT"
grep -q 'generateOtpCode' "$CLIENT"
grep -q 'OtpCodeResponse.codeFor' "$CLIENT"

AUTH="$ROOT/android-template/java/autofill/ui/AuthenticationActivity.java"
# BiometricPrompt pauses the fill host. Tearing down the worklet or UI
# there shows MissingConfiguration on first open and dismisses CombinedItems
# after fingerprint.
pause_body="$(awk '/void onPause\(/,/void onResume\(/' "$AUTH")"
printf '%s\n' "$pause_body" | grep -q 'AutofillHostTeardown.shouldReleaseWorklet(isFinishing())' || {
  echo "onPause must gate worklet teardown on AutofillHostTeardown.shouldReleaseWorklet(isFinishing())" >&2
  exit 1
}
printf '%s\n' "$pause_body" | grep -q 'hasPasswordSet = false' && {
  echo "onPause must not reset hasPasswordSet" >&2
  exit 1
}
printf '%s\n' "$pause_body" | grep -q 'remove(currentFragment)' && {
  echo "onPause must not remove the fill fragment" >&2
  exit 1
}
resume_body="$(awk '/void onResume\(/,/void initialize\(/' "$AUTH")"
printf '%s\n' "$resume_body" | grep -q 'LoadingFragment' && {
  echo "onResume must not replace the fill sheet with LoadingFragment" >&2
  exit 1
}
grep -q 'LoginFillPlan.values' "$AUTH"
grep -q 'LoginFillPlan.OTP' "$AUTH"
grep -q 'generateOtpCode' "$AUTH"
grep -q 'EXTRA_PRESELECT_RECORD_ID' "$AUTH"
grep -q 'EXTRA_IDENTITY_NAME_ID' "$AUTH"
grep -q 'IdentityFillPlan.values' "$AUTH"
grep -q 'TYPE_IDENTITY' "$AUTH"

SERVICE="$ROOT/android-template/java/autofill/service/PearPassAutofillService.java"
grep -q 'LoginFillPlan.values' "$SERVICE"
grep -q 'fillOtp' "$SERVICE"
grep -q 'ChipFillDecision.openAppForTotp' "$SERVICE"
grep -q 'EXTRA_PRESELECT_RECORD_ID' "$SERVICE"

COMBINED="$ROOT/android-template/java/autofill/ui/CombinedItemsFragment.java"
grep -q 'TYPE_IDENTITY' "$COMBINED"
grep -q 'fullName' "$COMBINED"

MASTER="$ROOT/android-template/java/autofill/ui/MasterPasswordFragment.java"
master_resume="$(awk '/void onResume\(/,/void onCreateView/' "$MASTER")"
printf '%s\n' "$master_resume" | grep -q 'isAuthenticatingBiometric' || {
  echo "onResume must not vaultsClose while fingerprint is in flight" >&2
  exit 1
}

HELPER="$ROOT/android-template/java/autofill/utils/AutofillHelper.java"
grep -q 'FieldClassifier.isPassword' "$HELPER"
grep -q 'FieldClassifier.isUsername' "$HELPER"
grep -q 'FieldClassifier.isOtp' "$HELPER"
grep -q 'FieldClassifier.isIdentity' "$HELPER"
grep -q 'applyPrecedingUsername' "$HELPER"
