package com.technicjelle.BlueMapChatMarkers;

import com.technicjelle.MCUtils.ConfigUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;

public class Config {
	public static final String MARKER_SET_ID = "chat-markers";

	private final BlueMapChatMarkers plugin;

	private final String markerSetName;
	private final boolean toggleable;
	private final boolean defaultHidden;
	private final long markerDuration;
	private final boolean forceful;

	public Config(BlueMapChatMarkers plugin) {
		this.plugin = plugin;

		try {
			ConfigUtils.copyPluginResourceToConfigDir(plugin, "config.yml", "config.yml", false);
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

	public String getMarkerSetName() {
		return markerSetName;
	}

	public boolean isToggleable() {
		return toggleable;
	}

	public boolean isDefaultHidden() {
		return defaultHidden;
	}

	public long getMarkerDuration() {
		return markerDuration;
	}

	public boolean getForceful() {
		return forceful;
	}
}
