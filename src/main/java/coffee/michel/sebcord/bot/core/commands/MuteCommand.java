/*
 * Erstellt am: 19 Oct 2019 14:51:41
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import coffee.michel.sebcord.Factory;
import coffee.michel.sebcord.bot.core.JDADCClient;
import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.persistence.PersistenceManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;

/**
 * @author Jonas Michel
 *
 */
@Component
public class MuteCommand implements Command {

	private static final Pattern			DURATION_PATTERN	= Pattern.compile("(\\d+)(m|M|d|D|h|H)");
	private static final Pattern			pattern				= Command.createPattern("(mute|unmute)");

	@Autowired
	private ConfigurationPersistenceManager	cpm;
	@Autowired
	private PersistenceManager				persistenceMgr;
	@Autowired
	private JDADCClient						client;
	private ScheduledExecutorService		exe					= Factory.executor();

	@PostConstruct
	public void init() {
		exe.scheduleWithFixedDelay(() -> {
			final var muteRoleId = cpm.getBotConfig().getMuteRoleId();
			try {
				Map<Long, Instant> mutedUsers = persistenceMgr.getMutedUsers();
				Set<Long> userIdsToUnmute = mutedUsers.entrySet().stream()
						.filter(e -> Instant.now().isAfter(e.getValue())).map(Entry::getKey)
						.collect(Collectors.toSet());

				Guild guild = client.getGuild();
				for (Long userId : userIdsToUnmute)
					guild.removeRoleFromMember(userId, guild.getRoleById(muteRoleId)).queue();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}, 0, 30, TimeUnit.SECONDS);
	}

	@Override
	public String getName() {
		return "Mute";
	}

	@Override
	public List<String> getVariations() {
		return Arrays.asList("mute", "unmute");
	}

	@Override
	public Pattern getCommandRegex() {
		return pattern;
	}

	@Override
	public String getDescription() {
		return "Muted einen oder mehrere erwähnte User. Die Dauer muss wie folgt angegeben werden: Zuerst die dauer, dann die Zeiteinheit (D,H,M).\n\tBsp.: o/ mute @User1 @User2 12D";
	}

	@Override
	public void onMessage(CommandEvent event) {
		Message message = event.getMessage();
		MessageChannel channel = message.getChannel();

		if (!client.isAdminOrDev(message)) {
			channel.sendMessage("Das kannst du nicht.").queue();
			return;
		}

		String actualCommand = event.getMatchedGroups().get(0);

		if (actualCommand.startsWith("mute"))
			mute(event.getText(), message, channel);
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
			message.getGuild().addRoleToMember(user.getIdLong(), muteRole).queue();
			message.addReaction("✅").queue();
		});
	}

	private void unmute(Message message, MessageChannel channel) {
		List<Member> mentionedMembers = message.getMentionedMembers();
		if (mentionedMembers.isEmpty()) {
			channel.sendMessage("Was auch immer, aber um jemanden zu entmuten musst du auch jemanden erwähnen.")
					.queue();
			return;
		}
		final Role muteRole = message.getGuild().getRoleById(cpm.getBotConfig().getMuteRoleId());
		mentionedMembers.forEach(user -> {
			persistenceMgr.removeMutedUser(user.getIdLong());
			message.getGuild().removeRoleFromMember(user.getIdLong(), muteRole).queue();
			message.addReaction("✅").queue();
		});
	}

}
