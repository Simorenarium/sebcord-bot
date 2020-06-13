
package coffee.michel.sebcord.ui.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import ch.carnet.kasparscherrer.VerticalScrollLayout;
import coffee.michel.sebcord.bot.core.JDADCClient;
import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.configuration.persistence.SebcordBot;
import coffee.michel.sebcord.configuration.persistence.SebcordBot.MemeCommand;
import coffee.michel.sebcord.ui.api.ParentContainer;
import coffee.michel.sebcord.ui.api.SebcordUIPage.BaseUIPage;
import coffee.michel.sebcord.ui.components.ChannelComboBox;
import coffee.michel.sebcord.ui.components.ChannelListBox;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;

@Route(value = "meme", layout = CommandContainer.class)
public class MemeCommandView extends VerticalScrollLayout {
	private static final long serialVersionUID = -2653637655434962690L;

	@Component
	@ParentContainer("CommandContainer")
	public static class MemeCommandPage extends BaseUIPage {

		public MemeCommandPage() {
			super(0, "Meme-Command", MemeCommandView.class);
		}

		@Override
		public boolean matchesPermissions(Collection<String> permissions) {
			return permissions.contains(Permission.ADMINISTRATOR.toString());
		}

	}

	@Autowired
	private ConfigurationPersistenceManager	cpm;
	@Autowired
	private JDADCClient						client;

	@PostConstruct
	public void init() {
		setSizeFull();

		SebcordBot botConfig = cpm.getBotConfig();
		MemeCommand _memeCommand = botConfig.getMemeCommand();
		if (_memeCommand == null) {
			botConfig.setMemeCommand(_memeCommand = new MemeCommand());
			cpm.persist(botConfig, _memeCommand);
		}
		MemeCommand memeCommand = _memeCommand;

		List<GuildChannel> textChannels = client.getGuild().getChannels().stream()
				.filter(ch -> ch instanceof TextChannel).collect(Collectors.toList());
		Map<Long, GuildChannel> mappedChannels = textChannels.stream()
				.collect(Collectors.toMap(GuildChannel::getIdLong, Function.identity()));

		add(new H1("Meme-Command Einstellungen"));

		H3 devUserHeader = new H3("Erlaubte Channel");
		HorizontalLayout devUserLayout = new HorizontalLayout();
		ChannelComboBox allowedChannelSelect = new ChannelComboBox(textChannels);
		ChannelListBox channelIds = new ChannelListBox();
		channelIds.setItems(
				memeCommand.getAllowedChannels().stream().map(mappedChannels::get).collect(Collectors.toList()));
		Button addDevUserButton = new Button("Hinzufügen");
		addDevUserButton.addClickListener(ce -> {
			GuildChannel value = allowedChannelSelect.getValue();
			if (value == null)
				return;
			memeCommand.getAllowedChannels().add(value.getIdLong());
			channelIds.setItems(
					memeCommand.getAllowedChannels().stream().map(mappedChannels::get).collect(Collectors.toList()));
		});
		Button removeDevUserButton = new Button("Entfernen");
		removeDevUserButton.addClickListener(ce -> channelIds.getOptionalValue().ifPresent(val -> {
			botConfig.getDeveloperIds().remove(val.getIdLong());
			channelIds.setItems(
					memeCommand.getAllowedChannels().stream().map(mappedChannels::get).collect(Collectors.toList()));
		}));
		devUserLayout.add(allowedChannelSelect, addDevUserButton, removeDevUserButton);
		add(devUserHeader, devUserLayout, channelIds);

		add(new H3("Blockierte Subreddits"));
		TextArea blockedSRs = new TextArea();
		blockedSRs.setValue(memeCommand.getBlockedSubreddits().stream().collect(Collectors.joining(", ")));
		blockedSRs.setWidthFull();
		blockedSRs.setHeight("20em");
		add(blockedSRs);

		add(new H3("Pause pro User in Minuten"));
		TextField pauseTimeField = new TextField("", String.valueOf(memeCommand.getPauseTime() / 60 / 1000), "");
		add(pauseTimeField);

		add(new H3("Aktiv"));
		Checkbox active = new Checkbox(memeCommand.isActive());
		add(active);

		add(new H3(""));
		add(new Button("Speichern", ce -> {
			Long pauseTimeInMinutes = Long.valueOf(pauseTimeField.getValue());
			memeCommand.setPauseTime(pauseTimeInMinutes * 60 * 1000);
			memeCommand.setActive(active.getValue());

			String orElse = Optional.ofNullable(blockedSRs.getValue()).orElse("");
			String[] blockedSubReddits = orElse.split("\\s*,\\s*");

			memeCommand.getBlockedSubreddits().clear();
			Arrays.stream(blockedSubReddits).map(String::trim).map(String::toLowerCase).filter(s -> !s.isEmpty())
					.forEach(memeCommand.getBlockedSubreddits()::add);

			cpm.persist(memeCommand, memeCommand.getAllowedChannels());
		}));
	}

}
