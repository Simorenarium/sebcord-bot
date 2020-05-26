package de.sebcord.api.discord;

public interface Member {

	String getId();
	String getDescriminator();
	String getUsername();
	String getDisplayName();
	
	boolean isJoined();

}
