package ru.armishev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CloudAware {
	public static void main(String[] args) {
		SpringApplication.run(CloudAware.class, args);
	}
}
