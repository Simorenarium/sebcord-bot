
package coffee.michel.sebcord.ui.configuration;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;

import ch.carnet.kasparscherrer.VerticalScrollLayout;
import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.configuration.persistence.TwitchConfiguration.TrackedChannel;
import coffee.michel.sebcord.ui.api.ParentContainer;
import coffee.michel.sebcord.ui.api.SebcordUIPage.BaseUIPage;
import net.dv8tion.jda.api.Permission;

@Route(value = "twitch", layout = ConfigurationMainContainer.class)
public class TwitchConfigurationView extends VerticalScrollLayout {
	private static final long serialVersionUID = 3329152364488054246L;

	@Component
	@ParentContainer("ConfigurationMainContainer")
	public static class TwitchConfigurationpage extends BaseUIPage {

		public TwitchConfigurationpage() {
			super(1, "Twitch-App", TwitchConfigurationView.class);
		}

		@Override
		public boolean matchesPermissions(Collection<String> permissions) {
			return permissions.contains(Permission.ADMINISTRATOR.toString());
		}

	}

	private H3								generalHeader, liveNotificationHeader;
	private FormLayout						formLayout, tracketChannelLayout;
	private Button							saveButton;

	@Autowired
	private ConfigurationPersistenceManager	persistence;

	@PostConstruct
	public void init() {
		generalHeader = new H3("Twitch-Application Info");
		formLayout = new FormLayout();
		tracketChannelLayout = new FormLayout();
		liveNotificationHeader = new H3("Live-Benachrichtigung");
		saveButton = new Button("Speichern");

		Anchor forwardToApplicationOverview = new Anchor("https://dev.twitch.tv/console/apps",
				new Icon(VaadinIcon.ARROW_FORWARD));
		forwardToApplicationOverview.getStyle().set("padding-left", "5px");
		generalHeader.add(forwardToApplicationOverview);

		var twitchConfig = persistence.getTwitchConfig();
		var botConfiguration = persistence.getBotConfig();

		TextField clientIdField = new TextField();
		clientIdField.setValue(twitchConfig.getClientId());
		TextField clientSecretField = new TextField();
		clientSecretField.setValue(twitchConfig.getClientSecret());
		TextField liveNotificationChannelField = new TextField();
		liveNotificationChannelField
				.setValue(String.valueOf(botConfiguration.getTwitchStreamerLiveNotificationChannelId()));

		formLayout.addFormItem(clientIdField, "Client-ID");
		formLayout.addFormItem(clientSecretField, "Client-Secret");
		formLayout.addFormItem(liveNotificationChannelField, "Kanal für Benachrichtigungen");
		formLayout.setWidthFull();

		TextField nameField = new TextField();
		TextField urlField = new TextField();
		tracketChannelLayout.addFormItem(nameField, "Name");
		tracketChannelLayout.addFormItem(urlField, "URL");

		ListBox<TrackedChannel> lb = new ListBox<>();
		lb.setRenderer(new TextRenderer<>(bean -> bean.getName()));
		lb.setItems(twitchConfig.getTrackedChannels());

		HorizontalLayout addAndRemove = new HorizontalLayout();
		Button button = new Button("Hinzufügen");
		button.addClickListener(ce -> {
			var tc = new TrackedChannel();
			tc.setName(nameField.getValue());
			tc.setUrl(urlField.getValue());
			twitchConfig.getTrackedChannels().add(tc);
			lb.setItems(twitchConfig.getTrackedChannels());
		});
		addAndRemove.add(button);
		Button removeButton = new Button("Entfernen");
		addAndRemove.add(removeButton);

		removeButton.addClickListener(ce -> {
			lb.getOptionalValue().ifPresent(val -> {
				twitchConfig.getTrackedChannels().remove(val);
				lb.setItems(twitchConfig.getTrackedChannels());
			});
		});

		saveButton.getStyle().set("align-self", "stretch");
		saveButton.addClickListener(ce -> {
			twitchConfig.setClientId(clientIdField.getValue());
			twitchConfig.setClientSecret(clientSecretField.getValue());
			botConfiguration.setTwitchStreamerLiveNotificationChannelId(
					Optional.ofNullable(liveNotificationChannelField.getValue()).filter(s -> !s.isEmpty())
							.map(Long::valueOf).orElse(0L));

			twitchConfig.getTrackedChannels().forEach(persistence::persist);
			persistence.persist(twitchConfig, twitchConfig.getTrackedChannels());
			persistence.persist(botConfiguration);
		});

		getStyle().set("overflow-x", "hidden");
		getStyle().set("overflow-y", "auto");

		add(generalHeader, formLayout, liveNotificationHeader, tracketChannelLayout, addAndRemove, lb, saveButton);
		setWidthFull();
	}

}
