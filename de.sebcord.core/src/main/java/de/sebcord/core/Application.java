package de.sebcord.core;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sebcord.api.application.ApplicationInitEvent;

public class Application {

	private static final Logger logger = LoggerFactory.getLogger(Application.class);
	private final int identityHashCode = System.identityHashCode(this);
	
	private final Weld weld;
	private final WeldContainer weldContainer;

	private final CountDownLatch latch;
	
	public Application() {
		logger.trace("#{} Application(): Weld DI is being set up.");
		this.weld = new Weld();
		this.weldContainer = weld.initialize();
		
		logger.debug("#{} Application(): The Weld DI Container was initialized.", identityHashCode);
		
		logger.trace("#{} Application(): Preparing shutdown prevention latch.", identityHashCode);
		latch = new CountDownLatch(1);
	}

	public void startApplication() {
		logger.trace("#{} startApplication(): Setting up initialization blocker.", identityHashCode);
		Set<Object> initializingClasses = new HashSet<>();
		final CountDownLatch startBlocker = new CountDownLatch(1);

		logger.debug("#{} startApplication(): Starting Component initialization.", identityHashCode);
		logger.trace("#{} startApplication(): Firing Initialization Event.", identityHashCode);
		weldContainer.event().select(ApplicationInitEvent.class).fireAsync(new ApplicationInitEvent(this, type -> {
			logger.trace("#{} startApplication(): {}.{} has started initilization.", identityHashCode, type.getClass().getSimpleName(), System.identityHashCode(type));
			initializingClasses.add(type);
		}, type -> {
			logger.trace("#{} startApplication(): {}.{} has finished initilization.", identityHashCode, type.getClass().getSimpleName(), System.identityHashCode(type));
			initializingClasses.remove(type);
			if(initializingClasses.isEmpty())
				startBlocker.countDown();
		}));

		logger.debug("#{} startApplication(): Blocking until initialization is done.", identityHashCode);
		
		try {
			startBlocker.await();
		} catch (InterruptedException e) {
			logger.warn("#{} startApplication(): Waiting for the completion of the component initialization failed, succesful completion is assumed.", identityHashCode, e);
		}
		
		logger.info("#{} startApplication(): Component initialization completed", identityHashCode);
	}
	
	public void stopApplication() {
		weld.shutdown();
		latch.countDown();
	}
	
	private boolean isStopped() {
		return !weldContainer.isRunning();
	}
	
	public void awaitTermination() throws InterruptedException {
		latch.await();
	}
	
	public static void main(String[] args) throws InterruptedException {
		logger.info("main(): Initialization started.");
		
		Application app = new Application();
		app.startApplication();
		
		logger.debug("main(): Registering shutdown hook.");
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if(!app.isStopped())
			app.stopApplication();
		}));

		logger.debug("main(): Awaiting termination.");
		app.awaitTermination();
		
		logger.info("main(): Application was terminated.");
	}

}
