package coffee.michel.sebcord.ui.components;

import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.data.renderer.TextRenderer;

import net.dv8tion.jda.api.entities.GuildChannel;

public class ChannelListBox extends ListBox<GuildChannel> {
	private static final long serialVersionUID = -2366749693015353080L;

	public ChannelListBox() {
		setRenderer(new TextRenderer<>(ch -> ch == null ? "null" : ch.getName()));
	}

}
