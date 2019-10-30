/*
 * Erstellt am: 26 Oct 2019 13:25:26
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands.ui;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import coffee.michel.sebcord.bot.core.commands.Command;
import coffee.michel.sebcord.bot.ui.MainRouterLayout;
import coffee.michel.sebcord.bot.ui.menu.UIFeature;
import coffee.michel.sebcord.bot.ui.menu.UIFeature.UIVisualization;

/**
 * @author Jonas Michel
 *
 */
@Route(value = "commands", layout = MainRouterLayout.class)
public class CommandOverview extends VerticalLayout implements UIVisualization {
	private static final long serialVersionUID = -2171143643889877516L;

	@Override
	public Class<? extends UIFeature> getFeature() {
		return CommandOverviewFeature.class;
	}

	@Inject
	public CommandOverview(Instance<Command> commands) {
		setSizeFull();

		commands.forEach(command -> {
			HorizontalLayout commandWrapper = new HorizontalLayout();
			commandWrapper.setWidth("100%");

			VerticalLayout descriptionLayout = new VerticalLayout();
			H2 commandName = new H2(command.getName());
			descriptionLayout.add(commandName);
			descriptionLayout.add(new Paragraph(command.getDescription()));

			add(commandWrapper);
		});
	}

}
