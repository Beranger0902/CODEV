package com.novacore.notification;

public interface MailService {

	/**
	 * Envoie un e-mail de façon asynchrone. Si une transaction est en cours,
	 * l'envoi n'a lieu qu'après son commit : aucun e-mail pour une opération annulée.
	 */
	void send(OutgoingMail mail);

}
