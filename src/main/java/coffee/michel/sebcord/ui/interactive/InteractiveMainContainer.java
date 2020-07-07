
package coffee.michel.sebcord.ui.interactive;

import java.util.Collection;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.RoutePrefix;
import com.vaadin.flow.router.RouterLayout;

import coffee.michel.sebcord.ui.MainContainer;
import coffee.michel.sebcord.ui.api.ContainerHelper;
import coffee.michel.sebcord.ui.api.ParentContainer;
import coffee.michel.sebcord.ui.api.SebcordUIPage;

@ParentLayout(MainContainer.class)
@RoutePrefix("interactive")
public class InteractiveMainContainer extends HorizontalLayout implements RouterLayout {
	private static final long serialVersionUID = -4294741955103500853L;

	@Autowired
	@ParentContainer("InteractiveMainContainer")
	private Set<SebcordUIPage> pages;

	@PostConstruct
	public void init() {
		setHeight("100%");
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

		ContainerHelper.ifAuthorized(() -> {
			Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication()
					.getAuthorities();
			pages.stream().sorted().forEach(page -> ContainerHelper.addMenuItem(navMenu, page, authorities));
		});

		navMenu.setSizeUndefined();
		removeAll();
		add(navMenu);
	}

	@Override
	public void showRouterLayoutContent(final HasElement content) {
		RouterLayout.super.showRouterLayoutContent(content);
		this.getElement().appendChild(content.getElement());
	}
}
