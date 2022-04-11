package com.dib;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ImapTicketCreatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImapTicketCreatorApplication.class, args);
	}

}
