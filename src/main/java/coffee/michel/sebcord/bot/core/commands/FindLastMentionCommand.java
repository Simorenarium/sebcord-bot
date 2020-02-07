/*
 * Erstellt am: 19 Oct 2019 14:51:41
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

/**
 * @author Jonas Michel
 *
 */
@Component
public class FindLastMentionCommand implements Command {

	private static final Pattern pattern = Pattern.compile("lastMention");

	@Override
	public String getName() {
		return "Erwähnungs-Suche";
	}

	@Override
	public List<String> getVariations() {
		return Arrays.asList("lastMention");
	}

	@Override
	public Pattern getCommandRegex() {
		return pattern;
	}

	@Override
	public String getDescription() {
		return "Sucht nach der letzten Erwähnung im aktuellen Channel.";
	}

	@Override
	public void onMessage(CommandEvent event) {
		var message = event.getMessage();
		var channel = message.getChannel();
		if (channel == null)
			return;

		// implement some way to skip mentions
		channel.sendTyping().complete();
		List<String> mentionedUsers = message.getMentionedUsers().stream().map(User::getId)
				.collect(Collectors.toCollection(ArrayList::new));
		if (mentionedUsers.isEmpty())
			mentionedUsers = new ArrayList<String>(Arrays.asList(message.getAuthor().getId()));

		var channelHistory = channel.getIterableHistory();
		for (var prevMessage : channelHistory) {
			if (prevMessage.getTimeCreated().isAfter(message.getTimeCreated()))
				continue;

			if (!mentionedUsers
					.removeIf(uId -> prevMessage.getMentionedUsers().stream().anyMatch(u -> u.getId().equals(uId))))
				continue;

			channel.sendMessage(new EmbedBuilder()
					.setTitle("Nachricht von: " + prevMessage.getMember().getEffectiveName(),
							prevMessage.getJumpUrl())
					.setDescription(prevMessage.getContentDisplay())
					.setTimestamp(prevMessage.getTimeCreated())
					.build())
					.queue();

			if (mentionedUsers.isEmpty())
				return;
			else
				channel.sendTyping().complete();
		}
	}

}
