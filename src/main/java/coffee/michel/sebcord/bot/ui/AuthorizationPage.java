/*
 * Erstellt am: 14 Oct 2019 23:20:27
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.ui;

import javax.inject.Inject;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import coffee.michel.sebcord.bot.core.permission.AuthorizationManager;

/**
 * @author Jonas Michel
 *
 */
@Route("login")
@Theme(value = Lumo.class, variant = Lumo.DARK)
public class AuthorizationPage extends VerticalLayout {
	private static final long serialVersionUID = -2585297208960680141L;

	@Inject
	private AuthorizationManager authMgr;

	public AuthorizationPage() {

		H1 title = new H1("Sebcord-Bot");
		Image image = new Image("icons/sebcord.png", "");
		image.setMaxWidth("75px");
		image.setMaxHeight("75px");
		HorizontalLayout horizontalLayout = new HorizontalLayout(title, image);
		horizontalLayout.setAlignItems(Alignment.CENTER);
		add(horizontalLayout);

		Span text = new Span("Um die Features des Discord-Bots nutzen zu können, muss du dich mit deinem Discod-Account einloggen.Achso und hier noch die obligatorische Warnung:Diese Seite benutzt Cookies!");
		text.setMaxWidth("300px");
		Button login = new Button("Fortfahren");
		login.addClickListener(ce -> UI.getCurrent().getPage().open(authMgr.getDiscordAuthPage()));

		var wrapper = new HorizontalLayout(text, login);
		wrapper.setAlignItems(Alignment.START);

		var centeringWrapper = new HorizontalLayout(wrapper);
		centeringWrapper.setHeightFull();
		centeringWrapper.setDefaultVerticalComponentAlignment(Alignment.CENTER);

		setSizeFull();
		setDefaultHorizontalComponentAlignment(Alignment.CENTER);
		add(centeringWrapper);
	}

}
