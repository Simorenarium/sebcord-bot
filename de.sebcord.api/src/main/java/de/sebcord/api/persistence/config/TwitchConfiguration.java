package de.sebcord.api.persistence.config;

import java.util.Set;

public record TwitchConfiguration(String clientId,
								  String clientSecret,
								  Long notificationChannel,
								  Set<TrackedChannel> trackedChannels) {

	public static record TrackedChannel(String displayName, String channelName) {} 
	
}
