/*
 * Erstellt am: 19 Oct 2019 14:51:41
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import coffee.michel.sebcord.Factory;
import coffee.michel.sebcord.bot.core.JDADCClient;
import coffee.michel.sebcord.bot.core.messages.MessageEvent;
import coffee.michel.sebcord.bot.core.messages.MessageListener;
import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.persistence.PersistenceManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

/**
 * @author Jonas Michel
 *
 */
@Component
public class BlacklistCommand implements Command, MessageListener {

	private static final Predicate<String>	wordMatcher	= Pattern.compile("[a-zA-z]{1,}").asMatchPredicate();
	private static final Pattern			pattern		= Command.createPattern("(?:blacklist\\s)(add|remove|show)");

	@Autowired
	private ConfigurationPersistenceManager	cpm;
	private ScheduledExecutorService		exe			= Factory.executor();
	@Autowired
	private PersistenceManager				persist;
	@Autowired
	private JDADCClient						client;

	@Override
	public String getName() {
		return "Blacklist";
	}

	@Override
	public List<String> getVariations() {
		return Arrays.asList("blacklist add", "blacklist remove", "blacklist show");
	}

	@Override
	public Pattern getCommandRegex() {
		return pattern;
	}

	@Override
	public String getDescription() {
		// TODO
		return "Verwaltet die Blacklist.";
	}

	@Override
	public void onMessage(CommandEvent event) {
		Message message = event.getMessage();
		String text = event.getText();
		List<String> matchedGroups = event.getMatchedGroups();
		MessageChannel channel = message.getChannel();
		if (!(matchedGroups.contains("show") || client.isAdminOrDev(message))) {
			channel.sendMessage("Das kannst du nicht!").queue();
			return;
		}

		if (matchedGroups.contains("add")) {
			if (!wordMatcher.test(text)) {
				channel.sendMessage("Dat Wort darf nur Buchstaben enthalten und nich leer sein.").queue();
				return;
			}
			persist.addWordToBlacklist(text);
			message.addReaction("✅").queue();
		} else if (matchedGroups.contains("remove")) {
			boolean found = persist.removeWordFromBlacklist(text);
			if (found)
				message.addReaction("✅").queue();
			else
				channel.sendMessage("Hab ich nicht in der Blacklist gefunden.").queue();
		} else if (matchedGroups.contains("show")) {
			String collect = persist.getBlacklistedWords().stream().collect(Collectors.joining(", "));
			if (!collect.isEmpty())
				channel.sendMessage(collect).queue();
			else
				channel.sendMessage("Is leer ¯\\_(ツ)_/¯").queue();
		}
	}

	@Override
	public void onMessage(MessageEvent event) {
		Message message = event.getMessage();
		String content = message.getContentStripped();
		User author = message.getAuthor();
		String blockedWord = persist.getBlockedWord(content);
		if (blockedWord == null || blockedWord.isEmpty() || author.isBot() || client.isAdminOrDev(message))
			return;
		message.delete().complete();

		author.openPrivateChannel().queue(pChannel -> {
			pChannel.sendMessage("Das Wort '" + blockedWord
					+ "' ist auf der Blacklist.\nDeine Nachricht wurde entfernt.\nHier ist die entfernte Nachricht, falls du sie anpassen willst:\n"
					+ content).queue();
		});

		var muteRoleId = cpm.getBotConfig().getMuteRoleId();

		Guild guild = message.getGuild();
		Role role = guild.getRoleById(muteRoleId);
		guild.addRoleToMember(author.getIdLong(), role);
		exe.schedule(() -> {
			guild.removeRoleFromMember(author.getIdLong(), role);
		}, 15, TimeUnit.SECONDS);
	}

}
