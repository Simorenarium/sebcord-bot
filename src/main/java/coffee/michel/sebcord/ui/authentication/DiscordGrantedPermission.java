package coffee.michel.sebcord.ui.authentication;

import org.springframework.security.core.GrantedAuthority;

import net.dv8tion.jda.api.Permission;

public class DiscordGrantedPermission implements GrantedAuthority {
	private static final long	serialVersionUID	= -3773757698171786274L;
	private Permission			perm;

	public DiscordGrantedPermission(Permission perm) {
		this.perm = perm;
	}

	@Override
	public String getAuthority() {
		return perm.toString();
	}

}
