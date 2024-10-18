package eu.avalanche7.commands;

import eu.avalanche7.BetterChunkLoader;
import eu.avalanche7.CChunkLoader;
import eu.avalanche7.CommandExec;
import eu.avalanche7.PermissionNode;
import eu.avalanche7.datastore.DataStoreManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.repeat;

public class ListCommand {
    private BetterChunkLoader instance;
    CommandExec commandExec = new CommandExec(instance);

    public ListCommand(BetterChunkLoader instance) {
        this.instance = instance;
    }

    public boolean list(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            Player player = (Player) sender;
            player.sendMessage(ChatColor.GREEN + repeat("\u2500", 20));
            commandExec.sendInteractiveMessage(player, label, "list own", "Show list of your chunkloaders.", ChatColor.YELLOW, ChatColor.AQUA);
            commandExec.sendInteractiveMessage(player, label, "list PlayerName", "Show list of chunkloaders for specific player", ChatColor.YELLOW, ChatColor.AQUA);
            commandExec.sendInteractiveMessage(player, label, "list all", "Show list of all chunkloaders", ChatColor.YELLOW, ChatColor.AQUA);
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
            if (!sender.hasPermission(PermissionNode.COMMAND_LIST_OTHER)) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
                return false;
            }
            List<CChunkLoader> clList = DataStoreManager.getDataStore().getChunkLoaders();
            printChunkLoadersList(clList, sender, page);
        } else if (args[1].equalsIgnoreCase("alwayson")) {
            if (!sender.hasPermission(PermissionNode.COMMAND_LIST_OTHER)) {
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
                if (!sender.hasPermission(PermissionNode.COMMAND_LIST_OWN)) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
                    return false;
                }
            } else if (!sender.hasPermission(PermissionNode.COMMAND_LIST_OTHER)) {
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
            for (int i = (page - 1) * 5; i < page * 5 && i < clSize; i++) {
                CChunkLoader chunkLoader = clList.get(i);
                TextComponent message = new TextComponent(ChatColor.YELLOW + chunkLoader.getOwnerName() + " - " + chunkLoader.getLoc());

                String hoverText = ChatColor.AQUA + "Owner: " + chunkLoader.getOwnerName() + "\n" +
                        "AlwaysOn: " + (chunkLoader.isAlwaysOn() ? "Yes" : "No") + "\n" +
                        "Size: " + chunkLoader.sizeX() + "\n" +
                        "Position: " + chunkLoader.getLoc().toString() + "\n" +
                        "Created: " + chunkLoader.getCreationDate().toString();

                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + chunkLoader.getLoc().getX() + " " + chunkLoader.getLoc().getY() + " " + chunkLoader.getLoc().getZ()));

                ((Player) sender).spigot().sendMessage(message);
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
        for (int i = (page - 1) * 5; i < page * 5 && i < clSize; i++) {
            CChunkLoader chunkLoader = clList.get(i);
            TextComponent message = new TextComponent(ChatColor.YELLOW + chunkLoader.getOwnerName() + " - " + chunkLoader.getLoc());

            String hoverText = ChatColor.AQUA + "Owner: " + chunkLoader.getOwnerName() + "\n" +
                    "AlwaysOn: " + (chunkLoader.isAlwaysOn() ? "Yes" : "No") + "\n" +
                    "Size: " + chunkLoader.sizeX() + "\n" +
                    "Position: " + chunkLoader.getLoc().toString() + "\n" +
                    "Created: " + chunkLoader.getCreationDate().toString();

            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + chunkLoader.getLoc().getX() + " " + chunkLoader.getLoc().getY() + " " + chunkLoader.getLoc().getZ()));

            ((Player) sender).spigot().sendMessage(message);
        }
        return true;
    }
}
