package org.ecospace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@EnableFeignClients(basePackages = "org.ecospace.notification.client")
@SpringBootApplication
public class EcoSpaceApplication {

	public static void main(String[] args) {

		SpringApplication.run(EcoSpaceApplication.class, args);
	}

}
