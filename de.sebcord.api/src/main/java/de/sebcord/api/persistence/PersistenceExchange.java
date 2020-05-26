package de.sebcord.api.persistence;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import de.sebcord.api.persistence.config.DiscordConfiguration;
import de.sebcord.api.persistence.config.SebcordBotConfiguration;
import de.sebcord.api.persistence.config.TwitchConfiguration;
import de.sebcord.api.persistence.config.bot.features.command.ConvertCommandFeature;
import de.sebcord.api.persistence.config.bot.features.command.MemeCommandFeature;
import de.sebcord.api.persistence.config.bot.features.function.JoinRoleFeature;
import de.sebcord.api.persistence.config.bot.features.function.MuteRoleFeature;
import de.sebcord.api.persistence.config.bot.features.function.RoleTransitionFeature;
import de.sebcord.api.persistence.config.bot.features.function.RoleTransitionFeature.RoleAction;

public interface PersistenceExchange {

	<T> T get(Class<T> type);
	
	void doUpdate(DiscordConfiguration oldVersion, DiscordConfiguration newVersion);

	void doUpdate(SebcordBotConfiguration oldVersion, SebcordBotConfiguration newVersion);

	void doUpdate(TwitchConfiguration oldVersion, TwitchConfiguration newVersion);

	void doUpdate(ConvertCommandFeature oldVersion, ConvertCommandFeature newVersion);

	void doUpdate(MemeCommandFeature oldVersion, MemeCommandFeature newVersion);

	void doUpdate(JoinRoleFeature oldVersion, JoinRoleFeature newVersion);

	void doUpdate(MuteRoleFeature oldVersion, MuteRoleFeature newVersion);

	void doUpdate(RoleTransitionFeature oldVersion, RoleTransitionFeature newVersion);

	void doUpdate(RoleAction oldVersion, RoleAction newVersion);

	<T> void onUpdate(T updatedValue, Consumer<T> newVersion);

	<T> void onUpdate(Class<T> updatedType, BiConsumer<T, T> versionsConsumer);

}
