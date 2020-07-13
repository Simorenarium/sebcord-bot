/*
 *
 * Erstellt am: 22 Jan 2020 20:48:25
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;
import javax.security.auth.login.LoginException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import coffee.michel.sebcord.Application;
import coffee.michel.sebcord.Factory;
import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.configuration.persistence.DiscordApplication;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

/**
 * @author Jonas Michel
 *
 */
@Component
@Scope("singleton")
public class JDADCClient implements ApplicationListener<ApplicationStartedEvent> {

	private static JDADCClient				INSTANCE;

	@Autowired
	private ConfigurationPersistenceManager	cpm;
	@Autowired
	private JDAEventBroadcaster				eventBroadcaster;
	private JDA								jda;
	private ScheduledExecutorService		exe		= Factory.executor();

	private CountDownLatch					latch	= new CountDownLatch(1);

	public static JDADCClient getINSTANCE() {
		return INSTANCE;
	}

	public JDADCClient() {
		INSTANCE = this;
	}

	@PostConstruct
	public void init() {
		if(Application.predefToken != null) {
			cpm.getDiscordApp().setToken(Application.predefToken);
			cpm.persist(cpm.getDiscordApp(), cpm.getDiscordApp().getToken());
			cpm.getDiscordApp().setEnabled(true);
		}
		if(Application.predefClientID != null) {
			cpm.getDiscordApp().setClientId(Long.valueOf(Application.predefClientID));
			cpm.persist(cpm.getDiscordApp(), cpm.getDiscordApp().getClientId());
			cpm.getDiscordApp().setEnabled(true);
		}
		if(Application.predefUrl != null) {
			cpm.getDiscordApp().setRedirectURL(Application.predefUrl);
			cpm.persist(cpm.getDiscordApp(), cpm.getDiscordApp().getRedirectURL());
			cpm.getDiscordApp().setEnabled(true);
		}
		if(Application.predefGuildID != null) {
			cpm.getBotConfig().setHandledServerId(Long.valueOf(Application.predefGuildID));
			cpm.persist(cpm.getBotConfig(), cpm.getBotConfig().getHandledServerId());
			cpm.getDiscordApp().setEnabled(true);
		}
		if(Application.predefGuildID != null) {
			cpm.getDiscordApp().setClientSecret(Application.predefClientSecret);
			cpm.persist(cpm.getDiscordApp(), cpm.getDiscordApp().getClientSecret());
			cpm.getDiscordApp().setEnabled(true);
		}
		
		cpm.addSaveListener(() -> {
			DiscordApplication updatedDcApp = cpm.getDiscordApp();
			if (!updatedDcApp.isEnabled()) {
				jda.shutdown();
				jda.removeEventListener(eventBroadcaster);
				latch = new CountDownLatch(1);
			}

			initialize(updatedDcApp);
		});
		DiscordApplication discordApp = cpm.getDiscordApp();
		if (!discordApp.isEnabled())
			return;

		initialize(discordApp);
	}

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		// nothing here, the class just has to be initialized
	}

	private void initialize(DiscordApplication discordApp) {
		try {
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
			eventBroadcaster.setSourceJda(jda);
		} catch (LoginException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean isAdminOrDev(Member member) {
		boolean isDev = cpm.getBotConfig().getDeveloperIds().contains(member.getIdLong());
		if (isDev)
			return true;
		var isAdmin = member.getPermissions().contains(Permission.ADMINISTRATOR);
		if (isAdmin)
			return true;
		return false;
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

	public Optional<Member> getMemberByName(String username) {
		return Optional.ofNullable(getGuild().getMembersByNickname(username, true))
				.filter(members -> !members.isEmpty()).map(ls -> ls.get(0));
	}

	public Optional<User> getUserById(Long developerUserId) {
		return Optional.ofNullable(jda.getUserById(developerUserId));
	}

	public Optional<Member> getMemberById(String userId) {
		return Optional.ofNullable(getGuild().getMemberById(userId));
	}

	public boolean isConfigured() {
		return latch.getCount() == 0;
	}

	public JDA getJda() {
		try {
			latch.await();
		} catch (InterruptedException e) {
		}
		return jda;
	}

}
