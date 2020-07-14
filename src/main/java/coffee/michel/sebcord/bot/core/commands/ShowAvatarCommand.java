/*
 * Erstellt am: 19 Oct 2019 14:51:41
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

/**
 * @author Jonas Michel
 *
 */
@Component
public class ShowAvatarCommand implements Command {

	private static final Pattern			pattern	= Command.createPattern("avatar");

	private static final Map<Double, Long>	SCALE;
	static {
		Map<Double, Long> scale = new HashMap<Double, Long>();
		scale.put(0.5, 128L);
		scale.put(1.0, 256L);
		scale.put(2.0, 512L);
		scale.put(4.0, 1024L);
		scale.put(8.0, 2048L);
		SCALE = Collections.unmodifiableMap(scale);
	}

	@Override
	public String getName() {
		return "Avatar";
	}

	@Override
	public List<String> getVariations() {
		return Arrays.asList("avatar");
	}

	@Override
	public Pattern getCommandRegex() {
		return pattern;
	}

	@Override
	public String getDescription() {
		return "Zeigt den Avatar in groß an. \n\tAm Ende das Command könnt ihr auch eine Skalierung angeben\n\tDie Standardgröße ist 1x (256 Pixel), je größer das Bild desto länger dauert es.\n\tFolgende Scalierung sind möglich: (0,5; 1; 2; 4; 8).\nBei unbekannter skalierung wird 1 verwendet.";
	}

	@Override
	public void onMessage(CommandEvent event) {
		Message message = event.getMessage();
		MessageChannel channel = message.getChannel();
		var text = event.getText();
		text = text.replaceAll("<.*>", "");
		List<String> results = Pattern.compile("\\d*").matcher(text).results().map(MatchResult::group)
				.filter(s -> !s.isEmpty()).collect(Collectors.toList());

		long size = 256;
		if (!results.isEmpty()) {
			try {
				String s = results.get(results.size() - 1);
				if (s.trim().equals("0.5") || s.trim().equals("0,5")) {
					size = SCALE.get(0.5);
				} else {
					var scale = Double.valueOf(s);
					size = SCALE.getOrDefault(scale, 256L);
				}
			} catch (NumberFormatException e) {
			}
		}

		final long detSize = size;

		message.getMentionedUsers().stream().map(u -> u.getAvatarUrl()).filter(s -> !s.isBlank()).forEach(avatar -> {
			channel.sendMessage(new EmbedBuilder().setImage(avatar + "?size=" + detSize).build())
					.queue();
		});
	}

}
