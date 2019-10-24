/*
 * Erstellt am: 14 Oct 2019 20:54:57
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import coffee.michel.sebcord.bot.core.commands.Command;
import coffee.michel.sebcord.bot.core.commands.CommandEvent;
import coffee.michel.sebcord.bot.core.messages.MessageEvent;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Snowflake;

/**
 * @author Jonas Michel
 *
 */
@ApplicationScoped
public class DCClient {

	private static final Logger logger = LoggerFactory.getLogger(DCClient.class);
	private final int identityHashCode = System.identityHashCode(this);

	private static final String USER_EP = "https://discordapp.com/api/users/@me";
	private static final String TOKEN_EP = "https://discordapp.com/api/oauth2/token";

	@Inject
	@ConfigProperty(name = "discord.bot.clientId")
	private String clientId;
	@Inject
	@ConfigProperty(name = "discord.bot.token")
	private String token;
	@Inject
	@ConfigProperty(name = "discord.bot.redirect_url")
	private String redirectURL;
	@Inject
	@ConfigProperty(name = "discord.bot.handled_server")
	private long handledServerId;

	@Inject
	private Event<CommandEvent> cmdEvent;
	@Inject
	private Event<MessageEvent> msgEvent;

	@Resource
	private ManagedExecutorService exe;
	private DiscordClient client;

	/**
	 * @param unused
	 */
	public void init(@Observes @Initialized(ApplicationScoped.class) Object unused) {
		logger.debug("({}): init: Initialising bot with token {}", identityHashCode, token);
		client = new DiscordClientBuilder(token).build();
		client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> {
			Message message = event.getMessage();
			long originGuild = message.getGuild().block().getId().asLong();
			logger.debug("({}): init: Message origin Guild-Id {}", identityHashCode, originGuild);
			if (originGuild != handledServerId)
				return;
			logger.debug("({}): init: Message is from handled Guild.", identityHashCode);
			Optional<String> content = message.getContent();
			if (!content.isPresent())
				return;
			String c = content.get();
			logger.debug("({}): init: Nachricht empfangen {}", identityHashCode, message);
			if (c.startsWith(Command.COMMAND_INDICATOR)) {
				CommandEvent event2 = new CommandEvent();
				event2.setMessage(event.getMessage());
				cmdEvent.fireAsync(event2);
			} else {
				MessageEvent messageEvent = new MessageEvent();
				messageEvent.setMessage(message);
				msgEvent.fireAsync(messageEvent);
			}
		});

		exe.submit(() -> {
			client.login().block();
		});
	}

	@PreDestroy
	public void shutdown() {
		client.logout().block(Duration.of(30L, ChronoUnit.SECONDS));
	}

	public String getAccessToken(String loginToken) {
		Client c = ClientBuilder.newClient();
		WebTarget target = c.target(TOKEN_EP);

		Form form = new Form();
		form.param("client_id", clientId);
		form.param("client_secret", token);
		form.param("grant_type", "authorization_code");
		form.param("code", loginToken);
		form.param("redirect_url", redirectURL);
		form.param("scope", "identify");

		Response response = target.request().post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		if (response.getStatus() != 200)
			return null;
		response.bufferEntity();
		AccessTokenResponse resp = response.readEntity(AccessTokenResponse.class);
		return resp.getAccess_token();
	}

	public long getUserId(String accessToken) {
		Client c = ClientBuilder.newClient();
		WebTarget target = c.target(USER_EP);
		Response response = target.request().header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).get();
		if (response.getStatus() != 200)
			return -1;
		String readEntity = response.readEntity(String.class);
		JsonObject json = new Gson().fromJson(readEntity, JsonObject.class);
		JsonElement idElement = json.get("id");
		logger.debug("({}): getUserId: Folgender User wurde authorisiert: {}:{}", identityHashCode, json.get("username"), idElement.getAsString());
		return idElement.getAsLong();
	}

	public boolean isUserOnKnownServer(long userId) {
		return client.getGuilds().map(g -> {
			return g.getMemberById(Snowflake.of(userId)).block();
		}).count().map(c -> c > 0).block();
	}

	public String getAuthorizeWithDiscordLink() {
		return "https://discordapp.com/api/oauth2/authorize?client_id=" + clientId + "&redirect_uri=" + redirectURL + "&response_type=code&scope=identify";
	}

	public static class AccessTokenResponse {
		String access_token;
		String token_type;
		long expires_in;
		String refresh_token;
		String scope;

		/**
		 * @return the access_token
		 */
		public String getAccess_token() {
			return access_token;
		}

		/**
		 * @param access_token
		 *            the access_token to set
		 */
		public void setAccess_token(String access_token) {
			this.access_token = access_token;
		}

		/**
		 * @return the token_type
		 */
		public String getToken_type() {
			return token_type;
		}

		/**
		 * @param token_type
		 *            the token_type to set
		 */
		public void setToken_type(String token_type) {
			this.token_type = token_type;
		}

		/**
		 * @return the expires_in
		 */
		public long getExpires_in() {
			return expires_in;
		}

		/**
		 * @param expires_in
		 *            the expires_in to set
		 */
		public void setExpires_in(long expires_in) {
			this.expires_in = expires_in;
		}

		/**
		 * @return the refresh_token
		 */
		public String getRefresh_token() {
			return refresh_token;
		}

		/**
		 * @param refresh_token
		 *            the refresh_token to set
		 */
		public void setRefresh_token(String refresh_token) {
			this.refresh_token = refresh_token;
		}

		/**
		 * @return the scope
		 */
		public String getScope() {
			return scope;
		}

		/**
		 * @param scope
		 *            the scope to set
		 */
		public void setScope(String scope) {
			this.scope = scope;
		}

	}

}
