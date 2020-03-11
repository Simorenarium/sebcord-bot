package coffee.michel.sebcord.ui;

import java.util.Arrays;

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
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import coffee.michel.sebcord.bot.core.JDADCClient;
import coffee.michel.sebcord.ui.commands.CommandsOverview;
import coffee.michel.sebcord.ui.configuration.ConfigurationMain;
import net.dv8tion.jda.api.entities.Member;

@Theme(value = Lumo.class, variant = Lumo.DARK)
public class MainContainer extends VerticalLayout implements RouterLayout {
	private static final long	serialVersionUID	= 5473853593L;

	private HorizontalLayout	navContainerMain;
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

	private void addMenuItem(String string, Icon create, Class<? extends Component> type, Member member) {
		Permissions permissionsAnnotation = type.getAnnotation(Permissions.class);
		if (permissionsAnnotation != null) {
			if (!member.getPermissions().containsAll(Arrays.asList(permissionsAnnotation.value())))
				return;
		}

		Button button = new Button(string, create);
		button.addClickListener(ce -> {
			UI.getCurrent().navigate(type);
		});
		navContainerMain.add(button);
	}

	@Override
	public void showRouterLayoutContent(final HasElement content) {
		if (navContainerMain == null) {
			Member member = VaadinSession.getCurrent().getAttribute(Member.class);
			if (member != null && client.isConfigured()) {
				navContainerMain = new HorizontalLayout();
				addMenuItem(null, VaadinIcon.HOME.create(), MainView.class, member);
				addMenuItem("Einstellungen", VaadinIcon.COG.create(), ConfigurationMain.class, member);
				addMenuItem("Commands", VaadinIcon.COG.create(), CommandsOverview.class, member);
				add(navContainerMain);
			}
		}

		this.getElement().appendChild(content.getElement());
	}

}
