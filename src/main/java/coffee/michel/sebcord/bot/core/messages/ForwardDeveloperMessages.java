/*
 *
 * Erstellt am: 23 Jan 2020 19:20:39
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.messages;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import coffee.michel.sebcord.bot.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.bot.core.JDADCClient;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * @author Jonas Michel
 *
 */
public class ForwardDeveloperMessages {

	@Inject
	private ConfigurationPersistenceManager cpm;
	@Inject
	private JDADCClient                     client;

	public void onMessageReceived(@Observes MessageEvent event) {
		Message message = event.getMessage();
		User author = message.getAuthor();
		long authorId = author.getIdLong();

		if (cpm.getBotConfig().getDeveloperIds().contains(authorId)) {
			String string = message.getContentDisplay();
			String[] msgParts = string.split("\\|");
			Guild guild = client.getGuild();
			String expectedChannelName = msgParts[0];

			guild.getChannels().stream().filter(ch -> ch.getType() == ChannelType.TEXT).map(TextChannel.class::cast).forEach(channel -> {
				String name = channel.getName();
				if (name.equals(expectedChannelName))
					channel.sendMessage(msgParts[1]).queue();
			});
		}
	}

}
