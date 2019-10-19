/*
 * Erstellt am: 18 Oct 2019 22:40:11
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.persistence;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;

import one.microstream.persistence.lazy.Lazy;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;

/**
 * @author Jonas Michel
 *
 */
@ApplicationScoped
public class PersistenceManager {

	private DataRoot droot = new DataRoot();
	private EmbeddedStorageManager storage;

	@PostConstruct
	public void init() {
		String dataDir = System.getProperty("jboss.server.data.dir");

		storage = EmbeddedStorage.start(droot, new File(dataDir));
		droot = (DataRoot) storage.root();
	}

	@PreDestroy
	public void deinit() {
		storage.shutdown();
	}

	public Set<String> getAuthorizedFeatures(String authKey) {
		Map<String, Lazy<Set<String>>> authorziedFeatures = droot.getAuthorziedFeatures();
		Lazy<Set<String>> authorizedFeatures = authorziedFeatures.get(authKey);
		if (authorizedFeatures == null)
			return Collections.emptySet();
		return authorizedFeatures.get();
	}

	public void setAuthorizedFeatures(String authKey, Set<String> features) {
		Map<String, Lazy<Set<String>>> authorziedFeatures = droot.getAuthorziedFeatures();
		authorziedFeatures.put(authKey, Lazy.Reference(features));

		storage.store(authorziedFeatures);
	}

}
