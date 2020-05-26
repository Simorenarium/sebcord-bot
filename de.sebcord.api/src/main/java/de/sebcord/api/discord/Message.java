package de.sebcord.api.discord;

import java.time.Instant;
import java.util.List;

public interface Message {

	String getContent();
	List<Member> getMentionedMembers();
	List<Channel> getMentionedChannels();
	
	Instant getAuthorDate();
	Instant getEditDate();
	
	
}
