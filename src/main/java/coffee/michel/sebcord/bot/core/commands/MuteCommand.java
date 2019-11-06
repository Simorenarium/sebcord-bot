/*
 * Erstellt am: 19 Oct 2019 14:51:41
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import coffee.michel.sebcord.bot.core.DCClient;
import coffee.michel.sebcord.bot.persistence.PersistenceManager;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Flux;

/**
 * @author Jonas Michel
 *
 */
@ApplicationScoped
public class MuteCommand extends AbstractCommand {

	private static final Pattern DURATION_PATTERN = Pattern.compile("\\d*(m|M|d|D|h|H)");

	@Inject
	private PersistenceManager persistenceMgr;
	@Inject
	private DCClient client;

	@Inject
	@ConfigProperty(name = "discord.bot.mute-role")
	private Long muteRoleId;

	@Resource
	private ManagedScheduledExecutorService exe;

	public void init(@SuppressWarnings("unused") @Observes @Initialized(ApplicationScoped.class) Object unused) {
		exe.scheduleWithFixedDelay(() -> {
			try {
				Map<Long, Instant> mutedUsers = CDI.current().select(PersistenceManager.class).get().getMutedUsers();
				Set<Long> userIdsToUnmute = mutedUsers.entrySet().stream().filter(e -> Instant.now().isAfter(e.getValue())).map(Entry::getKey).collect(Collectors.toSet());

				Guild guild = client.getGuild();
				for (Long userId : userIdsToUnmute)
					guild.getMemberById(Snowflake.of(userId)).subscribe(member -> member.removeRole(Snowflake.of(muteRoleId)).block());
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}, 2, 1, TimeUnit.MINUTES);
	}

	@Override
	public String getName() {
		return "Mute/Unmute";
	}

	@Override
	public String getCommandRegex() {
		return "(mute)|(unmute)";
	}

	@Override
	public String getDescription() {
		return "Muted einen oder mehrere erwähnte User. Die Dauer muss wie folgt angegeben werden: Zuerst die dauer, dann die Zeiteinheit (D,H,M).\n\tBsp.: o/ mute @User1 @User2 12D";
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

		if (!client.isAdminOrDev(message)) {
			channel.createMessage("Das kannst du nicht.").subscribe();
			return;
		}

		var guildId = message.getGuild().block().getId();
		var textWithCommand = message.getContent().map(c -> c.replace("o/", "").trim()).get();

		if (textWithCommand.startsWith("mute"))
			mute(text, message, channel, guildId);
		else
			unmute(message, channel, guildId);
	}

	private void mute(String text, Message message, MessageChannel channel, Snowflake guildId) {
		Matcher matcher = DURATION_PATTERN.matcher(text);
		if (!matcher.find()) {
			channel.createMessage("Zeitangaben müssen wie folgt angegeben werden: [<nummer><zeiteinheit>]").subscribe();
			return;
		}
		String stringDuration = matcher.group();
		String bracketsRemoved = stringDuration;
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
			user.asMember(guildId).subscribe(member -> member.addRole(Snowflake.of(muteRoleId)).subscribe());
			channel.createMessage(user.getMention() + " wurde gemuted.").block();
		});
	}

	private void unmute(Message message, MessageChannel channel, Snowflake guildId) {
		Flux<User> userMentions = message.getUserMentions();
		if (!userMentions.hasElements().block()) {
			channel.createMessage("Was auch immer, aber um jemanden zu entmuten musst du auch jemanden erwähnen.").subscribe();
			return;
		}
		userMentions.subscribe(user -> {
			persistenceMgr.removeMutedUser(user.getId().asLong());
			user.asMember(guildId).subscribe(member -> member.removeRole(Snowflake.of(muteRoleId)).subscribe());
			channel.createMessage(user.getMention() + " wurde entmuted.").block();
		});
	}

}
