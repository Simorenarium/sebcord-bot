package coffee.michel.sebcord.ui.authentication;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import bell.oauth.discord.domain.User;
import bell.oauth.discord.main.OAuthBuilder;
import bell.oauth.discord.main.Response;
import coffee.michel.sebcord.bot.core.JDADCClient;
import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import net.dv8tion.jda.api.entities.Member;

@Component
public class Authenticator {

	private OAuthBuilder					bld;
	private Member							member;

	@Autowired
	private ConfigurationPersistenceManager	cpm;

	@PostConstruct
	public void init() {
		bld = new OAuthBuilder(String.valueOf(cpm.getDiscordApp().getClientId()),
				cpm.getDiscordApp().getClientSecret())
						.setScopes(new String[] { "identify" })
						.setRedirectURI("http://localhost:8080/login");
	}

	public String getAuthURL() {
		return bld.getAuthorizationUrl(null);
	}

	public boolean setToken(String token) {
		if (member != null)
			return true;
		bld.setAccess_token(token);
		if (bld.getUser() == null)
			return false;
		return true;
	}

	public String login(String code) {
		Response exchange = bld.exchange(code);
		if (exchange == Response.ERROR)
			return null;
		else
			return bld.getAccess_token();
	}

	public String getUID() {
		if (member != null)
			return member.getId();
		if (bld.getAccess_token() == null)
			return null;
		User user = bld.getUser();
		if (user == null)
			return null;
		return user.getId();
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public Member getMember() {
		if (member != null)
			return member;
		return member = JDADCClient.getINSTANCE().getMemberById(getUID()).orElse(null);
	}
}
