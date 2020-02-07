/*
 * Erstellt am: 18 Oct 2019 22:40:11
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.configuration.persistence;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;

/**
 * @author Jonas Michel
 *
 */
@Component
@Scope("singleton")
public class ConfigurationPersistenceManager {

	private static class DataRoot {
		private TwitchConfiguration	twitchConfig	= new TwitchConfiguration();
		private DiscordApplication	discordApp		= new DiscordApplication();
		private SebcordBot			botConfig		= new SebcordBot();

	}

	private DataRoot						droot		= new DataRoot();
	private Set<Runnable>					listeners	= new HashSet<>();
	private static EmbeddedStorageManager	storage;

	public ConfigurationPersistenceManager() {
		File configDir = getConfigurationDir();

		synchronized (ConfigurationPersistenceManager.class) {
			if (storage == null)
				storage = EmbeddedStorage.start(droot, new File(configDir, "sebcord"));
		}

		droot = (DataRoot) storage.root();
	}

	public void addSaveListener(Runnable listener) {
		this.listeners.add(listener);
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
		listeners.forEach(Runnable::run);
	}

	public void persist(Object parent, Object o) {
		storage.store(o);
		storage.store(parent);
		listeners.forEach(Runnable::run);
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
		var botConfigurationDir = System.getProperty("botConfigDirectory");
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
