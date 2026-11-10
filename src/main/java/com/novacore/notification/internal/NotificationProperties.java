package com.novacore.notification.internal;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param from expéditeur des e-mails de la plateforme
 */
@ConfigurationProperties("novacore.mail")
record NotificationProperties(String from) {
}
