/*
 * Erstellt am: 19 Oct 2019 14:51:41
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import coffee.michel.sebcord.bot.core.DCClient;
import coffee.michel.sebcord.bot.core.messages.MessageEvent;
import coffee.michel.sebcord.bot.persistence.PersistenceManager;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Snowflake;

/**
 * @author Jonas Michel
 *
 */
@ApplicationScoped
public class BlacklistCommands {

	private static final Predicate<String> wordMatcher = Pattern.compile("[a-zA-z]{1,}").asMatchPredicate();

	@ApplicationScoped
	static class BlacklistAdd extends AbstractCommand {

		@Inject
		private PersistenceManager persist;
		@Inject
		private DCClient client;

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
		public void onMessage(@ObservesAsync CommandEvent event) {
			super.onMessage(event);
		}

		@Override
		protected void handleCommand(CommandEvent event, String text) {
			Message message = event.getMessage();
			message.getChannel().subscribe(channel -> {
				if (!client.isAdminOrDev(message)) {
					channel.createMessage("Das kannst du nicht.").subscribe();
					return;
				}

				if (!wordMatcher.test(text)) {
					channel.createMessage("Dat Wort darf nur Buchstaben enthalten und nich leer sein.").subscribe();
					return;
				}
				persist.addWordToBlacklist(text);
				message.addReaction(ReactionEmoji.unicode("✅")).subscribe();
			});
		}

	}

	@ApplicationScoped
	static class BlacklistRemove extends AbstractCommand {

		@Inject
		private PersistenceManager persist;
		@Inject
		private DCClient client;

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
		public void onMessage(@ObservesAsync CommandEvent event) {
			super.onMessage(event);
		}

		@Override
		protected void handleCommand(CommandEvent event, String text) {
			Message message = event.getMessage();
			message.getChannel().subscribe(channel -> {
				if (!client.isAdminOrDev(message)) {
					channel.createMessage("Das kannst du nicht.").subscribe();
					return;
				}

				boolean found = persist.removeWordFromBlacklist(text);
				if (found)
					message.addReaction(ReactionEmoji.unicode("✅")).subscribe();
				else
					channel.createMessage("Hab ich nicht in der Blacklist gefunden.").subscribe();
			});
		}

	}

	@ApplicationScoped
	static class BlacklistShow extends AbstractCommand {

		@Inject
		private PersistenceManager persist;
		@Inject
		private DCClient client;

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
		public void onMessage(@ObservesAsync CommandEvent event) {
			super.onMessage(event);
		}

		@Override
		protected void handleCommand(CommandEvent event, String text) {
			Message message = event.getMessage();
			message.getChannel().subscribe(channel -> {
				if (!client.isAdminOrDev(message)) {
					channel.createMessage("Das kannst du nicht.").subscribe();
					return;
				}
				channel.createMessage(persist.getBlacklistedWords().stream().collect(Collectors.joining(", "))).subscribe();
			});
		}

	}

	@ApplicationScoped
	static class BlacklistMessageListener {

		@Inject
		private PersistenceManager persist;
		@Inject
		@ConfigProperty(name = "discord.bot.mute-role")
		private Long muteRoleId;
		@Resource
		private ManagedScheduledExecutorService exe;

		public void onMessage(@ObservesAsync MessageEvent event) {
			Message message = event.getMessage();
			message.getContent().ifPresent(content -> {
				Optional<User> optAuthor = message.getAuthor();
				String blockedWord = persist.getBlockedWord(content);
				if (blockedWord == null || optAuthor.isEmpty())
					return;
				User author = optAuthor.get();
				if (author.isBot())
					return;
				message.delete("'" + blockedWord + "' ist geblacklisted.").subscribe();

				author.getPrivateChannel().subscribe(pChannel -> {
					pChannel.createMessage("Das Wort '" + blockedWord + "' ist auf der Blacklist.\nDeine Nachricht wurde entfernt.\nHier ist die entfernte Nachricht, falls du sie anpassen willst:\n" + content).subscribe();
				});

				message.getAuthorAsMember().subscribe(member -> {
					exe.schedule(() -> {
						member.removeRole(Snowflake.of(muteRoleId)).subscribe();
					}, 15, TimeUnit.SECONDS);
					member.addRole(Snowflake.of(muteRoleId)).subscribe();
				});

			});
		}

	}

}
