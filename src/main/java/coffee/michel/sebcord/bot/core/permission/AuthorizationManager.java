/*
 * Erstellt am: 18 Oct 2019 22:36:14
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.permission;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.server.VaadinSession;

import coffee.michel.sebcord.bot.core.DCClient;
import coffee.michel.sebcord.bot.persistence.PersistenceManager;

/**
 * @author Jonas Michel
 *
 */
@SessionScoped
public class AuthorizationManager implements Serializable {
	private static final long serialVersionUID = 793268929709364939L;
	private static final String AUTH_KEY_ATTRIBUTE_KEY = "coffee.michel.sebcord-bot.feature.authorization.key";

	private static final Logger logger = LoggerFactory.getLogger(AuthorizationManager.class);
	private final int identityHashCode = System.identityHashCode(this);

	@Inject
	private transient FeatureController featureController;
	@Inject
	private transient PersistenceManager persistenceManager;
	@Inject
	private transient DCClient client;

	public boolean isAuthorized() {
		String accessToken = getAuthKey();
		if (accessToken == null)
			return false;

		long userId = client.getUserId(accessToken);
		if (userId <= 0)
			return false;
		return client.isUserOnKnownServer(userId);
	}

	public Set<String> getPermissions() {
		String authKey = getAuthKey();
		if (authKey == null)
			return Collections.emptySet();
		Set<String> authorizedFeatures = persistenceManager.getAuthorizedFeatures(authKey);

		return featureController.getAllFeatures().stream().filter(feature -> authorizedFeatures.contains(feature.getKey())).flatMap(f -> f.getPermissions().parallelStream()).collect(Collectors.toSet());
	}

	private String getAuthKey() {
		Object authKey = VaadinSession.getCurrent().getAttribute(AUTH_KEY_ATTRIBUTE_KEY);
		if (authKey == null)
			return null;
		return String.valueOf(authKey);
	}

	private void setAuthKey(String value) {
		String accessToken = client.getAccessToken(value);
		if (accessToken == null)
			return;

		logger.debug("({}): setAuthKey: Authkey wird gesetzt. {}", identityHashCode, accessToken);
		VaadinSession.getCurrent().setAttribute(AUTH_KEY_ATTRIBUTE_KEY, accessToken);
	}

	public String getDiscordAuthPage() {
		return client.getAuthorizeWithDiscordLink();
	}

	public boolean isAuthorized(Location location) {
		if (isAuthorized())
			return true;

		QueryParameters queryParameters = location.getQueryParameters();
		List<String> list = queryParameters.getParameters().get("code");
		if (list == null || list.isEmpty())
			return false;
		String token = list.get(0);
		setAuthKey(token);
		// TODO validate the user to be on the sebcord-discord
		return true;
	}

}
