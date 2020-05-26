package de.sebcord.api.persistence.config;

import java.util.List;
import java.util.Set;

import de.sebcord.api.persistence.config.bot.features.SebcordBotFeature;

/**
 * Contains all Core-Configuration
 * 
 * @author Jonas Michel
 *
 */
public record SebcordBotConfiguration(boolean enabled, 
									  Long handledServer, 
									  List<Long> developers,
									  Set<SebcordBotFeature> features) {
}
