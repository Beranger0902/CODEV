package com.novacore.notification;

public record OutgoingMail(String to, String subject, String body) {
}
