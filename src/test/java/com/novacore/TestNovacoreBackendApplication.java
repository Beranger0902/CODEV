package com.novacore;

import org.springframework.boot.SpringApplication;

public class TestNovacoreBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(NovacoreBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
