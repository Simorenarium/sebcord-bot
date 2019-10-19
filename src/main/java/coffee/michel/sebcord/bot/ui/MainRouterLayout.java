/*
 * Erstellt am: 18 Oct 2019 23:21:52
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.ui;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import coffee.michel.sebcord.bot.core.permission.AuthorizationManager;
import coffee.michel.sebcord.bot.ui.menu.PageContainer;
import coffee.michel.sebcord.bot.ui.menu.UIFeature;

/**
 * @author Jonas Michel
 *
 */
@Route("")
@Theme(value = Lumo.class, variant = Lumo.DARK)
public class MainRouterLayout extends HorizontalLayout implements RouterLayout, BeforeEnterObserver {
	private static final long serialVersionUID = -8601586731968270221L;

	private AuthorizationManager authMgr;
	private PageContainer pageContainer;

	private MenuBar menuBar;

	@Inject
	public MainRouterLayout(AuthorizationManager authMgr,
							PageContainer pageContainer) {
		this.authMgr = authMgr;
		this.pageContainer = pageContainer;
		menuBar = new MenuBar();
		menuBar.getStyle().set("border-right", "1px solid hsl(214, 34%, 34%)");
		Set<String> currentPermissions = authMgr.getPermissions();
		buildMenu(currentPermissions, menuBar, pageContainer.getPages(currentPermissions));

		add(menuBar);
		setHeightFull();
	}

	private void buildMenu(Set<String> currentPermissions, HasMenuItems parentItem, List<UIFeature> pages) {
		for (UIFeature page : pages) {
			@SuppressWarnings("unchecked")
			MenuItem item = parentItem.addItem(page.getName(), ce -> UI.getCurrent().navigate((Class<? extends Component>) page.getUIClass()));
			buildMenu(currentPermissions, item.getSubMenu(), page.getChildFeatures(currentPermissions));
		}
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		if (!authMgr.isAuthorized())
			event.rerouteTo(AuthorizationPage.class);

		Optional<UIFeature> optFeature = pageContainer.getPages().stream().filter(p -> p.getUIClass() == event.getNavigationTarget()).findAny();
		if (!optFeature.isPresent())
			return;
		UIFeature uiFeature = optFeature.get();
		Set<String> currentPermissions = authMgr.getPermissions();
		boolean allRequirementsMet = currentPermissions.containsAll(uiFeature.getPermissions());
		if (!allRequirementsMet)
			event.rerouteTo(UnauthorizedPage.class);
	}

}
