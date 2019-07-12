package com.rsachdev.Games.API;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GamesApiApplication {

	public static final String APPLICATION_NAMESPACE = "Games API";

	public static void main(String[] args) {
		SpringApplication.run(GamesApiApplication.class, args);
	}

}
