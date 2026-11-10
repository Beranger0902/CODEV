package com.novacore;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "novacore.security.jwt.secret=test-secret-for-integration-tests-0123456789")
@Testcontainers(disabledWithoutDocker = true)
class NovacoreBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
