package coffee.michel.sebcord.ui;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import coffee.michel.sebcord.ui.api.ContainerHelper;
import coffee.michel.sebcord.ui.api.ParentContainer;
import coffee.michel.sebcord.ui.api.SebcordUIPage;
import coffee.michel.sebcord.ui.authentication.Authenticator;
import coffee.michel.sebcord.ui.authentication.DiscordAuthentication;
import coffee.michel.sebcord.ui.authentication.DiscordGrantedPermission;
import net.dv8tion.jda.api.entities.Member;

@Theme(value = Lumo.class, variant = Lumo.DARK)
public class MainContainer extends VerticalLayout implements RouterLayout, BeforeEnterListener, HasUrlParameter<String> {
	private static final long serialVersionUID = 5473853593L;

	private HorizontalLayout navContainerMain;

	@Autowired
	@ParentContainer("MainContainer")
	public Set<SebcordUIPage> pages;
	
	@Autowired
	private Authenticator		auth;

	@PostConstruct
	public void init() {
		removeAll();
		setHeight("100%");
		setSpacing(false);
		setPadding(false);
		setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);

		navContainerMain = new HorizontalLayout();
		navContainerMain.getStyle().set("background-colour", "hsl(214, 35%, 29%)");

		ContainerHelper.ifAuthorized(() -> {
			Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication()
					.getAuthorities();
			pages.stream().sorted().forEach(page -> ContainerHelper.addMenuItem(navContainerMain, page, authorities));
		});

		add(navContainerMain);
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		init();
	}
	
	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String unused) {
		Map<String, List<String>> parameters = event.getLocation().getQueryParameters().getParameters();
		String code = Optional.ofNullable(parameters.get("code")).filter(codes -> !codes.isEmpty())
				.map(codes -> codes.get(0)).orElse(null);
		if (code != null) {
			String token = auth.login(code);
			if (token == null)
				return;
			auth.setToken(token);

			Member member = auth.getMember();
			if (member == null) {
				return;
			}

			doAuthenticate(token, member);
			event.forwardTo(MainView.class);
		}
		init();
	}

	private void doAuthenticate(String token, Member member) {
		List<DiscordGrantedPermission> grantedPermissions = member.getPermissions().stream()
				.map(DiscordGrantedPermission::new).collect(Collectors.toList());

		SecurityContextHolder.getContext()
				.setAuthentication(new DiscordAuthentication(token, member, grantedPermissions));
	}
	
	@Override
	public void showRouterLayoutContent(final HasElement content) {
		this.getElement().appendChild(content.getElement());
	}

}
