
package coffee.michel.sebcord.ui.configuration;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

import ch.carnet.kasparscherrer.VerticalScrollLayout;
import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.configuration.persistence.SebcordBot;
import coffee.michel.sebcord.configuration.persistence.SebcordBot.RoleTransition;
import coffee.michel.sebcord.ui.api.ParentContainer;
import coffee.michel.sebcord.ui.api.SebcordUIPage.BaseUIPage;
import coffee.michel.sebcord.ui.components.EditableGrid;
import net.dv8tion.jda.api.Permission;

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
	private ConfigurationPersistenceManager persistence;

	@PostConstruct
	public void init() {
		removeAll();
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

		H3 devUserHeader = new H3("Developer IDs");
		HorizontalLayout devUserLayout = new HorizontalLayout();
		TextField devUserField = new TextField("User ID");
		ListBox<Long> devIds = new ListBox<>();
		devIds.setItems(botConfig.getDeveloperIds());
		Button addDevUserButton = new Button("Hinzufügen");
		addDevUserButton.addClickListener(ce -> {
			String value = devUserField.getValue();
			if (value == null || value.isEmpty())
				return;
			Long roleId = Long.valueOf(value);
			botConfig.getDeveloperIds().add(roleId);
			devIds.setItems(botConfig.getDeveloperIds());
		});
		Button removeDevUserButton = new Button("Entfernen");
		removeDevUserButton.addClickListener(ce -> devIds.getOptionalValue().ifPresent(val -> {
			botConfig.getDeveloperIds().remove(val);
			devIds.setItems(botConfig.getDeveloperIds());
		}));
		devUserLayout.add(devUserField, addDevUserButton, removeDevUserButton);
		add(devUserHeader, devUserLayout, devIds);

		H3 roleTransitionHeader = new H3("Rollen Übergänge");

		EditableGrid<RoleTransition> grid = new EditableGrid<>(RoleTransition.class);
		grid.setMinHeight("10em");
		grid.removeAllColumns();

		Binder<RoleTransition> trBinder = new Binder<>(RoleTransition.class);
		TextField triggerRoleField = new TextField();
		trBinder.bind(triggerRoleField, tr -> String.valueOf(tr.getTriggerAction().getRoleId()),
				(tr, vf) -> tr.getTriggerAction().setRoleId(Long.valueOf(vf)));
		Checkbox triggerRoleAdd = new Checkbox();
		trBinder.bind(triggerRoleAdd, "triggerAction.add");
		TextField actionToApplyRole = new TextField();
		trBinder.bind(actionToApplyRole, tr -> String.valueOf(tr.getActionToApply().getRoleId()),
				(tr, vf) -> tr.getActionToApply().setRoleId(Long.valueOf(vf)));
		Checkbox actionToApplyAdd = new Checkbox();
		trBinder.bind(actionToApplyAdd, "actionToApply.add");
		grid.getEditor().setBinder(trBinder);
		grid.addColumn("triggerAction.roleId").setHeader("Trigger-Rolle")
				.setEditorComponent(triggerRoleField);
		grid.addColumn("triggerAction.add").setHeader("Aktion").setEditorComponent(triggerRoleAdd);
		grid.addColumn("actionToApply.roleId").setHeader("Angewandte Rolle")
				.setEditorComponent(actionToApplyRole);
		grid.addColumn("actionToApply.add").setHeader("Aktion").setEditorComponent(actionToApplyAdd);
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
			botConfig.setMuteRoleId(Optional.ofNullable(muteRoleField.getValue()).filter(s -> !s.isEmpty())
					.map(Long::valueOf).orElse(0L));
			persistence.persist(botConfig);
			persistence.persist(botConfig, botConfig.getDeveloperIds());
			persistence.persist(botConfig, botConfig.getInitialRoles());
			persistence.persist(botConfig, botConfig.getRoleTransitions());
		});

		add(saveButton);
		setWidthFull();
	}

}
