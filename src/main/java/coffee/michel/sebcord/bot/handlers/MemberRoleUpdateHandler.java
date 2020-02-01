/*
 *
 * Erstellt am: 23 Jan 2020 19:27:20
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.handlers;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import coffee.michel.sebcord.bot.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.bot.configuration.persistence.SebcordBot.RoleTransition.RoleAction;
import coffee.michel.sebcord.bot.core.JDADCClient;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;

/**
 * @author Jonas Michel
 *
 */
public class MemberRoleUpdateHandler {

	@Inject
	private ConfigurationPersistenceManager cpm;
	@Inject
	private JDADCClient                     client;

	public void onMemberUpdate(@Observes GuildMemberRoleAddEvent event) {
		cpm.getBotConfig()
				.getRoleTransitions()
				.stream()
				.filter(rt -> rt.getTriggerAction().isAdd())
				.filter(rt -> event.getRoles().stream().map(Role::getIdLong).anyMatch(rId -> rt.getTriggerAction().getRoleId() == rId))
				.forEach(rt -> {
					RoleAction actionToApply = rt.getActionToApply();
					Role roleToApply = event.getJDA().getRoleById(actionToApply.getRoleId());
					if (actionToApply.isAdd()) {
						client.getGuild().addRoleToMember(event.getMember().getIdLong(), roleToApply).queue();
					} else
						client.getGuild().removeRoleFromMember(event.getMember().getIdLong(), roleToApply).queue();
				});
	}

	public void onMemberUpdate(@Observes GuildMemberRoleRemoveEvent event) {
		cpm.getBotConfig()
				.getRoleTransitions()
				.stream()
				.filter(rt -> !rt.getTriggerAction().isAdd())
				.filter(rt -> event.getRoles().stream().map(Role::getIdLong).anyMatch(rId -> rt.getTriggerAction().getRoleId() == rId))
				.forEach(rt -> {
					RoleAction actionToApply = rt.getActionToApply();
					Role roleToApply = event.getJDA().getRoleById(actionToApply.getRoleId());
					if (actionToApply.isAdd()) {
						client.getGuild().addRoleToMember(event.getMember().getIdLong(), roleToApply).queue();
					} else
						client.getGuild().removeRoleFromMember(event.getMember().getIdLong(), roleToApply).queue();
				});
	}

}
