package coffee.michel.sebcord;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Factory {

	private static final Context ctx = new Context();

	private static class Context {
		private final ScheduledExecutorService exe;

		public Context() {
			this.exe = Executors.newScheduledThreadPool(30);
		}
	}

	public static ScheduledExecutorService executor() {
		return ctx.exe;
	}

}
