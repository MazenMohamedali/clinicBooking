package com.clinicHelper;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
// @ComponentScan(basePackages = {"clinichelper.com.helper", "user", "Admin", "Clinic", "Security", "DTOs"})
// @EnableJpaRepositories(basePackages = {"user"})
public class HelperApplication {
	public static void main(String[] args) {
		SpringApplication.run(HelperApplication.class, args);
	}

}


/*
  docker compose down
  docker compose up --build -d

  docker compose start app
  docker compose stop app
*/