package de.sebcord.api.application;

import java.util.function.Consumer;

public class ApplicationInitEvent implements Event {

	private final Object source;
	private final Consumer<Object> recieverConsumer;
	private final Consumer<Object> completedConsumer;
	
	public ApplicationInitEvent(Object source, Consumer<Object> recieverConsumer,
			Consumer<Object> completedConsumer) {
		super();
		this.source = source;
		this.recieverConsumer = recieverConsumer;
		this.completedConsumer = completedConsumer;
	}

	@Override
	public Object getSource() {
		return source;
	}

	public void initializationComenced(Object receiver) {
		recieverConsumer.accept(receiver);
	}
	
	public void initializationCompleted(Object receiver) {
		completedConsumer.accept(receiver);
	}

}
