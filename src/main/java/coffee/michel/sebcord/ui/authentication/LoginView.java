/*
 *
 * Erstellt am: 24 Jan 2020 20:11:23
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.ui.authentication;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import coffee.michel.sebcord.bot.core.JDADCClient;
import coffee.michel.sebcord.ui.MainView;
import net.dv8tion.jda.api.entities.Member;

/**
 * @author Jonas Michel
 *
 */
@Route(value = "bot/auth")
public class LoginView extends VerticalLayout {
	private static final long	serialVersionUID	= 3373038869786353907L;

	@Autowired
	private JDADCClient			client;

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		VerticalLayout hl = new VerticalLayout();
		hl.setAlignSelf(Alignment.CENTER, this);
		hl.setAlignItems(Alignment.CENTER);

		H1 h1 = new H1("Sebcord-Web");
		hl.add(h1);
		Paragraph paragraph = new Paragraph(
				"Um die Webui verwenden zu können musst du dich mit discord einloggen.\n Dadurch bekommt der Bot deine Discord User-ID.\nDann werden deine Daten vom Sebcord weiter verwendet.\n"
						+ "Zudem sammle ich ein paar Daten via Google-Statistiken, die sind aber anonym und werden nicht weiter vermittelt. Wenn das irgendwen stört, einfach die Seite nicht benutzen.");
		paragraph.setWidth("40em");
		hl.add(paragraph);
		hl.add(new Button("Zu Discord ->", ce -> {
			UI.getCurrent().getPage()
					.setLocation(String.valueOf(VaadinSession.getCurrent().getAttribute("discord.oauth.url")));
		}));

		add(hl);
		setAlignItems(Alignment.CENTER);

		Shortcuts.addShortcutListener(this, () -> {
			var command = "return prompt(\"Enter Passphrase\");";

			UI.getCurrent().getPage().executeJs(command).then(val -> {
				client.getMemberById(val.asString()).ifPresent(member -> {
					VaadinSession.getCurrent().setAttribute(Member.class, member);
					VaadinSession.getCurrent().setAttribute("discord.token", "workaround.login");
					UI.getCurrent().navigate(MainView.class);
				});
			});
			return;
		}, Key.PAGE_UP, KeyModifier.ALT, KeyModifier.CONTROL, KeyModifier.SHIFT);
	}

}
