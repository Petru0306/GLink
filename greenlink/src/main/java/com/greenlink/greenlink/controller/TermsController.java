package com.greenlink.greenlink.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/termeni")
public class TermsController {
    // Explicitly allow access without authentication

    @Autowired
    private MessageSource messageSource;

    @GetMapping
    public String showTermsPage(Model model) {
        String pageTitle = messageSource.getMessage("terms.heading", null, LocaleContextHolder.getLocale());
        model.addAttribute("pageTitle", pageTitle + " - GreenLink");
        return "termeni"; // va căuta template-ul termeni.html
    }
}
