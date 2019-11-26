/*
 *
 * Erstellt am: 26 Nov 2019 17:29:20
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.endpoint;

import javax.inject.Inject;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import coffee.michel.sebcord.bot.core.DCClient;

/**
 * @author Jonas Michel
 *
 */
@Route("join")
public class InviteRedirect extends Div implements BeforeEnterObserver {
	private static final long serialVersionUID = 6414204170203170029L;
	@Inject
	private DCClient client;

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		String inviteURL = client.getGuild().getSystemChannel().flatMap(sysChannel -> {
			return sysChannel.createInvite(spec -> {
				spec.setMaxAge(300);
				spec.setMaxUses(1);
				spec.setReason("Invite create by sebcord-bot");
				spec.setTemporary(false);
			});
		}).map(invite -> "https://discord.gg/" + invite.getCode()).block();
		UI.getCurrent().getPage().setLocation(inviteURL);
	}

}
