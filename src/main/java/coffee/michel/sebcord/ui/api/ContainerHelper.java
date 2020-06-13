package coffee.michel.sebcord.ui.api;

import java.util.Collection;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public final class ContainerHelper {

	private ContainerHelper() {
	}

	public static <T extends GrantedAuthority> void addMenuItem(HorizontalLayout navComponent,
			SebcordUIPage page,
			Collection<T> authority) {
		createNavButton(page, authority).ifPresent(navComponent::add);
	}

	public static <T extends GrantedAuthority> void addMenuItem(VerticalLayout navComponent, SebcordUIPage page,
			Collection<T> authority) {
		createNavButton(page, authority).ifPresent(navComponent::add);
	}

	public static <T extends GrantedAuthority> void addMenuItem(HorizontalLayout navComponent,
			SebcordUICategory category,
			Collection<T> authority) {
		createNavButton(category, authority).ifPresent(navComponent::add);
	}

	public static <T extends GrantedAuthority> void addMenuItem(VerticalLayout navComponent, SebcordUICategory category,
			Collection<T> authority) {
		createNavButton(category, authority).ifPresent(navComponent::add);
	}

	private static <T extends GrantedAuthority> Optional<Button> createNavButton(SebcordUIPage page,
			Collection<T> authority) {
		if (page.matchesAuthority(authority)) {
			Button button = new Button(page.getName(), page.getIcon());
			button.addClickListener(ce -> {
				UI.getCurrent().navigate(page.getPageClass());
			});
			return Optional.of(button);
		}
		return Optional.empty();
	}

	private static <T extends GrantedAuthority> Optional<Button> createNavButton(SebcordUICategory category,
			Collection<T> authority) {
		if (category.matchesAuthority(authority)) {
			Button button = new Button(category.getName(), category.getIcon());
			button.addClickListener(ce -> {
				UI.getCurrent().navigate(category.getCategoryClass());
			});
			return Optional.of(button);
		}
		return Optional.empty();
	}

	public static void ifAuthorized(Runnable authorizedAction) {
		Optional.ofNullable(SecurityContextHolder.getContext())
				.map(SecurityContext::getAuthentication)
				.filter(Authentication::isAuthenticated)
				.map(Authentication::getAuthorities)
				.filter(c -> !c.isEmpty())
				.ifPresent(unused -> authorizedAction.run());
	}

	public static void ifAuthorized(String permission, Runnable authorizedAction) {
		Optional.ofNullable(SecurityContextHolder.getContext())
				.map(SecurityContext::getAuthentication)
				.filter(Authentication::isAuthenticated)
				.map(Authentication::getAuthorities)
				.filter(c -> c.stream().filter(authority -> authority.getAuthority().equals(permission)).count() > 0)
				.ifPresent(unused -> authorizedAction.run());
	}

}
