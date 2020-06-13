package coffee.michel.sebcord.ui.components;

import com.vaadin.flow.component.listbox.ListBox;

import net.dv8tion.jda.api.entities.Member;

public class MemberListBox extends ListBox<Member> {
	private static final long serialVersionUID = -2366749693015353080L;

	public MemberListBox() {
		setRenderer(new MemberComboBox.MemberRenderer());
	}

}
