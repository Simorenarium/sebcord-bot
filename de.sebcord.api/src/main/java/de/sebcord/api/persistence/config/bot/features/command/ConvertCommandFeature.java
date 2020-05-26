package de.sebcord.api.persistence.config.bot.features.command;

import java.util.Map;

import de.sebcord.api.persistence.config.bot.features.SebcordBotFeature;
import de.sebcord.api.util.Pair;

public record ConvertCommandFeature(Map<Pair<String, String>, Double> conversions,
									Map<Pair<String, String>, Double> submittedConversions) implements SebcordBotFeature {

	@Override
	public String getFeatureName() {
		return "Convert Command";
	}

}
