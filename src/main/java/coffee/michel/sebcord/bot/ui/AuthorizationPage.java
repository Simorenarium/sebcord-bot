/*
 * Erstellt am: 14 Oct 2019 23:20:27
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.ui;

import javax.inject.Inject;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import coffee.michel.sebcord.bot.core.permission.AuthorizationManager;

/**
 * @author Jonas Michel
 *
 */
@Route("authorization")
@Theme(value = Lumo.class, variant = Lumo.DARK)
public class AuthorizationPage extends VerticalLayout {
	private static final long serialVersionUID = -2585297208960680141L;

	@Inject
	private AuthorizationManager authMgr;

	public AuthorizationPage() {
		PasswordField authKey = new PasswordField("Auth-Key");
		authKey.setAutoselect(true);
		Button login = new Button("Login");
		login.addClickListener(e -> {
			authMgr.setAuthKey(authKey.getValue());
			UI.getCurrent().navigate("");
		});

		var wrapper = new HorizontalLayout(authKey, login);
		wrapper.setAlignItems(Alignment.BASELINE);

		var centeringWrapper = new HorizontalLayout(wrapper);
		centeringWrapper.setHeightFull();
		centeringWrapper.setDefaultVerticalComponentAlignment(Alignment.CENTER);

		setSizeFull();
		setDefaultHorizontalComponentAlignment(Alignment.CENTER);
		add(centeringWrapper);
	}

}
