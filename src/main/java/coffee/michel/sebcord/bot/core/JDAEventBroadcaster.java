/*
 *
 * Erstellt am: 22 Jan 2020 20:57:46
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import coffee.michel.sebcord.Factory;
import coffee.michel.sebcord.bot.core.commands.Command;
import coffee.michel.sebcord.bot.core.commands.CommandEvent;
import coffee.michel.sebcord.bot.core.messages.MessageEvent;
import coffee.michel.sebcord.bot.core.messages.MessageListener;
import coffee.michel.sebcord.bot.core.messages.PrivateMessageListener;
import coffee.michel.sebcord.bot.event.ReactionListener;
import coffee.michel.sebcord.bot.event.handlers.UserJoinHandler;
import coffee.michel.sebcord.bot.role.RoleChangeListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * @author Jonas Michel
 *
 */
@Component
public class JDAEventBroadcaster extends ListenerAdapter {

	@Autowired
	private List<RoleChangeListener>		roleChangeListeners;
	@Autowired
	private List<Command>					commands;
	@Autowired
	private List<MessageListener>			messageListeners;
	@Autowired
	private List<PrivateMessageListener>	privateMessageListeners;
//	@Autowired
	private List<ReactionListener>			reactionListeners	= Arrays.asList();
	@Autowired
	private List<UserJoinHandler>			userJoinListeners;
	private ScheduledExecutorService		exe					= Factory.executor();

	@Autowired
	private List<JDAEventFilter>			eventFilters;
	private JDA								jda;

	private void filter(GenericGuildEvent event, Runnable r) {
		exe.submit(() -> {
			if (event.getJDA() != jda)
				return;
			for (JDAEventFilter filter : eventFilters) {
				if (!filter.allow(event))
					return;
			}
			r.run();
		});
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		filter(event, () -> {
			Message message = event.getMessage();
			String contentDisplay = message.getContentDisplay();
			if (contentDisplay.startsWith(Command.COMMAND_INDICATOR))
				handleCommand(message);
			else
				handleMessage(message);
		});
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		privateMessageListeners.forEach(lstn -> lstn.onEvent(event));
	}

	private void handleCommand(Message message) {
		for (Command command : commands) {
			String text = message.getContentDisplay().trim();
			text = text.replace(Command.COMMAND_INDICATOR, "").trim();
			Pattern commandRegex = command.getCommandRegex();
			Matcher matcher = commandRegex
					.matcher(text);

			List<String> groups = new LinkedList<>();
			while (matcher.find()) {
				for (int i = 0; i <= matcher.groupCount(); i++) {
					String group = matcher.group(i);
					if (group != null) {
						text = Optional.ofNullable(text.replaceFirst(group, "")).map(String::trim).orElse("");
						groups.add(group);
					}
				}
			}
			if (groups.isEmpty())
				continue;

			try {
				CommandEvent commandEvent = new CommandEvent(message, text, groups);
				command.onMessage(commandEvent);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return;
		}
	}

	private void handleMessage(Message message) {
		MessageEvent event = new MessageEvent();
		event.setMessage(message);
		messageListeners.forEach(lstn -> lstn.onMessage(event));
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		CompletableFuture.runAsync(() -> {
			reactionListeners.forEach(lstn -> lstn.handle(event));
		});
	}

	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
		CompletableFuture.runAsync(() -> {
			reactionListeners.forEach(lstn -> lstn.handle(event));
		});
	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		filter(event, () -> userJoinListeners.forEach(ujl -> {
			ujl.memberJoined(event.getMember());
		}));
	}

	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
		filter(event, () -> event.getRoles().forEach(role -> roleChangeListeners.forEach(rclstn -> {
			rclstn.onRoleAdd(event.getMember(), role);
		})));
	}

	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
		filter(event, () -> event.getRoles().forEach(role -> roleChangeListeners.forEach(rclstn -> {
			rclstn.onRoleRemove(event.getMember(), role);
		})));
	}

	public void setSourceJda(JDA jda) {
		this.jda = jda;
	}

}
