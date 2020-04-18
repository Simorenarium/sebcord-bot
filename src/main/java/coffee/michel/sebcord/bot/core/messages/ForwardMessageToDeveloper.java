/*
 *
 * Erstellt am: 23 Jan 2020 18:44:53
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.messages;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * @author Jonas Michel
 *
 */
@Component
public class ForwardMessageToDeveloper implements PrivateMessageListener {

	@Autowired
	private ConfigurationPersistenceManager cpm;

	@Override
	public void onEvent(MessageReceivedEvent event) {
		List<Long> developerIds = cpm.getBotConfig().getDeveloperIds();

		Message message = event.getMessage();
		if (message.getAuthor().isBot() || message.getChannelType() != ChannelType.PRIVATE)
			return;
		forwardToDeveloper(developerIds, message);
	}

	private void forwardToDeveloper(List<Long> devIds, Message message) {
		devIds.forEach(developerUserId -> {
			PrivateChannel pChannel = message.getJDA().getUserById(developerUserId).openPrivateChannel().complete();
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
			msgAction.queue();
		});
	}
}
