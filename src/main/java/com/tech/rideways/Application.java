package com.tech.rideways;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.ServletContextApplicationContextInitializer;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Profile;
import org.springframework.util.ObjectUtils;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
@EnableSwagger2
public class Application {

	public static void main(String[] args) {
		SpringApplication application =
				new SpringApplicationBuilder()
						.sources(Application.class)
						.web(ObjectUtils.isEmpty(args) ? WebApplicationType.SERVLET : WebApplicationType.NONE)
						.build();

		application.run(args);

		if (args.length == 0) {
			System.out.println("Application is running...");
		}
	}

}
