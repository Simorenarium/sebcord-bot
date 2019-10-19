/*
 * Erstellt am: 19 Oct 2019 13:06:55
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.ui.giveaway;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import coffee.michel.sebcord.bot.ui.menu.UIFeature;

/**
 * @author Jonas Michel
 *
 */
public class CreateGiveawayFeature implements UIFeature {

	@Override
	public String getName() {
		return "Giveaways erstellen";
	}

	@Override
	public Class<? extends UIVisualization> getUIClass() {
		return CreateGiveaway.class;
	}

	@Override
	public List<UIFeature> getChildFeatures() {
		return Collections.emptyList();
	}

	@Override
	public Set<String> getPermissions() {
		return Collections.emptySet();
	}

}
