package com.technicjelle.bluemapchatmarkers;

import com.technicjelle.MCUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;

public class Config {
	public static final String MARKER_SET_ID = "chat-markers";

	private final BlueMapChatMarkers plugin;

	public String markerSetName;
	public boolean toggleable;
	public boolean defaultHidden;
	public boolean forceful;
	public long markerDuration;

	public Config(BlueMapChatMarkers plugin) {
		this.plugin = plugin;

		try {
			MCUtils.copyPluginResourceToConfigDir(plugin, "config.yml", "config.yml", false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		//Load config from disk
		plugin.reloadConfig();

		//Load config values into variables
		markerSetName = configFile().getString("MarkerSetName", "Chat Messages");
		toggleable = configFile().getBoolean("Toggleable", true);
		defaultHidden = configFile().getBoolean("DefaultHidden", false);
		markerDuration = configFile().getLong("MarkerDuration", 60);
		forceful = configFile().getBoolean("Forceful", false);
	}

	private FileConfiguration configFile() {
		return plugin.getConfig();
	}
}
