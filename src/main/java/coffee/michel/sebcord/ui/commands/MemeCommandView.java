
package coffee.michel.sebcord.ui.commands;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.configuration.persistence.SebcordBot;
import coffee.michel.sebcord.configuration.persistence.SebcordBot.MemeCommand;
import coffee.michel.sebcord.ui.Permissions;
import net.dv8tion.jda.api.Permission;

@Permissions({ Permission.ADMINISTRATOR })
@Route(value = "meme", layout = CommandsContainer.class)
public class MemeCommandView extends VerticalLayout {
	private static final long				serialVersionUID	= -2653637655434962690L;

	@Autowired
	private ConfigurationPersistenceManager	cpm;

	public MemeCommandView() {
		super();
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		SebcordBot botConfig = cpm.getBotConfig();
		MemeCommand _memeCommand = botConfig.getMemeCommand();
		if (_memeCommand == null) {
			botConfig.setMemeCommand(_memeCommand = new MemeCommand());
			cpm.persist(botConfig, _memeCommand);
		}
		MemeCommand memeCommand = _memeCommand;

		add(new H1("Meme-Command Einstellungen"));

		H3 devUserHeader = new H3("Erlaubte Channel");
		HorizontalLayout devUserLayout = new HorizontalLayout();
		TextField devUserField = new TextField("Channel ID");
		ListBox<Long> channelIds = new ListBox<>();
		channelIds.setItems(memeCommand.getAllowedChannels());
		Button addDevUserButton = new Button("Hinzufügen");
		addDevUserButton.addClickListener(ce -> {
			String value = devUserField.getValue();
			if (value == null || value.isEmpty())
				return;
			Long roleId = Long.valueOf(value);
			memeCommand.getAllowedChannels().add(roleId);
			channelIds.setItems(memeCommand.getAllowedChannels());
		});
		Button removeDevUserButton = new Button("Entfernen");
		removeDevUserButton.addClickListener(ce -> channelIds.getOptionalValue().ifPresent(val -> {
			botConfig.getDeveloperIds().remove(val);
			channelIds.setItems(botConfig.getDeveloperIds());
		}));
		devUserLayout.add(devUserField, addDevUserButton, removeDevUserButton);
		add(devUserHeader, devUserLayout, channelIds);

		add(new H3("Pause pro User in Minuten"));
		TextField pauseTimeField = new TextField("", String.valueOf(memeCommand.getPauseTime() / 60 / 1000), "");
		add(pauseTimeField);

		add(new H3("Aktiv"));
		Checkbox active = new Checkbox(memeCommand.isActive());
		add(active);

		add(new Button("Speichern", ce -> {
			Long pauseTimeInMinutes = Long.valueOf(pauseTimeField.getValue());
			memeCommand.setPauseTime(pauseTimeInMinutes * 60 * 1000);
			memeCommand.setActive(active.getValue());

			cpm.persist(memeCommand, memeCommand.getAllowedChannels());
		}));
	}

}
