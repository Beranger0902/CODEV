package com.novacore.shared.web;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Contexte technique d'une requête, conservé pour les sessions et le journal d'audit.
 */
public record ClientInfo(String ipAddress, String userAgent) {

	private static final int MAX_USER_AGENT_LENGTH = 255;

	public static ClientInfo from(HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent");
		if (userAgent != null && userAgent.length() > MAX_USER_AGENT_LENGTH) {
			userAgent = userAgent.substring(0, MAX_USER_AGENT_LENGTH);
		}
		return new ClientInfo(request.getRemoteAddr(), userAgent);
	}

	public static ClientInfo unknown() {
		return new ClientInfo(null, null);
	}

}
