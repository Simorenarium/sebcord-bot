package de.sebcord.api.persistence.config.bot.features.function;

import java.util.Map;

import de.sebcord.api.persistence.config.bot.features.SebcordBotFeature;

public record RoleTransitionFeature(Map<RoleAction, RoleAction> transitions) implements SebcordBotFeature {

	@Override
	public String getFeatureName() {
		return "Role Transitions";
	}
	
	public static record RoleAction(boolean isAdd, long roleId) {
		
	}

}
