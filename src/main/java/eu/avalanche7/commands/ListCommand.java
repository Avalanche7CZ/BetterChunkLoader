package eu.avalanche7.commands;

import eu.avalanche7.BetterChunkLoader;
import eu.avalanche7.CChunkLoader;
import eu.avalanche7.datastore.DataStoreManager;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ListCommand {
    private BetterChunkLoader instance;

    public ListCommand(BetterChunkLoader instance) {
        this.instance = instance;
    }

    public boolean list(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.GOLD + "Usage: /bcl list (own|PlayerName|all) [page]");
            return false;
        }
        int page = 1;
        if (args.length == 3) {
            try {
                page = Integer.parseInt(args[2]);
                if (page < 1) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid page");
                return false;
            }
        }
        if (args[1].equalsIgnoreCase("all")) {
            if (!sender.hasPermission("betterchunkloader.list.others")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
                return false;
            }
            List<CChunkLoader> clList = DataStoreManager.getDataStore().getChunkLoaders();
            printChunkLoadersList(clList, sender, page);
        } else if (args[1].equalsIgnoreCase("alwayson")) {
            if (!sender.hasPermission("betterchunkloader.list.others")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
                return false;
            }
            List<CChunkLoader> clList = new ArrayList<>();
            for (CChunkLoader cl : DataStoreManager.getDataStore().getChunkLoaders()) {
                if (cl.isAlwaysOn()) clList.add(cl);
            }
            printChunkLoadersList(clList, sender, page);
        } else {
            String playerName = args[1];
            if (playerName.equalsIgnoreCase("own")) playerName = sender.getName();
            if (sender.getName().equalsIgnoreCase(playerName)) {
                if (!sender.hasPermission("betterchunkloader.list.own")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
                    return false;
                }
            } else if (!sender.hasPermission("betterchunkloader.list.others")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
                return false;
            }
            OfflinePlayer player = this.instance.getServer().getOfflinePlayer(playerName);
            if (player == null || !player.hasPlayedBefore()) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return false;
            }
            List<CChunkLoader> clList = DataStoreManager.getDataStore().getChunkLoaders(player.getUniqueId());
            if (clList == null || clList.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "This player doesn't have any chunk loader.");
                return false;
            }
            int clSize = clList.size();
            int pages = (int) Math.ceil(clSize / 5.0D);
            if (page > pages) {
                sender.sendMessage(ChatColor.RED + "Invalid page");
                return false;
            }
            sender.sendMessage(ChatColor.GOLD + "== " + player.getName() + " chunk loaders list (" + page + "/" + pages + ") ==");
            sender.sendMessage(ChatColor.GRAY + "(AlwaysOn - Size - Position)");
            for (int i = (page - 1) * 5; i < page * 5 && i < clSize; i++) {
                CChunkLoader chunkLoader = clList.get(i);
                sender.sendMessage(chunkLoader.toString());
            }
        }
        return true;
    }

    private static boolean printChunkLoadersList(List<CChunkLoader> clList, CommandSender sender, int page) {
        int clSize = clList.size();
        if (clSize == 0) {
            sender.sendMessage(ChatColor.RED + "There isn't any chunk loader yet!");
            return false;
        }
        int pages = (int) Math.ceil(clSize / 5.0D);
        if (page > pages) {
            sender.sendMessage(ChatColor.RED + "Invalid page");
            return false;
        }
        sender.sendMessage(ChatColor.GOLD + "== Chunk loaders list (" + page + "/" + pages + ") ==");
        sender.sendMessage(ChatColor.GRAY + "(Owner - AlwaysOn - Size - Position)");
        for (int i = (page - 1) * 5; i < page * 5 && i < clSize; i++) {
            CChunkLoader chunkLoader = clList.get(i);
            sender.sendMessage(chunkLoader.getOwnerName() + " - " + chunkLoader.toString());
        }
        return true;
    }
}
