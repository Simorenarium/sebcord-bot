/*
 * Erstellt am: 18 Oct 2019 22:40:11
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.persistence;

import java.io.File;
import java.time.Instant;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

		storage = EmbeddedStorage.start(droot, new File(dataDir, "sebcord"));
		droot = (DataRoot) storage.root();

		Set<String> lowercasewords = droot.getWordblacklist().stream().map(String::toLowerCase).collect(Collectors.toSet());
		droot.getWordblacklist().clear();
		droot.getWordblacklist().addAll(lowercasewords);
		storage.store(droot);
		storage.store(droot.getWordblacklist());
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

	public void addMutedUser(Long user, Instant until) {
		droot.getMutedUsers().get().put(user, until);
		storage.storeRoot();
	}

	public void removeMutedUser(Long user) {
		droot.getMutedUsers().get().remove(user);
		storage.storeRoot();
	}

	public Map<Long, Instant> getMutedUsers() {
		return droot.getMutedUsers().get();
	}

	public Map<Long, Instant> getLastMessageForUsers() {
		return droot.getLastUserMessage();
	}

	public void persistLastMessageForUsers(Map<Long, Instant> val) {
		droot.getLastUserMessage().putAll(val);
		storage.storeRoot();
	}

	public String getLastAnnouncedStream() {
		return droot.getLastAnnouncedStreamId();
	}

	public void setLastAnnouncedStream(String lastAnnouncedStream) {
		droot.setLastAnnouncedStreamId(lastAnnouncedStream);
		storage.store(droot);
		storage.store(lastAnnouncedStream);
	}

	private static final String BLACKLIST_PATTERN_TEMPLATE = "(?:^|\\W)%s(?:$|\\W)";
	private Map<Predicate<String>, String> cachedMatchers = new IdentityHashMap<>();

	public Predicate<String> addWordToBlacklist(String word) {
		word = word.toLowerCase();
		Predicate<String> wordFinder = createCachedMatcher(word);
		Set<String> blacklist = droot.getWordblacklist();
		blacklist.add(word);
		storage.store(blacklist);
		return wordFinder;
	}

	private Predicate<String> createCachedMatcher(String word) {
		String completeWordPattern = "";
		String upperCase = word.toUpperCase();
		String lowerCase = word.toLowerCase();
		for (int i = 0; i < word.length(); i++) {
			completeWordPattern += "[" + upperCase.charAt(i) + "|" + lowerCase.charAt(i) + "]";
		}
		String completePattern = String.format(BLACKLIST_PATTERN_TEMPLATE, completeWordPattern);

		Predicate<String> wordFinder = Pattern.compile(completePattern).asPredicate();
		cachedMatchers.put(wordFinder, word);
		return wordFinder;
	}

	public String getBlockedWord(String text) {
		if (cachedMatchers.size() != droot.getWordblacklist().size())
			droot.getWordblacklist().forEach(this::createCachedMatcher);
		for (Entry<Predicate<String>, String> entry : cachedMatchers.entrySet())
			if (entry.getKey().test(text))
				return entry.getValue();
		return null;
	}

	public List<String> getBlacklistedWords() {
		return droot.getWordblacklist().stream().sorted().collect(Collectors.toList());
	}

	public boolean removeWordFromBlacklist(String word) {
		word = word.toLowerCase();
		Set<String> blacklist = droot.getWordblacklist();
		blacklist.remove(word);
		storage.store(blacklist);

		Set<Entry<Predicate<String>, String>> blacklistedWords = cachedMatchers.entrySet();
		for (Entry<Predicate<String>, String> entry : blacklistedWords) {
			if (entry.getValue().equalsIgnoreCase(word)) {
				cachedMatchers.remove(entry.getKey());
				return true;
			}
		}
		return false;
	}

}
