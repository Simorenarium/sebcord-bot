package coffee.michel.sebcord.bot.event.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import coffee.michel.sebcord.bot.event.MemberJoinListener;
import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

@Component
public class UserJoinHandler implements MemberJoinListener {

	@Autowired
	private ConfigurationPersistenceManager cpm;

	@Override
	public void memberJoined(Member member) {
		User user = member.getUser();
		if (user == null)
			return;
		String welcomeMessage = cpm.getBotConfig().getWelcomeMessage();
		if (welcomeMessage == null || welcomeMessage.isBlank())
			return;
		user.openPrivateChannel().queue(channel -> {
			channel.sendMessage(welcomeMessage).queue();
		});
	}

}
