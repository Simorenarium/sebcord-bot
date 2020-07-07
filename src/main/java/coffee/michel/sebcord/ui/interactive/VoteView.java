
package coffee.michel.sebcord.ui.interactive;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.Route;

import ch.carnet.kasparscherrer.VerticalScrollLayout;
import coffee.michel.sebcord.bot.core.JDADCClient;
import coffee.michel.sebcord.persistence.PersistenceManager;
import coffee.michel.sebcord.persistence.Vote;
import coffee.michel.sebcord.persistence.Vote.Option;
import coffee.michel.sebcord.ui.api.ParentContainer;
import coffee.michel.sebcord.ui.api.SebcordUIPage.BaseUIPage;
import coffee.michel.sebcord.ui.authentication.DiscordAuthentication;
import coffee.michel.sebcord.ui.authentication.SecurityUtils;
import coffee.michel.sebcord.ui.components.ChannelComboBox;
import coffee.michel.sebcord.ui.components.DateTimeField;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

@Route(value = "vote", layout = InteractiveMainContainer.class)
public class VoteView extends VerticalScrollLayout {
	private static final long serialVersionUID = -2653637655434962690L;

	private enum OptionPresets {
		// let the user choose one of these
		// should be enough

		// after choice, also has choice which options to enable (via checkbox)
		NORMAL_TEN("0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣"), UP_DOWN("⬇️", "⬆️"),
		LIKE_DISLIKE("👍", "👎");

		private String[] args;

		private OptionPresets(String... args) {
			this.args = args;

		}
	}

	@Component
	@ParentContainer("InteractiveMainContainer")
	public static class MemeCommandPage extends BaseUIPage {

		public MemeCommandPage() {
			super(0, "Votes", VoteView.class);
		}

		@Override
		public boolean matchesPermissions(Collection<String> permissions) {
			return permissions.contains(Permission.MESSAGE_WRITE.toString());
		}

	}

	@Autowired
	private PersistenceManager pm;
	@Autowired
	private JDADCClient client;

	@PostConstruct
	public void init() {
		setSizeFull();

		if (!SecurityUtils.isUserLoggedIn())
			return;

		var auth = (DiscordAuthentication) SecurityContextHolder.getContext().getAuthentication();
		var member = auth.getMember();
		List<GuildChannel> allChannels = client.getGuild().getChannels().stream()
				.filter(ch -> ch instanceof TextChannel).collect(Collectors.toList());
		List<GuildChannel> memberVisibleChannels = client.getGuild().getChannels().stream()
				.filter(ch -> ch.getMembers().contains(member)).collect(Collectors.toList());
		Map<Long, String> namesForChannels = allChannels.stream()
				.collect(Collectors.toMap(GuildChannel::getIdLong, GuildChannel::getName));

		List<Vote> memberVotes = pm.getVotes(member.getIdLong());
		for (Vote vote : memberVotes) {
			add(new UIVote(memberVotes, vote, namesForChannels.get(vote.getChannelId()), false));
		}

		Button save = new Button("Speichern");
		save.addClickListener(e -> {
			for (Vote vote : memberVotes) {
				if (vote.getStart().isAfter(vote.getTimeout())) {
					Notification.show("Das Start Datum muss vor dem Ende liegen.", 10000, Position.MIDDLE);
					return;
				}

			}
			pm.persist(memberVotes);
		});

		add(addAddVoteOption(member, save, memberVotes, memberVisibleChannels));

		add(save);
	}

	private com.vaadin.flow.component.Component addAddVoteOption(Member member, Button save, List<Vote> memberVotes,
			List<GuildChannel> memberVisibleChannels) {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setAlignItems(Alignment.END);
		ChannelComboBox channelSelect = new ChannelComboBox(memberVisibleChannels);
		channelSelect.setLabel("Kanal");
		Button addVote = new Button("Vote erstellen");
		channelSelect.addValueChangeListener(e -> addVote.setEnabled(e.getValue() != null));
		hl.add(channelSelect);
		hl.add(addVote);

		addVote.addClickListener(e -> {
			Vote vote = new Vote();
			vote.setId(new Random().nextLong());
			vote.setAuthor(member.getIdLong());
			GuildChannel channel = channelSelect.getValue();
			vote.setChannelId(channel.getIdLong());
			memberVotes.add(vote);
			UIVote component = new UIVote(memberVotes, vote, channel.getName(), true);
			remove(hl);
			remove(save);
			add(component);
			channelSelect.setValue(null);
			add(addAddVoteOption(member, save, memberVotes, memberVisibleChannels));
			add(save);
		});
		return hl;
	}

	private class UIVote extends VerticalLayout {
		private static final long serialVersionUID = -2048287895401397092L;

		private Vote vote;

		private TextField channelCb;
		private TextField title;
		private TextArea description;
		private DateTimeField start;
		private DateTimeField timeout;

		private String channelName;

		private List<Vote> allVotes;

		public UIVote(List<Vote> allVotes, Vote vote, String channelName, boolean renderEditable) {
			this.allVotes = allVotes;
			this.vote = vote;
			this.channelName = channelName;
			super.getStyle().set("border-top", "1px solid var(--lumo-contrast-60pct)");
			super.setSpacing(true);

			renderEditable(renderEditable);
		}

		public void renderEditable(boolean enableEdit) {
			channelCb = new TextField("Kanal");
			channelCb.setValue(channelName);
			channelCb.setReadOnly(true);
			title = editableTitleField(enableEdit, vote::getTitle, vote::setTitle);
			HorizontalLayout horizontalLayout = new HorizontalLayout(channelCb, title);
			horizontalLayout.setSpacing(true);
			horizontalLayout.setWidthFull();
			add(horizontalLayout);

			List<Option> options = vote.getOptions();
			VerticalLayout optionsLayout = new VerticalLayout();
			if (options.isEmpty()) {
				HorizontalLayout presetLayout = new HorizontalLayout();
				presetLayout.add(createPresetBtn(vote, OptionPresets.LIKE_DISLIKE, presetLayout, optionsLayout));
				presetLayout.add(createPresetBtn(vote, OptionPresets.UP_DOWN, presetLayout, optionsLayout));
				presetLayout.add(createPresetBtn(vote, OptionPresets.NORMAL_TEN, presetLayout, optionsLayout));
				optionsLayout.add(presetLayout);
			} else {
				for (Option option : options) {
					optionsLayout.add(new UIOption(optionsLayout, vote.getOptions(), option, true));
				}
			}
			add(optionsLayout);

			add(description = editableDescription(enableEdit, vote::getDescription, vote::setDescription));
			add(start = editableStartField(LocalDateTime.now(), enableEdit, vote::getStart, vote::setStart));
			add(timeout = editableTimeoutField(LocalDateTime.now().plusHours(12), enableEdit, vote::getTimeout,
					vote::setTimeout));

			HorizontalLayout modButtons = new HorizontalLayout();
			modButtons.setSpacing(true);

			Button editBtn = new Button("Bearbeiten");
			Button editEndBtn = new Button("Bearbeiten Beenden");
			editBtn.addClickListener(e -> {
				setReadonly(false);
				editBtn.setVisible(false);
				editEndBtn.setVisible(true);
			});
			modButtons.add(editBtn);
			editEndBtn.addClickListener(e -> {
				setReadonly(true);
				editBtn.setVisible(true);
				editEndBtn.setVisible(false);
			});
			modButtons.add(editEndBtn);
			Button deleteBtn = new Button("Löschen");
			deleteBtn.addClickListener(e -> {
				allVotes.remove(vote);
				VoteView.this.remove(UIVote.this);
			});
			deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
			modButtons.add(deleteBtn);

			editBtn.setVisible(!enableEdit);
			editEndBtn.setVisible(enableEdit);

			add(modButtons);
		}

		private com.vaadin.flow.component.Component createPresetBtn(Vote vote, OptionPresets preset,
				HorizontalLayout presetLayout, VerticalLayout optionsLayout) {
			Button btn = new Button(Arrays.stream(preset.args).collect(Collectors.joining(" ")));
			btn.addClickListener(ce -> {
				optionsLayout.remove(presetLayout);

				for (var str : preset.args) {
					Option opt = new Vote.Option();
					vote.getOptions().add(opt);
					opt.setUnicode(str);

					optionsLayout.add(new UIOption(optionsLayout, vote.getOptions(), opt, false));
				}
			});
			return btn;
		}

		private TextField editableTitleField(boolean enableEdit, Supplier<String> getter, Consumer<String> setter) {
			TextField tf = new TextField("Title");
			var val = getter.get();
			tf.setValue(val == null ? "Überschrift" : val);
			tf.addValueChangeListener(e -> setter.accept(e.getValue()));
			setter.accept(tf.getValue());
			tf.setReadOnly(!enableEdit);
			return tf;
		}

		private TextArea editableDescription(boolean enableEdit, Supplier<String> getter, Consumer<String> setter) {
			TextArea tf = new TextArea("Beschreibung");
			var val = getter.get();
			tf.setValue(val == null ? "Dies ist ein Votum." : val);
			tf.addValueChangeListener(e -> setter.accept(e.getValue()));
			setter.accept(tf.getValue());
			tf.setReadOnly(!enableEdit);
			tf.setWidthFull();
			return tf;
		}

		private DateTimeField editableStartField(LocalDateTime defVal, boolean enableEdit,
				Supplier<LocalDateTime> getter, Consumer<LocalDateTime> setter) {
			var picker = new DateTimeField();
			picker.setLabel("Start");
			picker.setWidthFull();
			var val = getter.get();
			LocalDateTime value = val == null ? defVal : val;
			picker.setValue(value);
			setter.accept(picker.getValue());
			picker.addValueChangeListener(e -> setter.accept(e.getValue()));
			picker.setReadonly(!enableEdit);
			return picker;
		}

		private DateTimeField editableTimeoutField(LocalDateTime defVal, boolean enableEdit,
				Supplier<LocalDateTime> getter, Consumer<LocalDateTime> setter) {
			var picker = new DateTimeField();
			picker.setLabel("Ende");
			picker.setWidthFull();
			var val = getter.get();
			LocalDateTime value = val == null ? defVal : val;
			picker.setValue(value);
			setter.accept(picker.getValue());
			picker.addValueChangeListener(e -> setter.accept(e.getValue()));
			picker.setReadonly(!enableEdit);
			return picker;
		}

		public void setReadonly(boolean readonly) {
			title.setReadOnly(readonly);
			description.setReadOnly(readonly);
			start.setReadonly(readonly);
			timeout.setReadonly(readonly);
		}

		private class UIOption extends HorizontalLayout {
			private static final long serialVersionUID = -5652404280179927159L;

			private TextField optionDescription;
			private Upload attachment;

			private OptionAttachmentReceiver receiver;

			public UIOption(VerticalLayout parent, List<Option> allOptions, Vote.Option option,
					boolean alreadyPersistent) {
				setAlignItems(Alignment.CENTER);
				setSpacing(true);

				Label emote = new Label(option.getUnicode());
				add(emote);

				optionDescription = new TextField(null,"Beschreibung der Option");
				if(option.getName() != null)
					optionDescription.setValue(option.getName());
				optionDescription.setWidth("20em");
				optionDescription.addValueChangeListener(vce -> option.setName(vce.getValue()));
				add(optionDescription);

				if (!alreadyPersistent) {
					receiver = new OptionAttachmentReceiver();
					attachment = new Upload(receiver);
					attachment.setMaxFiles(1);
					attachment.setAutoUpload(true);
					attachment.addSucceededListener(event -> {
						option.setAttachmentData(new String(receiver.getByteArrayOutputStream().toByteArray()));
						option.setAttachmentName(receiver.getFileName());
					});
					add(attachment);
				}

				Button rem = new Button("Löschen");
				rem.addThemeVariants(ButtonVariant.LUMO_ERROR);
				rem.addClickListener(ce -> {
					allOptions.remove(option);
					parent.remove(this);
				});
				add(rem);
			}

		}
	}

	private static class OptionAttachmentReceiver implements Receiver {
		private static final long serialVersionUID = 970470674394044697L;

		private String fileName;

		private ByteArrayOutputStream byteArrayOutputStream;

		public OptionAttachmentReceiver() {
		}

		public String getFileName() {
			return fileName;
		}

		public ByteArrayOutputStream getByteArrayOutputStream() {
			return byteArrayOutputStream;
		}

		@Override
		public OutputStream receiveUpload(String fileName, String mimeType) {
			this.fileName = fileName;
			return byteArrayOutputStream = new ByteArrayOutputStream();
		}

	}

}
