package coffee.michel.sebcord.ui;

import org.springframework.stereotype.Component;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

import coffee.michel.sebcord.ui.authentication.LoginView;
import coffee.michel.sebcord.ui.authentication.SecurityUtils;

@Component
public class ConfigureUIInitListener implements VaadinServiceInitListener {
	private static final long serialVersionUID = -5647968344335818438L;

	@Override
	public void serviceInit(ServiceInitEvent event) {
		event.getSource().addUIInitListener(uiEvent -> {
			final UI ui = uiEvent.getUI();
			ui.addBeforeEnterListener(this::beforeEnter); //
		});
	}

	/**
	 * Reroutes the user if (s)he is not authorized to access the view.
	 *
	 * @param event before navigation event with event details
	 */
	private void beforeEnter(BeforeEnterEvent event) {
		if (!LoginView.class.equals(event.getNavigationTarget()) //
				&& !SecurityUtils.isUserLoggedIn()) {
			event.rerouteTo(LoginView.class);
		}
	}

}
