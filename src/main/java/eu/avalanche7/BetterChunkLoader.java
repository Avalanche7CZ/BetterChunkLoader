package eu.avalanche7;

import java.io.File;
import java.util.UUID;

import eu.avalanche7.config.Config;
import eu.avalanche7.datastore.DataStoreManager;
import eu.avalanche7.datastore.MariaDBDataStore;
import eu.avalanche7.datastore.MySqlDataStore;
import eu.avalanche7.datastore.XmlDataStore;
import net.kaikk.mc.bcl.forgelib.BCLForgeLib;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class BetterChunkLoader extends JavaPlugin {
	private static BetterChunkLoader instance;

	private Config config;

	@Override
	public void onLoad() {
		instance = this;
		DataStoreManager.registerDataStore("XML", XmlDataStore.class);
		DataStoreManager.registerDataStore("MySQL", MySqlDataStore.class);
		DataStoreManager.registerDataStore("MariaDB", MariaDBDataStore.class);
	}

	@Override
	public void onEnable() {
		try {
			Class.forName("net.minecraftforge.common.ForgeVersion");
		} catch (ClassNotFoundException e) {
			getLogger().severe("Thermos/KCauldron/Crucible (or a compatible Forge environment) is NOT detected!");
			throw new RuntimeException("Thermos/KCauldron/Crucible and BCLForgeLib are needed to run this plugin!");
		}
		try {
			Class.forName("net.kaikk.mc.bcl.forgelib.BCLForgeLib");
		} catch (ClassNotFoundException e) {
			getLogger().severe("BCLForgeLib mod is NOT detected!");
			throw new RuntimeException("BCLForgeLib is needed to run this plugin!");
		}

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new PlaceHolderIntegration(this).register();
			getLogger().info("PlaceholderAPI found and integrated.");
		} else {
			getLogger().warning("PlaceholderAPI not found. Placeholders will not be available.");
		}

		Bukkit.getConsoleSender().sendMessage("=========================");
		Bukkit.getConsoleSender().sendMessage("BetterChunkLoader");
		Bukkit.getConsoleSender().sendMessage("Version " + getDescription().getVersion());
		Bukkit.getConsoleSender().sendMessage("Fork Author: Avalanche7CZ");
		Bukkit.getConsoleSender().sendMessage("=========================");

		instance = this;
		try {
			getLogger().info("Loading config...");
			this.config = new Config(this);
			if (DataStoreManager.getDataStore() == null) {
				DataStoreManager.setDataStoreInstance(this.config.dataStore);
			}
			getLogger().info("Loading " + DataStoreManager.getDataStore().getName() + " Data Store...");
			DataStoreManager.getDataStore().load();

			getLogger().info("Loaded " + DataStoreManager.getDataStore().getChunkLoaders().size() + " chunk loaders data from primary datastore.");
			getLogger().info("Loaded " + DataStoreManager.getDataStore().getPlayersData().size() + " players data from primary datastore.");

			getLogger().info("Synchronizing all chunk loaders with BCLForgeLib...");
			int activatedCount = 0;
			int deactivatedCount = 0;
			for (CChunkLoader cl : DataStoreManager.getDataStore().getChunkLoaders()) {
				if (cl.isLoadable()) {
					BCLForgeLib.instance().addChunkLoader(cl);
					activatedCount++;
				} else {
					BCLForgeLib.instance().removeChunkLoader(cl);
					deactivatedCount++;
				}
			}
			getLogger().info("Synchronization complete: Activated " + activatedCount + " chunk loaders, ensured " + deactivatedCount + " non-loadable ones are deactivated in BCLForgeLib.");

			getLogger().info("Loading Listeners...");
			getServer().getPluginManager().registerEvents(new EventListener(this), this);
			getCommand("betterchunkloader").setExecutor(new CommandExec(this));
			getLogger().info("Load complete.");
		} catch (Exception e) {
			e.printStackTrace();
			getLogger().severe("Load failed! Disabling plugin.");
			Bukkit.getPluginManager().disablePlugin(this);
		}
	}

	@Override
	public void onDisable() {
		getLogger().info("Disabling BetterChunkLoader...");
		if (DataStoreManager.getDataStore() != null && DataStoreManager.getDataStore().getChunkLoaders() != null) {
			getLogger().info("Removing all chunk loaders from BCLForgeLib...");
			for (CChunkLoader cl : DataStoreManager.getDataStore().getChunkLoaders()) {
				if (BCLForgeLib.instance() != null) {
					BCLForgeLib.instance().removeChunkLoader(cl);
				}
			}
			getLogger().info("All chunk loaders removed from BCLForgeLib.");
		} else {
			getLogger().warning("DataStore not available or no chunk loaders to remove during disable.");
		}
		instance = null;
		getLogger().info("BetterChunkLoader disabled.");
	}

	public static BetterChunkLoader instance() {
		return instance;
	}

	public static long getPlayerLastPlayed(UUID playerId) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
		if (player.getLastPlayed() != 0L) {
			return player.getLastPlayed();
		}
		if (player.getName() != null && !player.getName().isEmpty()) {
			return getPlayerDataLastModified(playerId);
		}
		return 0L;
	}

	public static long getPlayerDataLastModified(UUID playerId) {
		if (Bukkit.getWorlds().isEmpty()) {
			BetterChunkLoader.instance().getLogger().warning("Cannot get player data last modified: No worlds loaded.");
			return 0L;
		}
		World world = Bukkit.getWorlds().get(0);
		File worldFolder = world.getWorldFolder();

		File playerDataFile = new File(worldFolder, "playerdata" + File.separator + playerId.toString() + ".dat");
		if (playerDataFile.exists()) {
			return playerDataFile.lastModified();
		}
		return 0L;
	}

	public Config config() {
		return this.config;
	}
}
