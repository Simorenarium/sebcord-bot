/*
 * (C) Copyright 2016 Dinos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package coffee.michel.sebcord.bot.core.commands.utils;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class MessageUtil {

	public static boolean canNotTalk(TextChannel channel) {
		if (channel == null)
			return true;
		Member member = channel.getGuild().getSelfMember();
		return member == null
				|| !member.hasPermission(channel, Permission.MESSAGE_READ)
				|| !member.hasPermission(channel, Permission.MESSAGE_WRITE);
	}

	private static void sendMessage(Message message, MessageChannel channel) {
		if (channel instanceof TextChannel && canNotTalk((TextChannel) channel))
			return;
		channel.sendMessage(message).queue(null, null);
	}

	public static void sendMessage(MessageEmbed embed, MessageChannel channel) {
		sendMessage(new MessageBuilder().setEmbed(embed).build(), channel);
	}

	public static void sendMessage(String message, MessageChannel channel) {
		sendMessage(new MessageBuilder().append(filter(message)).build(), channel);
	}

	private static String filter(String msgContent) {
		return msgContent.length() > 2000
				? "*The output message is over 2000 characters!*"
				: msgContent.replace("@everyone", "@\u180Eeveryone").replace("@here", "@\u180Ehere");
	}

	public static String userDiscrimSet(User u) {
		return stripFormatting(u.getName()) + "#" + u.getDiscriminator();
	}

	public static String stripFormatting(String s) {
		return s.replace("*", "\\*")
				.replace("`", "\\`")
				.replace("_", "\\_")
				.replace("~~", "\\~\\~")
				.replace(">", "\u180E>");
	}
}