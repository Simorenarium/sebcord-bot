/*
 *
 * Erstellt am: 23 Jan 2020 19:44:50
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.enterprise.inject.Produces;

/**
 * @author Jonas Michel
 *
 */
public class ExecutorProducer {

	private static ScheduledExecutorService exe = Executors.newScheduledThreadPool(30);

	@Produces
	public ScheduledExecutorService getSExe() {
		return exe;
	}

	@Produces
	public ExecutorService getExe() {
		return exe;
	}

}
