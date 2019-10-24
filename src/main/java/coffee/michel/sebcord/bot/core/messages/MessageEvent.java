/*
 * Copyright GEMTEC GmbH 2019
 *
 * Erstellt am: 24 Oct 2019 21:36:34
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.messages;

import discord4j.core.object.entity.Message;

/**
 * Event for messages which are not commands.
 * 
 * @author Jonas Michel
 *
 */
public class MessageEvent {

	private Message message;

	/**
	 * @return the message
	 */
	public Message getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(Message message) {
		this.message = message;
	}

}
