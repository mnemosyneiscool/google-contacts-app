package com.quijote.google_contacts_app.Service;

import java.io.IOException;
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
    public void updateContact(String resourceName, String familyName, String email, String phoneNumber) throws IOException {
        PeopleService peopleService = createPeopleService();
        Person existingContact = peopleService.people().get(resourceName)
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();

        Person updatedContact = new Person()
                .setEtag(existingContact.getEtag())
                .setNames(List.of(new Name().setGivenName(null).setFamilyName(familyName)))
                .setEmailAddresses(email != null && !email.isEmpty() ? List.of(new EmailAddress().setValue(email)) : null)
                .setPhoneNumbers(phoneNumber != null && !phoneNumber.isEmpty() ? List.of(new PhoneNumber().setValue(phoneNumber)) : null);

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
