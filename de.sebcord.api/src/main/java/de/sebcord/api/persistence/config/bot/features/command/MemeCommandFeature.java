package de.sebcord.api.persistence.config.bot.features.command;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import de.sebcord.api.persistence.config.bot.features.SebcordBotFeature;

public record MemeCommandFeature(long pauseTime,
								 boolean active,
								 Set<Long> allowedChannels) implements SebcordBotFeature {
	
	public MemeCommandFeature() {
		pauseTime = Duration.ofMinutes(5).toMillis();
		active = false;
		allowedChannels = new HashSet<>();
	}
	
	@Override
	public String getFeatureName() {
		return "Meme Command";
	}

}
