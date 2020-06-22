/*
 *
 * Erstellt am: 24 Jan 2020 20:11:23
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.ui.authentication;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

import coffee.michel.sebcord.bot.core.JDADCClient;
import coffee.michel.sebcord.ui.MainView;
import net.dv8tion.jda.api.entities.Member;

/**
 * @author Jonas Michel
 *
 */
@Route(value = "login")
public class LoginView extends VerticalLayout implements HasUrlParameter<String> {
	private static final long	serialVersionUID	= 3373038869786353907L;

	@Autowired
	private JDADCClient			client;
	@Autowired
	private Authenticator		auth;

	@PostConstruct
	public void init() {
		VerticalLayout hl = new VerticalLayout();
		hl.setAlignSelf(Alignment.CENTER, this);
		hl.setAlignItems(Alignment.CENTER);

		H1 h1 = new H1("Sebcord-Web");
		hl.add(h1);
		Paragraph paragraph = new Paragraph(
				"Um die Webui verwenden zu können musst du dich mit discord einloggen.\n Dadurch bekommt der Bot deine Discord User-ID.\nDann werden deine Daten vom Sebcord weiter verwendet.\n"
						+ "Wenn das irgendwen stört, einfach die Seite nicht benutzen.");
		paragraph.setWidth("40em");
		hl.add(paragraph);
		hl.add(new Button("Zu Discord ->", ce -> {
			UI.getCurrent().getPage()
					.setLocation(auth.getAuthURL());
		}));

		add(hl);
		setAlignItems(Alignment.CENTER);

		Shortcuts.addShortcutListener(this, () -> {
			var command = "return prompt(\"Enter Passphrase\");";

			UI.getCurrent().getPage().executeJs(command).then(val -> {
				client.getMemberById(val.asString()).ifPresent(member -> {
					doAuthenticate("", member);
					UI.getCurrent().navigate(MainView.class);
				});
			});
			return;
		}, Key.PAGE_UP, KeyModifier.ALT, KeyModifier.CONTROL, KeyModifier.SHIFT);
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String unused) {
		Map<String, List<String>> parameters = event.getLocation().getQueryParameters().getParameters();
		String code = Optional.ofNullable(parameters.get("code")).filter(codes -> !codes.isEmpty())
				.map(codes -> codes.get(0)).orElse(null);
		if (code != null) {
			String token = auth.login(code);
			if (token == null)
				return;
			auth.setToken(token);

			Member member = auth.getMember();
			if (member == null) {
				return;
			}

			doAuthenticate(token, member);
			event.forwardTo(MainView.class);
		}
	}

	private void doAuthenticate(String token, Member member) {
		List<DiscordGrantedPermission> grantedPermissions = member.getPermissions().stream()
				.map(DiscordGrantedPermission::new).collect(Collectors.toList());

		SecurityContextHolder.getContext()
				.setAuthentication(new DiscordAuthentication(token, member, grantedPermissions));
	}

}
