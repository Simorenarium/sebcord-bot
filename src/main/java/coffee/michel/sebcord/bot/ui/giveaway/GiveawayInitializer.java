/*
 * Erstellt am: 19 Oct 2019 13:16:32
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.ui.giveaway;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import coffee.michel.sebcord.bot.ui.menu.PageContainer;

/**
 * @author Jonas Michel
 *
 */
@ApplicationScoped
public class GiveawayInitializer {

	@Inject
	private PageContainer container;

	@SuppressWarnings("unused")
	public void init(@Observes @Initialized(ApplicationScoped.class) Object unused) {
		container.addFeatures(new GiveawayFeature());
	}

}
