package com.abhishek.portfolio.auth;

import com.abhishek.portfolio.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
		"jwt.secret=test-only-context-secret-key-at-least-32-bytes",
		"jwt.expiration-ms=60000",
		"cors.allowed-origins=https://test.example",
		"spring.autoconfigure.exclude="
				+ "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration,"
				+ "org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration,"
				+ "org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration"
})
class AuthServiceApplicationTests {

	@MockitoBean
	private UserRepository userRepository;

	@MockitoBean
	private MongoOperations mongoOperations;

	@Autowired
	private HealthEndpoint healthEndpoint;

	@Test
	void contextLoads() {
		assertThat(healthEndpoint).isNotNull();
	}

}
