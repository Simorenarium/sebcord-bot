/*
 *
 * Erstellt am: 24 Jan 2020 20:11:23
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.ui;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * @author Jonas Michel
 *
 */
@Route(value = "bot/auth")
public class LoginView extends VerticalLayout {
	private static final long	serialVersionUID	= 3373038869786353907L;

	@Autowired
	private Authenticator		auth;

	public LoginView() {
		VerticalLayout hl = new VerticalLayout();
		hl.setAlignSelf(Alignment.CENTER, this);
		hl.setAlignItems(Alignment.CENTER);

		H1 h1 = new H1("Sebcord-Web");
		hl.add(h1);
		Paragraph paragraph = new Paragraph(
				"Um die Webui verwenden zu können musst du dich mit discord einloggen.\n Dadurch bekommt der Bot deine Discord User-ID.\nDann werden deine Daten vom Sebcord weiter verwendet.");
		paragraph.setWidth("40em");
		hl.add(paragraph);
		hl.add(new Button("Zu Discord ->", ce -> {
			UI.getCurrent().getPage().setLocation(auth.getAuthURL());
		}));

		add(hl);
		setAlignItems(Alignment.CENTER);
	}

}
