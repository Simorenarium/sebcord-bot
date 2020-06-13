
package coffee.michel.sebcord.ui.configuration;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import ch.carnet.kasparscherrer.VerticalScrollLayout;
import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.configuration.persistence.DiscordApplication;
import coffee.michel.sebcord.ui.api.ParentContainer;
import coffee.michel.sebcord.ui.api.SebcordUIPage.BaseUIPage;
import net.dv8tion.jda.api.Permission;

@Route(value = "discord-application", layout = ConfigurationMainContainer.class)
public class DiscordAppConfiguration extends VerticalScrollLayout {
	private static final long serialVersionUID = -1993565705335683085L;

	@Component
	@ParentContainer("ConfigurationMainContainer")
	public static class DiscordAppConfigurationpage extends BaseUIPage {

		public DiscordAppConfigurationpage() {
			super(0, "Discord-App", DiscordAppConfiguration.class);
		}

		@Override
		public boolean matchesPermissions(Collection<String> permissions) {
			return permissions.contains(Permission.ADMINISTRATOR.toString());
		}

	}

	private H3								header;
	private FormLayout						formLayout;
	private Button							saveButton;

	@Autowired
	private ConfigurationPersistenceManager	persistence;

	@PostConstruct
	public void init() {
		header = new H3("Discord-Application Info");
		formLayout = new FormLayout();
		saveButton = new Button("Speichern");

		Anchor forwardToApplicationOverview = new Anchor("https://discordapp.com/developers/applications",
				new Icon(VaadinIcon.ARROW_FORWARD));
		forwardToApplicationOverview.getStyle().set("padding-left", "5px");
		header.add(forwardToApplicationOverview);

		DiscordApplication discordApp = persistence.getDiscordApp();
		TextField tokenField = new TextField();
		TextField clientIdField = new TextField();
		TextField clientSecretField = new TextField();
		TextField redirectURLField = new TextField();
		TextField handlesServerId = new TextField();
		Checkbox enabled = new Checkbox();

		tokenField.setValue(discordApp.getToken());
		clientIdField.setValue(String.valueOf(discordApp.getClientId()));
		clientSecretField.setValue(discordApp.getClientSecret());
		redirectURLField.setValue(discordApp.getRedirectURL());
		handlesServerId.setValue(String.valueOf(persistence.getBotConfig().getHandledServerId()));
		enabled.setValue(discordApp.isEnabled());

		formLayout.addFormItem(tokenField, "Token");
		formLayout.addFormItem(clientIdField, "Client-ID");
		formLayout.addFormItem(clientSecretField, "Client-Secret");
		formLayout.addFormItem(redirectURLField, "Redirect-URL");
		formLayout.addFormItem(handlesServerId, "ID des behandelten Server");
		formLayout.addFormItem(enabled, "Active");
		formLayout.setWidthFull();

		saveButton.getStyle().set("align-self", "stretch");
		saveButton.addClickListener(ce -> {
			discordApp.setClientId(Optional.ofNullable(clientIdField.getValue()).filter(s -> !s.isEmpty())
					.map(Long::valueOf).orElse(0L));
			discordApp.setClientSecret(clientSecretField.getValue());
			discordApp.setRedirectURL(redirectURLField.getValue());
			discordApp.setToken(tokenField.getValue());
			persistence.getBotConfig()
					.setHandledServerId(Optional.ofNullable(handlesServerId.getValue()).map(Long::valueOf).orElse(0L));
			discordApp.setEnabled(enabled.getValue());
			persistence.persist(discordApp);
			persistence.persist(persistence.getBotConfig());
		});

		setMinHeight("0");
		getStyle().set("overflow-x", "hidden");
		getStyle().set("overflow-y", "auto");

		add(header, formLayout, saveButton);
		setWidthFull();
		setHeight(null);
	}

}
