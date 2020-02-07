/*
 *
 * Erstellt am: 23 Jan 2020 19:20:39
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.messages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import coffee.michel.sebcord.bot.core.JDADCClient;
import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * @author Jonas Michel
 *
 */
@Component
public class ForwardDeveloperMessages implements MessageListener {

	@Autowired
	private ConfigurationPersistenceManager	cpm;
	@Autowired
	private JDADCClient						client;

	@Override
	public void onMessage(MessageEvent event) {
		Message message = event.getMessage();
		User author = message.getAuthor();
		long authorId = author.getIdLong();

		if (cpm.getBotConfig().getDeveloperIds().contains(authorId)) {
			String string = message.getContentDisplay();
			String[] msgParts = string.split("\\|");
			Guild guild = client.getGuild();
			String expectedChannelName = msgParts[0];

			guild.getChannels().stream().filter(ch -> ch.getType() == ChannelType.TEXT).map(TextChannel.class::cast)
					.forEach(channel -> {
						String name = channel.getName();
						if (name.equals(expectedChannelName))
							channel.sendMessage(msgParts[1]).queue();
					});
		}
	}

}
