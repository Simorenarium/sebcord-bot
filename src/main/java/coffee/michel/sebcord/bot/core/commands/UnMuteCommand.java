/*
 * Erstellt am: 19 Oct 2019 14:51:41
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

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
public class UnMuteCommand extends AbstractCommand {

	@Inject
	private PersistenceManager persistenceMgr;

	@Override
	public String getCommand() {
		return "unmute";
	}

	@Override
	public String getDescription() {
		return "Entmuted einen Nutzer.";
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

		Flux<User> userMentions = message.getUserMentions();
		if (!userMentions.hasElements().block()) {
			channel.createMessage("Was auch immer, aber um jemanden zu entmuten musst du auch jemanden erwähnen.").subscribe();
			return;
		}
		userMentions.subscribe(user -> {
			persistenceMgr.removeMutedUser(user.getId().asLong());
			channel.createMessage(user.getMention() + " wurde entmuted.").subscribe();
		});
	}

}
