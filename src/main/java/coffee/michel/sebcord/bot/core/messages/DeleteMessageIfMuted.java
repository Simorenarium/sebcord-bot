/*
 * Copyright GEMTEC GmbH 2019
 *
 * Erstellt am: 24 Oct 2019 21:37:25
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.messages;

import java.time.Instant;

import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

import coffee.michel.sebcord.bot.persistence.PersistenceManager;

/**
 * @author Jonas Michel
 *
 */
public class DeleteMessageIfMuted {

	@Inject
	private PersistenceManager persistenceMgr;

	public void onMessage(@ObservesAsync MessageEvent event) {
		event.getMessage().getAuthorAsMember().subscribe(member -> {
			long memberId = member.getId().asLong();
			Instant instant = persistenceMgr.getMutedUsers().get(memberId);
			if (instant == null)
				return;
			if (instant.isAfter(Instant.now())) {
				event.getMessage().delete().subscribe();
			} else {
				persistenceMgr.removeMutedUser(memberId);
			}
		});
	}

}
