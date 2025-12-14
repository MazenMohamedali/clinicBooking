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
POST /doctor/register/receptionist
Authorization: Bearer <doctor-token>
{
  "name": "Sarah",
  "email": "sarah@clinic.com",
  "password": "receptionist123",
  "phone": "01098765432",
  "hireDate": "2024-01-15",
  "notes": "Full-time receptionist",
  "clinicIds": [1]
}
*/