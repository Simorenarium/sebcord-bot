/*
 * Erstellt am: 20 Oct 2019 12:21:09
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import javax.enterprise.event.ObservesAsync;

/**
 * @author Jonas Michel
 *
 */
public interface Command {

	static final String COMMAND_INDICATOR = "o/";

	String getName();

	String getCommandRegex();

	String getDescription();

	void onMessage(@ObservesAsync CommandEvent event);

}
