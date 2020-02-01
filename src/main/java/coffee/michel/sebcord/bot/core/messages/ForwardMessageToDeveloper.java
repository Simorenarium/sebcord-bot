/*
 *
 * Erstellt am: 23 Jan 2020 18:44:53
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.messages;

import java.io.File;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import coffee.michel.sebcord.bot.configuration.persistence.ConfigurationPersistenceManager;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * @author Jonas Michel
 *
 */
public class ForwardMessageToDeveloper {

	@Inject
	private ConfigurationPersistenceManager cpm;

	public void onMessageReceived(@Observes MessageEvent event) {
		List<Long> developerIds = cpm.getBotConfig().getDeveloperIds();

		Message message = event.getMessage();
		if (message.isFromType(ChannelType.PRIVATE)) {
			final long userId = message.getAuthor().getIdLong();
			if (developerIds.contains(userId))
				return;
			forwardToDeveloper(developerIds, message);
		} else if (message.getMentionedUsers().stream().map(User::getIdLong).anyMatch(id -> cpm.getDiscordApp().getClientId() == id)) {
			forwardToDeveloper(developerIds, message);
		}
	}

	private void forwardToDeveloper(List<Long> devIds, Message message) {
		devIds.forEach(developerUserId -> {
			PrivateChannel pChannel = message.getJDA().getPrivateChannelById(developerUserId);
			if (pChannel == null)
				return;

			var msgAction = pChannel.sendMessage(message.getAuthor().getName() + ": " + message.getContentDisplay());
			message.getAttachments().forEach(attc -> {
				try {
					File file = attc.downloadToFile().get();
					msgAction.addFile(file);

				} catch (Exception e) {
				}
			});
		});
	}
}
