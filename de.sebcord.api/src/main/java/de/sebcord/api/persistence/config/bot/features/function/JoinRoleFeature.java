package de.sebcord.api.persistence.config.bot.features.function;

import java.util.List;

import de.sebcord.api.persistence.config.bot.features.SebcordBotFeature;

public record JoinRoleFeature(List<Long> joinRoles) implements SebcordBotFeature {

	@Override
	public String getFeatureName() {
		return "Join Role";
	}
	
}
