package coffee.michel.sebcord.ui;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import coffee.michel.sebcord.bot.core.JDADCClient;
import coffee.michel.sebcord.ui.first.ConfigurationMain;
import coffee.michel.sebcord.ui.second.CommandsOverview;
import net.dv8tion.jda.api.entities.Member;

@Theme(value = Lumo.class, variant = Lumo.DARK)
public class MainContainer extends VerticalLayout
		implements RouterLayout, BeforeEnterObserver {
	private static final long	serialVersionUID	= 5473853593L;

	private HorizontalLayout	navContainerMain;
	@Autowired
	private Authenticator		auth;
	@Autowired
	private JDADCClient			client;

	public MainContainer() {
		super();

		setSpacing(false);
		setPadding(false);
		setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);

	}

	private void addMenuItem(String string, Icon create, Class<? extends Component> class1) {
		Button button = new Button(string, create);
		button.addClickListener(ce -> {
			UI.getCurrent().navigate(class1);
		});
		navContainerMain.add(button);
	}

	@Override
	public void showRouterLayoutContent(final HasElement content) {
		if (navContainerMain == null) {
			String uid = auth.getUID();
			if (uid != null) {

				Optional<Member> member = client.getMemberById(uid);
				if (member.isEmpty() && !client.isConfiguration()) {
					return;
				}

				if (!client.isAdminOrDev(member.get())) {
					// TODO insert user sites some day
					return;
				}
			}

			navContainerMain = new HorizontalLayout();
			addMenuItem(null, VaadinIcon.HOME.create(), MainView.class);
			addMenuItem("Einstellungen", VaadinIcon.COG.create(), ConfigurationMain.class);
			addMenuItem("Commands", VaadinIcon.COG.create(), CommandsOverview.class);
			add(navContainerMain);
		}

		this.getElement().appendChild(content.getElement());
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		VaadinSession currentSession = VaadinSession.getCurrent();
		Object _discordToken = currentSession.getAttribute("discord.token");
		Object _code = VaadinRequest.getCurrent().getParameter("code");
		if (_discordToken != null) {
			String discordToken = String.valueOf(_discordToken);
			if (!discordToken.isEmpty() && auth.setToken(discordToken))
				return;
			else
				event.forwardTo(LoginView.class);
		}
		if (_code != null) {
			String code = String.valueOf(_code);
			if (!code.isEmpty()) {
				String accessToken = auth.login(code);
				if (accessToken != null) {
					currentSession.setAttribute("discord.token", accessToken);
					return;
				} else
					event.forwardTo(LoginView.class);
			}
		}
		event.forwardTo(LoginView.class);
	}

}
