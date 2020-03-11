/*
 * Erstellt am: 18 Oct 2019 22:40:11
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.configuration.persistence;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.types.StorageDataConverterCsvConfiguration;
import one.microstream.storage.types.StorageDataConverterTypeBinaryToCsv;
import one.microstream.storage.types.StorageEntityTypeConversionFileProvider;
import one.microstream.storage.types.StorageEntityTypeExportFileProvider;
import one.microstream.storage.types.StorageLockedFile;

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

	@PreDestroy
	public void deinit() {
		var connection = storage.createConnection();
		File exportFile = new File(getConfigurationDir(), "export");
		exportFile.mkdir();
		connection.exportTypes(new StorageEntityTypeExportFileProvider.Default(exportFile, "bin"), typeHandler -> true);

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		File csvDir = new File(getConfigurationDir(), "csv");
		StorageDataConverterTypeBinaryToCsv converter = new StorageDataConverterTypeBinaryToCsv.UTF8(
				StorageDataConverterCsvConfiguration.defaultConfiguration(),
				new StorageEntityTypeConversionFileProvider.Default(csvDir, "csv"),
				storage.typeDictionary(),
				null, // no type name mapping
				4096, // read buffer size
				4096 // write buffer size
		);
		for (File file : exportFile.listFiles()) {
			StorageLockedFile storageFile = StorageLockedFile.openLockedFile(file);
			try {
				converter.convertDataFile(storageFile);
			} finally {
				storageFile.close();
			}
		}
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

	public void persist(boolean notifyListeners, Object o) {
		storage.store(o);
		if (notifyListeners)
			listeners.forEach(Runnable::run);
	}

	public void persist(Object o) {
		persist(true, o);
	}

	public void persist(boolean notifyListener, Object parent, Object o) {
		storage.store(o);
		storage.store(parent);
		if (notifyListener)
			listeners.forEach(Runnable::run);
	}

	public void persist(Object parent, Object o) {
		persist(true, parent, o);
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
		System.out.println("Using dir: " + file.getAbsolutePath());
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
		System.out.println("Using dir: " + file.getAbsolutePath());
		return file;
	}
}
