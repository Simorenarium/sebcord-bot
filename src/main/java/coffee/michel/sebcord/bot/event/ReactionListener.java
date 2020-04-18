package coffee.michel.sebcord.bot.event;

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;

public interface ReactionListener {

	void handle(MessageReactionAddEvent event);

	void handle(MessageReactionRemoveEvent event);

}
