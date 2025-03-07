package com.quijote.google_contacts_app.Controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    // Home Page
    @GetMapping("/")
    public String home() {
        return "login";
    }

    // For White Label Error Page
    @GetMapping("/error")
    public String error() {
        return "error";
    }

    // Contacts Page
    @GetMapping("/contacts")
    public String contacts(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return "redirect:/";
        }
        model.addAttribute("userName", principal.getAttribute("name"));
        return "contacts"; 
    }
}