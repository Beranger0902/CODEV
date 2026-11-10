package com.novacore.identity;

import java.util.UUID;

/**
 * Vue minimale d'un compte, exposée aux autres modules (profil, projets…).
 * N'expose ni e-mail ni donnée sensible.
 */
public record AccountSummary(UUID id, String username, String displayName, boolean active) {
}
