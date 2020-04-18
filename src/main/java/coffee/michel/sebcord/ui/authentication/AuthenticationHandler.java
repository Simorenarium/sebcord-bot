package coffee.michel.sebcord.ui.authentication;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.Cookie;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;

import coffee.michel.sebcord.bot.core.JDADCClient;
import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import net.dv8tion.jda.api.entities.Member;

public class AuthenticationHandler implements VaadinServiceInitListener {
	private static final long				serialVersionUID		= -451989947374561509L;

	private Map<String, Authenticator>		authenticatorBySession	= new HashMap<>();

	@Autowired
	private ConfigurationPersistenceManager	cpm						= new ConfigurationPersistenceManager();
	@Autowired
	private JDADCClient						jda;

	@Override
	public void serviceInit(ServiceInitEvent event) {
		event.getSource()
				.addUIInitListener(initEvent -> {
					UI ui = initEvent.getUI();
					ui.getPage().addJavaScript("https://www.googletagmanager.com/gtag/js?id=UA-158165853-1");
					String reqUri = ((VaadinServletRequest) VaadinRequest.getCurrent()).getRequestURL().toString();
					try {
						URI uri = new URI(reqUri);
						int port = uri.getPort();
						String newURI = uri.getScheme() + "://" + uri.getHost() + (port == -1 ? "" : ":" + port)
								+ "/resources/gtag.js";
						ui.getPage().addJavaScript(newURI);
					} catch (Exception e) {
					}
					ui.addBeforeEnterListener(this::handleBeforeEnter);
				});
	}

	@SuppressWarnings("unchecked")
	private void handleBeforeEnter(BeforeEnterEvent event) {
		VaadinSession currentSession = event.getUI().getSession();
		String sessionId = currentSession.getSession().getId();

		Authenticator authenticator;

		synchronized (authenticatorBySession) {
			authenticator = authenticatorBySession.computeIfAbsent(sessionId,
					unused -> new Authenticator(cpm, jda));
		}

		if (!cpm.getDiscordApp().isEnabled())
			return;

		Member alreadySetMember = currentSession.getAttribute(Member.class);
		if (alreadySetMember != null) {
			authenticator.setMember(alreadySetMember);
			VaadinResponse.getCurrent().addCookie(new Cookie("sebcord.bot.userId", alreadySetMember.getId()));
			return;
		}
		if (Objects.equals(event.getNavigationTarget(), LoginView.class))
			return;

		Object _discordToken = currentSession.getAttribute("discord.token");
		Object _code = VaadinRequest.getCurrent().getParameter("code");
		boolean isAuthenticated = false;
		if (_discordToken != null) {
			String discordToken = String.valueOf(_discordToken);
			if (!discordToken.isEmpty() && authenticator.setToken(discordToken))
				isAuthenticated = true;

		} else if (_code != null) {
			String code = String.valueOf(_code);
			if (!code.isEmpty()) {
				String accessToken = authenticator.login(code);
				if (accessToken != null) {
					currentSession.setAttribute("discord.token", accessToken);
					isAuthenticated = true;
				}
			}
		}
		if (!isAuthenticated) {
			currentSession.setAttribute("coffee.michel.targetLocation", event.getNavigationTarget());
			currentSession.setAttribute("discord.oauth.url", authenticator.getAuthURL());
			event.forwardTo(LoginView.class);
			return;
		}

		currentSession.setAttribute(Member.class, authenticator.getMember());
		VaadinResponse.getCurrent().addCookie(new Cookie("sebcord.bot.userId", authenticator.getMember().getId()));
		Object attribute = currentSession.getAttribute("coffee.michel.targetLocation");
		if (attribute instanceof Class<?> && !Objects.equals(event.getNavigationTarget(), attribute))
			event.forwardTo((Class<? extends Component>) attribute);
	}

}
