package de.sebcord.discord.jda.binding;

import java.util.List;

import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sebcord.api.application.ApplicationInitEvent;
import de.sebcord.api.discord.Category;
import de.sebcord.api.discord.Discord;
import de.sebcord.api.discord.Member;
import de.sebcord.api.discord.Permission;
import de.sebcord.api.discord.Role;
import de.sebcord.api.persistence.PersistenceExchange;
import de.sebcord.api.persistence.config.DiscordConfiguration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;

public class JdaDiscordBinding implements Discord {

	private static final Logger logger = LoggerFactory.getLogger(JdaDiscordBinding.class);
	private final int identityHashCode = System.identityHashCode(this);

	@Inject
	private PersistenceExchange pe;
	private JDA jda;

	public void onInit(@ObservesAsync ApplicationInitEvent event) {
		event.initializationComenced(this);

		logger.info("#{} onInit(): Initializing connection to discord.", identityHashCode);
		logger.debug("#{} onInit(): JDA Binding will be configured.", identityHashCode);

		DiscordConfiguration discordConfiguration = pe.get(DiscordConfiguration.class);
		
		try {
			jda = new JDABuilder(discordConfiguration.token())
					.setActivity(Activity.of(ActivityType.DEFAULT, "Bin am starten"))
					.setBulkDeleteSplittingEnabled(false)
					.setAutoReconnect(true)
					.build();

			jda.awaitReady();
			jda.getPresence().setActivity(Activity.of(ActivityType.DEFAULT, "Big Sister is watching you."));
		} catch (LoginException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		event.initializationCompleted(this);
	}

	@Override
	public List<Role> getRoles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Permission> getPermissions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Member> getMembers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Category> getCategories() {
		// TODO Auto-generated method stub
		return null;
	}

}
