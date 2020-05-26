package de.sebcord.api.persistence.config.bot.features.function;

import de.sebcord.api.persistence.config.bot.features.SebcordBotFeature;

public record MuteRoleFeature(Long muteRole) implements SebcordBotFeature {

	@Override
	public String getFeatureName() {
		return "Mute Role";
	}
	
}
