package eu.avalanche7.commands;

import eu.avalanche7.BetterChunkLoader;
import eu.avalanche7.PermissionNode;
import eu.avalanche7.datastore.DataStoreManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import static eu.avalanche7.CommandExec.chunksInfo;

public class ChunksCommand {
    private BetterChunkLoader instance;

    public ChunksCommand(BetterChunkLoader instance) {
        this.instance = instance;
    }

    public boolean chunks(CommandSender sender, String label, String[] args) {
        Integer amount;
        String usage = "Usage: /" + label + " chunks (add|set) (PlayerName) (alwayson|onlineonly) (amount)";
        if (args.length < 5) {
            sender.sendMessage(chunksInfo((OfflinePlayer)sender));
            return false;
        }
        if (!sender.hasPermission(PermissionNode.COMMAND_CHUNKS)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
            return false;
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(args[2]);
        if (player == null) {
            sender.sendMessage(args[2] + " is not a valid player name\n" + usage);
            return false;
        }
        try {
            amount = Integer.valueOf(args[4]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid argument " + args[4] + "\n" + usage);
            return false;
        }
        sender.sendMessage(chunksInfo(player));
        if (args[1].equalsIgnoreCase("add")) {
            if (sender.hasPermission(PermissionNode.COMMAND_CHUNKS_ADD)) {
                if (args[3].equalsIgnoreCase("alwayson")) {
                    DataStoreManager.getDataStore().addAlwaysOnChunksLimit(player.getUniqueId(), amount.intValue());
                    sender.sendMessage("Added " + amount + " always-on chunks to " + player.getName());
                } else if (args[3].equalsIgnoreCase("onlineonly")) {
                    DataStoreManager.getDataStore().addOnlineOnlyChunksLimit(player.getUniqueId(), amount.intValue());
                    sender.sendMessage("Added " + amount + " online-only chunks to " + player.getName());
                } else {
                    sender.sendMessage("Invalid argument " + args[3] + "\n" + usage);
                    return false;
                }
            } else {
                sender.sendMessage("You do not have permission to use this command.");
                return false;
            }
        } else if (args[1].equalsIgnoreCase("set")) {
            if (sender.hasPermission(PermissionNode.COMMAND_CHUNKS_SET)) {
                if (amount.intValue() < 0) {
                    sender.sendMessage("Invalid argument " + args[4] + "\n" + usage);
                    return false;
                }
                if (args[3].equalsIgnoreCase("alwayson")) {
                    DataStoreManager.getDataStore().setAlwaysOnChunksLimit(player.getUniqueId(), amount.intValue());
                    sender.sendMessage("Set " + amount + " always-on chunks to " + player.getName());
                } else if (args[3].equalsIgnoreCase("onlineonly")) {
                    DataStoreManager.getDataStore().setOnlineOnlyChunksLimit(player.getUniqueId(), amount.intValue());
                    sender.sendMessage("Set " + amount + " online-only chunks to " + player.getName());
                } else {
                    sender.sendMessage("Invalid argument " + args[3] + "\n" + usage);
                    return false;
                }
            } else {
                sender.sendMessage("You do not have permission to use this command.");
                return false;
            }
        } else {
            sender.sendMessage("Invalid argument " + args[1] + "\n" + usage);
            return false;
        }
        return true;
    }
}
