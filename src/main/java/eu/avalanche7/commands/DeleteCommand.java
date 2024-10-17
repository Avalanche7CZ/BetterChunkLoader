package eu.avalanche7.commands;

import eu.avalanche7.BetterChunkLoader;
import eu.avalanche7.CChunkLoader;
import eu.avalanche7.datastore.DataStoreManager;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;

public class DeleteCommand {
    private BetterChunkLoader instance;

    public DeleteCommand(BetterChunkLoader instance) {
        this.instance = instance;
    }

    public boolean delete(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("betterchunkloader.delete")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
            return false;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.GOLD + "Usage: /bcl delete (PlayerName)");
            return false;
        }
        OfflinePlayer player = this.instance.getServer().getOfflinePlayer(args[1]);
        if (player == null || !player.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return false;
        }
        List<CChunkLoader> clList = DataStoreManager.getDataStore().getChunkLoaders(player.getUniqueId());
        if (clList == null) {
            sender.sendMessage(ChatColor.RED + "This player doesn't have any chunk loader.");
            return false;
        }
        DataStoreManager.getDataStore().removeChunkLoaders(player.getUniqueId());
        sender.sendMessage(ChatColor.RED + "All chunk loaders placed by this player have been removed.");
        this.instance.getLogger().info(sender.getName() + " deleted all chunk loaders placed by " + player.getName());
        return true;
    }
}
