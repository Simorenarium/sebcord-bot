/*
 * Erstellt am: 18 Oct 2019 23:01:19
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.permission;

import java.util.Set;

/**
 * @author Jonas Michel
 *
 */
public class Feature {

	private final String key;
	private final Set<String> permissions;

	public Feature(	String key,
					Set<String> permissions) {
		super();
		this.key = key;
		this.permissions = permissions;
	}

	public String getKey() {
		return this.key;
	}

	public Set<String> getPermissions() {
		return this.permissions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((permissions == null) ? 0 : permissions.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Feature other = (Feature) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (permissions == null) {
			if (other.permissions != null)
				return false;
		} else if (!permissions.equals(other.permissions))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Feature [key=" + key + ", permissions=" + permissions + "]";
	}

}
