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

HELPER="$ROOT/android-template/java/autofill/utils/AutofillHelper.java"
grep -q 'FieldClassifier.isPassword' "$HELPER"
grep -q 'FieldClassifier.isUsername' "$HELPER"
grep -q 'FieldClassifier.isOtp' "$HELPER"
grep -q 'FieldClassifier.isIdentity' "$HELPER"
grep -q 'applyPrecedingUsername' "$HELPER"
