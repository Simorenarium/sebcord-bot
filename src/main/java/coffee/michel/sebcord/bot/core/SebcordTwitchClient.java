/*
 * Erstellt am: 30 Oct 2019 23:23:44
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import coffee.michel.sebcord.bot.persistence.PersistenceManager;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;

/**
 * @author Jonas Michel
 *
 */
@ApplicationScoped
public class SebcordTwitchClient {

	@Inject
	@ConfigProperty(name = "twitch.bot.clientId")
	private String clientId;
	@Inject
	@ConfigProperty(name = "twitch.bot.clientSecret")
	private String clientSecret;
	@Inject
	@ConfigProperty(name = "twitch.bot.trackedChannel.name")
	private String trackedChannelName;
	@Inject
	@ConfigProperty(name = "twitch.bot.trackedChannel.url")
	private String trackedChannelUrl;

	@Inject
	@ConfigProperty(name = "discord.bot.liveNotificationChannel")
	private Long liveNotificationChannelId;

	@Inject
	private DCClient dcClient;

	@Inject
	private PersistenceManager persistence;

	@Resource
	private ManagedScheduledExecutorService exe;

	public void init(@SuppressWarnings("unused") @Observes @Initialized(ApplicationScoped.class) Object unused) {

		Client client = ClientBuilder.newClient();

		WebTarget streamsTarget = client.target("https://api.twitch.tv/helix/streams").queryParam("user_login", trackedChannelName);
		Gson gson = new Gson();

		exe.scheduleWithFixedDelay(() -> {
			try {
				Response response = streamsTarget.request().header("Client-ID", clientId).get();
				response.bufferEntity();
				if (response.getStatus() != 200)
					return;
				String rawJson = response.readEntity(String.class);
				JsonObject json = gson.fromJson(rawJson, JsonObject.class);
				JsonElement data = json.get("data");
				JsonArray dataArray = data.getAsJsonArray();
				if (dataArray.size() < 1)
					return;

				for (Iterator<JsonElement> iter = dataArray.iterator(); iter.hasNext();) {
					JsonObject stream = iter.next().getAsJsonObject();
					if (!stream.get("user_name").getAsString().equalsIgnoreCase(trackedChannelName))
						continue;

					var lastStreamId = persistence.getLastAnnouncedStream();

					String newestId = stream.get("id").getAsString();
					if (newestId.equals(lastStreamId) && !lastStreamId.isEmpty()) {
						persistence.setLastAnnouncedStream(newestId);
						return;
					}
					lastStreamId = newestId;
					persistence.setLastAnnouncedStream(newestId);

					Guild guild = dcClient.getGuild();
					GuildChannel channel = guild.getChannelById(Snowflake.of(liveNotificationChannelId)).block();
					TextChannel textChannel = (TextChannel) channel;
					textChannel.createMessage(msgSpec -> {
						msgSpec.setContent("@everyone Der Mann is Live! " + trackedChannelUrl);
					}).subscribe();
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}, 60, 15, TimeUnit.SECONDS);
	}

}
