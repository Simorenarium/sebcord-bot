/*
 *
 * Erstellt am: 5 Dec 2019 18:42:52
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.configuration.persistence;

import java.util.Objects;

/**
 * Holds all Properties wich are configured in
 * https://discordapp.com/developers/applications/<id>/information
 * 
 * @author Jonas Michel
 */
public class DiscordApplication {

	public static final String	PROPERTY_KEY	= "discord.application";

	private long				clientId		= 0;
	private String				clientSecret	= "";
	private String				token			= "";
	private String				redirectURL		= "";
	private boolean				enabled			= false;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public long getClientId() {
		return clientId;
	}

	public void setClientId(long clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		if (clientSecret == null)
			clientSecret = "";
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getToken() {
		if (token == null)
			token = "";
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getRedirectURL() {
		if (redirectURL == null)
			redirectURL = "";
		return redirectURL;
	}

	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}

	@Override
	public int hashCode() {
		return Objects.hash(clientId, clientSecret, redirectURL, token);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DiscordApplication other = (DiscordApplication) obj;
		return clientId == other.clientId && Objects.equals(clientSecret, other.clientSecret)
				&& Objects.equals(redirectURL, other.redirectURL) && Objects.equals(token, other.token);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DiscordApplication [clientId=").append(clientId).append(", clientSecret=").append(clientSecret)
				.append(", token=").append(token).append(", redirectURL=").append(redirectURL).append("]");
		return builder.toString();
	}

}
