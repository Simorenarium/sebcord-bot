package coffee.michel.sebcord.bot.core.commands;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import coffee.michel.sebcord.bot.core.JDADCClient;
import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;

@Component
public class BlockMemeCommand implements Command {

	private static final Pattern			pattern	= Pattern.compile("blockmeme\\s*(.*)");

	@Autowired
	private ConfigurationPersistenceManager	cpm;
	@Autowired
	private JDADCClient						client;

	@Override
	public String getName() {
		return "Blockmeme";
	}

	@Override
	public List<String> getVariations() {
		return Arrays.asList("blockmeme");
	}

	@Override
	public Pattern getCommandRegex() {
		return pattern;
	}

	@Override
	public String getDescription() {
		return "Blockiert einen Subreddit vom Meme-Command";
	}

	@Override
	public void onMessage(CommandEvent event) {
		var message = event.getMessage();
		var channel = message.getChannel();

		if (!client.isAdminOrDev(message)) {
			channel.sendMessage("Frag die Mods.").queue();
			return;
		}

		List<String> groups = event.getMatchedGroups();

		if (groups.size() == 2) {
			String subReddit = groups.get(1).trim().toLowerCase();
			if (!subReddit.isBlank() && !subReddit.isEmpty()) {

				List<String> blockedSubreddits = cpm.getBotConfig().getMemeCommand().getBlockedSubreddits();
				blockedSubreddits.add(subReddit);
				cpm.persist(blockedSubreddits, subReddit);

				message.addReaction("✅").queue();
				return;
			}
		}
		channel.sendMessage("Versuchs so: o/blockmeme FiftyFifty").queue();
	}

}
