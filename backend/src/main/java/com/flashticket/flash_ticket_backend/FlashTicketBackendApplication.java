package com.flashticket.flash_ticket_backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootApplication
@EnableCaching
public class FlashTicketBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlashTicketBackendApplication.class, args);
	}

}
