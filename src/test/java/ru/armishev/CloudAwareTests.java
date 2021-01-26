package ru.armishev;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.armishev.controller.AmazonController;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CloudAwareTests {
	@Autowired
	private AmazonController amazonController;

	@Test
	void contextLoads() {
		assertThat(amazonController).isNotNull();
	}
}
