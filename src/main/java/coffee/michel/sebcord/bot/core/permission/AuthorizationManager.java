/*
 * Erstellt am: 18 Oct 2019 22:36:14
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.permission;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.servlet.http.Cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;

import coffee.michel.sebcord.bot.core.DCClient;
import coffee.michel.sebcord.bot.core.DCClient.AccessTokenResponse;
import discord4j.core.object.util.Permission;

/**
 * @author Jonas Michel
 *
 */
@SessionScoped
public class AuthorizationManager implements Serializable {

	private static final String DISCORD_ACCESS_TOKEN_COOKIE = "discord_accessToken";
	private static final long serialVersionUID = 793268929709364939L;
	private static final String AUTH_KEY_ATTRIBUTE_KEY = "coffee.michel.sebcord-bot.feature.authorization.key";

	private static final Logger logger = LoggerFactory.getLogger(AuthorizationManager.class);
	private final int identityHashCode = System.identityHashCode(this);

	@Inject
	private transient DCClient client;
	private final String discordState = UUID.randomUUID().toString();

	public boolean isAuthorized() {
		Object userIdInSession = VaadinSession.getCurrent().getAttribute("discordUserId");
		if (userIdInSession != null)
			return true;

		String accessToken = getAuthKey();
		if (accessToken == null)
			return false;

		long userId = client.getUserId(accessToken);
		if (userId < 0)
			return false;
		VaadinSession.getCurrent().setAttribute("discordUserId", userId);
		return true;
	}

	public Set<Permission> getPermissions() {
		if (!isAuthorized())
			return Collections.emptySet();
		Object rawUserId = VaadinSession.getCurrent().getAttribute("discordUserId");
		if (rawUserId != null) {
			Long attribute = (Long) rawUserId;
			return client.getPermissionsInGuild(attribute);
		}
		return Collections.emptySet();
	}

	private String getAuthKey() {
		Object authKey = VaadinSession.getCurrent().getAttribute(AUTH_KEY_ATTRIBUTE_KEY);
		if (authKey == null)
			return null;
		return String.valueOf(authKey);
	}

	private boolean setAuthKey(String value) {
		AccessTokenResponse accessToken = client.getAccessToken(value, discordState);
		if (accessToken == null)
			return false;

		logger.debug("({}): setAuthKey: Authkey wird gesetzt. {}", identityHashCode, accessToken);
		VaadinSession.getCurrent().setAttribute(AUTH_KEY_ATTRIBUTE_KEY, accessToken.getAccess_token());
		Cookie cookie = new Cookie(DISCORD_ACCESS_TOKEN_COOKIE, accessToken.getAccess_token());
		cookie.setMaxAge(accessToken.getExpires_in());
		VaadinResponse.getCurrent().addCookie(cookie);
		return true;
	}

	public String getDiscordAuthPage() {
		return client.getAuthorizeWithDiscordLink();
	}

	public boolean isAuthorized(Location location) {
		if (isAuthorized())
			return true;

		QueryParameters queryParameters = location.getQueryParameters();
		List<String> list = queryParameters.getParameters().get("code");
		String token;
		if (list == null || list.isEmpty()) {
			Cookie[] cookies = VaadinRequest.getCurrent().getCookies();
			if (cookies == null)
				return false;
			Optional<String> accessTokenFromCookie = Arrays.stream(cookies).filter(c -> c.getName().contentEquals(DISCORD_ACCESS_TOKEN_COOKIE)).findAny().map(Cookie::getValue);
			if (accessTokenFromCookie.isEmpty())
				return false;
			token = accessTokenFromCookie.get();
		} else {
			token = list.get(0);
		}
		return setAuthKey(token);
	}

}
