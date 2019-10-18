/*
 * Copyright GEMTEC GmbH 2019
 *
 * Erstellt am: 14 Oct 2019 22:10:39
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;

/**
 * @author Jonas Michel
 *
 */
@ApplicationScoped
public class MatchCommandImpl {

	private static final String MATCH_COMMAND_IDENTIFIER = "match";

	public void onMessageEvent(@Observes MessageEvent e) {
		Message message = e.getMessage();
		Optional<String> content = message.getContent();
		if (!content.isPresent())
			return;
		String text = content.get().replaceFirst(MessageEvent.COMMAND_IDENTIFIER, "").trim();
		if (!text.startsWith(MATCH_COMMAND_IDENTIFIER))
			return;

		message.getUserMentions()
			   .map(User::getUsername)
			   .map(s -> s.chars().sum())
			   // reverse sort
			   .collectSortedList((i1,i2) -> Integer.compare(i2, i1))
			   .subscribe(sums -> {
				   if(sums.isEmpty() || sums.size() == 1)
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
