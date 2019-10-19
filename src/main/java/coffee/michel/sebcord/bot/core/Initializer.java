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

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;

/**
 * @author Jonas Michel
 *
 */
@ApplicationScoped
public class Initializer {

	private static final Logger logger = LoggerFactory.getLogger(Initializer.class);
	private final int identityHashCode = System.identityHashCode(this);

	@Inject
	@ConfigProperty(name = "discord.bot.token")
	private String token;

	@Inject
	private Event<MessageEvent> cmdEvent;

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
			Optional<String> content = message.getContent();
			if (!content.isPresent())
				return;
			String c = content.get();
			logger.debug("({}): init: Nachricht empfangen {}", identityHashCode, message);
			if (c.startsWith(MessageEvent.COMMAND_IDENTIFIER)) {
				MessageEvent event2 = new MessageEvent();
				event2.setMessage(event.getMessage());
				cmdEvent.fire(event2);
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

}
