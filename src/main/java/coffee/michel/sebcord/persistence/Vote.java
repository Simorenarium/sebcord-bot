package coffee.michel.sebcord.persistence;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Vote {

	public static class Option {
		private String	name;
		private String	unicode;
		private String	emoteId;

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
			return Objects.hash(emoteId, name, unicode);
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
					&& Objects.equals(unicode, other.unicode);
		}

		@Override
		public String toString() {
			return "Option [name=" + name + ", unicode=" + unicode + ", emoteId=" + emoteId + "]";
		}

	}

	private String					title;
	private String					description;
	private String					author;
	private List<Option>			options;
	private Map<Option, Integer>	votes;
	private Duration				timeout;
	private Instant					start;
	private long					messageId;

	public long getMessageId() {
		return messageId;
	}

	public void setMessageId(long messageId) {
		this.messageId = messageId;
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

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public List<Option> getOptions() {
		return options;
	}

	public void setOptions(List<Option> options) {
		this.options = options;
	}

	public Map<Option, Integer> getVotes() {
		return votes;
	}

	public void setVotes(Map<Option, Integer> votes) {
		this.votes = votes;
	}

	public Duration getTimeout() {
		return timeout;
	}

	public void setTimeout(Duration timeout) {
		this.timeout = timeout;
	}

	public Instant getStart() {
		return start;
	}

	public void setStart(Instant start) {
		this.start = start;
	}

	@Override
	public int hashCode() {
		return Objects.hash(author, description, options, start, timeout, title, votes, messageId);
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
				&& Objects.equals(options, other.options) && Objects.equals(start, other.start)
				&& Objects.equals(timeout, other.timeout) && Objects.equals(title, other.title)
				&& Objects.equals(votes, other.votes) && Objects.equals(messageId, other.messageId);
	}

	@Override
	public String toString() {
		return "Vote [title=" + title + ", description=" + description + ", author=" + author + ", options=" + options
				+ ", votes=" + votes + ", timeout=" + timeout + ", start=" + start + ",messageId=" + messageId + "]";
	}

}
