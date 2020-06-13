package coffee.michel.sebcord.ui.components;

import java.awt.Color;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.data.renderer.TextRenderer;

import net.dv8tion.jda.api.entities.Role;

public class RoleListBox extends ListBox<Role> {
	private static final long serialVersionUID = -2366749693015353080L;

	public RoleListBox() {
		setRenderer(new RoleItemRenderer());
	}

	public static class RoleItemRenderer extends TextRenderer<Role> {
		private static final long serialVersionUID = -3105914437191668451L;

		@Override
		public Component createComponent(Role item) {
			if (item == null)
				return new Label("null");
			Label label = new Label(item.getName());
			Color color = item.getColor();
			if (color != null) {
				label.getStyle().set("color", "rgb(" + color.getRed() + "," + item.getColor().getGreen() + ","
						+ item.getColor().getBlue() + ")");
			}
			return label;
		}

	}

}
