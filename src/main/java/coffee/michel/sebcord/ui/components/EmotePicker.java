package coffee.michel.sebcord.ui.components;

import com.vaadin.flow.component.combobox.ComboBox;

import net.dv8tion.jda.api.entities.Guild;

public class EmotePicker extends ComboBox<String> {
	private static final long serialVersionUID = 5993802868304971025L;

	public EmotePicker(Guild guild) {
		guild.getEmotes();
	}
	
}
