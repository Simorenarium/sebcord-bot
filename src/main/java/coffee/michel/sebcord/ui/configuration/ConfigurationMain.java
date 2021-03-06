
package coffee.michel.sebcord.ui.configuration;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.Route;

import ch.carnet.kasparscherrer.VerticalScrollLayout;
import coffee.michel.sebcord.ui.api.ParentContainer;
import coffee.michel.sebcord.ui.api.SebcordUIPage.BaseUIPage;
import net.dv8tion.jda.api.Permission;

@Route(value = "overview", layout = ConfigurationMainContainer.class)
public class ConfigurationMain extends VerticalScrollLayout {
	private static final long serialVersionUID = -4457785277499235687L;

	@Component
	@ParentContainer("MainContainer")
	public static class ConfigurationMainPage extends BaseUIPage {

		public ConfigurationMainPage() {
			super(0, "Einstellungen", VaadinIcon.COG, ConfigurationMain.class);
		}

		@Override
		public boolean matchesPermissions(Collection<String> permissions) {
			return permissions.contains(Permission.ADMINISTRATOR.toString());
		}

	}

	private Label lblContent;

	@PostConstruct
	public void init() {
		this.lblContent = new Label();

		this.setClassName("my-view my-startview ");
		this.lblContent.setText("Einstellungen");
		this.lblContent.getStyle().set("font-size", "5em");

		this.lblContent.setSizeUndefined();
		this.add(this.lblContent);
		this.setSizeFull();
	}

}
