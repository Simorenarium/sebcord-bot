package coffee.michel.sebcord.bot.core.commands;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import kong.unirest.Unirest;
import net.dv8tion.jda.api.EmbedBuilder;

@Component
public class MemeCommand implements Command {

	private static final Pattern			pattern			= Pattern.compile("meme\\s*(.*)|dankmeme");

	private Map<Long, Instant>				lastExecByUser	= new HashMap<>();
	private Map<String, LocalDateTime>		previousMemes	= new HashMap<>();

	@Autowired
	private ConfigurationPersistenceManager	cpm				= new ConfigurationPersistenceManager();

	@Override
	public String getName() {
		return "Random memes";
	}

	@Override
	public List<String> getVariations() {
		return Arrays.asList("meme", "dankmeme", "meme <subreddit>");
	}

	@Override
	public Pattern getCommandRegex() {
		return pattern;
	}

	@Override
	public String getDescription() {
		return "Liefert ein zufälliges meme von Reddit. Wenn der Bot mit einer Uhr reagiert, müsst ihr warten.\n\to/meme\n\tdankmeme\n\to/meme <subreddit>";
	}

	@Override
	public void onMessage(CommandEvent event) {
		var message = event.getMessage();
		var channel = message.getChannel();

		if (!cpm.getBotConfig().getMemeCommand().isActive())
			return;

		if (!cpm.getBotConfig().getMemeCommand().getAllowedChannels().contains(channel.getIdLong())) {
			channel.sendMessage("Versuch es normal im Memes oder Bilders channel.").queue();
			return;
		}

		List<String> groups = event.getMatchedGroups();
		String apiUrl = "https://meme-api.herokuapp.com/gimme";

		if (groups.stream().filter(str -> str.startsWith("dankmeme")).findAny().isPresent()) {
			apiUrl += "/dankmemes";
		} else if (groups.size() == 2) {
			String subReddit = groups.get(1).trim();
			if (cpm.getBotConfig().getMemeCommand().getBlockedSubreddits().contains(subReddit.toLowerCase())) {
				channel.sendMessage("Der Subreddit " + subReddit + " is blockiert.").queue();
				return;
			}
			apiUrl += "/" + subReddit;
		}

		System.out.println("Meme requested: " + apiUrl);

		var userId = message.getAuthor().getIdLong();

		Instant lastActivateTime = lastExecByUser.get(userId);
		if (lastActivateTime != null && lastActivateTime.plusMillis(cpm.getBotConfig().getMemeCommand().getPauseTime())
				.isAfter(Instant.now())) {
			message.addReaction("⏱").queue();
			return;
		} else {
			lastExecByUser.put(userId, Instant.now());
		}

		String url = null;
		JsonObject fromJson;
		do {
			channel.sendTyping().queue();

			var response = Unirest.get(apiUrl).asBytes();
			if (response.getStatus() != 200) {
				channel.sendMessage("Geht grade nicht ¯\\_(ツ)_/¯. Versuchs später.").queue();
				return;
			}
			var body = new String(response.getBody());
			fromJson = new Gson().fromJson(body, JsonObject.class);
			// play as if this could never be null
			url = Optional.ofNullable(fromJson.get("url")).map(el -> el.getAsString()).get();
		} while (checkIfPosted(url));
		var postUrl = Optional.ofNullable(fromJson.get("postLink")).map(JsonElement::getAsString).orElse(null);

		EmbedBuilder setImage = new EmbedBuilder().setImage(url);
		if (postUrl != null)
			setImage = setImage.setFooter(url);
		channel.sendMessage(setImage.build()).queue();
	}

	private boolean checkIfPosted(String url) {
		Iterator<Entry<String, LocalDateTime>> iter = previousMemes.entrySet().iterator();
		LocalDateTime threeDaysPrior = LocalDateTime.now().minusDays(3);
		while (iter.hasNext()) {
			Entry<String, LocalDateTime> next = iter.next();
			if (next.getValue().isBefore(threeDaysPrior))
				iter.remove();
			else if (next.getKey().equals(url))
				return true;
		}
		return false;
	}
}
