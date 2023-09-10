package net.arcturus.mc.bcl;

public class Config {
	public int maxHoursOffline, defaultChunksAmountAlwaysOn, defaultChunksAmountOnlineOnly, maxChunksAmountAlwaysOn, maxChunksAmountOnlineOnly;
	public String dataStore, mySqlHostname, mySqlUsername, mySqlPassword, mySqlDatabase;
	public int alwaysOnBlockId, onlineOnlyBlockId, alwaysOnBlockData, onlineOnlyBlockData;

	Config(BetterChunkLoader instance) {
		instance.getConfig().options().copyDefaults(true);
		instance.saveDefaultConfig();

		this.maxHoursOffline = instance.getConfig().getInt("MaxHoursOffline", 168);

		this.defaultChunksAmountAlwaysOn = instance.getConfig().getInt("DefaultChunksAmount.AlwaysOn", 5);
		this.defaultChunksAmountOnlineOnly = instance.getConfig().getInt("DefaultChunksAmount.OnlineOnly", 50);

		this.maxChunksAmountAlwaysOn=instance.getConfig().getInt("MaxChunksAmount.AlwaysOn", 55);
		this.maxChunksAmountOnlineOnly=instance.getConfig().getInt("MaxChunksAmount.OnlineOnly", 150);

		// Parse the block ID and metadata for alwaysOnBlock
		String alwaysOnBlockConfig = instance.getConfig().getString("alwaysOnBlockId", "57:0");
		String[] alwaysOnBlockParts = alwaysOnBlockConfig.split(":");
		alwaysOnBlockId = Integer.parseInt(alwaysOnBlockParts[0]);
		alwaysOnBlockData = alwaysOnBlockParts.length > 1 ? Integer.parseInt(alwaysOnBlockParts[1]) : 0;

		// Parse the block ID and metadata for onlineOnlyBlock
		String onlineOnlyBlockConfig = instance.getConfig().getString("onlineOnlyBlockId", "42:0");
		String[] onlineOnlyBlockParts = onlineOnlyBlockConfig.split(":");
		onlineOnlyBlockId = Integer.parseInt(onlineOnlyBlockParts[0]);
		onlineOnlyBlockData = onlineOnlyBlockParts.length > 1 ? Integer.parseInt(onlineOnlyBlockParts[1]) : 0;

		this.dataStore = instance.getConfig().getString("DataStore");

		this.mySqlHostname = instance.getConfig().getString("MySQL.Hostname");
		this.mySqlUsername = instance.getConfig().getString("MySQL.Username");
		this.mySqlPassword = instance.getConfig().getString("MySQL.Password");
		this.mySqlDatabase = instance.getConfig().getString("MySQL.Database");

		int onlineOnlyBlockId = instance.getConfig().getInt("onlineOnlyBlockId", 42);
		int onlineOnlyBlockData = instance.getConfig().getInt("onlineOnlyBlockData", 0);

		if (onlineOnlyBlockData < 0 || onlineOnlyBlockData > 15) {
			onlineOnlyBlockData = 0;
			instance.getLogger().warning("Invalid data value: " + onlineOnlyBlockData);
		}

		int alwaysOnBlockId = instance.getConfig().getInt("alwaysOnBlockId", 57);
		int alwaysOnBlockData = instance.getConfig().getInt("alwaysOnBlockData", 0);

		if (alwaysOnBlockData < 0 || alwaysOnBlockData > 15) {
			alwaysOnBlockData = 0;
			instance.getLogger().warning("Invalid data value: " + alwaysOnBlockData);
		}
	}
}