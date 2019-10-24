package com.tech.rideways;

import com.tech.rideways.controllers.RideController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplicationTests {

	@Autowired
	private RideController rideController;

	@Test
	void contextLoads() throws Exception {
		assertThat(rideController).isNotNull();
	}

}
