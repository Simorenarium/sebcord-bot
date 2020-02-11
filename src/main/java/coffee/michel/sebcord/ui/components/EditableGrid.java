package coffee.michel.sebcord.ui.components;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ListDataProvider;

public class EditableGrid<T> extends Grid<T> {
	private static final long	serialVersionUID	= -7149705521810741171L;

	private List<T>				items				= new ArrayList<>();
	private ListDataProvider<T>	dataProvider		= new ListDataProvider<>(items);

	public EditableGrid() {
		super();
		setDataProvider(dataProvider);
	}

	public EditableGrid(Class<T> type, boolean initialize) {
		super(type, initialize);
		setDataProvider(dataProvider);
	}

	public EditableGrid(int pageSize) {
		super(pageSize);
		setDataProvider(dataProvider);
	}

	public EditableGrid(Class<T> type) {
		super(type);
		setDataProvider(dataProvider);
	}

	public void addItem(T item) {
		items.add(item);
		dataProvider.refreshAll();
	}

	public void removeItem(T item) {
		items.remove(item);
		dataProvider.refreshAll();
	}

	public List<T> getItems() {
		return items;
	}

	public int indexOfItem(T item) {
		return items.indexOf(item);
	}

	public void addItem(int index, T draggedItem) {
		items.add(index, draggedItem);
	}

}
