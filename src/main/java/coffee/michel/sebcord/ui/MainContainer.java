package coffee.michel.sebcord.ui;

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
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import coffee.michel.sebcord.ui.api.ContainerHelper;
import coffee.michel.sebcord.ui.api.ParentContainer;
import coffee.michel.sebcord.ui.api.SebcordUIPage;

@Theme(value = Lumo.class, variant = Lumo.DARK)
public class MainContainer extends VerticalLayout implements RouterLayout, BeforeEnterListener {
	private static final long serialVersionUID = 5473853593L;

	private HorizontalLayout navContainerMain;

	@Autowired
	@ParentContainer("MainContainer")
	public Set<SebcordUIPage> pages;

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
	public void showRouterLayoutContent(final HasElement content) {
		this.getElement().appendChild(content.getElement());
	}

}
