package coffee.michel.sebcord.bot.core.messages;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface PrivateMessageListener {

	void onEvent(MessageReceivedEvent event);

}
