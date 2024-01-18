package net.arcturus.mc.bcl;

import org.bukkit.Material;

public class Config {
	public int maxHoursOffline;

	public int defaultChunksAmountAlwaysOn;

	public int defaultChunksAmountOnlineOnly;

	public String dataStore;

	public String mySqlHostname;

	public String mySqlPort;

	public String mySqlUsername;

	public String mySqlPassword;

	public String mySqlDatabase;

	public int alwaysOnBlockId;

	public int onlineOnlyBlockId;

	public int alwaysOnBlockData;

	public int onlineOnlyBlockData;

	Config(BetterChunkLoader instance) {
		instance.getConfig().options().copyDefaults(true);
		instance.saveDefaultConfig();
		this.maxHoursOffline = instance.getConfig().getInt("MaxHoursOffline", 168);
		this.defaultChunksAmountAlwaysOn = instance.getConfig().getInt("DefaultChunksAmount.AlwaysOn", 5);
		this.defaultChunksAmountOnlineOnly = instance.getConfig().getInt("DefaultChunksAmount.OnlineOnly", 50);
		String alwaysOnBlockConfig = instance.getConfig().getString("alwaysOnBlockId", "57:0");
		String[] alwaysOnBlockParts = alwaysOnBlockConfig.split(":");
		this.alwaysOnBlockId = Integer.parseInt(alwaysOnBlockParts[0]);
		this.alwaysOnBlockData = (alwaysOnBlockParts.length > 1) ? Integer.parseInt(alwaysOnBlockParts[1]) : 0;
		String onlineOnlyBlockConfig = instance.getConfig().getString("onlineOnlyBlockId", "42:0");
		String[] onlineOnlyBlockParts = onlineOnlyBlockConfig.split(":");
		this.onlineOnlyBlockId = Integer.parseInt(onlineOnlyBlockParts[0]);
		this.onlineOnlyBlockData = (onlineOnlyBlockParts.length > 1) ? Integer.parseInt(onlineOnlyBlockParts[1]) : 0;
		this.dataStore = instance.getConfig().getString("DataStore");
		this.mySqlHostname = instance.getConfig().getString("MySQL.Hostname");
		this.mySqlPort = instance.getConfig().getString("MySQL.Port");
		if (mySqlPort == null || mySqlPort.isEmpty()) {
			this.mySqlPort = "3306";
		}
		this.mySqlUsername = instance.getConfig().getString("MySQL.Username");
		this.mySqlPassword = instance.getConfig().getString("MySQL.Password");
		this.mySqlDatabase = instance.getConfig().getString("MySQL.Database");
		String onlineOnlyBlockMaterialName = instance.getConfig().getString("OnlineOnlyBlockMaterial", "IRON_BLOCK");
		Material onlineOnlyMaterial = Material.getMaterial(onlineOnlyBlockMaterialName);
		if (onlineOnlyMaterial == null) {
			onlineOnlyMaterial = Material.getMaterial(this.onlineOnlyBlockId);
			if (onlineOnlyMaterial == null) {
				onlineOnlyMaterial = Material.IRON_BLOCK;
				instance.getLogger().warning("Invalid material and ID: " + onlineOnlyBlockMaterialName);
			}
		}
		String alwaysOnBlockMaterialName = instance.getConfig().getString("AlwaysOnBlockMaterial", "DIAMOND_BLOCK");
		Material alwaysOnMaterial = Material.getMaterial(alwaysOnBlockMaterialName);
		if (alwaysOnMaterial == null) {
			alwaysOnMaterial = Material.getMaterial(this.alwaysOnBlockId);
			if (alwaysOnMaterial == null) {
				alwaysOnMaterial = Material.DIAMOND_BLOCK;
				instance.getLogger().warning("Invalid material and ID: " + alwaysOnBlockMaterialName);
			}
		}
	}
}
