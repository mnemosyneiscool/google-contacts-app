package com.quijote.google_contacts_app.Controller;

import com.quijote.google_contacts_app.Service.GoogleContactsService;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contacts")
public class GoogleContactsController {

    private final GoogleContactsService googleContactsService;

    public GoogleContactsController(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
    }

    // READ FUNCTIONALITY
    @GetMapping
    public String getAllContacts(@AuthenticationPrincipal OAuth2User principal) {
        return googleContactsService.getContacts(principal.getName());
    }

    // CREATE FUNCTIONALITY
    @PostMapping
    public String createContact(@AuthenticationPrincipal OAuth2User principal, @RequestBody String contactJson) {
        return googleContactsService.createContacts(principal.getName(), contactJson);
    }

    // UPDATE FUNCTIONALITY
    @PostMapping("/update")
    public String updateContact(
            @RequestParam String resourceName,
            @RequestParam String familyName,
            @RequestParam(required = false) List<String> email,
            @RequestParam(required = false) List<String> phoneNumber) {

        try {
            googleContactsService.updateContact(resourceName, familyName, email, phoneNumber);
            return "Contact updated successfully";
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }

    // DELETE FUNCTIONALITY
    @PostMapping("/delete")
    public String deleteContact(@RequestParam String resourceName) {
        try {
            googleContactsService.deleteContact(resourceName);
            return "Contact deleted successfully";
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }
}
