
package coffee.michel.sebcord.ui.commands;

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
@RoutePrefix("commands")
public class CommandsContainer extends HorizontalLayout implements RouterLayout {
	private static final long serialVersionUID = -6143585657824753212L;

	public CommandsContainer() {
		super();
	}

	@Override
	public void showRouterLayoutContent(HasElement content) {
		var navMenu = new VerticalLayout();
		Member member = VaadinSession.getCurrent().getAttribute(Member.class);
		menuItem(navMenu, "Meme-Command", MemeCommandView.class, member);
		menuItem(navMenu, "Konverter-Command", ConversionCommandView.class, member);

		setSpacing(false);
		setDefaultVerticalComponentAlignment(FlexComponent.Alignment.STRETCH);
		navMenu.setMinHeight("0");
		navMenu.setSpacing(false);
		navMenu.setMaxWidth("300px");
		navMenu.setPadding(false);
		navMenu.setMinWidth("300px");
		navMenu.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
		navMenu.getStyle().set("overflow-x", "hidden");
		navMenu.getStyle().set("overflow-y", "auto");

		removeAll();
		add(navMenu);
		setSizeUndefined();

		RouterLayout.super.showRouterLayoutContent(content);
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
