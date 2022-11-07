package com.technicjelle.bluemapchatmarkers;

import com.flowpowered.math.vector.Vector3d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.HtmlMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public final class Main extends JavaPlugin implements Listener {

	private final int seconds = 60; //TODO: Make this configurable

	private String style; //This is configurable in the style.css file

	private final String MARKERSET_ID = "chat-markers"; //No need to make this configurable

	@Override
	public void onEnable() {
		// Plugin startup logic

		Metrics metrics = new Metrics(this, 16424);

		BlueMapAPI.onEnable(onEnableListener);
	}

	Consumer<BlueMapAPI> onEnableListener = (api) -> {
		getServer().getPluginManager().registerEvents(this, this);

		//noinspection ResultOfMethodCallIgnored
		getDataFolder().mkdirs();
		Path styleFile = getDataFolder().toPath().resolve("textStyle.css");
		if (!Files.exists(styleFile)) {
			try {
				Files.copy(getResource("textStyle.css"), styleFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			style = Files.readString(styleFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	};

	private void createMarkerSet(BlueMapWorld bmWorld) {
		getLogger().info("Creating MarkerSet for BlueMap World " + bmWorld.getId());
		MarkerSet markerSet = new MarkerSet("Chat Markers");
		markerSet.setDefaultHidden(false);
		markerSet.setToggleable(true);

		//add the markerset to all BlueMap maps of this world
		for (BlueMapMap map : bmWorld.getMaps()) {
			map.getMarkerSets().put(MARKERSET_ID, markerSet);
		}

		//make a marker that the loads in the style to be used for all the chat markers
		HtmlMarker marker = new HtmlMarker("MarkerStyleLoader", new Vector3d(0, 0, 0), "<style>" + style + "</style>");

		markerSet.put("style-loader", marker);
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		BlueMapAPI.getInstance().ifPresent(api -> {
			Player player = event.getPlayer();
			BlueMapWorld bmWorld = api.getWorld(player.getWorld()).orElse(null);
			if (bmWorld == null) return; //world not loaded in BlueMap, ignore
//			if (!api.getWebApp().getPlayerVisibility(player.getUniqueId())) return; //player hidden on BlueMap, ignore //TODO: Turn this back on once BlueMap fixes this

			Location location = player.getLocation();

			HtmlMarker marker = new HtmlMarker(player.getName() + ": " + event.getMessage(),
					new Vector3d(location.getX(), location.getY(), location.getZ()),
					"<div class='chatMarker'>" + event.getMessage() + "</div>");

			//for all BlueMap Maps belonging to the BlueMap World the Player is in, add the Marker to the MarkerSet of that BlueMap World
			bmWorld.getMaps().forEach(map -> {
				if(!map.getMarkerSets().containsKey(MARKERSET_ID)) //if this world doesn't have a MarkerSet yet, create it
					createMarkerSet(bmWorld); //creates a new MarkerSet, and assigns it to each Map of this World

				MarkerSet markerSet = map.getMarkerSets().get(MARKERSET_ID);

				String key = String.valueOf(event.hashCode());

				//add Marker to the MarkerSet
				markerSet.put(key, marker);

				//wait Seconds and remove the Marker
				Bukkit.getScheduler().runTaskLater(this,
						() -> markerSet.remove(key),
						seconds * 20);
			});
		});
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
		BlueMapAPI.unregisterListener(onEnableListener);
	}
}
