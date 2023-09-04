package net.arcturus.mc.bcl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Messages {
	private static Map<String, String> messages = new HashMap<String, String>();
	
	public static void load(JavaPlugin instance, String fileName) {
		if (!new File(instance.getDataFolder(), fileName).exists()) {
			instance.saveResource(fileName, false);
		}
		
		@SuppressWarnings("deprecation")
		FileConfiguration defaultMessages = YamlConfiguration.loadConfiguration(instance.getResource(fileName));
		FileConfiguration messages = YamlConfiguration.loadConfiguration(new File(instance.getDataFolder(), fileName));
		
		Messages.messages.clear();
		for (String key : defaultMessages.getKeys(false)) {
			Messages.messages.put(key, ChatColor.translateAlternateColorCodes('&', defaultMessages.getString(key)));
		}
		for (String key : messages.getKeys(false)) {
			Messages.messages.put(key, ChatColor.translateAlternateColorCodes('&', messages.getString(key)));
		}
		
	}
	
	public static String get(String id) {
		return messages.get(id);
	}
}
