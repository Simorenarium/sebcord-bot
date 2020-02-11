package coffee.michel.sebcord.bot.role;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public interface RoleChangeListener {

	void onRoleRemove(Member member, Role role);

	void onRoleAdd(Member member, Role role);

}
