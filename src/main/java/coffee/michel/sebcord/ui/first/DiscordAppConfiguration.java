
package coffee.michel.sebcord.ui.first;

import java.util.Optional;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.configuration.persistence.DiscordApplication;

@Route(value = "discord-application", layout = ConfigurationMainContainer.class)
public class DiscordAppConfiguration extends VerticalLayout {
	private static final long				serialVersionUID	= -1993565705335683085L;

	private H3								header;
	private FormLayout						formLayout;
	private Button							saveButton;
	private ConfigurationPersistenceManager	persistence			= new ConfigurationPersistenceManager();

	public DiscordAppConfiguration() {
		super();
		this.initUI();
	}

	private void initUI() {
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

		formLayout.setSizeFull();
		add(header, formLayout, saveButton);
		setWidthFull();
		setHeight(null);
	}

}
