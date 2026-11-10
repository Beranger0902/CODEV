package com.novacore.catalog.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlugsTest {

	@Test
	void lowercasesAndReplacesSpaces() {
		assertThat(Slugs.from("Spring Boot")).isEqualTo("spring-boot");
	}

	@Test
	void stripsAccents() {
		assertThat(Slugs.from("Développement Mobile")).isEqualTo("developpement-mobile");
	}

	@Test
	void keepsPlusSignsAsLetters() {
		assertThat(Slugs.from("C++")).isEqualTo("cpp");
		assertThat(Slugs.from("C#")).isEqualTo("c");
	}

	@Test
	void collapsesSeparatorsAndTrims() {
		assertThat(Slugs.from("  Node.js  ")).isEqualTo("node-js");
		assertThat(Slugs.from("Vue.js / Nuxt")).isEqualTo("vue-js-nuxt");
	}

}
