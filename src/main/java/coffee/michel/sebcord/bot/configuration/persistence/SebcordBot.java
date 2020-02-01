/*
 *
 * Erstellt am: 5 Dec 2019 19:00:59
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.configuration.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Jonas Michel
 *
 */
public class SebcordBot {

	public static final String PROPERTY_KEY = "sebcord.bot";

	public static class RoleTransition {
		public static class RoleAction {
			private boolean isAdd  = false;
			private long    roleId = 0;

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

		private RoleAction triggerAction = new RoleAction();
		private RoleAction actionToApply = new RoleAction();

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
			return Objects.equals(actionToApply, other.actionToApply) && Objects.equals(triggerAction, other.triggerAction);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("RoleTransition [triggerAction=").append(triggerAction).append(", actionToApply=").append(actionToApply).append("]");
			return builder.toString();
		}

	}

	private long                 handledServerId                         = 0;
	private List<Long>           developerIds                            = new ArrayList<>();
	private List<Long>           initialRoles                            = new ArrayList<>();
	private List<RoleTransition> roleTransitions                         = new ArrayList<>();
	private long                 twitchStreamerLiveNotificationChannelId = 0;
	private long                 muteRoleId                              = 0;

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
		return Objects.hash(developerIds, handledServerId, initialRoles, muteRoleId, roleTransitions, twitchStreamerLiveNotificationChannelId);
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
		return Objects.equals(developerIds, other.developerIds) && handledServerId == other.handledServerId && Objects.equals(initialRoles, other.initialRoles) && muteRoleId == other.muteRoleId && Objects.equals(roleTransitions, other.roleTransitions) && twitchStreamerLiveNotificationChannelId == other.twitchStreamerLiveNotificationChannelId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SebcordBot [handledServerId=").append(handledServerId).append(", developerIds=").append(developerIds).append(", initialRoles=").append(initialRoles).append(", roleTransitions=").append(roleTransitions).append(", twitchStreamerLiveNotificationChannelId=").append(twitchStreamerLiveNotificationChannelId).append(", muteRoleId=").append(muteRoleId).append("]");
		return builder.toString();
	}

}
