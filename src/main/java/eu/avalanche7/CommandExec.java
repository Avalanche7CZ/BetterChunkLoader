package eu.avalanche7;

import eu.avalanche7.commands.*;
import eu.avalanche7.datastore.DataStoreManager;
import eu.avalanche7.datastore.IDataStore;
import eu.avalanche7.datastore.PlayerData;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandExec implements CommandExecutor {
	BetterChunkLoader instance;

	public CommandExec(BetterChunkLoader instance) {
		this.instance = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("betterchunkloader")) {
			if (args.length == 0) {
				showCommandList(sender, label);
				return true;
			}

			switch (args[0].toLowerCase()) {
				case "info":
					return new InfoCommand(instance).info(sender);
				case "list":
					return new ListCommand(instance).list(sender, label, args);
				case "chunks":
					return new ChunksCommand(instance).chunks(sender, label, args);
				case "delete":
					return new DeleteCommand(instance).delete(sender, label, args);
				case "purge":
					return new PurgeCommand(instance).purge(sender);
				case "reload":
					return new ReloadCommand(instance).reload(sender);
				default:
					sender.sendMessage(ChatColor.RED + "Unknown command. Use /" + label + " for help.");
					return false;
			}
		}
		return false;
	}

	private void showCommandList(CommandSender sender, String label) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			player.sendMessage(ChatColor.GREEN + repeat("\u2500", 20));
			sendInteractiveMessage(player, label, "info", "Show information about the chunk loader.", ChatColor.YELLOW, ChatColor.AQUA);
			sendInteractiveMessage(player, label, "list", "List all chunk loaders.", ChatColor.YELLOW, ChatColor.AQUA);
			sendInteractiveMessage(player, label, "chunks", "Display your current chunk usage.", ChatColor.YELLOW, ChatColor.AQUA);
			sendInteractiveMessage(player, label, "delete", "Delete a specific chunk loader.", ChatColor.YELLOW, ChatColor.AQUA);
			sendInteractiveMessage(player, label, "purge", "Purge old chunk loaders.", ChatColor.YELLOW, ChatColor.AQUA);
			sendInteractiveMessage(player, label, "reload", "Reload the plugin configuration.", ChatColor.YELLOW, ChatColor.AQUA);
			player.sendMessage(ChatColor.GREEN + repeat("\u2500", 20));
		} else {
			sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
		}
	}

	public String repeat(String str, int count) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < count; i++) {
			builder.append(str);
		}
		return builder.toString();
	}

	public void sendInteractiveMessage(Player player, String label, String command, String description, ChatColor commandColor, ChatColor descriptionColor) {
		TextComponent arrow = new TextComponent(ChatColor.BLUE + " > ");
		TextComponent message = new TextComponent(commandColor + "/" + label + " " + command);
		message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(descriptionColor + description).create()));
		message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + label + " " + command));

		arrow.addExtra(message);
		player.spigot().sendMessage(arrow);
	}

	public static String chunksInfo(OfflinePlayer player) {
		IDataStore dataStore = DataStoreManager.getDataStore();
		int freeAlwaysOn = dataStore.getAlwaysOnFreeChunksAmount(player.getUniqueId());
		int freeOnlineOnly = dataStore.getOnlineOnlyFreeChunksAmount(player.getUniqueId());
		PlayerData pd = dataStore.getPlayerData(player.getUniqueId());
		int amountAlwaysOn = pd.getAlwaysOnChunksAmount();
		int amountOnlineOnly = pd.getOnlineOnlyChunksAmount();

		return ChatColor.GOLD + "=== " + player.getName() + " chunks amount ===\n" +
				ChatColor.GREEN + "Always-on - Free: " + freeAlwaysOn + " Used: " + (amountAlwaysOn - freeAlwaysOn) + " Total: " + amountAlwaysOn + "\n" +
				"Online-only - Free: " + freeOnlineOnly + " Used: " + (amountOnlineOnly - freeOnlineOnly) + " Total: " + amountOnlineOnly;
	}
}
