/*
 * Erstellt am: 30 Oct 2019 23:23:44
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import coffee.michel.sebcord.bot.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.bot.configuration.persistence.TwitchConfiguration;
import coffee.michel.sebcord.bot.configuration.persistence.TwitchConfiguration.TrackedChannel;
import coffee.michel.sebcord.bot.persistence.PersistenceManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * @author Jonas Michel
 *
 */
@ApplicationScoped
public class SebcordTwitchClient {

	@Inject
	private ConfigurationPersistenceManager cpm;
	@Inject
	private JDADCClient                     dccClient;
	@Inject
	private PersistenceManager              persistence;
	@Inject
	private ScheduledExecutorService        exe;

	private Map<String, Long>              lastNotifiedChannel = new HashMap<String, Long>();
	private Map<TrackedChannel, Future<?>> runningTasks        = new HashMap<>();

	@SuppressWarnings("unused")
	public void init(@Observes @Initialized(ApplicationScoped.class) Object unused) throws InterruptedException {
		final Client client = ClientBuilder.newClient();

		TwitchConfiguration twitchSettings = cpm.getTwitchConfig();
		var clientId = twitchSettings.getClientId();
		twitchSettings.getTrackedChannels().forEach(trackedChannel -> {
			schedulePolling(client, clientId, trackedChannel);
		});

		exe.scheduleAtFixedRate(() -> {

			Set<TrackedChannel> trackedChannels = cpm.getTwitchConfig().getTrackedChannels();
			for (TrackedChannel trackedChannel : trackedChannels) {
				Future<?> future = runningTasks.get(trackedChannel);
				if (future == null) {
					schedulePolling(client, clientId, trackedChannel);
					continue;
				}
				if (future.isDone() || future.isCancelled()) {
					schedulePolling(client, clientId, trackedChannel);
				}
			}

		}, 1, 1, TimeUnit.HOURS);
	}

	private void schedulePolling(final Client client, String clientId, TrackedChannel trackedChannel) {
		String trackedChannelName = trackedChannel.getName();
		WebTarget streamsTarget = client.target("https://api.twitch.tv/helix/streams").queryParam("user_login", trackedChannelName);
		Gson gson = new Gson();

		ScheduledFuture<?> scheduleWithFixedDelay = exe.scheduleWithFixedDelay(() -> {
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

					synchronized (lastNotifiedChannel) {
						long lastNotification = lastNotifiedChannel.getOrDefault(trackedChannelName, 0L);
						if ((System.currentTimeMillis() - lastNotification) < (1000 * 60 * 10))
							return;
					}

					String newestId = stream.get("id").getAsString();
					if (newestId.equals(lastStreamId) && !lastStreamId.isEmpty()) {
						persistence.setLastAnnouncedStream(newestId);
						return;
					}
					lastStreamId = newestId;
					persistence.setLastAnnouncedStream(newestId);

					Guild guild = dccClient.getGuild();
					guild.getChannels().stream().filter(channel -> cpm.getBotConfig().getTwitchStreamerLiveNotificationChannelId() == channel.getIdLong()).findAny().filter(TextChannel.class::isInstance).map(TextChannel.class::cast).ifPresent(textChannel -> {
						textChannel.sendMessage("@everyone Der Mann is Live! " + trackedChannel.getUrl()).queue();
						synchronized (lastNotifiedChannel) {
							lastNotifiedChannel.put(trackedChannelName, System.currentTimeMillis());
						}
					});
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}, 60, 15, TimeUnit.SECONDS);
		runningTasks.put(trackedChannel, scheduleWithFixedDelay);
	}

}
