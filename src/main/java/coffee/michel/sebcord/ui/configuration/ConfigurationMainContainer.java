
package coffee.michel.sebcord.ui.configuration;

import java.util.Arrays;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.RoutePrefix;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinSession;

import coffee.michel.sebcord.ui.MainContainer;
import coffee.michel.sebcord.ui.Permissions;
import net.dv8tion.jda.api.entities.Member;

@ParentLayout(MainContainer.class)
@RoutePrefix("configuration")
public class ConfigurationMainContainer extends HorizontalLayout implements RouterLayout {
	private static final long serialVersionUID = -4294741955103500853L;

	public ConfigurationMainContainer() {
		super();
	}

	@Override
	public void showRouterLayoutContent(final HasElement content) {
		var navMenu = new VerticalLayout();

		setClassName("my-category");
		setMinHeight("0");
		setSpacing(false);
		setDefaultVerticalComponentAlignment(FlexComponent.Alignment.STRETCH);
		navMenu.setClassName("my-menu");
		navMenu.setMinHeight("0");
		navMenu.setSpacing(false);
		navMenu.setMaxWidth("300px");
		navMenu.setPadding(false);
		navMenu.setMinWidth("300px");
		navMenu.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
		navMenu.getStyle().set("overflow-x", "hidden");
		navMenu.getStyle().set("overflow-y", "auto");

		Member member = VaadinSession.getCurrent().getAttribute(Member.class);

		menuItem(navMenu, "Discord-App", DiscordAppConfiguration.class, member);
		menuItem(navMenu, "Twitch-App", TwitchConfigurationView.class, member);
		menuItem(navMenu, "Rollen", RoleConfigurationView.class, member);
		menuItem(navMenu, "Blacklist", BlacklistView.class, member);
		menuItem(navMenu, "Andere", OtherConfigurationView.class, member);

		navMenu.setSizeUndefined();
		removeAll();
		add(navMenu);
		setSizeUndefined();

		RouterLayout.super.showRouterLayoutContent(content);
		this.getElement().appendChild(content.getElement());
	}

	private void menuItem(VerticalLayout navMenu, String name, Class<? extends Component> view, Member member) {
		Permissions permissionsAnnotation = view.getAnnotation(Permissions.class);
		if (permissionsAnnotation != null) {
			if (!member.getPermissions().containsAll(Arrays.asList(permissionsAnnotation.value())))
				return;
		}

		Button button = new Button(name, ce -> UI.getCurrent().navigate(view));
		button.setSizeUndefined();
		navMenu.add(button);
	}
}
