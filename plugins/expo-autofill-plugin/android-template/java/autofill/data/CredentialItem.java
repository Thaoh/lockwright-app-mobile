package com.pears.pass.autofill.data;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.pears.pass.autofill.utils.UriMatchHelper;

public class CredentialItem {
    public static final String TYPE_LOGIN = "login";
    public static final String TYPE_CREDIT_CARD = "creditCard";
    public static final String TYPE_IDENTITY = "identity";

    private String id;
    private String recordType = TYPE_LOGIN;
    private String title;
    private String username;
    private String password;
    private String url;
    private String notes;
    private List<String> websites;
    private List<UriMatchHelper.UriEntry> uris;

    // Credit card fields
    private String cardNumber;
    private String cardExpireDate;
    private String cardSecurityCode;
    private String cardholderName;

    // Identity fields
    private String fullName;
    private String phoneNumber;
    private String address;
    private String zip;
    private String city;
    private String region;
    private String country;

    // Passkey fields
    private boolean hasPasskey;
    private long passkeyCreatedAt;
    private Map<String, Object> credential;
    private String privateKeyBuffer;  // Base64URL-encoded PKCS#8
    private String userId;            // Base64URL user ID
    private String credentialId;      // Base64URL credential ID
    private String otpCode;

    public CredentialItem(String id, String title, String username, String password) {
        this.id = id;
        this.title = title;
        this.username = username;
        this.password = password;
        this.websites = new ArrayList<>();
        this.uris = new ArrayList<>();
        this.hasPasskey = false;
    }

    public CredentialItem(String id, String title, String username, String password, List<String> websites) {
        this.id = id;
        this.title = title;
        this.username = username;
        this.password = password;
        this.websites = websites != null ? websites : new ArrayList<>();
        this.uris = new ArrayList<>();
        this.hasPasskey = false;
    }

    /**
     * Constructor with passkey data.
     */
    public CredentialItem(String id, String title, String username, String password,
                          List<String> websites, boolean hasPasskey, long passkeyCreatedAt,
                          Map<String, Object> credential, String privateKeyBuffer,
                          String userId, String credentialId) {
        this.id = id;
        this.title = title;
        this.username = username;
        this.password = password;
        this.websites = websites != null ? websites : new ArrayList<>();
        this.uris = new ArrayList<>();
        this.hasPasskey = hasPasskey;
        this.passkeyCreatedAt = passkeyCreatedAt;
        this.credential = credential;
        this.privateKeyBuffer = privateKeyBuffer;
        this.userId = userId;
        this.credentialId = credentialId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<String> getWebsites() {
        return websites;
    }

    public void setWebsites(List<String> websites) {
        this.websites = websites != null ? websites : new ArrayList<>();
    }

    public List<UriMatchHelper.UriEntry> getUris() {
        return uris;
    }

    public void setUris(List<UriMatchHelper.UriEntry> uris) {
        this.uris = uris != null ? uris : new ArrayList<>();
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType != null ? recordType : TYPE_LOGIN;
    }

    public boolean isCreditCard() {
        return TYPE_CREDIT_CARD.equals(recordType);
    }

    public boolean isIdentity() {
        return TYPE_IDENTITY.equals(recordType);
    }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getCardExpireDate() { return cardExpireDate; }
    public void setCardExpireDate(String cardExpireDate) { this.cardExpireDate = cardExpireDate; }

    public String getCardSecurityCode() { return cardSecurityCode; }
    public void setCardSecurityCode(String cardSecurityCode) { this.cardSecurityCode = cardSecurityCode; }

    public String getCardholderName() { return cardholderName; }
    public void setCardholderName(String cardholderName) { this.cardholderName = cardholderName; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getZip() { return zip; }
    public void setZip(String zip) { this.zip = zip; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    // Passkey getters and setters

    public boolean hasPasskey() {
        return hasPasskey;
    }

    public void setHasPasskey(boolean hasPasskey) {
        this.hasPasskey = hasPasskey;
    }

    public long getPasskeyCreatedAt() {
        return passkeyCreatedAt;
    }

    public void setPasskeyCreatedAt(long passkeyCreatedAt) {
        this.passkeyCreatedAt = passkeyCreatedAt;
    }

    public Map<String, Object> getCredential() {
        return credential;
    }

    public void setCredential(Map<String, Object> credential) {
        this.credential = credential;
    }

    public String getPrivateKeyBuffer() {
        return privateKeyBuffer;
    }

    public void setPrivateKeyBuffer(String privateKeyBuffer) {
        this.privateKeyBuffer = privateKeyBuffer;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    /**
     * Get the passkey credential parsed from the raw credential map.
     */
    public PasskeyCredential getPasskeyCredential() {
        if (!hasPasskey || credential == null) {
            return null;
        }
        return PasskeyCredential.fromMap(credential);
    }
}
