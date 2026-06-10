package com.wip.workipedia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WorkipediaApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkipediaApplication.class, args);
	}

}
