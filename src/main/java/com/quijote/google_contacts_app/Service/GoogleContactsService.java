package com.quijote.google_contacts_app.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.*;
import org.springframework.security.core.Authentication;

@Service    
public class GoogleContactsService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final RestTemplate restTemplate;

    public GoogleContactsService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
        this.restTemplate = new RestTemplate();
    }

    private String getAccessToken(String userName) {    
        OAuth2AuthorizedClient client = authorizedClientService
                .loadAuthorizedClient("google", userName);
        OAuth2AccessToken accessToken = client.getAccessToken();
        return accessToken.getTokenValue();
    }

    // READ FUNCTIONALITY
    public String getContacts(String userName) {
        String url = "https://people.googleapis.com/v1/people/me/connections"
                   + "?personFields=names,emailAddresses,phoneNumbers,photos";
        return restTemplate.getForObject(url + "&access_token=" + getAccessToken(userName), String.class);
    }

    // CREATE FUNCTIONALITY
    public String createContacts(String userName, String contactJson) {
        String url = "https://people.googleapis.com/v1/people:createContact";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getAccessToken(userName));
        HttpEntity<String> request = new HttpEntity<>(contactJson, headers);
        return restTemplate.postForObject(url, request, String.class);
    }

    private String getAccessToken() {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName()
            );
            if (client != null && client.getAccessToken() != null) {
                return client.getAccessToken().getTokenValue();
            }
        }
        throw new RuntimeException("OAuth2 authentication failed!");
    }

    private PeopleService createPeopleService() {
        return new PeopleService.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                new com.google.api.client.json.gson.GsonFactory(),
                request -> request.getHeaders().setAuthorization("Bearer " + getAccessToken())
        ).setApplicationName("Google Contacts App").build();
    }

    // UPDATE FUNCTIONALITY
    public void updateContact(String resourceName, String familyName, List<String> emails, List<String> phoneNumbers) throws IOException {
        PeopleService peopleService = createPeopleService();
        Person existingContact = peopleService.people().get(resourceName)
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();
    
        // Ensure existing lists are not null
        List<EmailAddress> updatedEmails = existingContact.getEmailAddresses() != null ? new ArrayList<>(existingContact.getEmailAddresses()) : new ArrayList<>();
        List<PhoneNumber> updatedPhones = existingContact.getPhoneNumbers() != null ? new ArrayList<>(existingContact.getPhoneNumbers()) : new ArrayList<>();
    
        // Remove emails that are no longer present in the request
        if (emails != null) {
            updatedEmails.removeIf(email -> !emails.contains(email.getValue()));
    
            // Add new emails if they do not already exist
            for (String email : emails) {
                if (updatedEmails.stream().noneMatch(e -> e.getValue().equals(email))) {
                    updatedEmails.add(new EmailAddress().setValue(email));
                }
            }
        }
    
        // Remove phone numbers that are no longer present in the request
        if (phoneNumbers != null) {
            updatedPhones.removeIf(phone -> !phoneNumbers.contains(phone.getValue()));
    
            // Add new phone numbers if they do not already exist
            for (String phone : phoneNumbers) {
                if (updatedPhones.stream().noneMatch(p -> p.getValue().equals(phone))) {
                    updatedPhones.add(new PhoneNumber().setValue(phone));
                }
            }
        }
    
        // Create updated contact object
        Person updatedContact = new Person()
                .setEtag(existingContact.getEtag())
                .setNames(List.of(new Name().setFamilyName(familyName)))
                .setEmailAddresses(updatedEmails.isEmpty() ? null : updatedEmails)
                .setPhoneNumbers(updatedPhones.isEmpty() ? null : updatedPhones);
    
        // Send update request
        peopleService.people().updateContact(resourceName, updatedContact)
                .setUpdatePersonFields("names,emailAddresses,phoneNumbers")
                .execute();
    }        

    // DELETE FUNCTIONALITY
    public void deleteContact(String resourceName) throws IOException {
        PeopleService peopleService = createPeopleService();
        peopleService.people().deleteContact(resourceName).execute();
    }
}
