
package coffee.michel.sebcord.ui.second;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.RoutePrefix;
import com.vaadin.flow.router.RouterLayout;

import coffee.michel.sebcord.ui.MainContainer;

@ParentLayout(MainContainer.class)
@RoutePrefix("commands")
public class CommandsContainer extends HorizontalLayout implements RouterLayout {
	private static final long serialVersionUID = -6143585657824753212L;

	public CommandsContainer() {
		super();

		var navMenu = new VerticalLayout();
		navMenu.add(menuItem("Meme-Command", MemeCommandView.class));

		this.setSpacing(false);
		this.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.STRETCH);
		navMenu.setMinHeight("0");
		navMenu.setSpacing(false);
		navMenu.setMaxWidth("300px");
		navMenu.setPadding(false);
		navMenu.setMinWidth("300px");
		navMenu.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
		navMenu.getStyle().set("overflow-x", "hidden");
		navMenu.getStyle().set("overflow-y", "auto");

		this.add(navMenu);
		this.setSizeUndefined();
	}

	private Component menuItem(String name, Class<? extends Component> view) {
		Button button = new Button(name, ce -> UI.getCurrent().navigate(view));
		button.setSizeUndefined();
		return button;
	}

}
