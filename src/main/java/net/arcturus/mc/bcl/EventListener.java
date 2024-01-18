package net.arcturus.mc.bcl;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import net.arcturus.mc.bcl.datastore.DataStoreManager;
import net.kaikk.mc.bcl.forgelib.BCLForgeLib;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class EventListener implements Listener {
	private BetterChunkLoader instance;

	private int alwaysOnBlockId;

	private int alwaysOnBlockData;

	private int onlineOnlyBlockId;

	private int onlineOnlyBlockData;

	EventListener(BetterChunkLoader instance) {
		this.instance = instance;
		this.alwaysOnBlockId = instance.getConfig().getInt("alwaysOnBlockId", 57);
		this.alwaysOnBlockData = instance.getConfig().getInt("alwaysOnBlockData", 0);
		this.onlineOnlyBlockId = instance.getConfig().getInt("onlineOnlyBlockId", 42);
		this.onlineOnlyBlockData = instance.getConfig().getInt("onlineOnlyBlockData", 0);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	void onPlayerInteract(PlayerInteractEvent event) {
		Action action = event.getAction();
		Player player = event.getPlayer();
		Block clickedBlock = event.getClickedBlock();
		if (clickedBlock == null || player == null)
			return;
		if ((clickedBlock.getTypeId() == this.alwaysOnBlockId || clickedBlock.getTypeId() == this.onlineOnlyBlockId) &&
				action == Action.RIGHT_CLICK_BLOCK) {
			CChunkLoader chunkLoader = DataStoreManager.getDataStore().getChunkLoaderAt(new BlockLocation(clickedBlock.getLocation()));
			if (player.getItemInHand().getType() == Material.BLAZE_ROD) {
				if (chunkLoader != null) {
					if (player.getUniqueId().equals(chunkLoader.getOwner()) || player.hasPermission("betterchunkloader.edit") || (chunkLoader.isAdminChunkLoader() && player.hasPermission("betterchunkloader.adminloader"))) {
						chunkLoader.showUI(player);
					} else {
						player.sendMessage(ChatColor.RED + "You can't edit others' chunk loaders.");
					}
				} else if (canBreak(clickedBlock, player)) {
					UUID uid = player.getUniqueId();
					if (clickedBlock.getTypeId() == this.alwaysOnBlockId && clickedBlock.getData() == this.alwaysOnBlockData) {
						if (!player.hasPermission("betterchunkloader.alwayson")) {
							player.sendMessage(ChatColor.RED + "You don't have the permission to create always-on chunk loaders." + (player.isOp() ? " (betterchunkloader.alwayson is needed)" : ""));
							return;
						}
						if (player.isSneaking() && player.hasPermission("betterchunkloader.adminloader"))
							uid = CChunkLoader.adminUUID;
					} else if (clickedBlock.getTypeId() == this.onlineOnlyBlockId && clickedBlock.getData() == this.onlineOnlyBlockData) {
						if (!player.hasPermission("betterchunkloader.onlineonly")) {
							player.sendMessage(ChatColor.RED + "You don't have the permission to create online-only chunk loaders." + (player.isOp() ? " (betterchunkloader.onlineonly is needed)" : ""));
							return;
						}
					} else {
						return;
					}
					chunkLoader = new CChunkLoader((int)Math.floor(clickedBlock.getX() / 16.0D), (int)Math.floor(clickedBlock.getZ() / 16.0D), clickedBlock.getWorld().getName(), (byte)-1, uid, new BlockLocation(clickedBlock), null, (clickedBlock.getType() == Material.DIAMOND_BLOCK));
					chunkLoader.showUI(player);
				} else {
					player.sendMessage(ChatColor.RED + "You haven't build permission at this location");
				}
			} else if (chunkLoader != null) {
				player.sendMessage(chunkLoader.info());
			} else {
				player.sendMessage(ChatColor.GOLD + "This Blocks can be converted into chunk loaders. Right click it with a blaze rod.");
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		if (block == null || (block.getTypeId() != this.alwaysOnBlockId && block.getTypeId() != this.onlineOnlyBlockId))
			return;
		if (block.getData() != this.alwaysOnBlockData && block.getData() != this.onlineOnlyBlockData)
			return;
		CChunkLoader chunkLoader = DataStoreManager.getDataStore().getChunkLoaderAt(new BlockLocation(block.getLocation()));
		if (chunkLoader == null)
			return;
		DataStoreManager.getDataStore().removeChunkLoader(chunkLoader);
		Player player = event.getPlayer();
		player.sendMessage(ChatColor.RED + "Chunk loader removed.");
		Player owner = chunkLoader.getPlayer();
		if (owner != null && player != owner)
			owner.sendMessage(ChatColor.RED + "Your chunk loader at " + chunkLoader.getLoc().toString() + " has been removed by " + player.getDisplayName() + ".");
		BetterChunkLoader.instance().getLogger().info(player.getName() + " broke " + chunkLoader.getOwnerName() + "'s chunk loader at " + chunkLoader.getLocationString());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void onPlayerLogin(PlayerLoginEvent event) {
		if (event.getResult() != PlayerLoginEvent.Result.ALLOWED)
			return;
		List<CChunkLoader> clList = DataStoreManager.getDataStore().getChunkLoaders(event.getPlayer().getUniqueId());
		for (CChunkLoader chunkLoader : clList) {
			if (!chunkLoader.isAlwaysOn() && chunkLoader.blockCheck())
				BCLForgeLib.instance().addChunkLoader(chunkLoader);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void onPlayerQuit(PlayerQuitEvent event) {
		List<CChunkLoader> clList = DataStoreManager.getDataStore().getChunkLoaders(event.getPlayer().getUniqueId());
		for (CChunkLoader chunkLoader : clList) {
			if (!chunkLoader.isAlwaysOn())
				BCLForgeLib.instance().removeChunkLoader(chunkLoader);
		}
	}

	@EventHandler(ignoreCancelled = true)
	void onInventoryClick(InventoryClickEvent event) {
		if (event.getInventory().getHolder() instanceof CChunkLoader && event.getWhoClicked() instanceof Player) {
			Player player = (Player)event.getWhoClicked();
			event.setCancelled(true);
			CChunkLoader chunkLoader = (CChunkLoader)event.getInventory().getHolder();
			if (chunkLoader == null)
				return;
			if (chunkLoader.isAdminChunkLoader()) {
				if (!player.hasPermission("betterchunkloader.adminloader")) {
					player.sendMessage(ChatColor.RED + "You don't have permissions for this!");
					return;
				}
			} else if (!player.getUniqueId().equals(chunkLoader.getOwner()) && !player.hasPermission("betterchunkloader.edit")) {
				player.sendMessage(ChatColor.RED + "You can't edit others' chunk loaders.");
				return;
			}
			byte pos = (byte)event.getRawSlot();
			if (chunkLoader.getRange() != -1) {
				if (pos == 0) {
					DataStoreManager.getDataStore().removeChunkLoader(chunkLoader);
					closeInventory(player);
				} else if (pos > 1 && pos < 7) {
					pos = (byte)(pos - 2);
					if (!chunkLoader.isAdminChunkLoader() && !player.hasPermission("betterchunkloader.unlimitedchunks") &&
							pos > chunkLoader.getRange()) {
						int available, needed = (1 + pos * 2) * (1 + pos * 2) - chunkLoader.size();
						if (chunkLoader.isAlwaysOn()) {
							available = DataStoreManager.getDataStore().getAlwaysOnFreeChunksAmount(chunkLoader.getOwner());
						} else {
							available = DataStoreManager.getDataStore().getOnlineOnlyFreeChunksAmount(chunkLoader.getOwner());
						}
						if (needed > available) {
							player.sendMessage(ChatColor.RED + "Not enough free chunks! Needed: " + needed + ". Available: " + available + ".");
							closeInventory(player);
							return;
						}
					}
					BetterChunkLoader.instance().getLogger().info(player.getName() + " edited " + chunkLoader.getOwnerName() + "'s chunk loader at " + chunkLoader.getLocationString() + " range from " + chunkLoader.getRange() + " to " + pos);
					DataStoreManager.getDataStore().changeChunkLoaderRange(chunkLoader, pos);
					player.sendMessage(ChatColor.GOLD + "Chunk Loader updated.");
					closeInventory(player);
				}
			} else if (pos > 1 && pos < 7) {
				pos = (byte)(pos - 2);
				if (!chunkLoader.isAdminChunkLoader() && !player.hasPermission("betterchunkloader.unlimitedchunks")) {
					int available, needed = (1 + pos * 2) * (1 + pos * 2);
					if (chunkLoader.isAlwaysOn()) {
						available = DataStoreManager.getDataStore().getAlwaysOnFreeChunksAmount(chunkLoader.getOwner());
					} else {
						available = DataStoreManager.getDataStore().getOnlineOnlyFreeChunksAmount(chunkLoader.getOwner());
					}
					if (needed > available) {
						player.sendMessage(ChatColor.RED + "Not enough free chunks! Needed: " + needed + ". Available: " + available + ".");
						closeInventory(player);
						return;
					}
				}
				chunkLoader.setRange(pos);
				chunkLoader.setCreationDate(new Date());
				BetterChunkLoader.instance().getLogger().info(player.getName() + " made a new " + (chunkLoader.isAdminChunkLoader() ? "admin " : "") + "chunk loader at " + chunkLoader.getLocationString() + " with range " + pos);
				DataStoreManager.getDataStore().addChunkLoader(chunkLoader);
				closeInventory(player);
				player.sendMessage(ChatColor.GOLD + "Chunk Loader created.");
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void onWorldLoad(WorldLoadEvent event) {
		for (CChunkLoader cl : DataStoreManager.getDataStore().getChunkLoaders(event.getWorld().getName())) {
			if (cl.isLoadable())
				BCLForgeLib.instance().addChunkLoader(cl);
		}
	}

	private static void closeInventory(final Player p) {
		(new BukkitRunnable() {
			public void run() {
				p.closeInventory();
			}
		}).runTaskLater((Plugin)BetterChunkLoader.instance(), 1L);
	}

	static boolean canBreak(Block block, Player player) {
		BlockBreakEvent bbe = new BlockBreakEvent(block, player);
		BetterChunkLoader.instance().getServer().getPluginManager().callEvent((Event)bbe);
		return !bbe.isCancelled();
	}
}