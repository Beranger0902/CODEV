package com.novacore.identity;

import java.util.Optional;
import java.util.UUID;

/**
 * Point d'entrée « comptes » pour les autres modules : résolution d'un compte
 * par identifiant ou par nom d'utilisateur, sans accès aux entités internes.
 */
public interface Accounts {

	Optional<AccountSummary> findById(UUID id);

	Optional<AccountSummary> findByUsername(String username);

}
