/*
 * Erstellt am: 18 Oct 2019 22:36:14
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.permission;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

import com.vaadin.flow.server.VaadinSession;

import coffee.michel.sebcord.bot.persistence.PersistenceManager;

/**
 * @author Jonas Michel
 *
 */
@SessionScoped
public class AuthorizationManager implements Serializable {
	private static final long serialVersionUID = 793268929709364939L;
	private static final String AUTH_KEY_ATTRIBUTE_KEY = "coffee.michel.sebcord-bot.feature.authorization.key";

	@Inject
	private transient FeatureController featureController;
	@Inject
	private transient PersistenceManager persistenceManager;

	public boolean isAuthorized() {
		return getAuthKey() != null;
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

	public void setAuthKey(String value) {
		VaadinSession.getCurrent().setAttribute(AUTH_KEY_ATTRIBUTE_KEY, value);
	}

}
