
package coffee.michel.sebcord.ui.commands;

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

@Route(value = "overview", layout = CommandContainer.class)
public class CommandOverview extends VerticalScrollLayout {
	private static final long serialVersionUID = 8518324137441769109L;

	@Component
	@ParentContainer("MainContainer")
	public static class CommandOverviewPage extends BaseUIPage {

		public CommandOverviewPage() {
			super(1, "Commands", VaadinIcon.COG, CommandOverview.class);
		}

		@Override
		public boolean matchesPermissions(Collection<String> permissions) {
			return permissions.contains(Permission.MESSAGE_WRITE.toString());
		}

	}

	@PostConstruct
	public void init() {
		this.lblContent = new Label();

		this.lblContent.setText("Commands");
		this.lblContent.getStyle().set("font-size", "5em");

		this.lblContent.setSizeUndefined();
		this.add(this.lblContent);
		this.setSizeFull();
	} // </generated-code>

	// <generated-code name="variables">
	private Label lblContent;
	// </generated-code>

}
