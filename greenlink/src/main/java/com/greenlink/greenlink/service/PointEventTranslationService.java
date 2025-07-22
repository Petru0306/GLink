package com.greenlink.greenlink.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PointEventTranslationService {

    @Autowired
    private MessageSource messageSource;

    public String translateDescription(String eventType, String originalDescription) {
        if (originalDescription == null || originalDescription.trim().isEmpty()) {
            return originalDescription;
        }

        try {
            switch (eventType) {
                case "CHALLENGE_COMPLETED":
                    return translateChallengeCompleted(originalDescription);
                case "QUIZ_COMPLETED":
                    return translateQuizCompleted(originalDescription);
                case "MARKETPLACE_SALE":
                    return translateMarketplaceSale(originalDescription);
                case "MARKETPLACE_PURCHASE":
                    return translateMarketplacePurchase(originalDescription);
                case "LESSON_COMPLETED":
                    return translateLessonCompleted(originalDescription);
                case "CARBON_CALCULATOR_USED":
                    return messageSource.getMessage("points.event.used.carbon.calculator", null, LocaleContextHolder.getLocale());
                case "RECYCLING_MAP_EXPLORED":
                    return messageSource.getMessage("points.event.explored.recycling.map", null, LocaleContextHolder.getLocale());
                case "PROFILE_UPDATED":
                    return messageSource.getMessage("points.event.updated.profile", null, LocaleContextHolder.getLocale());
                case "FRIEND_ADDED":
                    return translateFriendAdded(originalDescription);
                case "MESSAGE_SENT":
                    return messageSource.getMessage("points.event.sent.message", null, LocaleContextHolder.getLocale());
                case "OFFER_MADE":
                    return translateOfferMade(originalDescription);
                case "OFFER_ACCEPTED":
                    return translateOfferAccepted(originalDescription);
                default:
                    return originalDescription;
            }
        } catch (Exception e) {
            // If translation fails, return original description
            return originalDescription;
        }
    }

    private String translateChallengeCompleted(String description) {
        // Extract challenge title from "Completed challenge: {title}" or "Provocare completată: {title}"
        Pattern pattern = Pattern.compile("(?:Completed challenge|Provocare completată): (.+)");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            String challengeTitle = matcher.group(1);
            return messageSource.getMessage("points.event.completed.challenge", 
                new Object[]{challengeTitle}, LocaleContextHolder.getLocale());
        }
        return description;
    }

    private String translateQuizCompleted(String description) {
        // Extract quiz title from "Completed quiz: {title}" or "Test completat: {title}"
        Pattern pattern = Pattern.compile("(?:Completed quiz|Test completat): (.+)");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            String quizTitle = matcher.group(1);
            return messageSource.getMessage("points.event.completed.quiz", 
                new Object[]{quizTitle}, LocaleContextHolder.getLocale());
        }
        return description;
    }

    private String translateMarketplaceSale(String description) {
        // Extract product name and amount from "Sold product: {name} for {amount} RON" or "Produs vândut: {name} pentru {amount} RON"
        Pattern pattern = Pattern.compile("(?:Sold product|Produs vândut): (.+) (?:for|pentru) (\\d+\\.\\d+) RON");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            String productName = matcher.group(1);
            String amount = matcher.group(2);
            return messageSource.getMessage("points.event.sold.product", 
                new Object[]{productName, amount}, LocaleContextHolder.getLocale());
        }
        return description;
    }

    private String translateMarketplacePurchase(String description) {
        // Extract product name and amount from "Purchased product: {name} for {amount} RON" or "Produs cumpărat: {name} pentru {amount} RON"
        Pattern pattern = Pattern.compile("(?:Purchased product|Produs cumpărat): (.+) (?:for|pentru) (\\d+\\.\\d+) RON");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            String productName = matcher.group(1);
            String amount = matcher.group(2);
            return messageSource.getMessage("points.event.purchased.product", 
                new Object[]{productName, amount}, LocaleContextHolder.getLocale());
        }
        return description;
    }

    private String translateLessonCompleted(String description) {
        // Extract lesson title from "Completed lesson: {title}" or "Lecție completată: {title}"
        Pattern pattern = Pattern.compile("(?:Completed lesson|Lecție completată): (.+)");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            String lessonTitle = matcher.group(1);
            return messageSource.getMessage("points.event.completed.lesson", 
                new Object[]{lessonTitle}, LocaleContextHolder.getLocale());
        }
        return description;
    }

    private String translateFriendAdded(String description) {
        // Extract friend name from "Added friend: {name}" or "Prieten adăugat: {name}"
        Pattern pattern = Pattern.compile("(?:Added friend|Prieten adăugat): (.+)");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            String friendName = matcher.group(1);
            return messageSource.getMessage("points.event.added.friend", 
                new Object[]{friendName}, LocaleContextHolder.getLocale());
        }
        return description;
    }

    private String translateOfferMade(String description) {
        // Extract amount and product name from "Made offer of {amount} RON for: {product}" or "Ofertă făcută de {amount} RON pentru: {product}"
        Pattern pattern = Pattern.compile("(?:Made offer|Ofertă făcută) of (\\d+\\.\\d+) RON (?:for|pentru): (.+)");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            String amount = matcher.group(1);
            String productName = matcher.group(2);
            return messageSource.getMessage("points.event.made.offer", 
                new Object[]{amount, productName}, LocaleContextHolder.getLocale());
        }
        return description;
    }

    private String translateOfferAccepted(String description) {
        // Extract amount and product name from "Accepted offer of {amount} RON for: {product}" or "Ofertă acceptată de {amount} RON pentru: {product}"
        Pattern pattern = Pattern.compile("(?:Accepted offer|Ofertă acceptată) of (\\d+\\.\\d+) RON (?:for|pentru): (.+)");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            String amount = matcher.group(1);
            String productName = matcher.group(2);
            return messageSource.getMessage("points.event.accepted.offer", 
                new Object[]{amount, productName}, LocaleContextHolder.getLocale());
        }
        return description;
    }
} 