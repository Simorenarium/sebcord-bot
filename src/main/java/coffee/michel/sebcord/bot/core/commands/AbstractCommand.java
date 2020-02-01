/*
 *
 * Erstellt am: 20 Oct 2019 12:22:56
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Message;

/**
 * @author Jonas Michel
 *
 */
public abstract class AbstractCommand implements Command {

	private static final Logger logger = LoggerFactory.getLogger(AbstractCommand.class);

	@Override
	public void onMessage(CommandEvent event) {
		Message message = event.getMessage();

		String text = message.getContentDisplay().trim();
		text = text.replace(Command.COMMAND_INDICATOR, "").trim();
		String commandRegex = getCommandRegex();
		Pattern pattern = Pattern.compile(commandRegex);
		Matcher matcher = pattern.matcher(text.substring(0, text.indexOf(" ")).trim());
		if (matcher.find())
			text = text.replaceFirst(matcher.group(), "").trim();
		else
			return;
		try {
			handleCommand(event, text);
		} catch (Throwable e) {
			logger.error("onMessage(): Error on Executing a Command: ", e);
		}
	}

	protected abstract void handleCommand(CommandEvent event, String text);

}
