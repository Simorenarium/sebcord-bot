/*
 *
 * Erstellt am: 22 Jan 2020 21:20:27
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import coffee.michel.sebcord.bot.core.messages.MessageEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * @author Jonas Michel
 *
 */
@ApplicationScoped
public class MessageReceivedToCommandMapper {

	@Inject
	private Event<CommandEvent> cmdEvent;
	@Inject
	private Event<MessageEvent> msgEvent;

	public void onMessageEvent(@Observes GuildMessageReceivedEvent event) {
		Message message = event.getMessage();
		if (message.getContentRaw().startsWith(Command.COMMAND_INDICATOR)) {
			CommandEvent event2 = new CommandEvent();
			event2.setMessage(message);
			cmdEvent.fire(event2);
		} else {
			MessageEvent event2 = new MessageEvent();
			event2.setMessage(message);
			msgEvent.fire(event2);
		}
	}

}
