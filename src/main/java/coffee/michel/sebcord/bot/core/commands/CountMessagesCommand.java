/*
 * Copyright GEMTEC GmbH 2019
 *
 * Erstellt am: 19 Oct 2019 14:51:41
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import javax.enterprise.event.ObservesAsync;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;

/**
 * @author Jonas Michel
 *
 */
public class CountMessagesCommand extends AbstractCommand {

	@Override
	public String getName() {
		return "Count";
	}

	@Override
	public String getCommandRegex() {
		return "count";
	}

	@Override
	public String getDescription() {
		return "Zählt die bissher geschrieben Nachrichten im aktuellen Channel.\n\t**Das kann durchaus eine Weile dauern.**";
	}

	@Override
	public void onMessage(@ObservesAsync CommandEvent event) {
		super.onMessage(event);
	}

	@Override
	protected void handleCommand(CommandEvent event, String text) {
		Message message = event.getMessage();
		MessageChannel channel = message.getChannel().block();
		if (channel == null)
			return;

		int messageCount = channel.getMessagesBefore(message.getId()).collectList().block().size();
		channel.createMessage("Es wurden bissher " + messageCount + " Nachrichten geschrieben").subscribe();
	}

}
