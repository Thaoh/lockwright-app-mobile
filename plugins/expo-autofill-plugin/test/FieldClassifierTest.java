package com.pears.pass.autofill.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Real checks against FieldClassifier. javac these with the production classes
 * and run the main. A silent pass means the heuristics still match firstline
 * parity with the extension.
 */
public final class FieldClassifierTest {
    private static int failures = 0;

    public static void main(String[] args) {
        htmlPasswordTypeIsPassword();
        nextcloudAsTextIsPassword();
        autocompleteCurrentPasswordIsPassword();
        searchIsNotUsername();
        unlabeledTextBeforePasswordIsUsernameCandidate();
        identityPostalIsNotUsernameCandidate();
        recoveryCodeIsNotOtp();
        oneTimeCodeIsOtp();
        cvvIsNotOtp();
        postalCodeIsIdentity();
        phoneWithoutLoginHintsIsUsername();
        emailTypeIsUsername();
        passportIsNotPassword();
        pickPrecedingSkipsSearch();

        if (failures > 0) {
            System.err.println(failures + " FieldClassifier checks failed");
            System.exit(1);
        }
        System.out.println("ok");
    }

    private static void htmlPasswordTypeIsPassword() {
        FieldSignals s = new FieldSignals();
        s.htmlType = "password";
        expect("html type=password", FieldClassifier.isPassword(s), true);
    }

    private static void nextcloudAsTextIsPassword() {
        FieldSignals s = new FieldSignals();
        s.htmlType = "text";
        s.htmlClass = "input-field__input";
        s.htmlVisibleAttr = true;
        s.htmlAriaLabel = "Password";
        expect("Nextcloud as-text password", FieldClassifier.isPassword(s), true);
    }

    private static void autocompleteCurrentPasswordIsPassword() {
        FieldSignals s = new FieldSignals();
        s.htmlType = "text";
        s.htmlAutocomplete = "current-password";
        expect("autocomplete=current-password", FieldClassifier.isPassword(s), true);
    }

    private static void searchIsNotUsername() {
        FieldSignals s = new FieldSignals();
        s.htmlType = "text";
        s.htmlName = "search";
        s.idEntry = "search";
        s.htmlPlaceholder = "Search";
        expect("search is ignored", FieldClassifier.isIgnored(s), true);
        expect("search is not username", FieldClassifier.isUsername(s), false);
    }

    private static void unlabeledTextBeforePasswordIsUsernameCandidate() {
        FieldSignals s = new FieldSignals();
        s.htmlType = "text";
        s.htmlName = "acct";
        expect("unlabeled text is username candidate", FieldClassifier.isUsernameCandidate(s), true);
    }

    private static void identityPostalIsNotUsernameCandidate() {
        FieldSignals s = new FieldSignals();
        s.htmlType = "text";
        s.htmlAutocomplete = "postal-code";
        s.idEntry = "zipCode";
        expect("postal-code is not a login username candidate", FieldClassifier.isUsernameCandidate(s), false);
    }

    private static void recoveryCodeIsNotOtp() {
        FieldSignals s = new FieldSignals();
        s.htmlType = "text";
        s.htmlName = "recovery_code";
        expect("recovery_code is not OTP", FieldClassifier.isOtp(s), false);
    }

    private static void oneTimeCodeIsOtp() {
        FieldSignals s = new FieldSignals();
        s.htmlType = "text";
        s.htmlAutocomplete = "one-time-code";
        expect("one-time-code is OTP", FieldClassifier.isOtp(s), true);
    }

    private static void cvvIsNotOtp() {
        FieldSignals s = new FieldSignals();
        s.htmlType = "text";
        s.htmlName = "security_code";
        s.htmlPlaceholder = "CVV";
        expect("CVV is not OTP", FieldClassifier.isOtp(s), false);
    }

    private static void postalCodeIsIdentity() {
        FieldSignals s = new FieldSignals();
        s.htmlType = "text";
        s.htmlAutocomplete = "postal-code";
        s.idEntry = "zipCode";
        expect("postal-code is identity", FieldClassifier.isIdentity(s), true);
        expect("postal-code kind", FieldClassifier.identityKind(s), "postal");
    }

    private static void phoneWithoutLoginHintsIsUsername() {
        FieldSignals s = new FieldSignals();
        s.inputType = FieldClassifier.TYPE_CLASS_PHONE;
        s.htmlType = "tel";
        s.htmlName = "callbackPhone";
        s.idEntry = "phone";
        expect("bare tel stays username on Android", FieldClassifier.isUsername(s), true);
        expect("username wins over identity for tel", FieldClassifier.isIdentity(s), false);
    }

    private static void emailTypeIsUsername() {
        FieldSignals s = new FieldSignals();
        s.htmlType = "email";
        expect("type=email is username", FieldClassifier.isUsername(s), true);
    }

    private static void passportIsNotPassword() {
        FieldSignals s = new FieldSignals();
        s.htmlType = "text";
        s.htmlName = "passport";
        s.idEntry = "passport";
        expect("passport is not password", FieldClassifier.isPassword(s), false);
    }

    private static void pickPrecedingSkipsSearch() {
        List<FieldClassifier.OrderedField> order = new ArrayList<>();
        order.add(new FieldClassifier.OrderedField("search", false, false));
        order.add(new FieldClassifier.OrderedField("acct", true, false));
        order.add(new FieldClassifier.OrderedField("pwd", false, true));
        expect("preceding username is acct", FieldClassifier.pickPrecedingUsername(order), "acct");

        List<FieldClassifier.OrderedField> searchOnly = new ArrayList<>();
        searchOnly.add(new FieldClassifier.OrderedField("search", false, false));
        searchOnly.add(new FieldClassifier.OrderedField("pwd", false, true));
        expect("search is not preceding username", FieldClassifier.pickPrecedingUsername(searchOnly), null);
    }

    private static void expect(String label, Object got, Object want) {
        if (got == null ? want != null : !got.equals(want)) {
            failures++;
            System.err.println("FAIL " + label + ": got " + got + ", want " + want);
        }
    }
}
