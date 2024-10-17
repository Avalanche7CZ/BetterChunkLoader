package eu.avalanche7;

import eu.avalanche7.commands.*;
import eu.avalanche7.datastore.DataStoreManager;
import eu.avalanche7.datastore.IDataStore;
import eu.avalanche7.datastore.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandExec implements CommandExecutor {
	BetterChunkLoader instance;

	CommandExec(BetterChunkLoader instance) {
		this.instance = instance;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equals("betterchunkloader")) {
			String usage = ChatColor.GOLD + "Usage: /" + label + " [info|list|chunks|delete|purge|reload]";
			if (args.length == 0) {
				sender.sendMessage(usage);
				return false;
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
			}
			sender.sendMessage(usage);
		}
		return false;
	}

	public static String chunksInfo(OfflinePlayer player) {
		IDataStore dataStore = DataStoreManager.getDataStore();
		int freeAlwaysOn = dataStore.getAlwaysOnFreeChunksAmount(player.getUniqueId());
		int freeOnlineOnly = dataStore.getOnlineOnlyFreeChunksAmount(player.getUniqueId());
		PlayerData pd = dataStore.getPlayerData(player.getUniqueId());
		int amountAlwaysOn = pd.getAlwaysOnChunksAmount();
		int amountOnlineOnly = pd.getOnlineOnlyChunksAmount();
		return ChatColor.GOLD + "=== " + player.getName() + " chunks amount ===\n" + ChatColor.GREEN + "Always-on - Free: " + freeAlwaysOn + " Used: " + (amountAlwaysOn - freeAlwaysOn) + " Total: " + amountAlwaysOn + "\nOnline-only - Free: " + freeOnlineOnly + " Used: " + (amountOnlineOnly - freeOnlineOnly) + " Total: " + amountOnlineOnly;
	}
}