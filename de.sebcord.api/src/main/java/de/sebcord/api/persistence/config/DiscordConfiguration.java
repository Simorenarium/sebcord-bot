package de.sebcord.api.persistence.config;

/**
 * Holds all Properties which are configured in
 * https://discordapp.com/developers/applications/<id>/information
 * 
 * @author Jonas Michel
 */
public record DiscordConfiguration(long clientId, 
					 			   String clientSecret, 
					 			   String token, 
					 			   String redirectURL) {}