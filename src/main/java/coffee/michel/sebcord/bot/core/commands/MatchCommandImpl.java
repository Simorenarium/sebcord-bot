/*
 * Erstellt am: 14 Oct 2019 22:10:39
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.util.List;

import javax.enterprise.event.ObservesAsync;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;

/**
 * @author Jonas Michel
 *
 */
public class MatchCommandImpl extends AbstractCommand {

	@Override
	public String getName() {
		return "Pseudo-Parship";
	}

	@Override
	public String getCommandRegex() {
		return "match";
	}

	@Override
	public String getDescription() {
		return "Gibt euch die Info wie sehr ihr zusammenpasst. Die erwähnten User werden gematched.\n\tPolygamie ist eine Option!";
	}

	@Override
	public void onMessage(@ObservesAsync CommandEvent event) {
		super.onMessage(event);
	}

	@Override
	protected void handleCommand(CommandEvent event, String text) {
		Message message = event.getMessage();
		message.getUserMentions().map(User::getUsername).map(s -> s.chars().sum())
			// reverse sort
			.collectSortedList((i1, i2) -> Integer.compare(i2, i1)).subscribe(sums -> {
				if (sums.isEmpty() || sums.size() == 1)
					sendForeverAllone(message);
				else
					sendMatchResult(message, calcMatchPercentage(sums));
			});
	}

	private void sendForeverAllone(Message message) {
		message.getChannel().subscribe(ch -> {
			ch.createMessage("Forever Alone!").subscribe();
		});
	}

	private double calcMatchPercentage(List<Integer> sums) {
		Integer max = sums.get(0);
		Integer ongoingResult = sums.get(1);
		for (int i = 2; i < sums.size(); i++)
			ongoingResult |= sums.get(i);
		return (double) ongoingResult / (double) max;
	}

	private void sendMatchResult(Message message, double calcMatchPercentage) {
		message.getChannel().subscribe(ch -> {
			ch.createMessage("Ihr matched zu " + ((long) Math.floor(calcMatchPercentage * 100)) + "% !!!").subscribe();
		});
	}

}
