/*
 * Erstellt am: 19 Oct 2019 14:51:41
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

import coffee.michel.sebcord.bot.core.DCClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;

/**
 * @author Jonas Michel
 *
 */
public class PruneCommand extends AbstractCommand {

	private Map<Long, Instant> lastPrunePerChannel = new ConcurrentHashMap<>();

	@Inject
	private DCClient client;

	@Override
	public String getName() {
		return "Prune";
	}

	@Override
	public String getCommandRegex() {
		return "prune";
	}

	@Override
	public String getDescription() {
		return "Löscht eine bestimmte Menge an Nachrichten (max. 100). Geht nur alle 30 Sekunden.";
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

		if (!client.isAdminOrDev(message)) {
			channel.createMessage("Du kannst dat nich.").subscribe();
			return;
		}

		long channelId = channel.getId().asLong();
		Instant lastPrumeTimestamp = lastPrunePerChannel.get(channelId);
		Instant _30SecondsBefore = message.getTimestamp().minusSeconds(30);
		if (lastPrumeTimestamp != null && lastPrumeTimestamp.isBefore(_30SecondsBefore)) {
			channel.createMessage("Das geht erst wieder in " + ((Instant.now().toEpochMilli() - _30SecondsBefore.toEpochMilli()) / 1000) + " Sekunden.").subscribe();
			return;
		}

		Matcher matcher = Pattern.compile("\\d*").matcher(text);
		if (!matcher.find()) {
			channel.createMessage("Du musst schon eine Anzahl festlegen.").subscribe();
			return;
		}
		Integer messagesToDelete = Integer.valueOf(matcher.group());
		if (messagesToDelete > 100) {
			channel.createMessage("Dat is zu viel.").subscribe();
			return;
		}

		channel.getMessagesBefore(message.getId()).take(messagesToDelete).subscribe(msg -> msg.delete().block());
		message.delete().block();
		channel.createMessage(messagesToDelete + " Nachrichten wurden gelöscht.").subscribe();
	}

}
