package com.greenlink.greenlink.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/confidentialitate")
public class PrivacyController {
    // Explicitly allow access without authentication

    @Autowired
    private MessageSource messageSource;
    
    @GetMapping
    public String showPrivacyPage(Model model) {
        String pageTitle = messageSource.getMessage("privacy.heading", null, LocaleContextHolder.getLocale());
        model.addAttribute("pageTitle", pageTitle + " - GreenLink");
        return "confidentialitate"; // va căuta template-ul confidentialitate.html
    }
}
