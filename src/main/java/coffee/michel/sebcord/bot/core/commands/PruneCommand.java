/*
 * Erstellt am: 19 Oct 2019 14:51:41
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import coffee.michel.sebcord.bot.core.JDADCClient;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

/**
 * @author Jonas Michel
 *
 */
@Component
public class PruneCommand implements Command {

	private static final Pattern	pattern				= Pattern.compile("prune|clear|clean");
	private Map<Long, Instant>		lastPrunePerChannel	= new ConcurrentHashMap<>();

	@Autowired
	private JDADCClient				client;

	@Override
	public String getName() {
		return "Prune";
	}

	@Override
	public List<String> getVariations() {
		return Arrays.asList("prune", "clear", "clean");
	}

	@Override
	public Pattern getCommandRegex() {
		return pattern;
	}

	@Override
	public String getDescription() {
		return "Löscht eine bestimmte Menge an Nachrichten (max. 100). Geht nur alle 30 Sekunden.";
	}

	@Override
	public void onMessage(CommandEvent event) {
		Message message = event.getMessage();
		MessageChannel channel = message.getChannel();
		if (channel == null)
			return;

		if (!client.isAdminOrDev(message)) {
			channel.sendMessage("Du kannst dat nich.").queue();
			return;
		}

		long channelId = channel.getIdLong();
		Instant lastPrumeTimestamp = lastPrunePerChannel.get(channelId);
		Instant _30SecondsBefore = message.getTimeCreated().minusSeconds(30).toInstant();
		if (lastPrumeTimestamp != null && lastPrumeTimestamp.isBefore(_30SecondsBefore)) {
			channel.sendMessage("Das geht erst wieder in "
					+ ((Instant.now().toEpochMilli() - _30SecondsBefore.toEpochMilli()) / 1000) + " Sekunden.").queue();
			return;
		}

		Matcher matcher = Pattern.compile("\\d*").matcher(event.getText());
		if (!matcher.find()) {
			channel.sendMessage("Du musst schon eine Anzahl festlegen.").queue();
			return;
		}
		Integer messagesToDelete = Integer.valueOf(matcher.group());
		if (messagesToDelete > 100) {
			channel.sendMessage("Dat is zu viel.").queue();
			return;
		}

		channel.getHistoryBefore(message, messagesToDelete)
				.complete()
				.getRetrievedHistory()
				.forEach(msg -> msg.delete().queue());
		message.delete().queue();
		channel.sendMessage(messagesToDelete + " Nachrichten wurden gelöscht.").queue();
	}

}
