package net.arcturus.mc.bcl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import net.arcturus.mc.bcl.datastore.DataStoreManager;
import net.arcturus.mc.bcl.datastore.IDataStore;
import net.arcturus.mc.bcl.datastore.PlayerData;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandExec implements CommandExecutor {
	BetterChunkLoader instance;
	
	CommandExec(BetterChunkLoader instance) {
		this.instance=instance;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equals("betterchunkloader")) {
			final String usage = ChatColor.GOLD + "Usage: /"+label+" [info|list|chunks|delete|purge|reload]";
			if (args.length==0) {
				sender.sendMessage(usage);
				return false;
			}
			
			switch(args[0].toLowerCase()) {
			case "info":
				return info(sender);
			case "list":
				return list(sender, label, args);
			case "chunks":
				return chunks(sender, label, args);
			case "delete":
				return delete(sender, label, args);
			case "purge":
				return purge(sender);
			case "reload":
				return reload(sender);
			}

			sender.sendMessage(usage);
		}
		
		return false;
	}
	
	private boolean info(CommandSender sender) {
		if (!sender.hasPermission("betterchunkloader.info")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
			return false;
		}
		
		List<CChunkLoader> chunkLoaders = DataStoreManager.getDataStore().getChunkLoaders();
		if (chunkLoaders.isEmpty()) {
			sender.sendMessage("No statistics available.");
			return true;
		}
		
		int alwaysOnLoaders=0, onlineOnlyLoaders=0, alwaysOnChunks=0, onlineOnlyChunks=0, maxChunksCount=0, players=0;
		UUID maxChunksPlayer=null;
		HashMap<UUID, Integer> loadedChunksForPlayer = new HashMap<>();
		
		for (CChunkLoader chunkLoader : chunkLoaders) {
			if (chunkLoader.isAlwaysOn()) {
				alwaysOnLoaders++;
				alwaysOnChunks+=chunkLoader.size();
			} else {
				onlineOnlyLoaders++;
				onlineOnlyChunks+=chunkLoader.size();
			}
			
			Integer count = loadedChunksForPlayer.get(chunkLoader.getOwner());
			if (count==null) {
				count=0;
			}
			count+=chunkLoader.size();
			loadedChunksForPlayer.put(chunkLoader.getOwner(), count);
		}

		loadedChunksForPlayer.remove(CChunkLoader.adminUUID);
		players=loadedChunksForPlayer.size();
		
		for (Entry<UUID, Integer> entry : loadedChunksForPlayer.entrySet()) {
			if (maxChunksCount<entry.getValue()) {
				maxChunksCount=entry.getValue();
				maxChunksPlayer=entry.getKey();
			}
		}

		sender.sendMessage(ChatColor.GOLD + "=== BetterChunkLoader statistics ===\n"
				+ ChatColor.WHITE + "OnlineOnly: "+onlineOnlyLoaders+" chunk loaders ("+onlineOnlyChunks+" chunks)\n"
									+ "AlwaysOn: "+alwaysOnLoaders+" chunk loaders ("+alwaysOnChunks+" chunks)\n"
									+ "Number of players using chunk loaders: "+players+"\n"
									+ "Player with the highest loaded chunks amount: "+instance.getServer().getOfflinePlayer(maxChunksPlayer).getName()+" ("+maxChunksCount+" chunks)\n");
		
		return true;
	}
	
	@SuppressWarnings("deprecation")
	private boolean list(CommandSender sender, String label, String[] args) {
		if (args.length<2) {
			sender.sendMessage(ChatColor.GOLD + "Usage: /bcl list (own|PlayerName|all) [page]");
			return false;
		}
		
		int page=1;
		if (args.length==3) {
			try {
				page=Integer.valueOf(args[2]);
				if (page<1) {
					throw new NumberFormatException();
				}
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
			
			List<CChunkLoader> clList = new ArrayList<CChunkLoader>();
			for (CChunkLoader cl : DataStoreManager.getDataStore().getChunkLoaders()) {
				if (cl.isAlwaysOn()) {
					clList.add(cl);
				}
			}
			
			printChunkLoadersList(clList, sender, page);
		} else {
			String playerName = args[1];
			if (playerName.equalsIgnoreCase("own")) {
				playerName=sender.getName();
			}
			
			if (sender.getName().equalsIgnoreCase(playerName)) {
				if (!sender.hasPermission("betterchunkloader.list.own")) {
					sender.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
					return false;
				}
			} else {
				if (!sender.hasPermission("betterchunkloader.list.others")) {
					sender.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
					return false;
				}
			}

			OfflinePlayer player = instance.getServer().getOfflinePlayer(playerName);
			if (player==null || !player.hasPlayedBefore()) {
				sender.sendMessage(ChatColor.RED + "Player not found.");
				return false;
			}
			List<CChunkLoader> clList = DataStoreManager.getDataStore().getChunkLoaders(player.getUniqueId());
			if (clList==null || clList.size()==0) {
				sender.sendMessage(ChatColor.RED + "This player doesn't have any chunk loader.");
				return false;
			}
			
			int clSize=clList.size();
			int pages=(int) Math.ceil(clSize/5.00);

			if (page>pages) {
				sender.sendMessage(ChatColor.RED + "Invalid page");
				return false;
			}
			
			sender.sendMessage(ChatColor.GOLD + "== "+player.getName()+" chunk loaders list ("+page+"/"+pages+") ==");
			sender.sendMessage(ChatColor.GRAY + "(AlwaysOn - Size - Position)");
			
			for(int i=(page-1)*5; i<page*5 && i<clSize; i++) {
				CChunkLoader chunkLoader=clList.get(i);
				sender.sendMessage(chunkLoader.toString());
			}
			
		}	
		return true;
	}
	
	static private boolean printChunkLoadersList(List<CChunkLoader> clList, CommandSender sender, int page) {

		int clSize=clList.size();
		if (clSize==0) {
			sender.sendMessage(ChatColor.RED + "There isn't any chunk loader yet!");
			return false;
		}
		
		int pages=(int) Math.ceil(clSize/5.00);

		if (page>pages) {
			sender.sendMessage(ChatColor.RED + "Invalid page");
			return false;
		}
		
		sender.sendMessage(ChatColor.GOLD + "== Chunk loaders list ("+page+"/"+pages+") ==");
		sender.sendMessage(ChatColor.GRAY + "(Owner - AlwaysOn - Size - Position)");
		
		for(int i=(page-1)*5; i<page*5 && i<clSize; i++) {
			CChunkLoader chunkLoader=clList.get(i);
			sender.sendMessage(chunkLoader.getOwnerName()+" - "+chunkLoader.toString());
		}
		return true;
	}

	private int getMaxChunksAllowed(Player player, boolean isOnlineOnly) {
		if (player.hasPermission("betterchunkloader.admin")) {
			// Admins have no maximum limit
			return Integer.MAX_VALUE;
		} else {
			String permissionPrefix = isOnlineOnly ? "betterchunkloader.onlineonly." : "betterchunkloader.alwayson.";

			int defaultLimit = isOnlineOnly ? this.instance.config().maxChunksAmountOnlineOnly : this.instance.config().maxChunksAmountAlwaysOn;

			for (int i = 1000; i >= 0; i--) {
				if (player.hasPermission(permissionPrefix + i)) {
					return i;
				}
			}

			// No permission-based limit found, return the default limit
			return defaultLimit;
		}
	}

	@SuppressWarnings("deprecation")
	private boolean chunks(CommandSender sender, String label, String[] args) {
		final String usage = "Usage: /"+label+" chunks (add|set) (PlayerName) (alwayson|onlineonly) (amount)";

		if (args.length < 5) {
			sender.sendMessage(chunksInfo((Player) sender));
			return false;
		}

		if (!sender.hasPermission("betterchunkloader.chunks")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
			return false;
		}

		OfflinePlayer player = Bukkit.getOfflinePlayer(args[2]);
		if (player == null) {
			sender.sendMessage(args[2] + " is not a valid player name\n" + usage);
			return false;
		}

		Integer amount;
		try {
			amount = Integer.valueOf(args[4]);
		} catch (NumberFormatException e) {
			sender.sendMessage("Invalid argument " + args[4] + "\n" + usage);
			return false;
		}

		if (args[1].equalsIgnoreCase("add")) {
			PlayerData playerData = DataStoreManager.getDataStore().getPlayerData(player.getUniqueId());
			if (sender.hasPermission("betterchunkloaders.chunks.add")) {
				boolean isOnlineOnly = args[3].equalsIgnoreCase("onlineonly");

				int maxChunksAllowed;
				int currentChunks;
				String chunksType;
				int maxChunksLimit;

				if (isOnlineOnly) {
					maxChunksAllowed = getMaxChunksAllowed((Player) player, true);
					currentChunks = playerData.getOnlineOnlyChunksAmount();
					chunksType = "online-only";
					maxChunksLimit = this.instance.config().maxChunksAmountOnlineOnly;
				} else {
					maxChunksAllowed = getMaxChunksAllowed((Player) player, false);
					currentChunks = playerData.getAlwaysOnChunksAmount();
					chunksType = "always-on";
					maxChunksLimit = this.instance.config().maxChunksAmountAlwaysOn;
				}

				if (currentChunks + amount > maxChunksAllowed) {
					if (args.length != 5 && args[5].equalsIgnoreCase("max")) {
						amount = maxChunksAllowed - currentChunks;
					} else if (args.length == 5 || !args[5].equalsIgnoreCase("force")) {
						sender.sendMessage("Couldn't add " + amount + " " + chunksType + " chunks to " + player.getName() +
								" because it would exceed the " + chunksType + " chunks limit of " + maxChunksLimit);
						return false;
					}
				}

				if (isOnlineOnly) {
					DataStoreManager.getDataStore().addOnlineOnlyChunksLimit(player.getUniqueId(), amount);
				} else {
					DataStoreManager.getDataStore().addAlwaysOnChunksLimit(player.getUniqueId(), amount);
				}

				sender.sendMessage("Added " + amount + " " + chunksType + " chunks to " + player.getName());
			} else {
				sender.sendMessage("You do not have permission to use this command.");
				return false;
			}
		} else if (args[1].equalsIgnoreCase("set")) {
			if (sender.hasPermission("betterchunkloaders.chunks.set")) {
				if (amount < 0) {
					sender.sendMessage("Invalid argument " + args[4] + "\n" + usage);
					return false;
				}

				boolean isOnlineOnly = args[3].equalsIgnoreCase("onlineonly");

				String chunksType;
				if (isOnlineOnly) {
					chunksType = "online-only";
				} else {
					chunksType = "always-on";
				}

				int maxChunksLimit;
				if (isOnlineOnly) {
					maxChunksLimit = this.instance.config().maxChunksAmountOnlineOnly;
				} else {
					maxChunksLimit = this.instance.config().maxChunksAmountAlwaysOn;
				}

				if (amount > maxChunksLimit) {
					sender.sendMessage("Cannot set more than " + maxChunksLimit + " " + chunksType + " chunks for " + player.getName());
					return false;
				}

				if (isOnlineOnly) {
					DataStoreManager.getDataStore().setOnlineOnlyChunksLimit(player.getUniqueId(), amount);
				} else {
					DataStoreManager.getDataStore().setAlwaysOnChunksLimit(player.getUniqueId(), amount);
				}

				sender.sendMessage("Set " + amount + " " + chunksType + " chunks to " + player.getName());
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

	@SuppressWarnings("deprecation")
	private boolean delete(CommandSender sender, String label, String[] args) {
		if (!sender.hasPermission("betterchunkloader.delete")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
			return false;
		}
		
		if (args.length<2) {
			sender.sendMessage(ChatColor.GOLD + "Usage: /bcl delete (PlayerName)");
			return false;
		}
		
		OfflinePlayer player = instance.getServer().getOfflinePlayer(args[1]);
		if (player==null || !player.hasPlayedBefore()) {
			sender.sendMessage(ChatColor.RED + "Player not found.");
			return false;
		}
		List<CChunkLoader> clList = DataStoreManager.getDataStore().getChunkLoaders(player.getUniqueId());
		if (clList==null) {
			sender.sendMessage(ChatColor.RED + "This player doesn't have any chunk loader.");
			return false;
		}
		
		DataStoreManager.getDataStore().removeChunkLoaders(player.getUniqueId());
		sender.sendMessage(ChatColor.RED + "All chunk loaders placed by this player have been removed.");
		instance.getLogger().info(sender.getName()+" deleted all chunk loaders placed by "+player.getName());
		return true;
	}
	
	private boolean purge(CommandSender sender) {
		if (!sender.hasPermission("betterchunkloader.purge")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
			return false;
		}
		
		IDataStore ds = DataStoreManager.getDataStore();
		List<CChunkLoader> chunkLoaders = new ArrayList<CChunkLoader>(DataStoreManager.getDataStore().getChunkLoaders());
		for (CChunkLoader cl : chunkLoaders) {
			if (!cl.blockCheck()) {
				ds.removeChunkLoader(cl);
			}
		}
		
		sender.sendMessage(ChatColor.GOLD+"All invalid chunk loaders have been removed.");

		return true;
	}

	private boolean reload(CommandSender sender) {
		if (!sender.hasPermission("betterchunkloader.reload")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
			return false;
		}

		instance.getLogger().info(sender.getName()+" reloaded this plugin");
		Bukkit.getPluginManager().disablePlugin(instance);
		Bukkit.getPluginManager().enablePlugin(instance);
		sender.sendMessage(ChatColor.RED + "BetterChunkLoader reloaded.");
		return true;
	}

	static String chunksInfo(OfflinePlayer player) {
		IDataStore dataStore = DataStoreManager.getDataStore();
		int freeAlwaysOn = dataStore.getAlwaysOnFreeChunksAmount(player.getUniqueId());
		int freeOnlineOnly = dataStore.getOnlineOnlyFreeChunksAmount(player.getUniqueId());
		PlayerData pd=dataStore.getPlayerData(player.getUniqueId());
		int amountAlwaysOn = pd.getAlwaysOnChunksAmount();
		int amountOnlineOnly = pd.getOnlineOnlyChunksAmount();

		return ChatColor.GOLD + "=== "+player.getName()+" chunks amount ===\n" + ChatColor.GREEN
				+ "Always-on - Free: "+freeAlwaysOn+" Used: "+(amountAlwaysOn-freeAlwaysOn)+" Total: "+amountAlwaysOn+"\n"
				+ "Online-only - Free: "+freeOnlineOnly+" Used: "+(amountOnlineOnly-freeOnlineOnly)+" Total: "+amountOnlineOnly;
	}
}
