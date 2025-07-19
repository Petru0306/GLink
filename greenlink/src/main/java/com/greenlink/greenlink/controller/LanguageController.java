package com.greenlink.greenlink.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;

@Controller
public class LanguageController {

    @Autowired
    private LocaleResolver localeResolver;

    @GetMapping("/change-language")
    public String changeLanguage(@RequestParam String lang, 
                                HttpServletRequest request, 
                                HttpServletResponse response) {
        Locale locale = new Locale(lang);
        localeResolver.setLocale(request, response, locale);
        
        // Redirect back to the referring page or home
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/";
    }
} 