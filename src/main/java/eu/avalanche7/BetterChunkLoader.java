package eu.avalanche7;

import java.io.File;
import java.util.UUID;

import eu.avalanche7.config.Config;
import eu.avalanche7.datastore.DataStoreManager;
import eu.avalanche7.datastore.MySqlDataStore;
import eu.avalanche7.datastore.XmlDataStore;
import net.kaikk.mc.bcl.forgelib.BCLForgeLib;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class BetterChunkLoader extends JavaPlugin {
	private static BetterChunkLoader instance;

	private Config config;

	public void onLoad() {
		DataStoreManager.registerDataStore("XML", XmlDataStore.class);
		DataStoreManager.registerDataStore("MySQL", MySqlDataStore.class);
	}

	public void onEnable() {
		try {
			Class.forName("net.minecraftforge.common.ForgeVersion");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Thermos/KCauldron/Crucible and BCLForgeLib are needed to run this plugin!");
		}
		try {
			Class.forName("net.kaikk.mc.bcl.forgelib.BCLForgeLib");
		} catch (ClassNotFoundException e) {
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
			if (DataStoreManager.getDataStore() == null)
				DataStoreManager.setDataStoreInstance(this.config.dataStore);
			getLogger().info("Loading " + DataStoreManager.getDataStore().getName() + " Data Store...");
			DataStoreManager.getDataStore().load();
			getLogger().info("Loaded " + DataStoreManager.getDataStore().getChunkLoaders().size() + " chunk loaders data.");
			getLogger().info("Loaded " + DataStoreManager.getDataStore().getPlayersData().size() + " players data.");
			int count = 0;
			for (CChunkLoader cl : DataStoreManager.getDataStore().getChunkLoaders()) {
				if (cl.isLoadable()) {
					BCLForgeLib.instance().addChunkLoader(cl);
					count++;
				}
			}
			getLogger().info("Loaded " + count + " always-on chunk loaders.");
			getLogger().info("Loading Listeners...");
			getServer().getPluginManager().registerEvents(new EventListener(this), (Plugin)this);
			getCommand("betterchunkloader").setExecutor(new CommandExec(this));
			getLogger().info("Load complete.");
		} catch (Exception e) {
			e.printStackTrace();
			getLogger().warning("Load failed!");
			Bukkit.getPluginManager().disablePlugin((Plugin)this);
		}
	}

	public void onDisable() {
		for (CChunkLoader cl : DataStoreManager.getDataStore().getChunkLoaders())
			BCLForgeLib.instance().removeChunkLoader(cl);
		instance = null;
	}

	public static BetterChunkLoader instance() {
		return instance;
	}

	public static long getPlayerLastPlayed(UUID playerId) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
		if (player.getLastPlayed() != 0L)
			return player.getLastPlayed();
		if (player.getName() != null && !player.getName().isEmpty())
			return getPlayerDataLastModified(playerId);
		return 0L;
	}

	public static long getPlayerDataLastModified(UUID playerId) {
		File playerData = new File(((World)Bukkit.getWorlds().get(0)).getWorldFolder(), "playerdata" + File.separator + playerId.toString() + ".dat");
		if (playerData.exists())
			return playerData.lastModified();
		return 0L;
	}

	public Config config() {
		return this.config;
	}
}

