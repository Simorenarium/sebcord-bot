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
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import coffee.michel.sebcord.Factory;
import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.configuration.persistence.TwitchConfiguration;
import coffee.michel.sebcord.configuration.persistence.TwitchConfiguration.TrackedChannel;
import coffee.michel.sebcord.persistence.PersistenceManager;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * @author Jonas Michel
 *
 */
@Component
@Scope("singleton")
public class SebcordTwitchClient implements ApplicationListener<ApplicationStartedEvent> {

	@Autowired
	private ConfigurationPersistenceManager cpm;
	@Autowired
	private JDADCClient dccClient;
	@Autowired
	private PersistenceManager persistence;
	private ScheduledExecutorService exe = Factory.executor();

	private Map<String, Long> lastNotifiedChannel = new HashMap<String, Long>();
	private Map<TrackedChannel, Future<?>> runningTasks = new HashMap<>();

	private AtomicReference<String> accessToken = new AtomicReference<>();

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		aquireAccessToken();

		exe.scheduleAtFixedRate(() -> {
			var clientId = cpm.getTwitchConfig().getClientId();
			Set<TrackedChannel> trackedChannels = cpm.getTwitchConfig().getTrackedChannels();
			for (TrackedChannel trackedChannel : trackedChannels) {
				Future<?> future = runningTasks.get(trackedChannel);
				if (future == null) {
					schedulePolling(clientId, trackedChannel);
					continue;
				}
				if (future.isDone() || future.isCancelled()) {
					schedulePolling(clientId, trackedChannel);
				}
			}

		}, 0, 1, TimeUnit.HOURS);
	}

	private void aquireAccessToken() {
		TwitchConfiguration twitchSettings = cpm.getTwitchConfig();
		
		HttpResponse<JsonNode> response = Unirest.post("https://id.twitch.tv/oauth2/token")
				.queryString("client_id", twitchSettings.getClientId())
				.queryString("client_secret", twitchSettings.getClientSecret())
				.queryString("grant_type", "client_credentials").queryString("scope", "viewing_activity_read").asJson();

		if (response.getStatus() != 200) {
			// TODO log
			return;
		}

		JsonNode json = response.getBody();

		JSONObject object = json.getObject();
		var accessToken = object.getString("access_token");
		var expiry = object.getLong("expires_in");

		this.accessToken.set(accessToken);
		exe.schedule(() -> aquireAccessToken(), expiry, TimeUnit.MILLISECONDS);
	}

	private void schedulePolling(String clientId, TrackedChannel trackedChannel) {
		String trackedChannelName = trackedChannel.getName();

		Gson gson = new Gson();

		ScheduledFuture<?> scheduleWithFixedDelay = exe.scheduleWithFixedDelay(() -> {
			try {
				String value = accessToken.get();
				if(value == null)
					return;
				
				HttpResponse<byte[]> response = Unirest.get("https://api.twitch.tv/helix/streams")
						.queryString("user_login", trackedChannelName).header("Client-ID", clientId)
						.header("Authorization","Bearer " + value).asBytes();
				if (response.getStatus() != 200)
					return;
				JsonObject json = gson.fromJson(new String(response.getBody()), JsonObject.class);
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
					guild.getChannels().stream()
							.filter(channel -> cpm.getBotConfig()
									.getTwitchStreamerLiveNotificationChannelId() == channel.getIdLong())
							.findAny().filter(TextChannel.class::isInstance).map(TextChannel.class::cast)
							.ifPresent(textChannel -> {
								textChannel.sendMessage("@everyone Der Mann is Live! " + trackedChannel.getUrl())
										.queue();
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
