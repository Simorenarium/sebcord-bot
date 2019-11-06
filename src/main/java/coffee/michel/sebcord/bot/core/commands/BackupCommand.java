/*
 * Erstellt am: 4 Nov 2019 17:50:27
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

import coffee.michel.sebcord.bot.core.DCClient;
import coffee.michel.sebcord.bot.core.ZipUtils;
import discord4j.core.object.Embed;
import discord4j.core.object.Embed.Image;
import discord4j.core.object.Embed.Type;
import discord4j.core.object.Embed.Video;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;

/**
 * @author Jonas Michel
 *
 */
public class BackupCommand extends AbstractCommand {

	@Inject
	private DCClient client;

	@Override
	public String getName() {
		return "Backup";
	}

	@Override
	public String getCommandRegex() {
		return "backup";
	}

	@Override
	public String getDescription() {
		return "Macht ein Backup, kann nur von Administratoren und Simon gemacht werden.";
	}

	@Override
	public void onMessage(@ObservesAsync CommandEvent event) {
		super.onMessage(event);
	}

	@Override
	protected void handleCommand(CommandEvent event, String text) {
		Message message = event.getMessage();
		MessageChannel channel = message.getChannel().block();

		if (!client.isAdminOrDev(message)) {
			channel.createMessage("Du kannst dat nich.").subscribe();
			return;
		}

		// TODO emotes downloaden
		// TODO zip verpacken

		Guild guild = client.getGuild();
		Snowflake guildId = guild.getId();

		String channelName = guild.getChannelById(channel.getId()).block().getName();

		File tmpDir = new File(System.getProperty("jboss.server.data.dir"));
		File backupDir = new File(new File(tmpDir, "sebcord"), "backup");
		File currentBackupDir = new File(backupDir, channelName + System.currentTimeMillis());
		if (!currentBackupDir.exists())
			currentBackupDir.mkdirs();

		File attachementDir = new File(currentBackupDir, "attachements");
		attachementDir.mkdirs();
		File embeddDir = new File(currentBackupDir, "embedds");
		embeddDir.mkdirs();

		channel.getMessagesBefore(message.getId()).map(m -> BackupCsvEntry.of(m, guildId)).filter(Objects::nonNull).collectList().subscribe(entries -> {

			List<String> messageCsv = new LinkedList<>();
			// header added later because of reverse
			Set<String> mentionCsv = new LinkedHashSet<>();
			mentionCsv.add("MentionTag|DisplayName");
			Set<String> imageURLs = new LinkedHashSet<>();
			imageURLs.add("AttachmentId|ImagePath");
			Set<String> embeddCsv = new LinkedHashSet<>();
			embeddCsv.add("EmbeddName|EmbeddPath");

			for (BackupCsvEntry entry : entries) {
				String csv = entry.date.toString();
				csv += "|" + entry.memberName;
				csv += "|" + entry.embedUrls.keySet().stream().collect(Collectors.joining(";"));
				csv += "|" + entry.attachementUrls.keySet().stream().map(Snowflake::asString).collect(Collectors.joining(";"));
				csv += "|" + entry.content.replaceAll("\n", "<br>");
				messageCsv.add(csv);

				entry.mentionTagToMemberName.forEach((mention, name) -> {
					mentionCsv.add(mention + "|" + name);
				});

				Set<Entry<Snowflake, String>> entrySet = entry.attachementUrls.entrySet();
				for (Entry<Snowflake, String> e : entrySet) {
					String url = e.getValue();
					String extension = getExtension(url);

					String targetFileName = e.getKey().asString() + extension;
					File targetFile = new File(attachementDir, targetFileName);
					copyToFile(url, targetFile);
					entry.attachementUrls.put(e.getKey(), attachementDir.getName() + "/" + targetFileName);
				}
				entry.attachementUrls.forEach((id, url) -> {
					imageURLs.add(id.asLong() + "|" + url);
				});

				Set<Entry<String, String>> embeddSet = entry.embedUrls.entrySet();
				for (Entry<String, String> e : embeddSet) {
					String url = e.getValue();
					String targetFileName = e.getKey();
					File targetFile = new File(embeddDir, targetFileName);
					copyToFile(url, targetFile);
					entry.embedUrls.put(e.getKey(), embeddDir.getName() + "/" + targetFileName);
				}
				entry.embedUrls.forEach((id, url) -> {
					embeddCsv.add(id + "|" + url);
				});
			}

			messageCsv.add("Timestamp|Author|Embeds|Attachments|Content");
			Collections.reverse(messageCsv);

			writeText(messageCsv.stream().collect(Collectors.joining("\n")), new File(currentBackupDir, "messages.csv"));
			writeText(mentionCsv.stream().collect(Collectors.joining("\n")), new File(currentBackupDir, "mentions.csv"));
			writeText(imageURLs.stream().collect(Collectors.joining("\n")), new File(currentBackupDir, "attachments.csv"));
			writeText(embeddCsv.stream().collect(Collectors.joining("\n")), new File(currentBackupDir, "embedds.csv"));
		}, Throwable::printStackTrace, () -> {
			// on complete
			try {
				File file = new File(backupDir, currentBackupDir.getName() + ".zip");
				file.createNewFile();
				ZipUtils.zipIt(currentBackupDir.getAbsolutePath(), file);

				channel.createMessage(spec -> {
					try {
						spec.addFile(file.getName(), new FileInputStream(file));
					} catch (FileNotFoundException e) {}
				}).subscribe();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

	}

	private String getExtension(String url) {
		Matcher matcher = Pattern.compile("\\.[a-zA-Z0-9]{1,3}").matcher(url);
		List<MatchResult> results = matcher.results().collect(Collectors.toList());
		return results.get(results.size() - 1).group();
	}

	private void writeText(String text, File messageCsvFile) {
		try {
			messageCsvFile.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
			// hope this doesn't happen
		}
		try (FileOutputStream fos = new FileOutputStream(messageCsvFile)) {
			fos.write(text.getBytes("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean copyToFile(String rawUrl, File targetFile) {
		try {
			URL website = new URL(rawUrl);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos = new FileOutputStream(targetFile);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	static class BackupCsvEntry {
		private String memberName = "";
		private Instant date;
		private String content = "";
		private Map<Snowflake, String> attachementUrls = new HashMap<>();
		private Map<String, String> mentionTagToMemberName = new HashMap<>();
		private Map<String, String> embedUrls = new HashMap<>();

		static BackupCsvEntry of(Message message, Snowflake guildId) {
			BackupCsvEntry entry = new BackupCsvEntry();

			String memberName = message.getAuthor().map(author -> {
				try {
					Member block = author.asMember(guildId).block();
					return block.getDisplayName();
				} catch (Throwable t) {
					return author.getUsername();
				}
			}).filter(Objects::nonNull).orElse("Unbekannt");
			entry.content = message.getContent().orElse("");
			entry.memberName = memberName;
			entry.date = message.getTimestamp();
			entry.attachementUrls = message.getAttachments().stream().collect(Collectors.toMap(Attachment::getId, Attachment::getUrl));
			entry.mentionTagToMemberName = message.getUserMentions().collectMap(u -> u.getMention(), u -> u.asMember(guildId).map(Member::getDisplayName).block()).block();
			List<Embed> embeds = message.getEmbeds();
			loop: for (Embed embed : embeds) {
				String url;
				Type type;
				try {
					type = embed.getType();
				} catch (Throwable t) {
					continue loop;
				}
				switch (type) {
					case RICH:
					case IMAGE:
						Optional<String> map = embed.getImage().map(Image::getUrl);
						if (!map.isPresent())
							continue loop;
						url = map.get();
						break;
					case LINK:
						Optional<String> url2 = embed.getUrl();
						if (!url2.isPresent())
							continue loop;
						url = url2.get();
						break;
					case VIDEO:
						Optional<String> map2 = embed.getVideo().map(Video::getUrl);
						if (!map2.isPresent())
							continue loop;
						url = map2.get();
						break;
					default:
						continue loop;
				}

				int indexOfAfterPathStuff = url.indexOf('?');
				if (indexOfAfterPathStuff <= 0)
					indexOfAfterPathStuff = url.indexOf('#');
				if (indexOfAfterPathStuff <= 0)
					indexOfAfterPathStuff = url.length() - 1;
				if (url.endsWith("/"))
					url = url.substring(0, url.length() - 1);
				String embeddId = url.substring(url.lastIndexOf('/') + 1, indexOfAfterPathStuff);
				entry.embedUrls.put(embeddId, url);
			}

			return entry;
		}

	}

}
