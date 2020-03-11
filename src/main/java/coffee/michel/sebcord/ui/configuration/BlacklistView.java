
package coffee.michel.sebcord.ui.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;

import coffee.michel.sebcord.persistence.PersistenceManager;
import coffee.michel.sebcord.ui.Permissions;
import net.dv8tion.jda.api.Permission;

@Permissions({ Permission.ADMINISTRATOR })
@Route(value = "blacklist", layout = ConfigurationMainContainer.class)
public class BlacklistView extends VerticalLayout {
	private static final long	serialVersionUID	= 3329152364488054246L;

	private PersistenceManager	persistence			= new PersistenceManager();

	public BlacklistView() {
		super();
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		removeAll();

		TextArea ta = new TextArea();
		ta.setSizeFull();
		add(ta);

		List<String> blacklistedWords = persistence.getBlacklistedWords();
		ta.setValue(blacklistedWords.stream().collect(Collectors.joining(", ")));

		Button save = new Button("Speichern");
		save.addClickListener(ce -> {
			String orElse = Optional.ofNullable(ta.getValue()).orElse("");
			String[] badWords = orElse.split(",");

			blacklistedWords.forEach(persistence::removeWordFromBlacklist);
			Arrays.stream(badWords).map(String::trim).filter(s -> s.isEmpty()).forEach(persistence::addWordToBlacklist);
		});
		add(save);
	}

}
