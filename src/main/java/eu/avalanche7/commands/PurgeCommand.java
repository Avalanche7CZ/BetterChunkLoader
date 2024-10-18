package eu.avalanche7.commands;

import eu.avalanche7.BetterChunkLoader;
import eu.avalanche7.CChunkLoader;
import eu.avalanche7.PermissionNode;
import eu.avalanche7.datastore.DataStoreManager;
import eu.avalanche7.datastore.IDataStore;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class PurgeCommand {
    private BetterChunkLoader instance;

    public PurgeCommand(BetterChunkLoader instance) {
        this.instance = instance;
    }

    public boolean purge(CommandSender sender) {
        if (!sender.hasPermission(PermissionNode.COMMAND_PURGE)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
            return false;
        }
        IDataStore ds = DataStoreManager.getDataStore();
        List<CChunkLoader> chunkLoaders = new ArrayList<>(DataStoreManager.getDataStore().getChunkLoaders());
        for (CChunkLoader cl : chunkLoaders) {
            if (!cl.blockCheck())
                ds.removeChunkLoader(cl);
        }
        sender.sendMessage(ChatColor.GOLD + "All invalid chunk loaders have been removed.");
        return true;
    }
}
