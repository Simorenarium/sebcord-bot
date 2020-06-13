/*
 *
 * Erstellt am: 5 Dec 2019 19:00:59
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.configuration.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import coffee.michel.sebcord.Pair;

/**
 * @author Jonas Michel
 *
 */
public class SebcordBot {

	public static final String PROPERTY_KEY = "sebcord.bot";

	public static class Conversions {
		private Map<Pair<String, String>, Double>	conversionFactors	= new HashMap<>();
		private Map<Pair<String, String>, String>	conversionSubmittee	= new HashMap<>();

		public void putConversion(String sourceUnit, String targetUnit, double factor, String submittee) {
			getConversionFactors().put(new Pair<>(sourceUnit, targetUnit), factor);
			getConversionSubmittee().put(new Pair<>(sourceUnit, targetUnit), submittee);
		}

		public String getSubmittee(String sourceUnit, String targetUnit) {
			return getConversionSubmittee().get(new Pair<>(sourceUnit, targetUnit));
		}

		public Double getConversion(String sourceUnit, String targetUnit) {
			return getConversionFactors().get(new Pair<>(sourceUnit, targetUnit));
		}

		public Map<Pair<String, String>, Double> getConversionFactors() {
			return conversionFactors == null ? (conversionFactors = new HashMap<>()) : conversionFactors;
		}

		public Map<Pair<String, String>, String> getConversionSubmittee() {
			return conversionSubmittee == null ? (conversionSubmittee = new HashMap<>()) : conversionSubmittee;
		}
	}

	public static class MemeCommand {
		private List<Long>		allowedChannels		= new ArrayList<>();
		private List<String>	blockedSubreddits	= new ArrayList<>();
		// 5 minutes
		private long			pauseTime			= 300000;
		private boolean			active;

		public List<String> getBlockedSubreddits() {
			if (blockedSubreddits == null)
				blockedSubreddits = new ArrayList<>();
			return blockedSubreddits;
		}

		public List<Long> getAllowedChannels() {
			return allowedChannels;
		}

		public long getPauseTime() {
			return pauseTime;
		}

		public void setPauseTime(long pauseTime) {
			this.pauseTime = pauseTime;
		}

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}
	}

	public static class RoleTransition {
		public static class RoleAction {
			private boolean	isAdd	= false;
			private long	roleId	= 0;

			public boolean isAdd() {
				return isAdd;
			}

			public void setAdd(boolean isAdd) {
				this.isAdd = isAdd;
			}

			public long getRoleId() {
				return roleId;
			}

			public void setRoleId(long roleId) {
				this.roleId = roleId;
			}

			@Override
			public int hashCode() {
				return Objects.hash(isAdd, roleId);
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				RoleAction other = (RoleAction) obj;
				return isAdd == other.isAdd && roleId == other.roleId;
			}

			@Override
			public String toString() {
				StringBuilder builder = new StringBuilder();
				builder.append("RoleAction [isAdd=").append(isAdd).append(", roleId=").append(roleId).append("]");
				return builder.toString();
			}

		}

		private RoleAction	triggerAction	= new RoleAction();
		private RoleAction	actionToApply	= new RoleAction();

		public RoleAction getTriggerAction() {
			return triggerAction;
		}

		public void setTriggerAction(RoleAction triggerAction) {
			this.triggerAction = triggerAction;
		}

		public RoleAction getActionToApply() {
			return actionToApply;
		}

		public void setActionToApply(RoleAction actionToApply) {
			this.actionToApply = actionToApply;
		}

		@Override
		public int hashCode() {
			return Objects.hash(actionToApply, triggerAction);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RoleTransition other = (RoleTransition) obj;
			return Objects.equals(actionToApply, other.actionToApply)
					&& Objects.equals(triggerAction, other.triggerAction);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("RoleTransition [triggerAction=").append(triggerAction).append(", actionToApply=")
					.append(actionToApply).append("]");
			return builder.toString();
		}

	}

	private long					handledServerId							= 0;
	private List<Long>				developerIds							= new ArrayList<>();
	private List<Long>				initialRoles							= new ArrayList<>();
	private List<RoleTransition>	roleTransitions							= new ArrayList<>();
	private long					twitchStreamerLiveNotificationChannelId	= 0;
	private long					muteRoleId								= 0;
	private MemeCommand				memeCommand								= new MemeCommand();
	private Conversions				conversions								= new Conversions();
	private Conversions				submittedConversions					= new Conversions();
	private String					welcomeMessage							= "";

	public String getWelcomeMessage() {
		return welcomeMessage;
	}

	public void setWelcomeMessage(String welcomeMessage) {
		this.welcomeMessage = welcomeMessage;
	}

	public Conversions getConversions() {
		return conversions;
	}

	public void setConversions(Conversions conversions) {
		this.conversions = conversions;
	}

	public Conversions getSubmittedConversions() {
		return submittedConversions == null ? (submittedConversions = new Conversions()) : submittedConversions;
	}

	public void setSubmittedConversions(Conversions submittedConversions) {
		this.submittedConversions = submittedConversions;
	}

	public long getMuteRoleId() {
		return muteRoleId;
	}

	public void setMuteRoleId(long muteRoleId) {
		this.muteRoleId = muteRoleId;
	}

	public long getHandledServerId() {
		return handledServerId;
	}

	public void setHandledServerId(long handledServerId) {
		this.handledServerId = handledServerId;
	}

	public List<Long> getDeveloperIds() {
		if (developerIds == null)
			developerIds = new LinkedList<>();
		return developerIds;
	}

	public void setDeveloperIds(List<Long> developerIds) {
		this.developerIds = developerIds;
	}

	public List<Long> getInitialRoles() {
		return initialRoles;
	}

	public void setInitialRoles(List<Long> initialRoles) {
		this.initialRoles = initialRoles;
	}

	public List<RoleTransition> getRoleTransitions() {
		return roleTransitions;
	}

	public void setRoleTransitions(List<RoleTransition> roleTransitions) {
		this.roleTransitions = roleTransitions;
	}

	public long getTwitchStreamerLiveNotificationChannelId() {
		return twitchStreamerLiveNotificationChannelId;
	}

	public void setTwitchStreamerLiveNotificationChannelId(long twitchStreamerLiveNotificationChannelId) {
		this.twitchStreamerLiveNotificationChannelId = twitchStreamerLiveNotificationChannelId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(developerIds, handledServerId, initialRoles, muteRoleId, roleTransitions,
				twitchStreamerLiveNotificationChannelId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SebcordBot other = (SebcordBot) obj;
		return Objects.equals(developerIds, other.developerIds) && handledServerId == other.handledServerId
				&& Objects.equals(initialRoles, other.initialRoles) && muteRoleId == other.muteRoleId
				&& Objects.equals(roleTransitions, other.roleTransitions)
				&& twitchStreamerLiveNotificationChannelId == other.twitchStreamerLiveNotificationChannelId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SebcordBot [handledServerId=").append(handledServerId).append(", developerIds=")
				.append(developerIds).append(", initialRoles=").append(initialRoles).append(", roleTransitions=")
				.append(roleTransitions).append(", twitchStreamerLiveNotificationChannelId=")
				.append(twitchStreamerLiveNotificationChannelId).append(", muteRoleId=").append(muteRoleId).append("]");
		return builder.toString();
	}

	public MemeCommand getMemeCommand() {
		return this.memeCommand;
	}

	public void setMemeCommand(MemeCommand memeCommand2) {
		this.memeCommand = memeCommand2;
	}

}
