package com.greenlink.greenlink;

import com.greenlink.greenlink.service.ChallengeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GreenLinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(GreenLinkApplication.class, args);
	}

	@Bean
	public CommandLineRunner initChallenges(ChallengeService challengeService) {
		return args -> {
			challengeService.createDefaultChallenges();
		};
	}
}
