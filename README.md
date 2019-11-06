# sebcord-bot

This is a highly configurable Discord-Bot specificly made for the Sebcord Guil but technically usable anywhere.
The bot is made to run on Wildfly 18 or newer.

## Base Configuration
All configuration is done via Microprofile Config.
The following properties must be configured to use the Sebcord Bot.

* discord.bot.token - You can copy this token from the Discord Developer Portal
                      (Applications > Bot > Token)
* discord.bot.clientId - Your Applications Client Id
* discord.bot.client_secret - Your Applications Client Secret
* discord.bot.handled_server - The id of the Guild the bot is meant to handle
* discord.bot.developer.userId - Id of the developer, this is used to gain Bot permissions as the developer.
                                 If this is unwanted just set it to -1

## General Autmated Tasks
### Role Transitions
Role Transitions are triggered by a role beeing added to or removed from a user.
A transition is defined the following way:
`(+|-)<roleId>=(+|-)<roleId>`
The first + or - is an indecator if the transition is triggered if the specified role was added or removed.
The first roleId is the id of the role which has to be added or removed to trigger the transition.
The Trigger and the role to remove or add are seperated by a `=`
The second + or - is indicate if the second roleId has to be added to the user or removed.
Multiple transitions are seperated by a `;`
Example: `+639172155656896534=-639191033485197322;-639172155656896534=+639191033485197322`
To configure these transitions they must be added under the property key `discord.bot.roleTransitions`

### Twitch-Live notifications
* twitch.bot.clientId - Client id of the Twitch application
* twitch.bot.clientSecret - Client secret of the Twitch application
* twitch.bot.trackedChannel.name - the name of the observed channel
* twitch.bot.trackedChannel.url - full url of the channel to be tracked (this will be written into the Guild once the user is live)
* discord.bot.liveNotificationChannel - Id of the channel that shall receive Notifications

# Commands
just use the help command

# Commands which need configuration
* mute - the mute roleId can be configured under the key `discord.bot.mute-role`

Of course this is not all that must be configured.
But telling everything is bothersome.
