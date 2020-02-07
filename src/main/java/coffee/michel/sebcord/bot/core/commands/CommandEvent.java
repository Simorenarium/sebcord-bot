/*
 * Erstellt am: 14 Oct 2019 22:09:57
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.util.List;

import net.dv8tion.jda.api.entities.Message;

/**
 * This is the same as a normal message event, but it was already determined to
 * be a command
 * 
 * @author Jonas Michel
 *
 */
public class CommandEvent {

	public static final String	COMMAND_IDENTIFIER	= "o/";

	private final Message		message;
	private final String		text;
	private final List<String>	matchedGroups;

	public CommandEvent(Message message, String text, List<String> matchedGroups) {
		super();
		this.message = message;
		this.text = text;
		this.matchedGroups = matchedGroups;
	}

	public Message getMessage() {
		return message;
	}

	public String getText() {
		return text;
	}

	public List<String> getMatchedGroups() {
		return this.matchedGroups;
	}

}
