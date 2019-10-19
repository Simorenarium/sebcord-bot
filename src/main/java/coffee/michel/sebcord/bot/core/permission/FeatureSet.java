/*
 * Erstellt am: 18 Oct 2019 22:59:59
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.permission;

import java.util.Set;

/**
 * @author Jonas Michel
 *
 */
public class FeatureSet {

	private final String name;
	private final Set<Feature> features;

	public FeatureSet(	String name,
						Set<Feature> features) {
		super();
		this.name = name;
		this.features = features;
	}

	public String getName() {
		return this.name;
	}

	public Set<Feature> getFeatures() {
		return this.features;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((features == null) ? 0 : features.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		FeatureSet other = (FeatureSet) obj;
		if (features == null) {
			if (other.features != null)
				return false;
		} else if (!features.equals(other.features))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FeatureSet [name=" + name + ", features=" + features + "]";
	}

}
