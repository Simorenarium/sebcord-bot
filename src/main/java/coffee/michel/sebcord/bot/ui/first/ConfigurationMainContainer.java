
package coffee.michel.sebcord.bot.ui.first;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.RoutePrefix;
import com.vaadin.flow.router.RouterLayout;

import coffee.michel.sebcord.bot.ui.MainContainer;

@ParentLayout(MainContainer.class)
@RoutePrefix("configuration")
public class ConfigurationMainContainer extends HorizontalLayout implements RouterLayout {
	private static final long serialVersionUID = -4294741955103500853L;

	private Button         btnView1, btnView2, btnView3, btnView4;
	private VerticalLayout navMenu;

	public ConfigurationMainContainer() {
		super();
		this.initUI();
	}

	@Override
	public void showRouterLayoutContent(final HasElement content) {
		RouterLayout.super.showRouterLayoutContent(content);
		this.getElement().appendChild(content.getElement());
	}

	private void btnView1_onClick(final ClickEvent<Button> event) {
		UI.getCurrent().navigate(DiscordAppConfiguration.class);
	}

	private void btnView2_onClick(final ClickEvent<Button> event) {
		UI.getCurrent().navigate(TwitchConfigurationView.class);
	}

	private void btnView3_onClick(final ClickEvent<Button> event) {
		UI.getCurrent().navigate(RoleConfigurationView.class);
	}

	private void btnView4_onClick(final ClickEvent<Button> event) {
		UI.getCurrent().navigate(BlacklistView.class);
	}

	private void initUI() {
		this.navMenu = new VerticalLayout();
		this.btnView1 = new Button();
		this.btnView2 = new Button();
		this.btnView3 = new Button();
		this.btnView4 = new Button();

		this.setClassName("my-category");
		this.setMinHeight("0");
		this.setSpacing(false);
		this.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.STRETCH);
		this.navMenu.setClassName("my-menu");
		this.navMenu.setMinHeight("0");
		this.navMenu.setSpacing(false);
		this.navMenu.setMaxWidth("300px");
		this.navMenu.setPadding(false);
		this.navMenu.setMinWidth("300px");
		this.navMenu.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
		this.navMenu.getStyle().set("overflow-x", "hidden");
		this.navMenu.getStyle().set("overflow-y", "auto");
		this.btnView1.setClassName("my-navbutton");
		this.btnView1.setText("Discord-App");
		this.btnView2.setClassName("my-navbutton");
		this.btnView2.setText("Twitch-App");
		this.btnView3.setClassName("my-navbutton");
		this.btnView3.setText("Rollen");

		this.btnView4.setClassName("my-navbutton");
		this.btnView4.setText("Blacklist");

		// TODO missing:
		// 1. role-transitions
		// 2. initial roles
		// 3. mute role
		// 4. developer accounts

		this.btnView1.setSizeUndefined();
		this.btnView2.setSizeUndefined();
		this.btnView3.setSizeUndefined();
		this.btnView4.setSizeUndefined();
		this.navMenu.add(this.btnView1, this.btnView2, this.btnView3, this.btnView4);
		this.navMenu.setSizeUndefined();
		this.add(this.navMenu);
		this.setSizeUndefined();

		this.btnView1.addClickListener(this::btnView1_onClick);
		this.btnView2.addClickListener(this::btnView2_onClick);
		this.btnView3.addClickListener(this::btnView3_onClick);
		this.btnView4.addClickListener(this::btnView4_onClick);
	}

}
