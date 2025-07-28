package com.greenlink.greenlink.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;
    
    private final Environment environment;

    public WebConfig(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Servirea resurselor statice din classpath (ex: CSS, JS)
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");

        // Servirea imaginilor încărcate de utilizatori
        exposeDirectory(uploadDir, registry);
    }

    private void exposeDirectory(String dirName, ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(dirName);
        String uploadAbsolutePath = uploadPath.toFile().getAbsolutePath();
        
        // Log the upload path for debugging
        System.out.println("WebConfig: Upload directory configured as: " + uploadAbsolutePath);
        System.out.println("WebConfig: Active profiles: " + String.join(", ", environment.getActiveProfiles()));

        registry.addResourceHandler("/" + dirName + "/**")
                .addResourceLocations("file:/" + uploadAbsolutePath + "/");
    }
} 