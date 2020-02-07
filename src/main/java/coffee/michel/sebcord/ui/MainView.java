
package coffee.michel.sebcord.ui;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainContainer.class)
public class MainView extends VerticalLayout {
	private static final long serialVersionUID = -4818883590469559192L;
	private Label             lblContent;

	public MainView() {
		super();
		this.initUI();
	}

	private void initUI() {
		this.lblContent = new Label();

		this.lblContent.setText("Sebcord-Bot");
		this.lblContent.getStyle().set("font-size", "5em");

		this.lblContent.setSizeUndefined();
		this.add(this.lblContent);
		this.setSizeFull();
	}

}
