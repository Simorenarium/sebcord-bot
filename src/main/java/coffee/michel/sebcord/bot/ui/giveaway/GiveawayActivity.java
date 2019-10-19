/*
 * Erstellt am: 19 Oct 2019 13:07:25
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.ui.giveaway;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import coffee.michel.sebcord.bot.ui.MainRouterLayout;
import coffee.michel.sebcord.bot.ui.menu.UIFeature;

/**
 * @author Jonas Michel
 *
 */
@Route(value = "giveaway", layout = MainRouterLayout.class)
public class GiveawayActivity extends VerticalLayout implements UIFeature.UIVisualization {
	private static final long serialVersionUID = -3610527236463711403L;

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked", "cast" })
	public Class<? extends UIFeature> getFeature() {
		return (Class) GiveawayFeature.class;
	}

}
