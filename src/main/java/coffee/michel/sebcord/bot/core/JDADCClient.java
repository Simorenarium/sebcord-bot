/*
 *
 * Erstellt am: 22 Jan 2020 20:48:25
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.security.auth.login.LoginException;

import coffee.michel.sebcord.bot.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.bot.configuration.persistence.DiscordApplication;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

/**
 * @author Jonas Michel
 *
 */
@ApplicationScoped
public class JDADCClient {

	@Inject
	private ConfigurationPersistenceManager cpm;
	@Inject
	private JDAEventBroadcaster             eventBroadcaster;
	private JDA                             jda;
	@Inject
	private ScheduledExecutorService        exe;

	private CountDownLatch latch = new CountDownLatch(1);

	@SuppressWarnings("unused")
	public void init(@Observes @Initialized(ApplicationScoped.class) Object unused) throws LoginException, InterruptedException {
		DiscordApplication discordApp = cpm.getDiscordApp();

		jda = new JDABuilder(discordApp.getToken())
				.addEventListeners(eventBroadcaster)
				.setActivity(Activity.of(ActivityType.DEFAULT, "Bin am starten"))
				.setBulkDeleteSplittingEnabled(false)
				.setAutoReconnect(true)
				.setCallbackPool(exe)
				.setGatewayPool(exe)
				.build();

		jda.awaitReady();
		latch.countDown();

		jda.getPresence().setActivity(Activity.of(ActivityType.DEFAULT, "Big Sister is watching you"));
	}

	public boolean isAdminOrDev(Message message) {
		var author = message.getAuthor();
		Guild guild = jda.getGuildById(cpm.getBotConfig().getHandledServerId());
		if (guild == null)
			return false;

		var isAuthorizedDev = cpm.getBotConfig().getDeveloperIds().contains(author.getIdLong());
		if (isAuthorizedDev)
			return true;

		var member = guild.getMember(author);
		var isAdmin = member.getPermissions().contains(Permission.ADMINISTRATOR);
		if (isAdmin)
			return true;
		return false;
	}

	public Guild getGuild() {
		return getJda().getGuildById(cpm.getBotConfig().getHandledServerId());
	}

	public Optional<User> getUserById(Long developerUserId) {
		return Optional.ofNullable(jda.getUserById(developerUserId));
	}

	public JDA getJda() {
		try {
			latch.await();
		} catch (InterruptedException e) {
		}
		return jda;
	}

}
