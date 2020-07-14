/*
 * Erstellt am: 20 Oct 2019 12:21:09
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Jonas Michel
 *
 */
public interface Command {

	static final String COMMAND_INDICATOR = "o/";
	
	static Pattern createPattern(String regex) {
		return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
	}

	String getName();

	List<String> getVariations();

	Pattern getCommandRegex();

	String getDescription();

	void onMessage(CommandEvent event);

}
