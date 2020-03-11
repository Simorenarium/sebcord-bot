
package coffee.michel.sebcord.ui.configuration;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import coffee.michel.sebcord.ui.Permissions;
import net.dv8tion.jda.api.Permission;

@Permissions({ Permission.ADMINISTRATOR })
@Route(value = "overview", layout = ConfigurationMainContainer.class)
public class ConfigurationMain extends VerticalLayout {
	private static final long	serialVersionUID	= -4457785277499235687L;
	private Label				lblContent;

	public ConfigurationMain() {
		super();
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		this.lblContent = new Label();

		this.setClassName("my-view my-startview ");
		this.lblContent.setText("Einstellungen");
		this.lblContent.getStyle().set("font-size", "5em");

		this.lblContent.setSizeUndefined();
		this.add(this.lblContent);
		this.setSizeFull();
	}

}
