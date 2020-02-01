/*
 * Erstellt am: 19 Oct 2019 14:51:41
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import coffee.michel.sebcord.bot.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.bot.core.JDADCClient;
import coffee.michel.sebcord.bot.core.messages.MessageEvent;
import coffee.michel.sebcord.bot.persistence.PersistenceManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

/**
 * @author Jonas Michel
 *
 */
public class BlacklistCommands {

	private static final Predicate<String> wordMatcher = Pattern.compile("[a-zA-z]{1,}").asMatchPredicate();

	public static class BlacklistAdd extends AbstractCommand {

		@Inject
		private PersistenceManager persist;
		@Inject
		private JDADCClient        client;

		@Override
		public String getName() {
			return "Blacklist Add";
		}

		@Override
		public String getCommandRegex() {
			return "blacklist add";
		}

		@Override
		public String getDescription() {
			return "Fügt ein Wort der Blacklist hinzu.";
		}

		@Override
		public void onMessage(@Observes CommandEvent event) {
			super.onMessage(event);
		}

		@Override
		protected void handleCommand(CommandEvent event, String text) {
			Message message = event.getMessage();
			MessageChannel channel = message.getChannel();
			if (!client.isAdminOrDev(message)) {
				channel.sendMessage("Das kannst du nicht!").queue();
				return;
			}

			if (!wordMatcher.test(text)) {
				channel.sendMessage("Dat Wort darf nur Buchstaben enthalten und nich leer sein.").queue();
				return;
			}
			persist.addWordToBlacklist(text);
			message.addReaction("✅").queue();
		}

	}

	public static class BlacklistRemove extends AbstractCommand {

		@Inject
		private PersistenceManager persist;
		@Inject
		private JDADCClient        client;

		@Override
		public String getName() {
			return "Blacklist Remove";
		}

		@Override
		public String getCommandRegex() {
			return "blacklist remove";
		}

		@Override
		public String getDescription() {
			return "Entfernt ein Wort aus der Blacklist.";
		}

		@Override
		public void onMessage(@Observes CommandEvent event) {
			super.onMessage(event);
		}

		@Override
		protected void handleCommand(CommandEvent event, String text) {
			Message message = event.getMessage();
			MessageChannel channel = message.getChannel();
			if (!client.isAdminOrDev(message)) {
				channel.sendMessage("Das kannst du nicht.").queue();
				return;
			}

			boolean found = persist.removeWordFromBlacklist(text);
			if (found)
				message.addReaction("✅").queue();
			else
				channel.sendMessage("Hab ich nicht in der Blacklist gefunden.").queue();
		}

	}

	public static class BlacklistShow extends AbstractCommand {

		@Inject
		private PersistenceManager persist;
		@Inject
		private JDADCClient        client;

		@Override
		public String getName() {
			return "Blacklist Show";
		}

		@Override
		public String getCommandRegex() {
			return "blacklist show";
		}

		@Override
		public String getDescription() {
			return "Zeigt die Blacklist an.";
		}

		@Override
		public void onMessage(@Observes CommandEvent event) {
			super.onMessage(event);
		}

		@Override
		protected void handleCommand(CommandEvent event, String text) {
			Message message = event.getMessage();
			MessageChannel channel = message.getChannel();
			if (!client.isAdminOrDev(message)) {
				channel.sendMessage("Das kannst du nicht.").queue();
				return;
			}
			channel.sendMessage(persist.getBlacklistedWords().stream().collect(Collectors.joining(", "))).queue();
		}

	}

	public static class BlacklistMessageListener {

		@Inject
		private ConfigurationPersistenceManager cpm;
		@Inject
		private PersistenceManager              persist;
		@Inject
		private ScheduledExecutorService        exe;

		public void onMessage(@Observes MessageEvent event) {
			Message message = event.getMessage();
			String content = message.getContentStripped();
			User author = message.getAuthor();
			String blockedWord = persist.getBlockedWord(content);
			if (blockedWord == null || author.isBot())
				return;
			message.delete().complete();

			author.openPrivateChannel().queue(pChannel -> {
				pChannel.sendMessage("Das Wort '" + blockedWord + "' ist auf der Blacklist.\nDeine Nachricht wurde entfernt.\nHier ist die entfernte Nachricht, falls du sie anpassen willst:\n" + content).queue();
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

}
