
package coffee.michel.sebcord.ui.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import ch.carnet.kasparscherrer.VerticalScrollLayout;
import coffee.michel.sebcord.Pair;
import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.configuration.persistence.SebcordBot;
import coffee.michel.sebcord.configuration.persistence.SebcordBot.Conversions;
import coffee.michel.sebcord.ui.api.ContainerHelper;
import coffee.michel.sebcord.ui.api.ParentContainer;
import coffee.michel.sebcord.ui.api.SebcordUIPage.BaseUIPage;
import coffee.michel.sebcord.ui.components.EditableGrid;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

@Route(value = "conversions", layout = CommandContainer.class)
public class ConversionCommandView extends VerticalScrollLayout {
	private static final long serialVersionUID = -2653637655434962690L;

	@Component
	@ParentContainer("CommandContainer")
	public static class ConversionCommandPage extends BaseUIPage {

		public ConversionCommandPage() {
			super(1, "Konverter-Command", ConversionCommandView.class);
		}

		@Override
		public boolean matchesPermissions(Collection<String> permissions) {
			return permissions.contains(Permission.MESSAGE_WRITE.toString());
		}

	}

	@Autowired
	private ConfigurationPersistenceManager cpm;

	@PostConstruct
	public void init() {
		setSizeFull();
		SebcordBot botConfig = cpm.getBotConfig();
		Conversions _conversions = botConfig.getConversions();
		Conversions conversions;
		if (_conversions == null) {
			botConfig.setConversions(_conversions = new Conversions());
			cpm.persist(botConfig, _conversions);
		}
		conversions = _conversions;

		add(new H1("Konvertierungs-Command Einstellungen"));

		EditableGrid<Entry> grid = createConversionsGrid();
		makeInlineEditable(grid);
		addItems(conversions, grid);

		ContainerHelper.ifAuthorized(Permission.ADMINISTRATOR.toString(), () -> {
			EditableGrid<Entry> submittedGrid = createConversionsGrid();
			submittedGrid.addColumn(Entry::getSubmittee).setHeader("eingereicht von");
			submittedGrid.setSelectionMode(SelectionMode.MULTI);
			Conversions submittedConversions = botConfig.getSubmittedConversions();
			addItems(submittedConversions, submittedGrid);
			Button delete = new Button("Löschen", ce -> {
				HashSet<Entry> entries = new HashSet<>(submittedGrid.getSelectedItems());
				for (Entry entry : entries) {
					grid.removeItem(entry);
					submittedConversions.getConversionFactors()
							.remove(new Pair<>(entry.getSource(), entry.getTarget()));
				}
			});
			add(delete);
			add(new Button("Speichern", ce -> {
				String nick = VaadinSession.getCurrent().getAttribute(Member.class).getUser().getName();
				grid.getItems().forEach(item -> {
					conversions.putConversion(item.getSource(), item.getTarget(), item.getFactor(), nick);
				});
				List<Entry> items = new ArrayList<>();
				items.forEach(item -> {
					submittedGrid.removeItem(item);
					conversions.putConversion(item.getSource(), item.getTarget(), item.getFactor(),
							item.getSubmittee());
				});
				cpm.persist(false, submittedConversions, submittedConversions.getConversionFactors());
				cpm.persist(false, conversions, conversions.getConversionFactors());
			}));
		});

		ContainerHelper.ifAuthorized(Permission.MESSAGE_WRITE.toString(), () -> {
			Conversions _submConversions = botConfig.getSubmittedConversions();
			Conversions submConversions;
			if (_submConversions == null) {
				botConfig.setSubmittedConversions(_submConversions = new Conversions());
				cpm.persist(botConfig, _submConversions);
			}
			submConversions = _submConversions;

			add(new H2("Neue Konvertierung Einreichen:"));

			HorizontalLayout hl = new HorizontalLayout();

			Binder<Entry> binder = new Binder<>(Entry.class);
			TextField sourceUnitField = new TextField();
			sourceUnitField.setPlaceholder("m");
			sourceUnitField.setRequired(true);
			binder.bind(sourceUnitField, "source");
			TextField targetUnitField = new TextField();
			targetUnitField.setPlaceholder("km");
			targetUnitField.setRequired(true);
			binder.bind(targetUnitField, "target");
			TextField factorField = new TextField();
			factorField.setRequired(true);
			factorField.setPlaceholder("0.001");
			binder.bind(factorField, e -> {
				Double factor = e.getFactor();
				return factor == null ? null : String.valueOf(factor);
			}, (e, val) -> {
				if (val == null || val.isBlank())
					e.setFactor(null);
				else
					e.setFactor(Double.parseDouble(val));
			});
			binder.setBean(new Entry());

			Button button = new Button("Absenden", ce -> {
				Entry written = new Entry();
				try {
					binder.writeBean(written);
				} catch (ValidationException e3) {
				}
				submConversions.putConversion(written.getSource(), written.getTarget(), written.getFactor(),
						VaadinSession.getCurrent().getAttribute(Member.class).getUser().getName());
				cpm.persist(false, submConversions, submConversions.getConversionFactors());
				binder.readBean(new Entry());
			});
			binder.setValidationStatusHandler(statusChange -> {
				button.setEnabled(statusChange.isOk());
			});

			hl.add(sourceUnitField, targetUnitField, factorField, button);
			add(hl);
		});
	}

	private void addItems(Conversions conversions, EditableGrid<Entry> grid) {
		conversions.getConversionFactors().entrySet().stream().map(e -> {
			Entry entry = new Entry();
			entry.setSource(e.getKey().x);
			entry.setTarget(e.getKey().y);
			entry.setFactor(e.getValue());
			entry.setSubmittee(conversions.getConversionSubmittee().get(e.getKey()));
			return entry;
		}).sorted((e1, e2) -> {
			var sourceCompare = e1.getSource().compareTo(e2.getSource());
			if (sourceCompare != 0)
				return sourceCompare;
			else
				return e1.getTarget().compareTo(e2.getTarget());
		}).forEach(grid::addItem);
	}

	private EditableGrid<Entry> createConversionsGrid() {
		Binder<Entry> binder = new Binder<>(Entry.class);
		TextField sourceUnitField = new TextField();
		sourceUnitField.setPlaceholder("m");
		binder.bind(sourceUnitField, "source");
		TextField targetUnitField = new TextField();
		targetUnitField.setPlaceholder("km");
		binder.bind(targetUnitField, "target");
		TextField factorField = new TextField();
		factorField.setPlaceholder("0.001");
		binder.bind(factorField, e -> {
			Double factor = e.getFactor();
			return factor == null ? null : String.valueOf(factor);
		}, (e, val) -> {
			if (val == null || val.isBlank())
				e.setFactor(null);
			else
				e.setFactor(Double.parseDouble(val));
		});

		EditableGrid<Entry> grid = new EditableGrid<>(Entry.class);
		grid.removeAllColumns();
		grid.getEditor().setBinder(binder);

		grid.addColumn("source").setHeader("Ursprungs-Einheit").setEditorComponent(sourceUnitField);
		grid.addColumn("target").setHeader("Ziel-Einheit").setEditorComponent(targetUnitField);
		grid.addColumn("factor").setHeader("Umrechnungsfaktor").setEditorComponent(factorField);

		add(grid);

		return grid;
	}

	private void makeInlineEditable(EditableGrid<Entry> grid) {
		Member member = VaadinSession.getCurrent().getAttribute(Member.class);
		if (member != null && member.getPermissions().contains(Permission.ADMINISTRATOR)) {
			grid.getElement().addEventListener("keydown", event -> {
				Editor<?> editor = grid.getEditor();
				editor.save();
				editor.closeEditor();
				grid.getDataProvider().refreshAll();
			}).setFilter("event.key === 'Enter'");
			grid.addSelectionListener(sel -> sel.getFirstSelectedItem().ifPresent(grid.getEditor()::editItem));

			Button addItemButton = new Button("Hinzufügen", ce -> grid.addItem(new Entry()));
			Button removeItemButton = new Button("Entfernen", ce -> grid.getSelectedItems().forEach(grid::removeItem));
			add(new HorizontalLayout(addItemButton, removeItemButton));
		}
	}

	public static class Entry {
		private String	source;
		private String	target;
		private String	submittee;
		private Double	factor;

		public String getSubmittee() {
			return submittee;
		}

		public void setSubmittee(String submittee) {
			this.submittee = submittee;
		}

		public String getSource() {
			return source;
		}

		public void setSource(String source) {
			this.source = source;
		}

		public String getTarget() {
			return target;
		}

		public void setTarget(String target) {
			this.target = target;
		}

		public Double getFactor() {
			return factor;
		}

		public void setFactor(Double factor) {
			this.factor = factor;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((factor == null) ? 0 : factor.hashCode());
			result = prime * result + ((source == null) ? 0 : source.hashCode());
			result = prime * result + ((target == null) ? 0 : target.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Entry other = (Entry) obj;
			if (factor == null) {
				if (other.factor != null)
					return false;
			} else if (!factor.equals(other.factor))
				return false;
			if (source == null) {
				if (other.source != null)
					return false;
			} else if (!source.equals(other.source))
				return false;
			if (target == null) {
				if (other.target != null)
					return false;
			} else if (!target.equals(other.target))
				return false;
			return true;
		}

	}

}
