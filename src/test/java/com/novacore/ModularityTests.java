package com.novacore;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Garde-fou du monolithe modulaire : échoue si un module utilise les classes internes d'un autre
 * ou si une dépendance circulaire apparaît. Ne nécessite ni base de données ni Docker.
 */
class ModularityTests {

	private final ApplicationModules modules = ApplicationModules.of(NovacoreBackendApplication.class);

	@Test
	void verifiesModuleStructure() {
		modules.verify();
	}

	/** Génère les diagrammes des modules dans target/spring-modulith-docs. */
	@Test
	void writesDocumentation() {
		new Documenter(modules).writeDocumentation();
	}

}
