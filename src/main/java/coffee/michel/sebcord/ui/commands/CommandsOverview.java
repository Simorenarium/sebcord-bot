
package coffee.michel.sebcord.ui.commands;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import coffee.michel.sebcord.ui.Permissions;
import net.dv8tion.jda.api.Permission;

@Permissions({ Permission.MESSAGE_WRITE })
@Route(value = "overview", layout = CommandsContainer.class)
public class CommandsOverview extends VerticalLayout {
	private static final long serialVersionUID = 8518324137441769109L;

	public CommandsOverview() {
		super();
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
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
