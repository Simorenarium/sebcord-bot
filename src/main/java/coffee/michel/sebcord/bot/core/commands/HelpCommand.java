/*
 * Erstellt am: 20 Oct 2019 12:29:48
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

/**
 * @author Jonas Michel
 *
 */
@Component
public class HelpCommand implements Command {

	private static final Pattern	pattern	= Pattern.compile("(help|hilfe)\\s*(--long)*");

	@Autowired
	private List<Command>			commands;

	@Override
	public String getName() {
		return "Hilfe";
	}

	@Override
	public List<String> getVariations() {
		return Arrays.asList("help", "hilfe", "help --long", "hilfe --long");
	}

	@Override
	public Pattern getCommandRegex() {
		return pattern;
	}

	@Override
	public String getDescription() {
		return "Zeigt diese Liste an.";
	}

	@Override
	public void onMessage(CommandEvent event) {
		Message message = event.getMessage();
		MessageChannel channel = message.getChannel();

		List<Command> allCommands = StreamSupport.stream(commands.spliterator(), false)
				.sorted((cmd1, cmd2) -> cmd1.getName().compareTo(cmd2.getName()))
				.collect(Collectors.toList());
		allCommands = new LinkedList<>(allCommands);
		allCommands.add(0, this);

		boolean longHelp = event.getMatchedGroups().contains("--long");
		if (!longHelp) {
			channel.sendTyping().complete();
			EmbedBuilder embedBuilder = new EmbedBuilder();
			embedBuilder = embedBuilder.setTitle("Hilfe");
			for (int i = 0; i < allCommands.size(); i++) {
				Command command = allCommands.get(i);
				embedBuilder.addField(command.getName(),
						"Variationen: " + command.getVariations().stream().collect(Collectors.joining(", ")), false);
				embedBuilder.setColor(Color.GREEN);
			}
			channel.sendMessage(embedBuilder.build()).queue();
			return;
		}
		List<MessageEmbed> embeds = new ArrayList<>(allCommands.size());
		for (int i = 0; i < allCommands.size(); i++) {
			Command command = allCommands.get(i);
			EmbedBuilder embedBuilder = new EmbedBuilder();
			embedBuilder = embedBuilder.setTitle(command.getName());
			embedBuilder.addField("Command-Variationen",
					command.getVariations().stream().collect(Collectors.joining(", ")), false);
			embedBuilder.addField("Beschreibung", command.getDescription(), false);
			embedBuilder.setColor(Color.GREEN);
			embeds.add(embedBuilder.build());
		}
		channel.sendTyping().queue();
		for (int i = 0; i < embeds.size(); i++) {
			MessageEmbed messageEmbed = embeds.get(i);
			channel.sendMessage(messageEmbed).queue();
			if (i < (embeds.size() - 2))
				channel.sendTyping().queue();
		}
	}
}
