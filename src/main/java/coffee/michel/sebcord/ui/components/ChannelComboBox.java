package coffee.michel.sebcord.ui.components;

import java.util.Collection;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.provider.ListDataProvider;

import net.dv8tion.jda.api.entities.GuildChannel;

public class ChannelComboBox extends ComboBox<GuildChannel> {
	private static final long serialVersionUID = 996220441133968420L;

	public ChannelComboBox(Collection<GuildChannel> allChannels) {
		setDataProvider(new ListDataProvider<>(allChannels));
		setItemLabelGenerator(ch -> ch.getName());
	}

}
