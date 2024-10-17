package eu.avalanche7.commands;

import eu.avalanche7.BetterChunkLoader;
import eu.avalanche7.CChunkLoader;
import eu.avalanche7.datastore.DataStoreManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InfoCommand {
    private BetterChunkLoader instance;

    public InfoCommand(BetterChunkLoader instance) {
        this.instance = instance;
    }

    public boolean info(CommandSender sender) {
        if (!sender.hasPermission("betterchunkloader.info")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
            return false;
        }
        List<CChunkLoader> chunkLoaders = DataStoreManager.getDataStore().getChunkLoaders();
        if (chunkLoaders.isEmpty()) {
            sender.sendMessage("No statistics available.");
            return true;
        }
        int alwaysOnLoaders = 0, onlineOnlyLoaders = 0, alwaysOnChunks = 0, onlineOnlyChunks = 0, maxChunksCount = 0, players = 0;
        UUID maxChunksPlayer = null;
        HashMap<UUID, Integer> loadedChunksForPlayer = new HashMap<>();
        for (CChunkLoader chunkLoader : chunkLoaders) {
            if (chunkLoader.isAlwaysOn()) {
                alwaysOnLoaders++;
                alwaysOnChunks += chunkLoader.size();
            } else {
                onlineOnlyLoaders++;
                onlineOnlyChunks += chunkLoader.size();
            }
            Integer count = loadedChunksForPlayer.get(chunkLoader.getOwner());
            if (count == null)
                count = 0;
            count += chunkLoader.size();
            loadedChunksForPlayer.put(chunkLoader.getOwner(), count);
        }
        loadedChunksForPlayer.remove(CChunkLoader.adminUUID);
        players = loadedChunksForPlayer.size();
        for (Map.Entry<UUID, Integer> entry : loadedChunksForPlayer.entrySet()) {
            if (maxChunksCount < entry.getValue()) {
                maxChunksCount = entry.getValue();
                maxChunksPlayer = entry.getKey();
            }
        }
        sender.sendMessage(ChatColor.GOLD + "=== BetterChunkLoader statistics ===\n" + ChatColor.WHITE + "OnlineOnly: " + onlineOnlyLoaders + " chunk loaders (" + onlineOnlyChunks + " chunks)\nAlwaysOn: " + alwaysOnLoaders + " chunk loaders (" + alwaysOnChunks + " chunks)\nNumber of players using chunk loaders: " + players + "\nPlayer with the highest loaded chunks amount: " + this.instance
                .getServer().getOfflinePlayer(maxChunksPlayer).getName() + " (" + maxChunksCount + " chunks)\n");
        return true;
    }
}
