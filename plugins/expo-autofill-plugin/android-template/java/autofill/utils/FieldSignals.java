package com.pears.pass.autofill.utils;

/**
 * Plain snapshot of a field. AutofillHelper maps AssistStructure nodes here
 * so FieldClassifier can run in a javac-only test without the Android SDK.
 */
public final class FieldSignals {
    public String[] autofillHints = new String[0];
    public String hintText = "";
    public String idEntry = "";
    public String contentDescription = "";
    public String htmlType = "";
    public String htmlAutocomplete = "";
    public String htmlName = "";
    public String htmlClass = "";
    public String htmlPlaceholder = "";
    public boolean htmlVisibleAttr;
    public String htmlAriaLabel = "";
    public int inputType;
}
