/*
 * Copyright GEMTEC GmbH 2019
 *
 * Erstellt am: 20 Oct 2019 12:29:48
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import static coffee.michel.sebcord.bot.core.commands.Command.COMMAND_INDICATOR;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.enterprise.event.ObservesAsync;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import discord4j.core.object.entity.Message;

/**
 * @author Jonas Michel
 *
 */
public class HelpCommand {

	@Inject
	private Instance<Command> commands;

	public void onMessage(@ObservesAsync MessageEvent event) {
		Message message = event.getMessage();
		Optional<String> content = message.getContent();
		if (!content.isPresent())
			return;

		String text = content.get().trim();
		if (text.startsWith(COMMAND_INDICATOR))
			text = text.replaceFirst(COMMAND_INDICATOR, "").trim();
		else
			return;
		if (text.startsWith("help"))
			text = text.replaceFirst("help", "").trim();
		else
			return;

		List<Command> allCommands = StreamSupport.stream(commands.spliterator(), false).collect(Collectors.toList());
		String s = "Hier sind alle Commands:\n\n";
		s += "1. `help`\n\tZeigt diese Liste an.";
		int i = 2;
		for (Command command : allCommands) {
			s += "\n" + (i++) + ". `" + command.getCommand() + "`\n\t" + command.getDescription();
		}
		final String commandList = s;
		message.getChannel().subscribe(ch -> ch.createMessage(commandList).subscribe());
	}
}
