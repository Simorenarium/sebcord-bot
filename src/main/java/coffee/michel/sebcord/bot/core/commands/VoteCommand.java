package coffee.michel.sebcord.bot.core.commands;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import coffee.michel.sebcord.bot.event.ReactionListener;
import coffee.michel.sebcord.persistence.Vote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;

//@Component
public class VoteCommand implements Command, ReactionListener {

	private static final Pattern	TITLE_MATCHER	= Pattern.compile("title\\((.*)\\)");
	private static final Pattern	TIMEOUT_MATCHER	= Pattern.compile("timeout\\((\\d+[a-zA-Z])\\)");

	private static final Pattern	pattern			= Command.createPattern("(vote|poll)");

	@Override
	public String getName() {
		return "Vote";
	}

	@Override
	public List<String> getVariations() {
		return Arrays.asList("vote", "poll");
	}

	@Override
	public Pattern getCommandRegex() {
		return pattern;
	}

	@Override
	public String getDescription() {
		return "Startet eine Vote, hier mal ein Beispiel:";
	}

	/*
//@formatter:off


o/vote 
title(Es gibt jetzt Votes!)
timeout(10m)
description:(Ich habe jetzt einen Vote-Command implementiert.
Wie findet ihr den?)
options:(
* Super (<:sebHi:631545093454823425>)
* Na endlich (<:sebReifen:631545094410993684>)
* ¯\\_(?)_/¯ (🤷‍♂️)
)

//@formatter:on
 * 
 */

	@Override
	public void onMessage(CommandEvent event) {
		Vote vote = parse(event.getMessage());
	}

	@Override
	public void handle(MessageReactionAddEvent event) {
		// TODO Auto-generated method stub
	}

	@Override
	public void handle(MessageReactionRemoveEvent event) {
		// TODO Auto-generated method stub
	}

	private Vote parse(Message message) {
		Vote vote = new Vote();
		String contentRaw = message.getContentRaw();
		Matcher titleMatcher = TITLE_MATCHER.matcher(contentRaw);
		Matcher timeoutMatcher = TIMEOUT_MATCHER.matcher(contentRaw);

		String description = parseDescription(contentRaw);
		String optionText = parseOptionText(contentRaw);

		return null;
	}

	private String parseDescription(String contentRaw) {
		int descIndex = contentRaw.indexOf("description");
		contentRaw = contentRaw.substring(descIndex);
		contentRaw = contentRaw.substring(contentRaw.indexOf('('));
		contentRaw = contentRaw.substring(0, contentRaw.indexOf(')'));
		return contentRaw;
	}

	private String parseOptionText(String contentRaw) {
		int optsIndex = contentRaw.indexOf("options");
		contentRaw = contentRaw.substring(optsIndex);
		contentRaw = contentRaw.substring(contentRaw.indexOf('('));
		contentRaw = contentRaw.substring(0, contentRaw.indexOf(')'));
		return contentRaw;
	}
}
