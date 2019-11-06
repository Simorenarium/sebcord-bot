/*
 * Erstellt am: 19 Oct 2019 13:06:55
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.ui.giveaway;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import coffee.michel.sebcord.bot.ui.menu.UIFeature;
import discord4j.core.object.util.Permission;

/**
 * @author Jonas Michel
 *
 */
public class ViewGiveawayFeature implements UIFeature {

	@Override
	public String getName() {
		return "Giveaway anzeigen";
	}

	@Override
	public Class<? extends UIVisualization> getUIClass() {
		return ViewGiveaway.class;
	}

	@Override
	public List<UIFeature> getChildFeatures() {
		return Collections.emptyList();
	}

	@Override
	public Set<Permission> getPermissions() {
		return new HashSet<>(Arrays.asList(Permission.READ_MESSAGE_HISTORY));
	}

}
