package com.novacore.catalog;

import java.util.UUID;

/**
 * Vue d'une technologie exposée aux autres modules et à l'API.
 *
 * @param kind catégorie textuelle : LANGUAGE, FRAMEWORK, LIBRARY, TOOL, PLATFORM, DATABASE, OTHER
 */
public record TechnologyView(UUID id, String slug, String name, String kind) {
}
