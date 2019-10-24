/*
 * Copyright GEMTEC GmbH 2019
 *
 * Erstellt am: 19 Oct 2019 14:51:41
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.event.ObservesAsync;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Flux;

/**
 * @author Jonas Michel
 *
 */
public class FindLastMentionCommand extends AbstractCommand {

	@Override
	public String getCommand() {
		return "lastMention";
	}

	@Override
	public String getDescription() {
		return "Sucht nach der letzten Erwähnung im aktuellen Channel.";
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

		Set<Snowflake> userMentionIds = message.getUserMentionIds();
		if (userMentionIds.isEmpty()) {
			Optional<Snowflake> optUserId = message.getAuthor().map(User::getId);
			if (optUserId.isEmpty()) {
				channel.createMessage("Nix gefunden").block();
				return;
			}
			userMentionIds = new HashSet<>(Arrays.asList(optUserId.get()));
		}

		for (Snowflake users : userMentionIds) {
			Message searchingMessage = channel.createMessage("Suche...").block();

			Flux<Message> foundMessage = channel.getMessagesBefore(message.getId()).skipUntil(msg -> msg.getUserMentionIds().contains(users));
			Message msg = foundMessage.blockFirst();
			User originalAuthor = msg.getAuthor().get();
			searchingMessage.edit(spec -> spec.setContent("Gefunden: \n" + originalAuthor.getMention() + ": " + msg.getContent().get())).block();
		}
	}

}
