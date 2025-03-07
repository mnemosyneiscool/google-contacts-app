package com.quijote.google_contacts_app.Controller;

import java.util.List;

public class ContactUpdateRequest {
    private String resourceName;
    private String familyName;
    private List<String> email;
    private List<String> phoneNumber;

    // Getters and Setters
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String getFamilyName() { return familyName; }
    public void setFamilyName(String familyName) { this.familyName = familyName; }

    public List<String> getEmail() { return email; }
    public void setEmail(List<String> email) { this.email = email; }

    public List<String> getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(List<String> phoneNumber) { this.phoneNumber = phoneNumber; }
}
