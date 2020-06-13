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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import coffee.michel.sebcord.Factory;
import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.configuration.persistence.TwitchConfiguration;
import coffee.michel.sebcord.configuration.persistence.TwitchConfiguration.TrackedChannel;
import coffee.michel.sebcord.persistence.PersistenceManager;
import kong.unirest.GetRequest;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * @author Jonas Michel
 *
 */
//@Component
@Scope("singleton")
public class SebcordTwitchClient implements ApplicationListener<ApplicationStartedEvent> {

	@Autowired
	private ConfigurationPersistenceManager	cpm;
	@Autowired
	private JDADCClient						dccClient;
	@Autowired
	private PersistenceManager				persistence;
	private ScheduledExecutorService		exe					= Factory.executor();

	private Map<String, Long>				lastNotifiedChannel	= new HashMap<String, Long>();
	private Map<TrackedChannel, Future<?>>	runningTasks		= new HashMap<>();

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		TwitchConfiguration twitchSettings = cpm.getTwitchConfig();
		var clientId = twitchSettings.getClientId();
		twitchSettings.getTrackedChannels().forEach(trackedChannel -> {
			schedulePolling(clientId, trackedChannel);
		});

		exe.scheduleAtFixedRate(() -> {

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

		}, 1, 1, TimeUnit.HOURS);
	}

	private void schedulePolling(String clientId, TrackedChannel trackedChannel) {
		String trackedChannelName = trackedChannel.getName();

		GetRequest getRequest = Unirest.get("https://api.twitch.tv/helix/streams")
				.queryString("user_login", trackedChannelName)
				.header("Client-ID", clientId);
		Gson gson = new Gson();

		ScheduledFuture<?> scheduleWithFixedDelay = exe.scheduleWithFixedDelay(() -> {
			try {
				HttpResponse<byte[]> response = getRequest.asBytes();
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
