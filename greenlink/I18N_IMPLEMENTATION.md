# Internationalization (i18n) Implementation for GreenLink

## Overview

This document describes the implementation of internationalization (i18n) for the GreenLink Spring Boot application, supporting English and Romanian languages.

## Features Implemented

### ✅ Core i18n Configuration
- **Locale Configuration**: Spring Boot configured to handle multiple locales
- **Message Bundles**: Separate properties files for English (`messages_en.properties`) and Romanian (`messages_ro.properties`)
- **Cookie-based Locale Storage**: User's language preference is stored in a cookie for persistence
- **Dynamic Language Switching**: Users can switch languages without session restart

### ✅ Language Toggle in Navbar
- **Flag Icons**: 🇬🇧 for English, 🇷🇴 for Romanian
- **Dropdown Menu**: Clean dropdown with language options
- **Persistent Selection**: Language choice is remembered across sessions
- **Visible on All Pages**: Language toggle appears in the navbar on every page

### ✅ Internationalized Content
- **Navigation Menu**: All navigation items are internationalized
- **Authentication**: Login, register, and logout buttons
- **Home Page**: Welcome message, tagline, and feature descriptions
- **Marketplace**: Product filters, categories, and action buttons
- **Login Modal**: Form labels and messages

## Technical Implementation

### 1. Configuration Files

#### `LocaleConfig.java`
```java
@Configuration
public class LocaleConfig implements WebMvcConfigurer {
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("locale");
        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setCookieMaxAge(60 * 60 * 24 * 365); // 1 year
        return resolver;
    }
    
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }
}
```

#### `LanguageController.java`
```java
@Controller
public class LanguageController {
    @GetMapping("/change-language")
    public String changeLanguage(@RequestParam String lang, 
                                HttpServletRequest request, 
                                HttpServletResponse response) {
        Locale locale = new Locale(lang);
        localeResolver.setLocale(request, response, locale);
        
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/";
    }
}
```

### 2. Message Bundles

#### `messages_en.properties`
Contains all English translations with organized sections:
- Navigation (`nav.*`)
- Authentication (`auth.*`)
- Common actions (`common.*`)
- Home page (`home.*`)
- Marketplace (`marketplace.*`)
- And more...

#### `messages_ro.properties`
Contains all Romanian translations with the same structure.

### 3. Thymeleaf Integration

#### Language Toggle in Navbar
```html
<div class="dropdown me-2">
    <button class="btn btn-outline-secondary dropdown-toggle" type="button" id="languageDropdown" data-bs-toggle="dropdown">
        <span th:if="${#locale.language == 'en'}">🇬🇧</span>
        <span th:if="${#locale.language == 'ro'}">🇷🇴</span>
    </button>
    <ul class="dropdown-menu">
        <li><a class="dropdown-item" href="/change-language?lang=en">🇬🇧 <span th:text="#{language.english}">English</span></a></li>
        <li><a class="dropdown-item" href="/change-language?lang=ro">🇷🇴 <span th:text="#{language.romanian}">Romanian</span></a></li>
    </ul>
</div>
```

#### Using Message Keys
```html
<a href="/marketplace" th:text="#{nav.marketplace}">Marketplace</a>
<button th:text="#{auth.login}">Login</button>
<h1 th:text="#{home.welcome}">Welcome to GreenLink</h1>
```

### 4. Application Properties

```properties
# Internationalization configuration
spring.messages.basename=messages
spring.messages.encoding=UTF-8
spring.messages.fallback-to-system-locale=false
```

## Usage Instructions

### For Users
1. **Language Toggle**: Click the flag icon (🇬🇧/🇷🇴) in the top-right corner of the navbar
2. **Language Selection**: Choose your preferred language from the dropdown
3. **Automatic Redirect**: You'll be redirected back to the same page with the new language
4. **Persistent Choice**: Your language preference is saved and will be remembered

### For Developers
1. **Adding New Text**: Add message keys to both `messages_en.properties` and `messages_ro.properties`
2. **Using in Templates**: Use `th:text="#{message.key}"` syntax in Thymeleaf templates
3. **Fallback Text**: Always provide fallback text in the HTML for better development experience

## Message Key Structure

### Navigation
- `nav.marketplace` - Marketplace link
- `nav.education` - Education link
- `nav.challenges` - Challenges link
- `nav.points` - Points link
- `nav.recycling` - Recycling Map link
- `nav.calculator` - Carbon Calculator link
- `nav.about` - About link

### Authentication
- `auth.register` - Register button
- `auth.login` - Login button
- `auth.logout` - Logout button
- `auth.invalid.credentials` - Invalid credentials message
- `auth.remember.me` - Remember me checkbox

### Home Page
- `home.welcome` - Welcome message
- `home.tagline` - Tagline
- `home.get.started` - Get Started button
- `home.learn.more` - Learn More button
- `home.what.we.offer` - What We Offer section title

### Marketplace
- `marketplace.title` - Marketplace title
- `marketplace.discover.products` - Discover products description
- `marketplace.filter.products` - Filter products title
- `marketplace.apply.filters` - Apply filters button
- `marketplace.reset.filters` - Reset filters button

## Benefits

1. **User Experience**: Users can use the application in their preferred language
2. **Accessibility**: Better accessibility for Romanian-speaking users
3. **Scalability**: Easy to add more languages in the future
4. **Maintainability**: Centralized text management
5. **SEO**: Better search engine optimization for different language markets

## Future Enhancements

1. **Browser Language Detection**: Automatically detect user's browser language
2. **More Languages**: Add support for additional languages
3. **Dynamic Content**: Internationalize dynamic content from database
4. **Date/Number Formatting**: Localize date and number formats
5. **RTL Support**: Add support for right-to-left languages

## Testing

To test the internationalization:

1. **Start the application**: `mvn spring-boot:run`
2. **Visit the homepage**: Navigate to `http://localhost:8080`
3. **Test language toggle**: Click the language toggle in the navbar
4. **Verify persistence**: Refresh the page and check if language preference is maintained
5. **Test all pages**: Navigate through different pages to ensure all text is translated

## Troubleshooting

### Common Issues

1. **Text not translating**: Ensure message key exists in both properties files
2. **Language not switching**: Check if the `LanguageController` is properly configured
3. **Cookie not persisting**: Verify cookie settings in `LocaleConfig`
4. **Encoding issues**: Ensure UTF-8 encoding is set in properties files

### Debug Tips

1. **Check current locale**: Use `${#locale}` in Thymeleaf templates
2. **Verify message keys**: Use `${#messages.msg('key')}` to test message resolution
3. **Browser developer tools**: Check cookies and network requests
4. **Spring Boot logs**: Monitor application logs for i18n-related messages

## Conclusion

The internationalization implementation provides a solid foundation for multi-language support in the GreenLink application. The implementation is user-friendly, maintainable, and easily extensible for future language additions. 