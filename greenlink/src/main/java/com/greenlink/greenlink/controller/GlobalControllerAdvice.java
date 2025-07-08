package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.MessageService;
import com.greenlink.greenlink.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private MessageService messageService;
    
    @Autowired
    private UserService userService;
    
    @ModelAttribute
    public void addAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            try {
                User currentUser = userService.getUserByEmail(auth.getName());
                if (currentUser != null) {
                    long unreadMessageCount = messageService.countUnreadMessages(currentUser);
                    model.addAttribute("unreadMessageCount", unreadMessageCount);
                }
            } catch (Exception e) {
                // If any error occurs, just don't add the attribute
                // This prevents errors during login/initialization
            }
        }
    }
} 