package net.arcturus.mc.bcl;

import org.bukkit.Material;

public class Config {
	public int maxHoursOffline, defaultChunksAmountAlwaysOn, defaultChunksAmountOnlineOnly, maxChunksAmountAlwaysOn, maxChunksAmountOnlineOnly, onlineOnlyMeta, alwaysOnMeta;
	public String dataStore, mySqlHostname, mySqlUsername, mySqlPassword, mySqlDatabase;
	public Material onlineOnlyMaterial, alwaysOnMaterial;
	
	Config(BetterChunkLoader instance) {
		instance.getConfig().options().copyDefaults(true);
		instance.saveDefaultConfig();

		this.onlineOnlyMeta = instance.getConfig().getInt("OnlineOnlyBlockMetadata", 1);
		this.alwaysOnMeta = instance.getConfig().getInt("AlwaysOnBlockMetadata", 2);
		
		this.maxHoursOffline=instance.getConfig().getInt("MaxHoursOffline", 168);
		
		this.defaultChunksAmountAlwaysOn=instance.getConfig().getInt("DefaultChunksAmount.AlwaysOn", 5);
		this.defaultChunksAmountOnlineOnly=instance.getConfig().getInt("DefaultChunksAmount.OnlineOnly", 50);

		this.maxChunksAmountAlwaysOn=instance.getConfig().getInt("MaxChunksAmount.AlwaysOn", 57);
		this.maxChunksAmountOnlineOnly=instance.getConfig().getInt("MaxChunksAmount.OnlineOnly", 75);

		this.dataStore=instance.getConfig().getString("DataStore");
		
		this.mySqlHostname=instance.getConfig().getString("MySQL.Hostname");
		this.mySqlUsername=instance.getConfig().getString("MySQL.Username");
		this.mySqlPassword=instance.getConfig().getString("MySQL.Password");
		this.mySqlDatabase=instance.getConfig().getString("MySQL.Database");
		
		String ms = instance.getConfig().getString("OnlineOnlyBlockMaterial", "IRON_BLOCK");
		Material m = Material.getMaterial(ms);
		if (m == null) {
			m = Material.IRON_BLOCK;
			instance.getLogger().warning("Invalid material: "+ms);
		}
		onlineOnlyMaterial = m;
		
		ms = instance.getConfig().getString("AlwaysOnBlockMaterial", "DIAMOND_BLOCK");
		m = Material.getMaterial(ms);
		if (m == null) {
			m = Material.DIAMOND_BLOCK;
			instance.getLogger().warning("Invalid material: "+ms);
		}
		alwaysOnMaterial = m;
	}
}