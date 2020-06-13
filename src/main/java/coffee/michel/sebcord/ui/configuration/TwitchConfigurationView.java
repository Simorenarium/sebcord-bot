
package coffee.michel.sebcord.ui.configuration;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
import coffee.michel.sebcord.bot.core.JDADCClient;
import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.configuration.persistence.TwitchConfiguration.TrackedChannel;
import coffee.michel.sebcord.ui.api.ParentContainer;
import coffee.michel.sebcord.ui.api.SebcordUIPage.BaseUIPage;
import coffee.michel.sebcord.ui.components.ChannelComboBox;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;

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
	@Autowired
	private JDADCClient						client;

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

		List<GuildChannel> textChannels = client.getGuild().getChannels().stream()
				.filter(ch -> ch instanceof TextChannel).collect(Collectors.toList());

		var twitchConfig = persistence.getTwitchConfig();
		var botConfiguration = persistence.getBotConfig();

		TextField clientIdField = new TextField();
		clientIdField.setValue(twitchConfig.getClientId());
		TextField clientSecretField = new TextField();
		clientSecretField.setValue(twitchConfig.getClientSecret());
		ChannelComboBox liveNotificationChannelField = new ChannelComboBox(textChannels);
		liveNotificationChannelField
				.setValue(textChannels.stream()
						.filter(ch -> ch.getIdLong() == botConfiguration.getTwitchStreamerLiveNotificationChannelId())
						.findAny().orElse(null));

		formLayout.addFormItem(clientIdField, "Client-ID");
		formLayout.addFormItem(clientSecretField, "Client-Secret");
		formLayout.addFormItem(liveNotificationChannelField, "Kanal für Benachrichtigungen");
		formLayout.setWidthFull();

		TextField nameField = new TextField();
		TextField urlField = new TextField();
		tracketChannelLayout.addFormItem(nameField, "Name");
		tracketChannelLayout.addFormItem(urlField, "URL");

		ListBox<TrackedChannel> lb = new ListBox<>();
		lb.setRenderer(new TextRenderer<>(bean -> bean.getName() + " (" + bean.getUrl() + " )"));
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
					Optional.ofNullable(liveNotificationChannelField.getValue()).filter(Objects::nonNull)
							.map(GuildChannel::getIdLong).orElse(0L));

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
