package coffee.michel.sebcord.ui.api;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public interface SebcordUIPage extends Comparable<SebcordUIPage> {

	int wheight();

	@Override
	default int compareTo(SebcordUIPage o) {
		return Integer.compare(wheight(), o.wheight());
	}

	String getName();

	default Icon getIcon() {
		return null;
	}

	Class<? extends Component> getPageClass();

	default boolean matchesPermissions(Collection<String> permissions) {
		return false;
	}

	default boolean matchesAuthority(Collection<? extends GrantedAuthority> authorities) {
		return matchesPermissions(authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()));
	}

	public abstract class BaseUIPage implements SebcordUIPage {

		private final int							wheight;
		private final String						name;
		private final Class<? extends Component>	categoryClass;
		private VaadinIcon							icon;

		public BaseUIPage(int wheight, String name, Class<? extends Component> categoryClass) {
			this.wheight = wheight;
			this.name = name;
			this.categoryClass = categoryClass;
		}

		public BaseUIPage(int wheight, String name, VaadinIcon icon, Class<? extends Component> categoryClass) {
			this(wheight, name, categoryClass);
			this.icon = icon;
		}

		@Override
		public int wheight() {
			return wheight;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Icon getIcon() {
			return icon == null ? SebcordUIPage.super.getIcon() : icon.create();
		}

		@Override
		public Class<? extends Component> getPageClass() {
			return categoryClass;
		}

	}

}
