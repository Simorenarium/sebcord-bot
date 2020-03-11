package coffee.michel.sebcord.bot.event;

import net.dv8tion.jda.api.entities.Member;

public interface MemberJoinListener {

	void memberJoined(Member member);

}
