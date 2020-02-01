/*
 * Erstellt am: 18 Oct 2019 22:40:11
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.configuration.persistence;

import java.io.File;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;

/**
 * @author Jonas Michel
 *
 */
@ApplicationScoped
public class ConfigurationPersistenceManager {

	private static class DataRoot {
		private TwitchConfiguration twitchConfig = new TwitchConfiguration();
		private DiscordApplication  discordApp   = new DiscordApplication();
		private SebcordBot          botConfig    = new SebcordBot();

	}

	private DataRoot                      droot = new DataRoot();
	private static EmbeddedStorageManager storage;

	public ConfigurationPersistenceManager() {
		File configDir = getConfigurationDir();

		synchronized (ConfigurationPersistenceManager.class) {
			if (storage == null)
				storage = EmbeddedStorage.start(droot, new File(configDir, "sebcord"));
		}

		droot = (DataRoot) storage.root();
	}

	@PreDestroy
	public void deinit() {
		storage.shutdown();
	}

	public SebcordBot getBotConfig() {
		return droot.botConfig;
	}

	public DiscordApplication getDiscordApp() {
		return droot.discordApp;
	}

	public TwitchConfiguration getTwitchConfig() {
		return droot.twitchConfig;
	}

	public void persist(Object o) {
		storage.store(o);
	}

	public void persist(Object parent, Object o) {
		storage.store(o);
		storage.store(parent);
	}

	public static File getDataDir() {
		var jbossDir = System.getProperty("jboss.server.data.dir");
		var botConfigurationDir = System.getProperty("coffee.michel.sebcord.bot.data.dir");
		var userHome = System.getProperty("user.home");

		String configDir;
		if (jbossDir != null)
			configDir = jbossDir + "/sebcord-bot";
		else if (botConfigurationDir != null)
			configDir = botConfigurationDir;
		else
			configDir = userHome + "/sebcord-bot/data";

		File file = new File(configDir);
		file.mkdirs();
		return file;
	}

	public static File getConfigurationDir() {
		var jbossDir = System.getProperty("jboss.server.config.dir");
		var botConfigurationDir = System.getProperty("coffee.michel.sebcord.bot.config.dir");
		var userHome = System.getProperty("user.home");

		String configDir;
		if (jbossDir != null)
			configDir = jbossDir + "/sebcord-bot";
		else if (botConfigurationDir != null)
			configDir = botConfigurationDir;
		else
			configDir = userHome + "/sebcord-bot/configuration";

		File file = new File(configDir);
		file.mkdirs();
		return file;
	}
}
