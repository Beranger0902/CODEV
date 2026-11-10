package com.novacore.catalog;

import java.util.UUID;

public record DomainView(UUID id, String slug, String name, String description) {
}
