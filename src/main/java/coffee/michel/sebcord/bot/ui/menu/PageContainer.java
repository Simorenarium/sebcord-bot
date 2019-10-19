/*
 * Erstellt am: 18 Oct 2019 23:40:49
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.ui.menu;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author Jonas Michel
 *
 */
@ApplicationScoped
public class PageContainer {

	private List<UIFeature> allFeatures = new LinkedList<>();

	public void addFeatures(UIFeature feature) {
		allFeatures.add(feature);
	}

	public List<UIFeature> getPages() {
		return allFeatures;
	}

	public List<UIFeature> getPages(Set<String> permissions) {
		return UIFeature.filter(allFeatures, permissions);
	}

}
