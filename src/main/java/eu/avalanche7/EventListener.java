package eu.avalanche7;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import eu.avalanche7.datastore.DataStoreManager;
import net.kaikk.mc.bcl.forgelib.BCLForgeLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
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
import org.bukkit.scheduler.BukkitRunnable;

public class EventListener implements Listener {
	private BetterChunkLoader pluginInstance;

	private int alwaysOnBlockId;
	private int alwaysOnBlockData;
	private int onlineOnlyBlockId;
	private int onlineOnlyBlockData;

	EventListener(BetterChunkLoader instance) {
		this.pluginInstance = instance;
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
					if (player.getUniqueId().equals(chunkLoader.getOwner()) || player.hasPermission(PermissionNode.ADMIN_EDIT_ALL) || (chunkLoader.isAdminChunkLoader() && player.hasPermission(PermissionNode.ADMIN_LOADER))) {
						chunkLoader.showUI(player);
					} else {
						player.sendMessage(ChatColor.RED + "You can't edit others' chunk loaders.");
					}
				} else if (canBreak(clickedBlock, player)) {
					UUID uid = player.getUniqueId();
					boolean isCreatingAlwaysOn = false;
					if (clickedBlock.getTypeId() == this.alwaysOnBlockId && clickedBlock.getData() == this.alwaysOnBlockData) {
						if (!player.hasPermission(PermissionNode.CHUNK_ALWAYSON)) {
							player.sendMessage(ChatColor.RED + "You don't have the permission to create always-on chunk loaders." + (player.isOp() ? " (betterchunkloader.alwayson is needed)" : ""));
							return;
						}
						if (player.isSneaking() && player.hasPermission(PermissionNode.ADMIN_LOADER)) {
							uid = CChunkLoader.adminUUID;
						}
						isCreatingAlwaysOn = true;
					} else if (clickedBlock.getTypeId() == this.onlineOnlyBlockId && clickedBlock.getData() == this.onlineOnlyBlockData) {
						if (!player.hasPermission(PermissionNode.CHUNK_ONLINEONLY)) {
							player.sendMessage(ChatColor.RED + "You don't have the permission to create online-only chunk loaders." + (player.isOp() ? " (betterchunkloader.onlineonly is needed)" : ""));
							return;
						}
						isCreatingAlwaysOn = false;
					} else {
						return;
					}
					chunkLoader = new CChunkLoader((int)Math.floor(clickedBlock.getX() / 16.0D), (int)Math.floor(clickedBlock.getZ() / 16.0D), clickedBlock.getWorld().getName(), (byte)-1, uid, new BlockLocation(clickedBlock), null, isCreatingAlwaysOn);
					chunkLoader.showUI(player);
				} else {
					player.sendMessage(ChatColor.RED + "You haven't build permission at this location");
				}
			} else if (chunkLoader != null) {
				player.sendMessage(chunkLoader.info());
				if (player.isSneaking()) {
					chunkLoader.showCorners(player);
				}
			} else {
				player.sendMessage(ChatColor.GOLD + "This Block can be converted into a chunk loader. Right click it with a blaze rod.");
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		if (block == null) return;

		boolean isChunkLoaderBlockType = (block.getTypeId() == this.alwaysOnBlockId && block.getData() == this.alwaysOnBlockData) ||
				(block.getTypeId() == this.onlineOnlyBlockId && block.getData() == this.onlineOnlyBlockData);
		if (!isChunkLoaderBlockType) return;

		CChunkLoader chunkLoader = DataStoreManager.getDataStore().getChunkLoaderAt(new BlockLocation(block.getLocation()));
		if (chunkLoader == null) return;

		Player player = event.getPlayer();
		if (!player.getUniqueId().equals(chunkLoader.getOwner()) &&
				!player.hasPermission(PermissionNode.ADMIN_EDIT_ALL) &&
				!(chunkLoader.isAdminChunkLoader() && player.hasPermission(PermissionNode.ADMIN_LOADER))) {
			player.sendMessage(ChatColor.RED + "You don't have permission to break this chunk loader.");
			event.setCancelled(true);
			return;
		}

		DataStoreManager.getDataStore().removeChunkLoader(chunkLoader);

		player.sendMessage(ChatColor.RED + "Chunk loader removed.");
		Player ownerOnline = chunkLoader.getPlayer();
		String ownerName = chunkLoader.getOwnerName();

		if (ownerOnline != null && !ownerOnline.equals(player)) {
			ownerOnline.sendMessage(ChatColor.RED + "Your chunk loader at " + chunkLoader.getLoc().toString() + " has been removed by " + player.getDisplayName() + ".");
		} else if (ownerOnline == null && !chunkLoader.isAdminChunkLoader() && ownerName != null && !ownerName.isEmpty() && !ownerName.equalsIgnoreCase("Admin")) {
			if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
				String mailMessage = "Your chunk loader at " + chunkLoader.getLoc().toString() + " was broken by " + player.getDisplayName() + ".";
				String mailCommand = "mail send " + ownerName + " " + mailMessage;
				this.pluginInstance.getLogger().info("Attempting to send Essentials mail to offline owner " + ownerName + ": " + mailCommand);
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), mailCommand);
			} else {
				this.pluginInstance.getLogger().info("Essentials plugin not found. Cannot send mail to offline owner " + ownerName + " for broken chunk loader.");
			}
		}
		this.pluginInstance.getLogger().info(player.getName() + " broke " + chunkLoader.getOwnerName() + "'s chunk loader at " + chunkLoader.getLocationString());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void onPlayerLogin(PlayerLoginEvent event) {
		if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) return;

		final UUID playerUUID = event.getPlayer().getUniqueId();
		new BukkitRunnable() {
			@Override
			public void run() {
				Player player = pluginInstance.getServer().getPlayer(playerUUID);
				if (player == null || !player.isOnline()) return;

				pluginInstance.getLogger().info("Player " + player.getName() + " logged in. Checking online-only chunk loaders.");
				List<CChunkLoader> clList = DataStoreManager.getDataStore().getChunkLoaders(playerUUID);
				int activatedCount = 0;
				for (CChunkLoader chunkLoader : clList) {
					if (!chunkLoader.isAlwaysOn() && chunkLoader.blockCheck()) {
						if (chunkLoader.isLoadable()) {
							BCLForgeLib.instance().addChunkLoader(chunkLoader);
							activatedCount++;
						}
					}
				}
				if (activatedCount > 0) {
					pluginInstance.getLogger().info("Activated " + activatedCount + " online-only chunk loaders for " + player.getName());
				}
			}
		}.runTaskLater(this.pluginInstance, 20L);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		pluginInstance.getLogger().info("Player " + player.getName() + " logged out. Deactivating their online-only chunk loaders.");
		List<CChunkLoader> clList = DataStoreManager.getDataStore().getChunkLoaders(player.getUniqueId());
		int deactivatedCount = 0;
		for (CChunkLoader chunkLoader : clList) {
			if (!chunkLoader.isAlwaysOn()) {
				BCLForgeLib.instance().removeChunkLoader(chunkLoader);
				deactivatedCount++;
			}
		}
		if (deactivatedCount > 0) {
			pluginInstance.getLogger().info("Deactivated " + deactivatedCount + " online-only chunk loaders for " + player.getName());
		}
	}

	@EventHandler(ignoreCancelled = true)
	void onInventoryClick(InventoryClickEvent event) {
		if (event.getInventory().getHolder() instanceof CChunkLoader && event.getWhoClicked() instanceof Player) {
			Player player = (Player)event.getWhoClicked();
			event.setCancelled(true);
			CChunkLoader chunkLoader = (CChunkLoader)event.getInventory().getHolder();
			if (chunkLoader == null) return;

			if (chunkLoader.isAdminChunkLoader()) {
				if (!player.hasPermission(PermissionNode.ADMIN_LOADER)) {
					player.sendMessage(ChatColor.RED + "You don't have permissions for this!");
					closeInventory(player);
					return;
				}
			} else if (!player.getUniqueId().equals(chunkLoader.getOwner()) && !player.hasPermission(PermissionNode.ADMIN_EDIT_ALL)) {
				player.sendMessage(ChatColor.RED + "You can't edit others' chunk loaders.");
				closeInventory(player);
				return;
			}

			byte pos = (byte)event.getRawSlot();
			if (chunkLoader.getRange() != -1) {
				if (pos == 0) {
					DataStoreManager.getDataStore().removeChunkLoader(chunkLoader);
					player.sendMessage(ChatColor.GREEN + "Chunk loader removed.");
					closeInventory(player);
				} else if (pos >= 2 && pos <= 6) {
					byte newRange = (byte)(pos - 2);
					if (newRange > this.pluginInstance.config().maxRange) {
						player.sendMessage(ChatColor.RED + "That range is too large! The maximum is " + this.pluginInstance.config().maxRange);
						closeInventory(player);
						return;
					}
					if (!chunkLoader.isAdminChunkLoader() && !player.hasPermission(PermissionNode.CHUNKS_UNLIMITED)) {
						int currentSize = chunkLoader.size();
						int newSize = (1 + newRange * 2) * (1 + newRange * 2);
						int needed = newSize - currentSize;

						int available;
						if (chunkLoader.isAlwaysOn()) {
							available = DataStoreManager.getDataStore().getAlwaysOnFreeChunksAmount(chunkLoader.getOwner());
						} else {
							available = DataStoreManager.getDataStore().getOnlineOnlyFreeChunksAmount(chunkLoader.getOwner());
						}

						if (needed > 0 && needed > available) {
							player.sendMessage(ChatColor.RED + "Not enough free chunks! Needed: " + needed + ". Available: " + available + ".");
							closeInventory(player);
							return;
						}
					}
					this.pluginInstance.getLogger().info(player.getName() + " edited " + chunkLoader.getOwnerName() + "'s chunk loader at " + chunkLoader.getLocationString() + " range from " + chunkLoader.getRange() + " to " + newRange);
					DataStoreManager.getDataStore().changeChunkLoaderRange(chunkLoader, newRange);
					player.sendMessage(ChatColor.GOLD + "Chunk Loader updated to size " + chunkLoader.sizeX() + ".");
					closeInventory(player);
				}
			} else {
				if (pos >= 2 && pos <= 6) {
					byte newRange = (byte)(pos - 2);
					if (newRange > this.pluginInstance.config().maxRange) {
						player.sendMessage(ChatColor.RED + "That range is too large! The maximum is " + this.pluginInstance.config().maxRange);
						closeInventory(player);
						return;
					}
					if (!chunkLoader.isAdminChunkLoader() && !player.hasPermission(PermissionNode.CHUNKS_UNLIMITED)) {
						int needed = (1 + newRange * 2) * (1 + newRange * 2);
						int available;
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
					chunkLoader.setRange(newRange);
					chunkLoader.setCreationDate(new Date());
					this.pluginInstance.getLogger().info(player.getName() + " made a new " + (chunkLoader.isAdminChunkLoader() ? "admin " : "") + "chunk loader at " + chunkLoader.getLocationString() + " with range " + newRange);
					DataStoreManager.getDataStore().addChunkLoader(chunkLoader);
					player.sendMessage(ChatColor.GOLD + "Chunk Loader created with size " + chunkLoader.sizeX() + ".");
					closeInventory(player);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void onWorldLoad(WorldLoadEvent event) {
		this.pluginInstance.getLogger().info("Bukkit WorldLoadEvent for: " + event.getWorld().getName() + ". Synchronizing its chunk loaders with BCLForgeLib.");
		int activatedInWorld = 0;
		int deactivatedInWorld = 0;
		if (DataStoreManager.getDataStore() == null) {
			this.pluginInstance.getLogger().warning("DataStore not available during WorldLoadEvent for " + event.getWorld().getName());
			return;
		}

		for (CChunkLoader cl : DataStoreManager.getDataStore().getChunkLoaders(event.getWorld().getName())) {
			if (cl.isLoadable()) {
				BCLForgeLib.instance().addChunkLoader(cl);
				activatedInWorld++;
			} else {
				BCLForgeLib.instance().removeChunkLoader(cl);
				deactivatedInWorld++;
			}
		}
		this.pluginInstance.getLogger().info("For world " + event.getWorld().getName() + ": Activated " + activatedInWorld + ", ensured deactivation for " + deactivatedInWorld + " via BCLForgeLib.");
	}

	private static void closeInventory(final Player p) {
		new BukkitRunnable() {
			public void run() {
				p.closeInventory();
			}
		}.runTaskLater(BetterChunkLoader.instance(), 1L);
	}

	static boolean canBreak(Block block, Player player) {
		BlockBreakEvent bbe = new BlockBreakEvent(block, player);
		BetterChunkLoader.instance().getServer().getPluginManager().callEvent(bbe);
		return !bbe.isCancelled();
	}
}