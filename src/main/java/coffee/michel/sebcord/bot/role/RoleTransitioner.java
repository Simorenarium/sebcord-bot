package coffee.michel.sebcord.bot.role;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import coffee.michel.sebcord.bot.core.JDADCClient;
import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.configuration.persistence.SebcordBot.RoleTransition;
import coffee.michel.sebcord.configuration.persistence.SebcordBot.RoleTransition.RoleAction;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

@Component
public class RoleTransitioner implements ApplicationListener<ApplicationStartedEvent>, RoleChangeListener {

	@Autowired
	private JDADCClient						client;
	@Autowired
	private ConfigurationPersistenceManager	cpm;

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		Guild guild = client.getGuild();
		if (guild == null)
			return;
		List<RoleTransition> roleTransitions = cpm.getBotConfig().getRoleTransitions();

		guild.getMembers().forEach(member -> {
			member.getRoles().forEach(role -> {
				for (RoleTransition roleTransition : roleTransitions) {
					RoleAction triggerAction = roleTransition.getTriggerAction();
					if (!triggerAction.isAdd())
						continue;

					if (triggerAction.getRoleId() == role.getIdLong())
						applyTransition(member, roleTransition);
				}
			});
		});
	}

	@Override
	public void onRoleRemove(Member member, Role role) {
		List<RoleTransition> roleTransitions = cpm.getBotConfig().getRoleTransitions();
		for (RoleTransition roleTransition : roleTransitions) {
			RoleAction ta = roleTransition.getTriggerAction();
			if (!ta.isAdd() && ta.getRoleId() == role.getIdLong())
				applyTransition(member, roleTransition);
		}
	}

	@Override
	public void onRoleAdd(Member member, Role role) {
		List<RoleTransition> roleTransitions = cpm.getBotConfig().getRoleTransitions();
		for (RoleTransition roleTransition : roleTransitions) {
			RoleAction ta = roleTransition.getTriggerAction();
			if (ta.isAdd() && ta.getRoleId() == role.getIdLong())
				applyTransition(member, roleTransition);
		}
	}

	private void applyTransition(Member member, RoleTransition roleTransition) {
		RoleAction rta = roleTransition.getActionToApply();
		Role roleToApply = member.getGuild().getRoleById(rta.getRoleId());
		if (rta.isAdd())
			member.getGuild().addRoleToMember(member, roleToApply).queue();
		else
			member.getGuild().removeRoleFromMember(member, roleToApply).queue();
	}
}
