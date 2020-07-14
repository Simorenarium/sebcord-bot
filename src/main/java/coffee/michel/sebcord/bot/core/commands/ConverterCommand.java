package coffee.michel.sebcord.bot.core.commands;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;

@Component
public class ConverterCommand implements Command {

	private static final Pattern pattern = Command
			.createPattern("(convert|umrechnen)\\s(list|(\\d+)([a-zA-Z]+)\\s+([a-zA-Z]+))");

	@Autowired
	private ConfigurationPersistenceManager cpm = new ConfigurationPersistenceManager();

	@Override
	public String getName() {
		return "Rechnet sachen um";
	}

	@Override
	public List<String> getVariations() {
		return Arrays.asList("convert", "convert list", "umrechnen", "umrechnen list");
	}

	@Override
	public Pattern getCommandRegex() {
		return pattern;
	}

	@Override
	public String getDescription() {
		return "Rechnet einen Wert in einen anderen um.\nFolgendes Muster muss eingehalten werden: `<Wert><Einheit><Leerzeichen><Ziel-Einheit>`";
	}

	@Override
	public void onMessage(CommandEvent event) {
		var message = event.getMessage();
		var channel = message.getChannel();

		channel.sendTyping().complete();
		List<String> matchedGroups = event.getMatchedGroups();
		if (matchedGroups.size() < 3) {
			channel.sendMessage("Prüf nochmal die Eingabe, da stimmt wat nicht.").queue();
			return;
		}

		String execPlan = matchedGroups.get(2);
		if (execPlan.equals("list")) {
			String listedConversions = cpm.getBotConfig().getConversions().getConversionFactors().entrySet().stream()
					.sorted((e1, e2) -> {
						int sourceDiff = e1.getKey().x.compareTo(e2.getKey().x);
						if (sourceDiff != 0)
							return sourceDiff;
						return e1.getKey().y.compareTo(e2.getKey().y);
					}).map(e -> {
						return padRight(e.getKey().x, 4) + " -> " + padRight(e.getKey().y, 4) + ": " + e.getValue();
					}).collect(Collectors.joining("\n"));
			channel.sendMessage("Alle Konvertierungsmöglichkeiten:\n```" + listedConversions + "```").queue();
			return;
		}

		if (matchedGroups.size() < 6) {
			channel.sendMessage("Prüf nochmal die Eingabe, da stimmt wat nicht.").queue();
			return;
		}

		String _sourceValue = matchedGroups.get(3);
		String sourceUnit = matchedGroups.get(4);
		String targetUnit = matchedGroups.get(5);

		Double conversion = cpm.getBotConfig().getConversions().getConversion(sourceUnit, targetUnit);

		String result;
		try {
			_sourceValue = _sourceValue.replace(',', ',');
			double source = Double.valueOf(_sourceValue);
			result = String.valueOf(conversion * source);
		} catch (NumberFormatException e) {
			try {
				int source = Integer.valueOf(_sourceValue);
				result = String.valueOf(conversion * source);
			} catch (NumberFormatException e1) {
				result = "Irgendwat stimmt mit deinem Wert nicht.";
			}
		}

		channel.sendMessage(result).queue();
	}

	private static String padRight(String source, int totalLength) {
		while (source.length() < totalLength)
			source += " ";
		return source;
	}

}
