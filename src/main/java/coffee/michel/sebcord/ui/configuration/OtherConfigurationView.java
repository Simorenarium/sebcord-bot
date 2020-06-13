package coffee.michel.sebcord.ui.configuration;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;

import ch.carnet.kasparscherrer.VerticalScrollLayout;
import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.ui.api.ParentContainer;
import coffee.michel.sebcord.ui.api.SebcordUIPage.BaseUIPage;
import net.dv8tion.jda.api.Permission;

@Route(value = "other", layout = ConfigurationMainContainer.class)
public class OtherConfigurationView extends VerticalScrollLayout {
	private static final long serialVersionUID = -4104289635957731725L;

	@Component
	@ParentContainer("ConfigurationMainContainer")
	public static class OtherConfigurationPage extends BaseUIPage {

		public OtherConfigurationPage() {
			super(4, "Andere", OtherConfigurationView.class);
		}

		@Override
		public boolean matchesPermissions(Collection<String> permissions) {
			return permissions.contains(Permission.ADMINISTRATOR.toString());
		}

	}

	@Autowired
	private ConfigurationPersistenceManager cpm;

	@PostConstruct
	public void init() {
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
	}

}
