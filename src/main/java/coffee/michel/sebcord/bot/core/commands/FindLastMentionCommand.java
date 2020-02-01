/*
 * Erstellt am: 19 Oct 2019 14:51:41
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.event.Observes;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

/**
 * @author Jonas Michel
 *
 */
public class FindLastMentionCommand extends AbstractCommand {

	@Override
	public String getName() {
		return "Erwähnungs-Suche";
	}

	@Override
	public String getCommandRegex() {
		return "lastMention";
	}

	@Override
	public String getDescription() {
		return "Sucht nach der letzten Erwähnung im aktuellen Channel.";
	}

	@Override
	public void onMessage(@Observes CommandEvent event) {
		super.onMessage(event);
	}

	@Override
	protected void handleCommand(CommandEvent event, String text) {
		var message = event.getMessage();
		var channel = message.getChannel();
		if (channel == null)
			return;

		// implement some way to skip mentions
		channel.sendTyping().complete();
		List<String> mentionedUsers = message.getMentionedUsers().stream().map(User::getId).collect(Collectors.toCollection(ArrayList::new));
		if (mentionedUsers.isEmpty())
			mentionedUsers = new ArrayList<String>(Arrays.asList(message.getAuthor().getId()));

		var channelHistory = channel.getIterableHistory();
		for (var prevMessage : channelHistory) {
			if (prevMessage.getTimeCreated().isAfter(message.getTimeCreated()))
				continue;

			if (!mentionedUsers.removeIf(uId -> prevMessage.getMentionedUsers().stream().anyMatch(u -> u.getId().equals(uId))))
				continue;

			channel.sendMessage(prevMessage.getJumpUrl())
					.embed(new EmbedBuilder().setAuthor(prevMessage.getMember().getEffectiveName())
							.setTitle("Nachricht vom: " + prevMessage.getTimeCreated().toString(), prevMessage.getJumpUrl())
							.addField("Nachricht", prevMessage.getContentDisplay(), true)
							.build())
					.queue();

			if (mentionedUsers.isEmpty())
				return;
			else
				channel.sendTyping().complete();
		}
	}

}
