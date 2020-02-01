/*
 * Erstellt am: 4 Nov 2019 17:50:27
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core.commands;

import java.io.File;
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

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import coffee.michel.sebcord.bot.configuration.persistence.ConfigurationPersistenceManager;
import coffee.michel.sebcord.bot.core.JDADCClient;
import coffee.michel.sebcord.bot.core.ZipUtils;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.ImageInfo;
import net.dv8tion.jda.api.entities.MessageEmbed.VideoInfo;

/**
 * @author Jonas Michel
 *
 */
public class BackupCommand extends AbstractCommand {

	@Inject
	private JDADCClient client;

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
	public void onMessage(@Observes CommandEvent event) {
		super.onMessage(event);
	}

	@Override
	protected void handleCommand(CommandEvent event, String text) {
		Message message = event.getMessage();
		MessageChannel channel = message.getChannel();

		if (!client.isAdminOrDev(message)) {
			channel.sendMessage("Du kannst dat nich.").queue();
			return;
		}

		Guild guild = message.getGuild();
		String channelName = channel.getName();

		File tmpDir = ConfigurationPersistenceManager.getDataDir();
		File backupDir = new File(new File(tmpDir, "sebcord"), "backup");
		File currentBackupDir = new File(backupDir, channelName + System.currentTimeMillis());
		if (!currentBackupDir.exists())
			currentBackupDir.mkdirs();

		File attachementDir = new File(currentBackupDir, "attachements");
		attachementDir.mkdirs();
		File embeddDir = new File(currentBackupDir, "embedds");
		embeddDir.mkdirs();

		List<Message> completeHistory = new LinkedList<Message>();
		List<Message> history;
		do {
			history = channel.getHistoryBefore(message, 100).complete().getRetrievedHistory();
			completeHistory.addAll(0, history);
		} while (history.size() == 100);

		List<BackupCsvEntry> entries = completeHistory.stream().map(m -> BackupCsvEntry.of(m, guild)).filter(Objects::nonNull).collect(Collectors.toList());
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
			csv += "|" + entry.attachementUrls.keySet().stream().map(String::valueOf).collect(Collectors.joining(";"));
			csv += "|" + entry.content.replaceAll("\n", "<br>");
			messageCsv.add(csv);

			entry.mentionTagToMemberName.forEach((mention, name) -> {
				mentionCsv.add(mention + "|" + name);
			});

			Set<Entry<Long, String>> entrySet = entry.attachementUrls.entrySet();
			for (Entry<Long, String> e : entrySet) {
				String url = e.getValue();
				String extension = getExtension(url);

				String targetFileName = e.getKey() + extension;
				File targetFile = new File(attachementDir, targetFileName);
				copyToFile(url, targetFile);
				entry.attachementUrls.put(e.getKey(), attachementDir.getName() + "/" + targetFileName);
			}
			entry.attachementUrls.forEach((id, url) -> {
				imageURLs.add(id + "|" + url);
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

		try {
			File file = new File(backupDir, currentBackupDir.getName() + ".zip");
			file.createNewFile();
			ZipUtils.zipIt(currentBackupDir.getAbsolutePath(), file);

			channel.sendMessage("Fertitsch!").addFile(file).queue();
		} catch (IOException e) {
			// TODO report!
		}
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
		private String              memberName             = "";
		private Instant             date;
		private String              content                = "";
		private Map<Long, String>   attachementUrls        = new HashMap<>();
		private Map<String, String> mentionTagToMemberName = new HashMap<>();
		private Map<String, String> embedUrls              = new HashMap<>();

		static BackupCsvEntry of(Message message, Guild guild) {
			BackupCsvEntry entry = new BackupCsvEntry();

			entry.content = message.getContentRaw();
			entry.memberName = guild.getMember(message.getAuthor()).getNickname();
			entry.date = message.getTimeCreated().toInstant();
			entry.attachementUrls = message.getAttachments().stream().collect(Collectors.toMap(Attachment::getIdLong, Attachment::getUrl));
			entry.mentionTagToMemberName = message.getMentionedUsers().stream().collect(Collectors.toMap(u -> u.getAsMention(), u -> guild.getMember(u).getNickname()));
			List<MessageEmbed> embeds = message.getEmbeds();
			loop: for (MessageEmbed embed : embeds) {
				String url;
				EmbedType type;
				try {
					type = embed.getType();
				} catch (Throwable t) {
					continue loop;
				}
				switch (type) {
					case RICH:
					case IMAGE:
						Optional<String> map = Optional.ofNullable(embed.getImage()).map(ImageInfo::getUrl);
						if (!map.isPresent())
							continue loop;
						url = map.get();
					break;
					case LINK:
						String url2 = Optional.ofNullable(embed.getUrl()).orElse(null);
						if (url2 == null)
							continue loop;
						url = url2;
					break;
					case VIDEO:
						Optional<String> map2 = Optional.ofNullable(embed.getVideoInfo()).map(VideoInfo::getUrl);
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
