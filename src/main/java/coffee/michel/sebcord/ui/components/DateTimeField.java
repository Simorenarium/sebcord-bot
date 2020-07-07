package coffee.michel.sebcord.ui.components;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.timepicker.TimePicker;

public class DateTimeField extends CustomField<LocalDateTime>{
	private static final long serialVersionUID = 2608098381449047053L;
	
	private HorizontalLayout element;
	private DatePicker dp;
	private TimePicker tp;
	
	public DateTimeField() {
		
		dp = new DatePicker();
		tp = new TimePicker();
		element = new HorizontalLayout(dp,tp);
		
		add(element);
	}
	
	@Override
	protected LocalDateTime generateModelValue() {
		LocalDate dateVal = dp.getValue();
		LocalTime timeVal = tp.getValue();
		LocalDateTime combined = LocalDateTime.now();
		if(dateVal != null)
			combined = combined.with(dateVal);
		if(timeVal != null)
			combined = combined.with(timeVal);
		return combined;
	}

	@Override
	protected void setPresentationValue(LocalDateTime newPresentationValue) {
		if(newPresentationValue == null)
			newPresentationValue = LocalDateTime.now();
		dp.setValue(newPresentationValue.toLocalDate());
		tp.setValue(newPresentationValue.toLocalTime());
	}

	public void setReadonly(boolean readonly) {
		dp.setReadOnly(readonly);
		tp.setReadOnly(readonly);
	}
	
}
