/*
 * Erstellt am: 14 Oct 2019 22:09:57
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import net.dv8tion.jda.api.entities.Message;

/**
 * This is the same as a normal message event, but it was already determined to
 * be a command
 * 
 * @author Jonas Michel
 *
 */
public class CommandEvent {

	public static final String COMMAND_IDENTIFIER = "o/";

	private Message message;

	/**
	 * @return the message
	 */
	public Message getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(Message message) {
		this.message = message;
	}

}
