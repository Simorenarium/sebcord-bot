package coffee.michel.sebcord.bot.core.commands;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	private static final Pattern			pattern			= Pattern.compile("meme");

	private Map<Long, Instant>				lastExecByUser	= new HashMap<>();

	@Autowired
	private ConfigurationPersistenceManager	cpm				= new ConfigurationPersistenceManager();

	@Override
	public String getName() {
		return "Random memes";
	}

	@Override
	public List<String> getVariations() {
		return Arrays.asList("meme");
	}

	@Override
	public Pattern getCommandRegex() {
		return pattern;
	}

	@Override
	public String getDescription() {
		return "Liefert ein zufälliges meme von Reddit.\nKann jeder User alle 5 Minuten ausführen.";
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

		var userId = message.getAuthor().getIdLong();

		Instant lastActivateTime = lastExecByUser.get(userId);
		if (lastActivateTime != null && lastActivateTime.plusMillis(cpm.getBotConfig().getMemeCommand().getPauseTime())
				.isAfter(Instant.now())) {
			message.addReaction("⏱").queue();
			return;
		} else {
			lastExecByUser.put(userId, Instant.now());
		}

		channel.sendTyping().queue();

		var response = Unirest.get("https://meme-api.herokuapp.com/gimme").asBytes();
		if (response.getStatus() != 200) {
			channel.sendMessage("Geht grade nicht ¯\\_(ツ)_/¯. Versuchs später.").queue();
			return;
		}
		var body = new String(response.getBody());
		JsonObject fromJson = new Gson().fromJson(body, JsonObject.class);
		// play as if this could never be null
		Optional.ofNullable(fromJson.get("url")).map(el -> el.getAsString()).ifPresent(url -> {
			var postUrl = Optional.ofNullable(fromJson.get("postLink")).map(JsonElement::getAsString).orElse(null);

			EmbedBuilder setImage = new EmbedBuilder().setImage(url);
			if (postUrl != null)
				setImage = setImage.setFooter(url);
			channel.sendMessage(setImage.build()).queue();
		});
	}

}
