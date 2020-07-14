/*
 * Erstellt am: 14 Oct 2019 22:10:39
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * @author Jonas Michel
 *
 */
@Component
public class MatchCommandImpl implements Command {

	private static final Pattern pattern = Command.createPattern("match");

	@Override
	public String getName() {
		return "Pseudo-Parship";
	}

	@Override
	public List<String> getVariations() {
		return Arrays.asList("match");
	}

	@Override
	public Pattern getCommandRegex() {
		return pattern;
	}

	@Override
	public String getDescription() {
		return "Gibt euch die Info wie sehr ihr zusammenpasst. Die erwähnten User werden gematched.\n\tPolygamie ist eine Option!";
	}

	@Override
	public void onMessage(CommandEvent event) {
		Message message = event.getMessage();
		MessageChannel channel = message.getChannel();
		channel.sendTyping().complete();
		List<Integer> effectiveNameSums = message.getMentionedMembers().stream()
				.map(m -> {
					OffsetDateTime timeJoined = m.getTimeJoined();
					return (m.getActivities().toString() + timeJoined.toString() + m.getNickname() + m.getId()).chars()
							.sum();
				})
				.sorted((i1, i2) -> Integer.compare(i2, i1))
				.collect(Collectors.toList());

		if (effectiveNameSums.isEmpty() || effectiveNameSums.size() == 1)
			sendForeverAllone(message);
		else
			sendMatchResult(message, calcMatchPercentage(effectiveNameSums));
	}

	private void sendForeverAllone(Message message) {
		sendMessage(message.getTextChannel(), "Forever Alone!");
	}

	private double calcMatchPercentage(List<Integer> sums) {
		Integer max = sums.get(0);
		Integer ongoingResult = sums.get(1);
		for (int i = 2; i < sums.size(); i++)
			ongoingResult |= sums.get(i);
		return (double) ongoingResult / (double) max;
	}

	private void sendMatchResult(Message message, double calcMatchPercentage) {
		sendMessage(message.getTextChannel(),
				"Ihr matched zu " + ((long) Math.floor(calcMatchPercentage * 100)) + "% !!!");
	}

	private void sendMessage(TextChannel channel, String message) {
		channel.sendMessage(new EmbedBuilder().setTitle("❤️❤️❤️ Sebcord Partner Börse ❤️❤️❤️")
				.setDescription(message)
				.build())
				.complete();
	}

}
