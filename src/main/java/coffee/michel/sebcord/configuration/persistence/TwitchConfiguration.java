/*
 *
 * Erstellt am: 5 Dec 2019 19:20:38
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.configuration.persistence;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Jonas Michel
 *
 */
public class TwitchConfiguration {

	public static final String PROPERTY_KEY = "twitch.settings";

	public static class TrackedChannel {
		private String name = "";
		private String url  = "";

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, url);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TrackedChannel other = (TrackedChannel) obj;
			return Objects.equals(name, other.name) && Objects.equals(url, other.url);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("TrackedChannel [name=").append(name).append(", url=").append(url).append("]");
			return builder.toString();
		}

	}

	private String              clientId        = "";
	private String              clientSecret    = "";
	private Set<TrackedChannel> trackedChannels = new HashSet<>();

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public Set<TrackedChannel> getTrackedChannels() {
		return trackedChannels;
	}

	public void setTrackedChannels(Set<TrackedChannel> trackedChannels) {
		this.trackedChannels = trackedChannels;
	}

	@Override
	public int hashCode() {
		return Objects.hash(clientId, clientSecret, trackedChannels);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TwitchConfiguration other = (TwitchConfiguration) obj;
		return Objects.equals(clientId, other.clientId) && Objects.equals(clientSecret, other.clientSecret) && Objects.equals(trackedChannels, other.trackedChannels);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TwitchConfiguration [clientId=").append(clientId).append(", clientSecret=").append(clientSecret).append(", trackedChannels=").append(trackedChannels).append("]");
		return builder.toString();
	}

}