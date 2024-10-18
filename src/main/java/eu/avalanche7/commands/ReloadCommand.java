package eu.avalanche7.commands;

import eu.avalanche7.BetterChunkLoader;
import eu.avalanche7.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class ReloadCommand {
    private BetterChunkLoader instance;

    public ReloadCommand(BetterChunkLoader instance) {
        this.instance = instance;
    }

    public boolean reload(CommandSender sender) {
        if (!sender.hasPermission(PermissionNode.COMMAND_ADMIN)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
            return false;
        }
        this.instance.getLogger().info(sender.getName() + " reloaded this plugin");
        Bukkit.getPluginManager().disablePlugin((Plugin)this.instance);
        Bukkit.getPluginManager().enablePlugin((Plugin)this.instance);
        sender.sendMessage(ChatColor.RED + "BetterChunkLoader reloaded.");
        return true;
    }
}
