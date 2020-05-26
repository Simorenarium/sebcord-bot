package de.sebcord.api.discord;

import java.util.List;

public interface Category {

	String getName();
	List<Channel> getChannels();
	
}
