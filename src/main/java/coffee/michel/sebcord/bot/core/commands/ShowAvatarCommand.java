/*
 * Erstellt am: 19 Oct 2019 14:51:41
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.enterprise.event.ObservesAsync;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;

/**
 * @author Jonas Michel
 *
 */
public class ShowAvatarCommand extends AbstractCommand {

	@Override
	public String getName() {
		return "Count";
	}

	@Override
	public String getCommandRegex() {
		return "avatar";
	}

	@Override
	public String getDescription() {
		return "Zeigt den Avatar in groß an. \n\tAm Ende das Command könnt ihr auch die größe in Pixeln angeben\n\tDie Standardgröße ist 256, je größer das Bild desto länger dauert es.\n\tDie Pixelangabe muss ein Faktor von 2 sein. (128, 256, 512...)";
	}

	@Override
	public void onMessage(@ObservesAsync CommandEvent event) {
		super.onMessage(event);
	}

	@Override
	protected void handleCommand(CommandEvent event, String text) {
		Message message = event.getMessage();
		MessageChannel channel = message.getChannel().block();
		if (channel == null)
			return;

		text = text.replaceAll("<.*>", "");
		List<String> results = Pattern.compile("\\d*").matcher(text).results().map(MatchResult::group).filter(s -> !s.isEmpty()).collect(Collectors.toList());

		int size;
		if (!results.isEmpty()) {
			size = Integer.valueOf(results.get(results.size() - 1));
			if (!isPowerOfTwo(size) || size > 2048) {
				channel.createMessage("Die Pixelangabe muss ein Faktor von 2 sein und darf nicht 2048 überschreiten.").subscribe();
				return;
			}
		} else
			size = 256;

		message.getUserMentions().map(u -> u.getAvatarUrl()).subscribe(avatar -> {
			channel.createEmbed(spec -> spec.setImage(avatar + "?size=" + size)).subscribe();
		});
	}

	private static boolean isPowerOfTwo(int number) {

		if (number % 2 != 0) {
			return false;
		} else {

			for (int i = 0; i <= number; i++) {

				if (Math.pow(2, i) == number)
					return true;
			}
		}
		return false;
	}

}
