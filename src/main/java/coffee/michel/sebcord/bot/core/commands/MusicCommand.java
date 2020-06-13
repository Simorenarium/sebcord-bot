package coffee.michel.sebcord.bot.core.commands;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

//@Component
public class MusicCommand implements Command {

	private static final Pattern											pattern	= Pattern
			.compile("(music|musik).*");

	private coffee.michel.sebcord.bot.core.commands.commands.MusicCommand	mcmd;
	@Autowired
	private ConfigurationPersistenceManager									cpm;

	@PostConstruct
	public void init() {
		mcmd = new coffee.michel.sebcord.bot.core.commands.commands.MusicCommand(cpm);
	}

	@Override
	public String getName() {
		return "Music";
	}

	@Override
	public List<String> getVariations() {
		return Arrays.asList("music", "musik");
	}

	@Override
	public Pattern getCommandRegex() {
		return pattern;
	}

	@Override
	public String getDescription() {
		return coffee.michel.sebcord.bot.core.commands.commands.MusicCommand.getDescription();
	}

	@Override
	public void onMessage(CommandEvent event) {
		mcmd.onMessageReceived(new MessageReceivedEvent(event.getMessage().getJDA(), 0L, event.getMessage()));
	}

}
