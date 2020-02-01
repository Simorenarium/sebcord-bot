/*
 *
 * Erstellt am: 24 Jan 2020 20:11:23
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.ui;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

/**
 * @author Jonas Michel
 *
 */
@Route(value = "bot/auth")
public class LoginView extends VerticalLayout {
	private static final long serialVersionUID = 3373038869786353907L;

	public LoginView() {
		add(new PasswordField(lstn -> {

			String value = lstn.getValue();
			if (value != null && value.equals("")) {
				VaadinSession.getCurrent().setAttribute("discord.token", "true");
				getUI().ifPresent(ui -> ui.navigate(MainView.class));
			}

		}));
	}

}
