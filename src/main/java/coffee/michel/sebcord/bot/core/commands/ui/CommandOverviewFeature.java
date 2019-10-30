/*
 * Erstellt am: 26 Oct 2019 13:25:46
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands.ui;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import coffee.michel.sebcord.bot.ui.menu.UIFeature;

/**
 * @author Jonas Michel
 *
 */
public class CommandOverviewFeature implements UIFeature {

	@Override
	public String getName() {
		return "Commands";
	}

	@Override
	public Class<? extends UIVisualization> getUIClass() {
		return CommandOverview.class;
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
