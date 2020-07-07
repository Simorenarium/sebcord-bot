package coffee.michel.sebcord.persistence;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Vote {

	public static class Option {
		private String name;
		private String unicode;
		private String emoteId;
		private String attachmentData;
		private String attachmentName;

		public void setAttachmentData(String attachmentData) {
			this.attachmentData = attachmentData;
		}

		public String getAttachmentData() {
			return attachmentData;
		}

		public void setAttachmentName(String attachmentName) {
			this.attachmentName = attachmentName;
		}

		public String getAttachmentName() {
			return attachmentName;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getUnicode() {
			return unicode;
		}

		public void setUnicode(String unicode) {
			this.unicode = unicode;
		}

		public String getEmoteId() {
			return emoteId;
		}

		public void setEmoteId(String emoteId) {
			this.emoteId = emoteId;
		}

		@Override
		public int hashCode() {
			return Objects.hash(emoteId, name, unicode, attachmentName);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Option other = (Option) obj;
			return Objects.equals(emoteId, other.emoteId) && Objects.equals(name, other.name)
					&& Objects.equals(unicode, other.unicode)
					&& Objects.equals(attachmentName, other.attachmentName);
		}

		@Override
		public String toString() {
			return "Option [name=" + name + ", unicode=" + unicode + ", emoteId=" + emoteId + ", attachmentName="
					+ attachmentName + "]";
		}

	}

	private long id;
	private String title;
	private String description;
	private Long author;
	private List<Option> options;
	private Map<Option, List<Long>> votes;
	private LocalDateTime timeout;
	private LocalDateTime start;
	private Long messageId;
	private Long channelId;

	public long getChannelId() {
		return channelId == null ? -1 : channelId;
	}

	public void setChannelId(long channelId) {
		this.channelId = channelId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getAuthor() {
		return author;
	}

	public void setAuthor(Long author) {
		this.author = author;
	}

	public List<Option> getOptions() {
		return options == null ? options = new LinkedList<>() : options;
	}

	public void setOptions(List<Option> options) {
		this.options = options;
	}

	public Map<Option, List<Long>> getVotes() {
		return votes;
	}

	public void setVotes(Map<Option, List<Long>> votes) {
		this.votes = votes;
	}

	public LocalDateTime getTimeout() {
		return timeout;
	}

	public void setTimeout(LocalDateTime timeout) {
		this.timeout = timeout;
	}

	public LocalDateTime getStart() {
		return start;
	}

	public void setStart(LocalDateTime start) {
		this.start = start;
	}

	public long getMessageId() {
		return messageId == null ? -1 : messageId;
	}

	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(author, description, messageId, options, start, timeout, title, votes, id, channelId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vote other = (Vote) obj;
		return Objects.equals(author, other.author) && Objects.equals(description, other.description)
				&& messageId == other.messageId && Objects.equals(options, other.options)
				&& Objects.equals(start, other.start) && Objects.equals(timeout, other.timeout)
				&& Objects.equals(title, other.title) && Objects.equals(votes, other.votes) && id == other.id
				&& Objects.equals(channelId, other.channelId);
	}

	@Override
	public String toString() {
		return "Vote [id=" + id + ", title=" + title + ", description=" + description + ", author=" + author
				+ ", options=" + options + ", votes=" + votes + ", timeout=" + timeout + ", start=" + start
				+ ", messageId=" + messageId + ", channelId=" + channelId + "]";
	}

}
