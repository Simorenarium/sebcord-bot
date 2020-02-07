/*
 *
 * Erstellt am: 27 Jan 2020 18:44:06
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.data;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import coffee.michel.sebcord.Factory;
import coffee.michel.sebcord.bot.core.JDADCClient;
import coffee.michel.sebcord.persistence.PersistenceManager;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import one.microstream.persistence.lazy.Lazy;

/**
 * @author Jonas Michel
 *
 */
public class MessageStoreTask {

	private JDADCClient			dccClient			= new JDADCClient();
	private PersistenceManager	persistenceManager	= new PersistenceManager();

	private ExecutorService		exe					= Factory.executor();

	public void init() {
//		Map<Long, Lazy<List<Lazy<MessageData>>>> messages = persistenceManager.getMessages();
//		messages.forEach((id, data) -> {
//			List<Lazy<MessageData>> list = data.get();
//			for (Lazy<MessageData> lazy : list) {
//				System.out.println(lazy.get().loadedToString());
//			}
//
//		});
//
//		if ("".isEmpty())
//			return;

		exe.submit(() -> {
			Guild guild = dccClient.getGuild();
			List<TextChannel> allTextChannels = guild.getCategories().stream().flatMap(c -> c.getChannels().stream())
					.filter(TextChannel.class::isInstance).map(TextChannel.class::cast)
					.sorted((c1, c2) -> c2.getTimeCreated().compareTo(c1.getTimeCreated()))
					.collect(Collectors.toList());
			for (TextChannel channel : allTextChannels) {
				final var channelId = channel.getIdLong();

				exe.submit(() -> {
					Iterator<Message> iterator = channel.getHistoryFromBeginning(100).complete().getRetrievedHistory()
							.iterator();
					Message message = null;
					while (true) {
						if (!iterator.hasNext()) {
							if (message != null) {
								iterator = channel.getHistoryAfter(message, 100).complete().getRetrievedHistory()
										.iterator();
								continue;
							} else
								break;
						}

						Message next = iterator.next();
						store(channelId, next);
					}
				});
			}
		});
	}

	private void store(long channelId, Message next) {
		if (next.getType() != MessageType.DEFAULT)
			return;

		var data = new MessageData();

		// member
		Member member = next.getMember();
		if (member != null) {
			data.author = member.getEffectiveName();
			data.authorUserId = member.getIdLong();
		} else {
			User author = next.getAuthor();
			data.author = author.getName();
			data.authorUserId = author.getIdLong();
		}

		data.content = next.getContentDisplay();
		data.embedds = next.getEmbeds().stream().map(Lazy::Reference).collect(Collectors.toList());
		data.emotes = Lazy.Reference(next.getEmotes().stream().map(e -> new EmoteData(e.getName(), e.getImageUrl()))
				.collect(Collectors.toList()));
		data.id = next.getIdLong();
		data.jumpURL = next.getJumpUrl();
		data.reactions = Lazy.Reference(next.getReactions().stream().map(r -> {
			ReactionEmote reactionEmote = r.getReactionEmote();
			if (reactionEmote.isEmote()) {
				Emote emote = reactionEmote.getEmote();
				return new ReactionData(new EmoteData(emote.getName(), emote.getImageUrl()),
						r.hasCount() ? r.getCount() : 0, null);
			} else {
				return new ReactionData(reactionEmote.getEmoji(), r.hasCount() ? r.getCount() : 0, null);
			}
		}).collect(Collectors.toList()));

		data.channelMentions = Lazy.Reference(next.getMentionedChannels().stream()
				.map(c -> new Tuple<>(c.getName(), c.getIdLong())).collect(Collectors.toSet()));
		data.roleMentions = Lazy.Reference(next.getMentionedRoles().stream()
				.map(r -> new Tuple<>(r.getName(), r.getIdLong())).collect(Collectors.toSet()));
		data.userMentions = Lazy.Reference(next.getMentionedUsers().stream()
				.map(r -> new Tuple<>(r.getName(), r.getIdLong())).collect(Collectors.toSet()));
		data.memberMentions = Lazy.Reference(next.getMentionedMembers().stream()
				.map(r -> new Tuple<>(r.getEffectiveName(), r.getIdLong())).collect(Collectors.toSet()));
		OffsetDateTime timeCreated = next.getTimeCreated();
		if (timeCreated != null)
			data.createTime = timeCreated.toInstant();
		OffsetDateTime timeEdited = next.getTimeEdited();
		if (timeEdited != null)
			data.editTime = timeEdited.toInstant();

		persistenceManager.storeMessage(channelId, data);
	}

	static class Tuple<A, B> {
		public final A	a;
		public final B	b;

		public Tuple(A a, B b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Tuple [a=").append(a).append(", b=").append(b).append("]");
			return builder.toString();
		}

	}

	static class EmoteData {
		private final String	name;
		private final String	downloadURL;

		public EmoteData(String name, String downloadURL) {
			this.name = name;
			this.downloadURL = downloadURL;
		}

		public String getName() {
			return name;
		}

		public String getDownloadURL() {
			return downloadURL;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("EmoteData [name=").append(name).append(", downloadURL=").append(downloadURL).append("]");
			return builder.toString();
		}

	}

	static class ReactionData {
		private final String			emoji;
		private final EmoteData			emote;
		private final long				count;
		private final Lazy<Set<Long>>	reactors;

		public ReactionData(String emoji, long count, Lazy<Set<Long>> reactors) {
			this.emoji = emoji;
			this.emote = null;
			this.count = count;
			this.reactors = reactors;
		}

		public ReactionData(EmoteData emote, long count, Lazy<Set<Long>> reactors) {
			this.emoji = null;
			this.emote = emote;
			this.count = count;
			this.reactors = reactors;
		}

		public long getCount() {
			return count;
		}

		public EmoteData getEmote() {
			return emote;
		}

		public String getEmoji() {
			return emoji;
		}

		public Lazy<Set<Long>> getReactors() {
			return reactors;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ReactionData [emoji=")
					.append(emoji)
					.append(", emote=")
					.append(emote)
					.append(", count=")
					.append(count)
					.append(", reactors=")
					.append(Optional.ofNullable(reactors).map(l -> l.get()).orElse(Collections.emptySet()).toString())
					.append("]");
			return builder.toString();
		}

	}

	public static class MessageData {
		private long							id;
		private String							jumpURL;
		private String							author;
		private long							authorUserId;
		private String							content;
		private List<Lazy<MessageEmbed>>		embedds		= new ArrayList<>();
		private Lazy<List<EmoteData>>			emotes		= null;
		private Lazy<List<ReactionData>>		reactions	= null;
		private Instant							createTime;
		private Instant							editTime;
		private Lazy<Set<Tuple<String, Long>>>	channelMentions;
		private Lazy<Set<Tuple<String, Long>>>	roleMentions;
		private Lazy<Set<Tuple<String, Long>>>	userMentions;
		private Lazy<Set<Tuple<String, Long>>>	memberMentions;

		public String loadedToString() {
			StringBuilder builder = new StringBuilder();
			builder.append("MessageData [id=")
					.append(id)
					.append(", jumpURL=")
					.append(jumpURL)
					.append(", author=")
					.append(author)
					.append(", authorUserId=")
					.append(authorUserId)
					.append(", content=")
					.append(content)
					.append(", embedds=")
					.append(embedds.stream().map(l -> l.get()).map(MessageEmbed::toString)
							.collect(Collectors.joining("; ")))
					.append(", emotes=")
					.append(emotes.get().stream().map(EmoteData::toString).collect(Collectors.joining("; ")))
					.append(", reactions=")
					.append(reactions.get().stream().map(ReactionData::toString).collect(Collectors.joining("; ")))
					.append(", createTime=")
					.append(createTime)
					.append(", editTime=")
					.append(editTime)
					.append(", channelMentions=")
					.append(channelMentions.get().stream().map(Tuple::toString).collect(Collectors.joining(" ")))
					.append(", roleMentions=")
					.append(roleMentions.get().stream().map(Tuple::toString).collect(Collectors.joining(" ")))
					.append(", userMentions=")
					.append(userMentions.get().stream().map(Tuple::toString).collect(Collectors.joining(" ")))
					.append(", memberMentions=")
					.append(memberMentions.get().stream().map(Tuple::toString).collect(Collectors.joining(" ")))
					.append("]");
			return builder.toString();
		}

	}

}
