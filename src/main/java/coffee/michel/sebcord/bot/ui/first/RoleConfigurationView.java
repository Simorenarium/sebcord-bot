
package coffee.michel.sebcord.bot.ui.first;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.CDI;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;

import coffee.michel.sebcord.bot.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.bot.configuration.persistence.SebcordBot;
import coffee.michel.sebcord.bot.configuration.persistence.SebcordBot.RoleTransition;
import coffee.michel.sebcord.bot.configuration.persistence.SebcordBot.RoleTransition.RoleAction;

@Route(value = "roles", layout = ConfigurationMainContainer.class)
public class RoleConfigurationView extends VerticalLayout {
	private static final long serialVersionUID = 6152683733129390841L;

	private ConfigurationPersistenceManager persistence;

	public RoleConfigurationView() {
		super();
		persistence = CDI.current().select(ConfigurationPersistenceManager.class).get();
		this.initUI();
	}

	private void initUI() {
		SebcordBot botConfig = persistence.getBotConfig();

		H3 muteRoleHeader = new H3("Mute Rolle");
		TextField muteRoleField = new TextField("Rollen ID");
		muteRoleField.setValue(botConfig.getMuteRoleId() + "");
		add(muteRoleHeader, muteRoleField);

		H3 initialRoleHeader = new H3("Initiale Rollen");
		HorizontalLayout initRoleLayout = new HorizontalLayout();
		TextField initRoleField = new TextField("Rollen ID");
		ListBox<Long> ids = new ListBox<>();
		ids.setItems(botConfig.getInitialRoles());
		Button addInitRoleButton = new Button("Hinzufügen");
		addInitRoleButton.addClickListener(ce -> {
			String value = initRoleField.getValue();
			if (value == null || value.isEmpty())
				return;
			Long roleId = Long.valueOf(value);
			botConfig.getInitialRoles().add(roleId);
			ids.setItems(botConfig.getInitialRoles());
		});
		Button removeInitRoleButton = new Button("Entfernen");
		removeInitRoleButton.addClickListener(ce -> ids.getOptionalValue().ifPresent(val -> {
			botConfig.getInitialRoles().remove(val);
			ids.setItems(botConfig.getInitialRoles());
		}));
		initRoleLayout.add(initRoleField, addInitRoleButton, removeInitRoleButton);

		add(initialRoleHeader, initRoleLayout, ids);

		H3 roleTransitionHeader = new H3("Rollen Übergänge");
		VerticalLayout transitionItems = new VerticalLayout();
		Button button = new Button("Hinzufügen");
		button.addClickListener(ce -> transitionItems.addComponentAsFirst(new TransitionEntry()));
		botConfig.getRoleTransitions().forEach(rt -> transitionItems.add(new TransitionEntry(rt)));
		transitionItems.add(button);

		add(roleTransitionHeader, transitionItems);

		Button saveButton = new Button("Speichern");
		saveButton.addClickListener(ce -> {
			botConfig.getInitialRoles().forEach(persistence::persist);
			List<RoleTransition> transitions = transitionItems.getChildren().filter(TransitionEntry.class::isInstance).map(TransitionEntry.class::cast).map(TransitionEntry::get).collect(Collectors.toList());
			botConfig.setRoleTransitions(transitions);
			botConfig.setMuteRoleId(Optional.ofNullable(muteRoleField.getValue()).filter(s -> !s.isEmpty()).map(Long::valueOf).orElse(0L));
			persistence.persist(botConfig);
			persistence.persist(botConfig, botConfig.getInitialRoles());
			persistence.persist(botConfig, botConfig.getRoleTransitions());
		});

		add(saveButton);
		this.setSizeFull();
	}

	static enum AddOrRemove {
		ADD("hinzugefügt", true),
		REMOVE("entfernt", false);

		private String  caption;
		private boolean action;

		private AddOrRemove(String test, boolean action) {
			this.caption = test;
			this.action = action;
		}

		@Override
		public String toString() {
			return caption;
		}

	}

	static class TransitionEntry extends HorizontalLayout {
		private static final long     serialVersionUID = -1076545191858676142L;
		private TextField             role1Id;
		private ComboBox<AddOrRemove> role1AddOrRemove;
		private TextField             role2Id;
		private ComboBox<AddOrRemove> role2AddOrRemove;

		public TransitionEntry() {
			this(new RoleTransition());
		}

		public TransitionEntry(RoleTransition rt) {
			add(new Label("Wenn die Rolle "));
			var triggerAction = rt.getTriggerAction();
			var actionToApply = rt.getActionToApply();

			role1Id = new TextField("", "4589353859");
			role1Id.setValue("" + triggerAction.getRoleId());
			add(role1Id);
			role1AddOrRemove = new ComboBox<>("", AddOrRemove.ADD, AddOrRemove.REMOVE);
			role1AddOrRemove.setValue(triggerAction.isAdd() ? AddOrRemove.ADD : AddOrRemove.REMOVE);
			role1AddOrRemove.setRenderer(new TextRenderer<>(bean -> bean.caption));
			add(role1AddOrRemove);
			add(new Label("wird, wird die Rolle "));
			role2Id = new TextField("", "4589353859");
			role2Id.setValue("" + actionToApply.getRoleId());
			add(role2Id);
			role2AddOrRemove = new ComboBox<>("", AddOrRemove.ADD, AddOrRemove.REMOVE);
			role2AddOrRemove.setValue(actionToApply.isAdd() ? AddOrRemove.ADD : AddOrRemove.REMOVE);
			role2AddOrRemove.setRenderer(new TextRenderer<>(bean -> bean.caption));
			add(role2AddOrRemove);

			Button button = new Button(VaadinIcon.MINUS.create());
			button.addClickListener(ce -> getParent().ifPresent(p -> ((HasComponents) p).remove(this)));
			add(button);
		}

		RoleTransition get() {
			String value = role1Id.getValue();
			if (value == null || value.isEmpty())
				return null;
			long role1Id = Long.valueOf(value);

			AddOrRemove value2 = this.role1AddOrRemove.getValue();
			if (value2 == null)
				return null;
			boolean role1AddOrRemove = value2.action;

			value = role2Id.getValue();
			if (value == null || value.isEmpty())
				return null;
			long role2Id = Long.valueOf(value);

			value2 = this.role2AddOrRemove.getValue();
			if (value2 == null)
				return null;
			boolean role2AddOrRemove = value2.action;

			RoleTransition roleTransition = new RoleTransition();
			RoleAction roleAction = new RoleAction();
			roleAction.setAdd(role1AddOrRemove);
			roleAction.setRoleId(role1Id);
			roleTransition.setTriggerAction(roleAction);
			roleAction = new RoleAction();
			roleAction.setAdd(role2AddOrRemove);
			roleAction.setRoleId(role2Id);
			roleTransition.setActionToApply(roleAction);
			return roleTransition;
		}

	}

}
