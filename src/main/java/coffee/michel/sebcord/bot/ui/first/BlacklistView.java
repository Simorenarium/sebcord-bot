
package coffee.michel.sebcord.bot.ui.first;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.CDI;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;

import coffee.michel.sebcord.bot.persistence.PersistenceManager;

@Route(value = "blacklist", layout = ConfigurationMainContainer.class)
public class BlacklistView extends VerticalLayout {
	private static final long serialVersionUID = 3329152364488054246L;

	private PersistenceManager persistence;

	public BlacklistView() {
		super();
		persistence = CDI.current().select(PersistenceManager.class).get();
		this.initUI();
	}

	private void initUI() {
		TextArea ta = new TextArea();
		ta.setSizeFull();
		add(ta);

		List<String> blacklistedWords = persistence.getBlacklistedWords();
		ta.setValue(blacklistedWords.stream().collect(Collectors.joining(", ")));

		Button save = new Button("Speichern");
		save.addClickListener(ce -> {
			String orElse = Optional.ofNullable(ta.getValue()).orElse("");
			String[] badWords = orElse.split("\\s*,\\s*");

			Arrays.asList(badWords);
			blacklistedWords.forEach(persistence::removeWordFromBlacklist);
			Arrays.asList(badWords).forEach(persistence::addWordToBlacklist);
		});
		add(save);
	}

}
