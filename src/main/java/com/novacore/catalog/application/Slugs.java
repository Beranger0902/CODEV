package com.novacore.catalog.application;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Transforme un nom en identifiant lisible : minuscules, accents retirés,
 * séparateurs réduits à des tirets. « Spring Boot » → « spring-boot ».
 */
final class Slugs {

	private Slugs() {
	}

	static String from(String value) {
		String noAccents = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
		return noAccents.toLowerCase(Locale.ROOT)
			.replace('+', 'p') // c++ -> cpp, évite un slug vide
			.replaceAll("[^a-z0-9]+", "-")
			.replaceAll("(^-+)|(-+$)", "");
	}

}
