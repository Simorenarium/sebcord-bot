package coffee.michel.sebcord.bot.role;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import coffee.michel.sebcord.bot.core.JDADCClient;
import coffee.michel.sebcord.bot.event.MemberJoinListener;
import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

@Component
public class InitialRoleApplicant implements MemberJoinListener {

	@Autowired
	private JDADCClient						client;
	@Autowired
	private ConfigurationPersistenceManager	cpm;

	@Override
	public void memberJoined(Member member) {
		List<Long> initialRoles = cpm.getBotConfig().getInitialRoles();
		Set<Role> rolesToApply = client.getGuild().getRoles().stream().filter(r -> initialRoles.contains(r.getIdLong()))
				.collect(Collectors.toSet());
		for (Role role : rolesToApply) {
			member.getGuild().addRoleToMember(member, role).queue();
		}
	}

}
