/*
 * Erstellt am: 14 Oct 2019 20:54:57
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
	@ConfigProperty(name = "discord.bot.client_secret")
	private String clientSecret;
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
	@ConfigProperty(name = "discord.bot.roleTransitions")
	private String roleTransitions;
	private List<RoleTransition> mappedRoleTransitions = new LinkedList<>();

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
		EventDispatcher eventDispatcher = client.getEventDispatcher();
		eventDispatcher.on(MessageCreateEvent.class).subscribe(event -> {
			handleMessageCreateEvent(event);
		});
		eventDispatcher.on(MemberUpdateEvent.class).subscribe(memberUpdate -> {
			if (memberUpdate.getGuildId().asLong() == handledServerId)
				handleMemberUpdate(memberUpdate);
		});

		exe.submit(() -> {
			try {
				client.login().subscribe();

				Guild guild = getGuild();
				mappedRoleTransitions = Arrays.stream(roleTransitions.split(RoleTransition.SPLITTER)).map(tr -> new RoleTransition(tr, guild)).collect(Collectors.toList());

				List<Member> allMembers = guild.getMembers().collectList().block();
				for (Member member : allMembers) {
					handleMemberUpdate(new MemberUpdateEvent(client, guild.getId().asLong(), member.getId().asLong(), null, member.getRoleIds().stream().map(Snowflake::asLong).mapToLong(Long::valueOf).toArray(), member.getNickname().orElse(null)));
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		});
	}

	private void handleMemberUpdate(MemberUpdateEvent memberUpdate) {
		Set<Snowflake> currentRoles = memberUpdate.getCurrentRoles();
		Set<Snowflake> oldRoles = memberUpdate.getOld().map(m -> m.getRoleIds()).orElseGet(() -> Collections.emptySet());

		Set<Long> addedRoles = currentRoles.stream().filter(r -> !oldRoles.contains(r)).map(Snowflake::asLong).collect(Collectors.toSet());
		Set<Long> removedRoles = oldRoles.stream().filter(r -> !currentRoles.contains(r)).map(Snowflake::asLong).collect(Collectors.toSet());

		memberUpdate.getMember().subscribe(member -> {
			mappedRoleTransitions.forEach(tr -> tr.apply(member, addedRoles, removedRoles));
		});
	}

	private void handleMessageCreateEvent(MessageCreateEvent event) {
		exe.submit(() -> {
			try {
				Message message = event.getMessage();
				logger.debug("Nachricht empfangen: {}", message);
				System.out.println("test" + message);
				Mono<Guild> guild = message.getGuild();
				if (!guild.hasElement().block()) {
					Optional<User> author = message.getAuthor();
					if (author.isEmpty())
						return;
					forwardMessage(message, author);
					return;
				}
				long originGuild = guild.block().getId().asLong();
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
			} catch (Throwable t) {
				logger.error("Fehler beim Verarbeiten der Nachricht.", t);
			}
		});
	}

	private void forwardMessage(Message message, Optional<User> author) {
		User user = author.get();
		if (user.getUsername().equals("Simorenarium")) {
			String string = message.getContent().get();
			String[] msgParts = string.split("\\|");
			Guild targetGuild = client.getGuilds().filter(g -> g.getId().asLong() == handledServerId).blockFirst();
			targetGuild.getChannels().subscribe(channel -> {
				String name = channel.getName();
				String expectedChannelName = msgParts[0];
				if (channel instanceof MessageChannel && name.equals(expectedChannelName)) {
					((MessageChannel) channel).createMessage(msgParts[1]).subscribe();
				}
			});
		}
	}

	@PreDestroy
	public void shutdown() {
		client.logout().block(Duration.of(30L, ChronoUnit.SECONDS));
	}

	public Guild getGuild() {
		Flux<Guild> guilds = client.getGuilds().filter(g -> Objects.deepEquals(g.getId().asLong(), handledServerId));
		return guilds.collectList().block().get(0);
	}

	public AccessTokenResponse getAccessToken(String loginToken, String state) {
		Client c = ClientBuilder.newClient();
		WebTarget target = c.target(TOKEN_EP);

		Form form = new Form();
		target = target.queryParam("client_id", clientId);
		form.param("client_id", clientId);
		target = target.queryParam("client_secret", clientSecret);
		form.param("client_secret", clientSecret);
		target = target.queryParam("grant_type", "authorization_code");
		form.param("grant_type", "authorization_code");
		target = target.queryParam("code", loginToken);
		form.param("code", loginToken);
		target = target.queryParam("redirect_url", redirectURL);
		form.param("redirect_url", redirectURL);
		target = target.queryParam("scope", "identify");
		form.param("scope", "identify");

		Response response = target.request().post(Entity.form(form));
		response.bufferEntity();
		if (response.getStatus() != 200) {
			logger.debug("({}): getAccessToken: Access-Token abfrage fehlgeschlagen: ", identityHashCode, response.readEntity(String.class));
			return null;
		}
		return response.readEntity(AccessTokenResponse.class);
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
		int expires_in;
		String refresh_token;
		String scope;
		String state;

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
		public int getExpires_in() {
			return expires_in;
		}

		/**
		 * @param expires_in
		 *            the expires_in to set
		 */
		public void setExpires_in(int expires_in) {
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

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("AccessTokenResponse [access_token=").append(access_token).append(", token_type=").append(token_type).append(", expires_in=").append(expires_in).append(", refresh_token=").append(refresh_token).append(", scope=").append(scope).append("]");
			return builder.toString();
		}

	}

	private static class RoleTransition {

		static final String SPLITTER = ";";

		private Long sourceRole;
		// private String roleName;
		private boolean sourceRoleHasToBeAdded;
		private Snowflake targetRole;
		private boolean addTargetRole;

		public RoleTransition(	String singleTransition,
								Guild g) {
			String[] transitionPart = singleTransition.split("=");
			sourceRole = Long.valueOf(transitionPart[0].substring(1));
			sourceRoleHasToBeAdded = transitionPart[0].startsWith("+");
			targetRole = Snowflake.of(Long.valueOf(transitionPart[1].substring(1)));
			addTargetRole = transitionPart[1].startsWith("+");

			// roleName = g.getRoleById(targetRole).block().getName();
		}

		void apply(Member member, Set<Long> addedRoles, Set<Long> removedRoles) {
			if (sourceRoleHasToBeAdded ? addedRoles.contains(sourceRole) : removedRoles.contains(sourceRole)) {
				if (addTargetRole)
					// System.out.println("Add Role " + roleName + " to member " +
					// member.getUsername());
					member.addRole(targetRole).subscribe();
				else
					// System.out.println("Remove Role " + roleName + " from member " +
					// member.getUsername());
					member.removeRole(targetRole).subscribe();
			}
		}

	}

}
