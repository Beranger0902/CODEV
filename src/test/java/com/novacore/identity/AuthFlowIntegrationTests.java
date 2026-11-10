package com.novacore.identity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jayway.jsonpath.JsonPath;
import com.novacore.TestcontainersConfiguration;
import com.novacore.notification.MailService;
import com.novacore.notification.OutgoingMail;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Parcours complet : inscription → vérification → connexion → /me → rotation → réutilisation détectée.
 * Nécessite Docker (PostgreSQL via Testcontainers) ; ignoré automatiquement sinon.
 */
@SpringBootTest(properties = "novacore.security.jwt.secret=test-secret-for-integration-tests-0123456789")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class AuthFlowIntegrationTests {

	private static final Pattern TOKEN_IN_LINK = Pattern.compile("token=([A-Za-z0-9_-]+)");

	@Autowired
	MockMvc mvc;

	@MockitoBean
	MailService mailService;

	@Test
	void fullAuthenticationFlow() throws Exception {
		mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
				{"email":"Ada@Example.com","username":"ada","displayName":"Ada Lovelace",
				 "password":"une-phrase-de-passe-solide","acceptTerms":true}"""))
			.andExpect(status().isAccepted());

		// Connexion refusée tant que l'e-mail n'est pas confirmé
		mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
				{"email":"ada@example.com","password":"une-phrase-de-passe-solide"}"""))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("EMAIL_NOT_VERIFIED"));

		String verificationToken = lastMailToken();
		mvc.perform(post("/api/v1/auth/verify-email").contentType(MediaType.APPLICATION_JSON)
			.content("{\"token\":\"%s\"}".formatted(verificationToken)))
			.andExpect(status().isOk());

		String login = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
				{"email":"ada@example.com","password":"une-phrase-de-passe-solide"}"""))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		String accessToken = JsonPath.read(login, "$.accessToken");
		String refreshToken = JsonPath.read(login, "$.refreshToken");

		mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.username").value("ada"))
			.andExpect(jsonPath("$.roles[0]").value("MEMBER"));

		mvc.perform(get("/api/v1/me")).andExpect(status().isUnauthorized());

		// Rotation : le nouveau refresh token fonctionne, l'ancien est refusé et révoque la session
		String refreshed = mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
			.content("{\"refreshToken\":\"%s\"}".formatted(refreshToken)))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		String rotatedToken = JsonPath.read(refreshed, "$.refreshToken");
		assertThat(rotatedToken).isNotEqualTo(refreshToken);

		mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
			.content("{\"refreshToken\":\"%s\"}".formatted(refreshToken)))
			.andExpect(status().isUnauthorized());
		mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
			.content("{\"refreshToken\":\"%s\"}".formatted(rotatedToken)))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void rejectsInvalidRegistration() throws Exception {
		mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
				{"email":"pas-un-email","username":"a","displayName":"","password":"court","acceptTerms":false}"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
			.andExpect(jsonPath("$.errors.length()").value(5));
	}

	private String lastMailToken() {
		ArgumentCaptor<OutgoingMail> mail = ArgumentCaptor.forClass(OutgoingMail.class);
		verify(mailService, atLeastOnce()).send(mail.capture());
		Matcher matcher = TOKEN_IN_LINK.matcher(mail.getValue().body());
		assertThat(matcher.find()).isTrue();
		return matcher.group(1);
	}

}
