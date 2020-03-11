package coffee.michel.sebcord.ui.configuration;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;

import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.ui.Permissions;
import net.dv8tion.jda.api.Permission;

@Permissions({ Permission.ADMINISTRATOR })
@Route(value = "other", layout = ConfigurationMainContainer.class)
public class OtherConfigurationView extends VerticalLayout {
	private static final long				serialVersionUID	= -4104289635957731725L;

	@Autowired
	private ConfigurationPersistenceManager	cpm					= new ConfigurationPersistenceManager();

	public OtherConfigurationView() {
		super();
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {

		removeAll();

		H3 welcomeMessageHeader = new H3("Wilkommensnachricht.");
		TextArea welcomMessageField = new TextArea();
		welcomMessageField.setWidth("100%");
		welcomMessageField.setHeight("6em");
		String welcomeMessage = cpm.getBotConfig().getWelcomeMessage();
		welcomMessageField.setValue(welcomeMessage == null ? "" : welcomeMessage);

		add(welcomeMessageHeader, welcomMessageField);
		add(new Button("Speichern", ce -> {
			String wcm = welcomMessageField.getValue();
			cpm.getBotConfig().setWelcomeMessage(wcm);

			cpm.persist(cpm.getBotConfig(), wcm);
		}));

		super.onAttach(attachEvent);
	}

}
