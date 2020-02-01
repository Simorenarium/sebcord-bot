/*
 *
 * Erstellt am: 23 Jan 2020 19:48:38
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.handlers;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import coffee.michel.sebcord.bot.configuration.persistence.ConfigurationPersistenceManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

/**
 * @author Jonas Michel
 *
 */
public class InitialRoleHandler {

	@Inject
	private ConfigurationPersistenceManager cpm;

	public void onJoinEvent(@Observes GuildMemberJoinEvent event) {
		Guild guild = event.getGuild();

		cpm.getBotConfig()
				.getInitialRoles()
				.stream()
				.map(guild::getRoleById)
				.forEach(initialRole -> {
					guild.addRoleToMember(event.getUser().getIdLong(), initialRole).queue();
				});
	}

}
