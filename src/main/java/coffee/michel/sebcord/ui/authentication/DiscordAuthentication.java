package coffee.michel.sebcord.ui.authentication;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import net.dv8tion.jda.api.entities.Member;

public class DiscordAuthentication extends AbstractAuthenticationToken {
	private static final long	serialVersionUID	= 7659989352067304980L;

	private final String		token;
	private final Member		member;

	public DiscordAuthentication(String token, Member member, Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
		this.token = token;
		this.member = member;
	}

	@Override
	public boolean isAuthenticated() {
		return true;
	}

	public String getToken() {
		return token;
	}

	public Member getMember() {
		return member;
	}

	@Override
	public Object getCredentials() {
		return token;
	}

	@Override
	public Object getPrincipal() {
		return member;
	}

}
