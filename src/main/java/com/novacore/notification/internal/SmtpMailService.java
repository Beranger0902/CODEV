package com.novacore.notification.internal;

import com.novacore.notification.MailService;
import com.novacore.notification.OutgoingMail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
class SmtpMailService implements MailService {

	private static final Logger log = LoggerFactory.getLogger(SmtpMailService.class);

	private final JavaMailSender mailSender;

	private final TaskExecutor executor;

	private final NotificationProperties properties;

	SmtpMailService(JavaMailSender mailSender, @Qualifier("applicationTaskExecutor") TaskExecutor executor,
			NotificationProperties properties) {
		this.mailSender = mailSender;
		this.executor = executor;
		this.properties = properties;
	}

	@Override
	public void send(OutgoingMail mail) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					executor.execute(() -> deliver(mail));
				}
			});
		}
		else {
			executor.execute(() -> deliver(mail));
		}
	}

	private void deliver(OutgoingMail mail) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(properties.from());
		message.setTo(mail.to());
		message.setSubject(mail.subject());
		message.setText(mail.body());
		try {
			mailSender.send(message);
		}
		catch (MailException ex) {
			// Le corps n'est jamais journalisé : il peut contenir un lien avec jeton.
			log.error("Échec de l'envoi de l'e-mail « {} »", mail.subject(), ex);
		}
	}

}
