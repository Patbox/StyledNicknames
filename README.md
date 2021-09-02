# Styled Nicknames
It's a configurable mod allowing your server's players (and admins) to change their nickname with 
full Simplified Text Formatting support. It supports LuckPerms, PlayerRoles or any other 
fabric permission api compatible mod, with automatic removal of nicknames for players without permissions.
If you have any questions, you can ask them on my [Discord](https://discord.com/invite/AbqPPppgrd)


## Commands (and permissions):
- `/styled-nicknames` - Main command (`stylednicknames.main`, available by default)
- `/styled-nicknames reload` - Reloads configuration (requires `stylednicknames.reload`)
- `/styled-nicknames set <player> <value>` - Changes target players nickname (requires `stylednicknames.change_others`)
- `/styled-nicknames clears <player>` - Clears target players nickname (requires `stylednicknames.change_others`)
- `/nickname set <value>`/`/nick set <value>` - Changes own nickname (requires `stylednicknames.use`)
- `/nickname clear`/`/nick clear` - Clears own nickname (requires `stylednicknames.use`)

## Configuration:
You can find config file in `./config/styled-nicknames.json`.
[Formatting uses PlaceholderAPI's Simplified Text Format for which docs you can find here](https://placeholders.pb4.eu/user/text-format/).

```json5
{
    "CONFIG_VERSION_DONT_TOUCH_THIS": 1,
    "allowByDefault": false,           // Enables player commands by default
    "defaultPrefix": "#",              // Default prefix of nickname
    "maxLength": 32,                   // Max length (without formatting) of the nickname, set to 0 to disable it 
    "changeDisplayName": true,         // Changes player's display name
    "changePlayerListName": false,     // Changes nickname in player list (This option will be incompatible with some mods, use placeholder instead when possible)
    "allowLegacyFormatting": false,    // Allows usage of legacy text format in nicknames (&X)
    "nicknameChangedMessage": "...",   // Message send after changing nickname
    "nicknameResetMessage": "...",     // Message send after clearing nickname
    "defaultEnabledFormatting": {
      /*"tagname": value*/                // These values allow you to change tags enabled by default
    }
}
```

## Permission
To use commands/permissions, players require `stylednicknames.use` to use commands and 
optionally permissions `stylednicknames.format.[tag_name]` (`[tagname]` is Simplified Text Format tag), 
for additional formatting.

You can also give `stylednicknames.ignore_limit` to disable nickname length limit for them (which already ignores tags).

You should also give `stylednicknames.change_others` permission to your admins, so they can remove bad nicknames