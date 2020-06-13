
package coffee.michel.sebcord.ui;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.Route;

import ch.carnet.kasparscherrer.VerticalScrollLayout;
import coffee.michel.sebcord.ui.api.ParentContainer;
import coffee.michel.sebcord.ui.api.SebcordUIPage.BaseUIPage;

@Route(value = "", layout = MainContainer.class)
public class MainView extends VerticalScrollLayout {
	private static final long serialVersionUID = -4818883590469559192L;

	@Component
	@ParentContainer("MainContainer")
	public static class MainPage extends BaseUIPage {

		public MainPage() {
			super(-1, "", VaadinIcon.HOME, MainView.class);
		}

	}

	@PostConstruct
	public void init() {
		setHeight("100%");

		Label lblContent = new Label();

		lblContent.setText("Sebcord-Bot");
		lblContent.getStyle().set("font-size", "5em");

		lblContent.setSizeUndefined();
		add(lblContent);
		setSizeFull();
	}

}
