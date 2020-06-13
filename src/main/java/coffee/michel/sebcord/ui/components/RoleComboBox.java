package coffee.michel.sebcord.ui.components;

import java.awt.Color;
import java.util.Collection;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.TextRenderer;

import net.dv8tion.jda.api.entities.Role;

public class RoleComboBox extends ComboBox<Role> {
	private static final long serialVersionUID = -4295333045183105087L;

	public RoleComboBox(Collection<Role> data) {
		setDataProvider(new ListDataProvider<>(data));
		setItemLabelGenerator(role -> role.getName());
		setRenderer(new RoleItemRenderer());
	}

	public static class RoleItemRenderer extends TextRenderer<Role> {
		private static final long serialVersionUID = -3105914437191668451L;

		@Override
		public Component createComponent(Role item) {
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
