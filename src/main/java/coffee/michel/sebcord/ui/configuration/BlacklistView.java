
package coffee.michel.sebcord.ui.configuration;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;

import ch.carnet.kasparscherrer.VerticalScrollLayout;
import coffee.michel.sebcord.persistence.PersistenceManager;
import coffee.michel.sebcord.ui.api.ParentContainer;
import coffee.michel.sebcord.ui.api.SebcordUIPage.BaseUIPage;
import net.dv8tion.jda.api.Permission;

@Route(value = "blacklist", layout = ConfigurationMainContainer.class)
public class BlacklistView extends VerticalScrollLayout {
	private static final long serialVersionUID = 3329152364488054246L;

	@Component
	@ParentContainer("ConfigurationMainContainer")
	public static class BlacklistPage extends BaseUIPage {

		public BlacklistPage() {
			super(3, "Blacklist", BlacklistView.class);
		}

		@Override
		public boolean matchesPermissions(Collection<String> permissions) {
			return permissions.contains(Permission.ADMINISTRATOR.toString());
		}

	}

	@Autowired
	private PersistenceManager persistence;

	@PostConstruct
	public void init() {
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
