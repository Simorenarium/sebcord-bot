/*
 * Erstellt am: 19 Oct 2019 14:51:41
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import coffee.michel.sebcord.bot.core.JDADCClient;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

/**
 * @author Jonas Michel
 *
 */
@Component
public class InfoCommand implements Command {

	private static final Pattern pattern = Pattern.compile("info");
	private static final DateTimeFormatter dtForm = DateTimeFormatter.ofPattern("dd.mm.yyyy hh:MM");

	@Autowired
	private JDADCClient client;

	@Override
	public String getName() {
		return "User-Info";
	}

	@Override
	public List<String> getVariations() {
		return Arrays.asList("info");
	}

	@Override
	public Pattern getCommandRegex() {
		return pattern;
	}

	@Override
	public String getDescription() {
		return "Zeigt Infos zu einem User. Bsp.: o/info @Sebcord-Bot";
	}

	@Override
	public void onMessage(CommandEvent event) {
		Message message = event.getMessage();
		MessageChannel channel = message.getChannel();

		List<User> mentionedUsers = message.getMentionedUsers();
		if (mentionedUsers.isEmpty()) {
			channel.sendMessage("Musst schon wen erwähnen.").complete();
			return;
		}

		CompletableFuture<Void> sendTyping = CompletableFuture.runAsync(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				channel.sendTyping().complete();
				try {
					Thread.sleep(5000L);
				} catch (InterruptedException e) {
					return;
				}
			}
		});

		mentionedUsers.forEach(user -> {
			var id = user.getId();
			var avatar = user.getAvatarUrl();
			var name = user.getName();
			var discrim = user.getDiscriminator();
			var accountCreated = user.getTimeCreated();

			Optional<Member> optMember = client.getMemberById(user.getId());
			var joinDate = optMember.map(m -> m.getTimeJoined()).map(dtForm::format).orElse("Unbekannt");
			var roles = optMember.map(m -> m.getRoles().stream().map(Role::getName).collect(Collectors.joining(", ")))
					.orElse("?");
			var memberColor = optMember.map(m -> m.getColor().getRGB());

			var embed = new EmbedBuilder() //
					.setAuthor(name, avatar, avatar) //
					.setThumbnail(avatar) //
					.addField(new Field("ID", id, true)) //
					.addField(new Field("Nickname", name + "#" + discrim, true)) //
					.addField(new Field("Account Erstellt", dtForm.format(accountCreated), false)) //
					.addField(new Field("Beigetreten", joinDate, false)) //
					.addField(new Field("Rollen", roles, false)); //
			
			memberColor.ifPresent(embed::setColor);
			
			channel.sendMessage(embed.build()).queue();
		});

		sendTyping.cancel(true);
	}
	
}
