
package coffee.michel.sebcord.bot.ui.first;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = ConfigurationMainContainer.class)
public class ConfigurationMain extends VerticalLayout {
	private static final long serialVersionUID = -4457785277499235687L;
	private Label             lblContent;

	public ConfigurationMain() {
		super();
		this.initUI();
	}

	private void initUI() {
		this.lblContent = new Label();

		this.setClassName("my-view my-startview ");
		this.lblContent.setText("Einstellungen");
		this.lblContent.getStyle().set("font-size", "5em");

		this.lblContent.setSizeUndefined();
		this.add(this.lblContent);
		this.setSizeFull();
	}

}
