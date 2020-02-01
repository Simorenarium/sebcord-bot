/*
 *
 * Erstellt am: 26 Nov 2019 17:29:20
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.endpoint;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import coffee.michel.sebcord.bot.core.JDADCClient;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * @author Jonas Michel
 *
 */
@Path("join")
public class InviteRedirect {

	@Inject
	private JDADCClient client;

	@GET
	public Response join() {
		TextChannel systemChannel = client.getGuild().getSystemChannel();
		String inviteURL = systemChannel.createInvite().setMaxAge(300).setMaxUses(1).setTemporary(true).reason("Invite created by sebcord-bot").complete().getUrl();

		return Response.status(Status.TEMPORARY_REDIRECT).entity(inviteURL).header(HttpHeaders.LOCATION, inviteURL).build();
	}

}
