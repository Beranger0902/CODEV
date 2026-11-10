package com.novacore.shared;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Paramètres globaux de la plateforme.
 *
 * @param frontendUrl URL de l'application Angular, utilisée pour construire les liens envoyés par e-mail
 */
@ConfigurationProperties("novacore")
public record NovacoreProperties(String frontendUrl) {
}
