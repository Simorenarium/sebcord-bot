/*
 * Erstellt am: 19 Oct 2019 14:51:41
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import coffee.michel.sebcord.bot.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.bot.core.JDADCClient;
import coffee.michel.sebcord.bot.persistence.PersistenceManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;

/**
 * @author Jonas Michel
 *
 */
public class MuteCommand extends AbstractCommand {

	private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)(m|M|d|D|h|H)");

	@Inject
	private ConfigurationPersistenceManager cpm = new ConfigurationPersistenceManager();
	@Inject
	private PersistenceManager              persistenceMgr;
	@Inject
	private JDADCClient                     client;
	@Inject
	private ScheduledExecutorService        exe;

	@PostConstruct
	public void init() {
		exe.scheduleWithFixedDelay(() -> {
			final var muteRoleId = cpm.getBotConfig().getMuteRoleId();
			try {
				Map<Long, Instant> mutedUsers = persistenceMgr.getMutedUsers();
				Set<Long> userIdsToUnmute = mutedUsers.entrySet().stream().filter(e -> Instant.now().isAfter(e.getValue())).map(Entry::getKey).collect(Collectors.toSet());

				Guild guild = client.getGuild();
				for (Long userId : userIdsToUnmute)
					guild.removeRoleFromMember(userId, guild.getRoleById(muteRoleId));
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}, 0, 30, TimeUnit.SECONDS);
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
	public void onMessage(@Observes CommandEvent event) {
		super.onMessage(event);
	}

	@Override
	protected void handleCommand(CommandEvent event, String text) {
		Message message = event.getMessage();
		MessageChannel channel = message.getChannel();

		if (!client.isAdminOrDev(message)) {
			channel.sendMessage("Das kannst du nicht.").queue();
			return;
		}

		var textWithCommand = message.getContentRaw().replace("o/", "").trim();

		if (textWithCommand.startsWith("mute"))
			mute(text, message, channel);
		else
			unmute(message, channel);
	}

	private void mute(String text, Message message, MessageChannel channel) {
		Matcher matcher = DURATION_PATTERN.matcher(text);
		if (!matcher.find()) {
			channel.sendMessage("Zeitangaben müssen wie folgt angegeben werden: [<nummer><zeiteinheit>]").queue();
			return;
		}
		String duration = matcher.group(1);
		String _timeUnit = matcher.group(2);
		char timeUnitChar = _timeUnit.charAt(0);

		Long amountToAdd = Long.valueOf(duration);

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
				channel.sendMessage("Zeitangaben müssen wie folgt angegeben werden: [<nummer><zeiteinheit>]").queue();
				return;
		}

		List<Member> mentionedMembers = message.getMentionedMembers();
		if (mentionedMembers.isEmpty()) {
			channel.sendMessage("Was auch immer, aber um jemanden zu muten musst du auch jemanden erwähnen.").queue();
			return;
		}
		final Role muteRole = message.getGuild().getRoleById(cpm.getBotConfig().getMuteRoleId());
		mentionedMembers.forEach(user -> {
			persistenceMgr.addMutedUser(user.getIdLong(), Instant.now().plus(amountToAdd, timeUnit));
			message.getGuild().addRoleToMember(user.getIdLong(), muteRole);
			message.addReaction("✅").queue();
		});
	}

	private void unmute(Message message, MessageChannel channel) {
		List<Member> mentionedMembers = message.getMentionedMembers();
		if (mentionedMembers.isEmpty()) {
			channel.sendMessage("Was auch immer, aber um jemanden zu entmuten musst du auch jemanden erwähnen.").queue();
			return;
		}
		final Role muteRole = message.getGuild().getRoleById(cpm.getBotConfig().getMuteRoleId());
		mentionedMembers.forEach(user -> {
			persistenceMgr.removeMutedUser(user.getIdLong());
			message.getGuild().removeRoleFromMember(user.getIdLong(), muteRole);
			message.addReaction("✅").queue();
		});
	}

}
