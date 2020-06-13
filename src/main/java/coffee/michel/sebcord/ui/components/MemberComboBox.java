package coffee.michel.sebcord.ui.components;

import java.awt.Color;
import java.util.Collection;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.TextRenderer;

import net.dv8tion.jda.api.entities.Member;

public class MemberComboBox extends ComboBox<Member> {
	private static final long serialVersionUID = -6061224158635388368L;

	public MemberComboBox(Collection<Member> members) {
		setDataProvider(new ListDataProvider<>(members));
		setItemLabelGenerator(member -> member.getEffectiveName());
		setRenderer(new MemberRenderer());
	}

	public static class MemberRenderer extends TextRenderer<Member> {
		private static final long serialVersionUID = 4539255656650632568L;

		@Override
		public Component createComponent(Member item) {
			Label label = new Label(item.getEffectiveName());
			Color color = item.getColor();
			if (color != null) {
				label.getStyle().set("color", "rgb(" + color.getRed() + "," + item.getColor().getGreen() + ","
						+ item.getColor().getBlue() + ")");
			}
			return label;
		}
	}

}
