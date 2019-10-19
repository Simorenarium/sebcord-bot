/*
 * Erstellt am: 18 Oct 2019 23:02:13
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.permission;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author Jonas Michel
 *
 */
@ApplicationScoped
public class FeatureController {

	private Set<FeatureSet> featureSets = new CopyOnWriteArraySet<>();

	public void add(FeatureSet featuresSet) {
		featureSets.add(featuresSet);
	}

	public Set<FeatureSet> getFeatureSets() {
		return Collections.unmodifiableSet(featureSets);
	}

	public Set<Feature> getAllFeatures() {
		return featureSets.stream().flatMap(fs -> fs.getFeatures().stream()).collect(Collectors.toSet());
	}

}
