/*
 *
 * Erstellt am: 22 Jan 2020 21:13:46
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core;

import net.dv8tion.jda.api.events.GenericEvent;

/**
 * @author Jonas Michel
 *
 */
public interface JDAEventFilter {

	boolean allow(GenericEvent e);

}
