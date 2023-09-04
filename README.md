### BetterChunkLoader
This is a plugin for Cauldron-like MC1.7.10 servers that allows players to make chunk loaders.

This was made for big multiplayer servers that need to restrict chunk loaders.

It has an online-only (work only when the player is online), and an always-on chunk loader (works even if the player is offline).

The amount of chunks that can be loaded per player can be set manually with ingame commands.

It provides ingame-commands for admins so they can remove all chunk loaders for a player, and chunk loaders can be disabled by disabling the plugin.

This plugin requires [Vault](https://dev.bukkit.org/bukkit-plugins/vault/) and [BCLForgeLib](https://github.com/KaiKikuchi/BCLForgeLib), a server-side Forge mod library, so it can be added to any modpack without requiring the mod on client-side.

#### Installation
- Download BetterChunkLoader and place it on the plugins folder
- Download BCLForgeLib and place it on the mods folder

#### Commands
- /bcl <info|list|chunks|delete|purge|reload|enable|disable> - main commands
- /bcl info - shows generic info about the plugin
- /bcl list (own|PlayerName|all) [page] - list your own, the player name, or all chunk loaders
- /bcl chunks (get|add|set) (PlayerName) (alwayson|onlineonly) (amount) - show, add, or set the amount of chunks that can be loaded by the specified player
- /bcl delete (PlayerName) - delete all the chunk loaders for the specified player
- /bcl purge - purge all the invalid chunk loaders. Useful if you reset the world
- /bcl <enable|disable> - enable or disable the plugin

#### Permissions
- betterchunkloader.onlineonly - Permission to create new onlineonly chunk loaders
- betterchunkloader.alwayson - Permission to create new alwayson chunk loaders
- betterchunkloader.list.own - List own chunk loaders
- betterchunkloader.unlimitedchunks - Override chunks amount limits
- betterchunkloader.list.others - List others chunk loaders
- betterchunkloader.edit - Edit others chunk loaders
- betterchunkloader.adminloader - Create admin chunk loaders
- betterchunkloader.info - Show general statistics
- betterchunkloader.delete - Delete player chunk loaders
- betterchunkloader.chunks - Manage players chunks amount
- betterchunkloader.purge - Allows the use of the purge command
- betterchunkloader.reload - Reload the plugin
- betterchunkloader.enable - Enable the plugin
- betterchunkloader.disable - Disable the plugin

#### How to use
Place a iron block or a diamond block, then right click with a blaze rod. A GUI will appear. Click the range (of course it's in chunks). If you have the adminloader permission, you can create admin chunk loaders by shift+right clicking the iron/diamond block with a blaze rod. Left-click a chunk loader to show basic information about the chunk loader. Right click a chunk loader to edit its range or disable it.

