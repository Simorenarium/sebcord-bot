/*
 * Copyright GEMTEC GmbH 2019
 *
 * Erstellt am: 20 Oct 2019 12:22:56
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.util.Optional;

import discord4j.core.object.entity.Message;

/**
 * @author Jonas Michel
 *
 */
public abstract class AbstractCommand implements Command {

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
		if (text.startsWith(getCommand()))
			text = text.replaceFirst(getCommand(), "").trim();
		else
			return;
		handleCommand(event, text);
	}

	protected abstract void handleCommand(CommandEvent event, String text);

}
