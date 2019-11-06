/*
 *
 * Erstellt am: 20 Oct 2019 12:22:56
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import discord4j.core.object.entity.Message;

/**
 * @author Jonas Michel
 *
 */
public abstract class AbstractCommand implements Command {

	private static final Logger logger = LoggerFactory.getLogger(AbstractCommand.class);

	@Override
	public void onMessage(CommandEvent event) {
		Message message = event.getMessage();
		Optional<String> content = message.getContent();
		if (!content.isPresent())
			return;

		String text = content.get().trim();
		if (text.startsWith(COMMAND_INDICATOR))
			text = text.replaceFirst(Command.COMMAND_INDICATOR, "").trim();
		else
			return;
		String commandRegex = getCommandRegex();
		Pattern pattern = Pattern.compile(commandRegex);
		Matcher matcher = pattern.matcher(text);
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
