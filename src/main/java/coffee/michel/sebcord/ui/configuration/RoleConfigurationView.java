
package coffee.michel.sebcord.ui.configuration;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;

import ch.carnet.kasparscherrer.VerticalScrollLayout;
import coffee.michel.sebcord.bot.core.JDADCClient;
import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.configuration.persistence.SebcordBot;
import coffee.michel.sebcord.configuration.persistence.SebcordBot.RoleTransition;
import coffee.michel.sebcord.ui.api.ParentContainer;
import coffee.michel.sebcord.ui.api.SebcordUIPage.BaseUIPage;
import coffee.michel.sebcord.ui.components.EditableGrid;
import coffee.michel.sebcord.ui.components.MemberComboBox;
import coffee.michel.sebcord.ui.components.MemberListBox;
import coffee.michel.sebcord.ui.components.RoleComboBox;
import coffee.michel.sebcord.ui.components.RoleListBox;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

@Route(value = "roles", layout = ConfigurationMainContainer.class)
public class RoleConfigurationView extends VerticalScrollLayout {
	private static final long serialVersionUID = 6152683733129390841L;

	@Component
	@ParentContainer("ConfigurationMainContainer")
	public static class RoleConfigurationPage extends BaseUIPage {

		public RoleConfigurationPage() {
			super(2, "Rollen", RoleConfigurationView.class);
		}

		@Override
		public boolean matchesPermissions(Collection<String> permissions) {
			return permissions.contains(Permission.ADMINISTRATOR.toString());
		}

	}

	@Autowired
	private ConfigurationPersistenceManager	persistence;
	@Autowired
	private JDADCClient						client;

	@PostConstruct
	public void init() {
		removeAll();
		SebcordBot botConfig = persistence.getBotConfig();

		List<Member> members = client.getGuild().getMembers();
		Map<Long, Member> mappedMembers = members.stream()
				.collect(Collectors.toMap(Member::getIdLong, Function.identity()));
		List<Role> roles = client.getGuild().getRoles();
		Map<Long, Role> mappedRoles = roles.stream().collect(Collectors.toMap(Role::getIdLong, Function.identity()));

		H3 muteRoleHeader = new H3("Mute Rolle");
		RoleComboBox muteRoleField = new RoleComboBox(roles);
		Optional<Role> configuredRole = roles.stream().filter(r -> r.getIdLong() == botConfig.getMuteRoleId())
				.findAny();
		configuredRole.ifPresent(muteRoleField::setValue);
		add(muteRoleHeader, muteRoleField);

		H3 initialRoleHeader = new H3("Initiale Rollen");
		HorizontalLayout initRoleLayout = new HorizontalLayout();
		RoleComboBox initRoleField = new RoleComboBox(roles);
		RoleListBox ids = new RoleListBox();
		ids.setItems(botConfig.getInitialRoles().stream().map(mappedRoles::get).collect(Collectors.toList()));
		Button addInitRoleButton = new Button("Hinzufügen");
		addInitRoleButton.addClickListener(ce -> {
			Role value = initRoleField.getValue();
			if (value == null)
				return;
			botConfig.getInitialRoles().add(value.getIdLong());
			ids.setItems(botConfig.getInitialRoles().stream().map(mappedRoles::get).collect(Collectors.toList()));
		});
		Button removeInitRoleButton = new Button("Entfernen");
		removeInitRoleButton.addClickListener(ce -> ids.getOptionalValue().ifPresent(val -> {
			botConfig.getInitialRoles().remove(val.getIdLong());
			ids.setItems(botConfig.getInitialRoles().stream().map(mappedRoles::get).collect(Collectors.toList()));
		}));
		initRoleLayout.add(initRoleField, addInitRoleButton, removeInitRoleButton);
		add(initialRoleHeader, initRoleLayout, ids);

		H3 devUserHeader = new H3("Developer IDs");
		HorizontalLayout devUserLayout = new HorizontalLayout();
		MemberComboBox devUserField = new MemberComboBox(members);
		MemberListBox devIds = new MemberListBox();
		devIds.setItems(botConfig.getDeveloperIds().stream().map(mappedMembers::get).collect(Collectors.toList()));
		Button addDevUserButton = new Button("Hinzufügen");
		addDevUserButton.addClickListener(ce -> {
			Member value = devUserField.getValue();
			if (value == null)
				return;
			botConfig.getDeveloperIds().add(value.getIdLong());
			devIds.setItems(botConfig.getDeveloperIds().stream().map(mappedMembers::get).collect(Collectors.toList()));
		});
		Button removeDevUserButton = new Button("Entfernen");
		removeDevUserButton.addClickListener(ce -> devIds.getOptionalValue().ifPresent(val -> {
			botConfig.getDeveloperIds().remove(val.getIdLong());
			devIds.setItems(botConfig.getDeveloperIds().stream().map(mappedMembers::get).collect(Collectors.toList()));
		}));
		devUserLayout.add(devUserField, addDevUserButton, removeDevUserButton);
		add(devUserHeader, devUserLayout, devIds);

		H3 roleTransitionHeader = new H3("Rollen Übergänge");

		EditableGrid<RoleTransition> grid = new EditableGrid<>(RoleTransition.class);
		grid.setMinHeight("10em");
		grid.removeAllColumns();

		Binder<RoleTransition> trBinder = new Binder<>(RoleTransition.class);
		RoleComboBox triggerRoleField = new RoleComboBox(roles);
		trBinder.bind(triggerRoleField, tr -> mappedRoles.get(tr.getTriggerAction().getRoleId()),
				(tr, vf) -> tr.getTriggerAction().setRoleId(vf.getIdLong()));
		Checkbox triggerRoleAdd = new Checkbox();
		trBinder.bind(triggerRoleAdd, "triggerAction.add");
		RoleComboBox actionToApplyRole = new RoleComboBox(roles);
		trBinder.bind(actionToApplyRole, tr -> mappedRoles.get(tr.getActionToApply().getRoleId()),
				(tr, vf) -> tr.getActionToApply().setRoleId(vf.getIdLong()));
		Checkbox actionToApplyAdd = new Checkbox();
		trBinder.bind(actionToApplyAdd, "actionToApply.add");
		grid.getEditor().setBinder(trBinder);
		grid.addColumn(new RoleTransitionTriggerRoleRenderer(mappedRoles), "triggerAction.roleId")
				.setHeader("Trigger-Rolle")
				.setEditorComponent(triggerRoleField);
		grid.addColumn(new RoleTransitionTriggerBooleanRenderer(), "triggerAction.add").setHeader("Aktion")
				.setEditorComponent(triggerRoleAdd);
		grid.addColumn(new RoleTransitionActionRoleRenderer(mappedRoles), "actionToApply.roleId")
				.setHeader("Angewandte Rolle")
				.setEditorComponent(actionToApplyRole);
		grid.addColumn(new RoleTransitionActionBooleanRenderer(), "actionToApply.add").setHeader("Aktion")
				.setEditorComponent(actionToApplyAdd);
		grid.setDropMode(GridDropMode.BETWEEN);
		grid.addSelectionListener(sel -> sel.getFirstSelectedItem().ifPresent(grid.getEditor()::editItem));
		grid.getElement().addEventListener("keydown", event -> {
			Editor<RoleTransition> editor = grid.getEditor();
			editor.save();
			editor.closeEditor();
			grid.getDataProvider().refreshAll();
		}).setFilter("event.key === 'Enter'");
		grid.setRowsDraggable(true);

		AtomicReference<RoleTransition> draggedItem = new AtomicReference<>();
		grid.addDragStartListener(event -> {
			draggedItem.set(event.getDraggedItems().get(0));
			grid.setDropMode(GridDropMode.BETWEEN);
		});

		grid.addDragEndListener(event -> {
			draggedItem.set(null);
			grid.setDropMode(null);
		});

		grid.addDropListener(event -> {
			var dropOverItem = event.getDropTargetItem().get();
			if (!dropOverItem.equals(draggedItem.get())) {
				grid.removeItem(draggedItem.get());
				int dropIndex = grid.indexOfItem(dropOverItem)
						+ (event.getDropLocation() == GridDropLocation.BELOW ? 1
								: 0);
				grid.addItem(dropIndex, draggedItem.get());
				grid.getDataProvider().refreshAll();
			}
		});
		botConfig.getRoleTransitions().forEach(grid::addItem);

		Button addItemButton = new Button("Hinzufügen", ce -> grid.addItem(new RoleTransition()));
		Button removeItemButton = new Button("Entfernen", ce -> grid.getSelectedItems().forEach(grid::removeItem));

		add(roleTransitionHeader, grid, new HorizontalLayout(addItemButton, removeItemButton));

		Button saveButton = new Button("Speichern");
		saveButton.addClickListener(ce -> {
			botConfig.getInitialRoles().forEach(persistence::persist);
			List<RoleTransition> transitions = grid.getItems();
			botConfig.setRoleTransitions(transitions);
			botConfig.setMuteRoleId(Optional.ofNullable(muteRoleField.getValue()).filter(Objects::nonNull)
					.map(Role::getIdLong)
					.map(Long::valueOf).orElse(0L));
			persistence.persist(botConfig);
			persistence.persist(botConfig, botConfig.getDeveloperIds());
			persistence.persist(botConfig, botConfig.getInitialRoles());
			persistence.persist(botConfig, botConfig.getRoleTransitions());
		});

		add(saveButton);
		setWidthFull();
	}

	private static class RoleTransitionTriggerRoleRenderer extends TextRenderer<RoleTransition> {
		private static final long	serialVersionUID	= -5379647957743960953L;
		private Map<Long, Role>		mappedRoles;

		public RoleTransitionTriggerRoleRenderer(Map<Long, Role> mappedRoles) {
			this.mappedRoles = mappedRoles;
		}

		@Override
		public com.vaadin.flow.component.Component createComponent(RoleTransition item) {
			if (item == null)
				return new Label("null");

			Role role = mappedRoles.get(item.getTriggerAction().getRoleId());

			return renderRole(role);
		}

	}

	private static class RoleTransitionActionRoleRenderer extends TextRenderer<RoleTransition> {
		private static final long	serialVersionUID	= 8938674831323060573L;
		private Map<Long, Role>		mappedRoles;

		public RoleTransitionActionRoleRenderer(Map<Long, Role> mappedRoles) {
			this.mappedRoles = mappedRoles;
		}

		@Override
		public com.vaadin.flow.component.Component createComponent(RoleTransition item) {
			if (item == null)
				return new Label("null");

			Role role = mappedRoles.get(item.getActionToApply().getRoleId());

			return renderRole(role);
		}

	}

	private static com.vaadin.flow.component.Component renderRole(Role role) {
		if (role == null)
			return new Label("null");
		Label label = new Label(role.getName());
		Color color = role.getColor();
		if (color != null) {
			label.getStyle().set("color", "rgb(" + color.getRed() + "," + color.getGreen() + ","
					+ color.getBlue() + ")");
		}
		return label;
	}

	private static class RoleTransitionTriggerBooleanRenderer extends TextRenderer<RoleTransition> {
		private static final long serialVersionUID = -8039629890010593115L;

		@Override
		public com.vaadin.flow.component.Component createComponent(RoleTransition item) {
			if (item == null)
				return new Label("null");
			Checkbox checkbox = new Checkbox(item.getTriggerAction().isAdd());
			checkbox.setReadOnly(true);
			return checkbox;
		}
	}

	private static class RoleTransitionActionBooleanRenderer extends TextRenderer<RoleTransition> {
		private static final long serialVersionUID = 5455859066810149324L;

		@Override
		public com.vaadin.flow.component.Component createComponent(RoleTransition item) {
			if (item == null)
				return new Label("null");
			Checkbox checkbox = new Checkbox(item.getActionToApply().isAdd());
			checkbox.setReadOnly(true);
			return checkbox;
		}
	}
}
