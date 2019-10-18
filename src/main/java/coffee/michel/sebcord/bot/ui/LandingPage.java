/*
 * Copyright GEMTEC GmbH 2019
 *
 * Erstellt am: 14 Oct 2019 23:20:27
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.ui;

import javax.annotation.PostConstruct;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/**
 * @author Jonas Michel
 *
 */
@Route
public class LandingPage extends Div {
	private static final long serialVersionUID = -2585297208960680141L;

	@PostConstruct
	public void init() {
		setSizeFull();
		
		TextField tf = new TextField();
		
		tf.setTitle("Auth Key");
		tf.addInputListener(kup -> {
			System.out.println(tf.getValue());
		});
		
		
		add(tf);
	}
	
}
