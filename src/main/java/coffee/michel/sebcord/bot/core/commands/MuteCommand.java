/*
 * Erstellt am: 19 Oct 2019 14:51:41
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

import coffee.michel.sebcord.bot.persistence.PersistenceManager;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Permission;
import reactor.core.publisher.Flux;

/**
 * @author Jonas Michel
 *
 */
public class MuteCommand extends AbstractCommand {

	private static final Pattern DURATION_PATTERN = Pattern.compile("\\[\\d*(m|M|d|D|h|H)\\]");

	@Inject
	private PersistenceManager persistenceMgr;

	@Override
	public String getCommand() {
		return "mute";
	}

	@Override
	public String getDescription() {
		return "Muted einen oder mehrere erwähnte User. Die Dauer muss in `[..]` angegeben, zuerst die dauer, dann die Zeiteinheit (D,H,M).\n\tBsp.: o/ mute @User1 @User2 [12D]";
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

		User author = message.getAuthor().get();
		Member mAuthor = author.asMember(message.getGuild().block().getId()).block();
		Boolean hasPermissions = mAuthor.getRoles().filter(role -> role.getPermissions().contains(Permission.KICK_MEMBERS)).hasElements().block();
		if (!hasPermissions) {
			channel.createMessage("Das kannst du nicht.").subscribe();
			return;
		}

		Matcher matcher = DURATION_PATTERN.matcher(text);
		if (!matcher.find()) {
			channel.createMessage("Zeitangaben müssen wie folgt angegeben werden: [<nummer><zeiteinheit>]").subscribe();
			return;
		}
		String stringDuration = matcher.group();
		String bracketsRemoved = stringDuration.replace("[", "").replace("]", "");
		char timeUnitChar = bracketsRemoved.charAt(bracketsRemoved.length() - 1);

		Long amountToAdd = Long.valueOf(bracketsRemoved.substring(0, bracketsRemoved.length() - 1));

		TemporalUnit timeUnit;
		switch (timeUnitChar) {
			case 'd':
			case 'D':
				timeUnit = ChronoUnit.DAYS;
				break;
			case 'h':
			case 'H':
				timeUnit = ChronoUnit.HOURS;
				break;
			case 'm':
			case 'M':
				timeUnit = ChronoUnit.MINUTES;
				break;
			default:
				channel.createMessage("Zeitangaben müssen wie folgt angegeben werden: [<nummer><zeiteinheit>]").subscribe();
				return;
		}

		Flux<User> userMentions = message.getUserMentions();
		if (!userMentions.hasElements().block()) {
			channel.createMessage("Was auch immer, aber um jemanden zu muten musst du auch jemanden erwähnen.").subscribe();
			return;
		}
		userMentions.subscribe(user -> {
			persistenceMgr.addMutedUser(user.getId().asLong(), Instant.now().plus(amountToAdd, timeUnit));
			channel.createMessage(user.getMention() + " wurde gemuted.").subscribe();
		});
	}

}
