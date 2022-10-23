package com.technicjelle.bluemapchatmarkers;

import com.flowpowered.math.vector.Vector3d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.HtmlMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public final class Main extends JavaPlugin implements Listener {

	private final int seconds = 60;

	private String style;

	private Map<UUID, MarkerSet> markerSets;

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

		markerSets = new HashMap<>();

		//loop through all worlds on the server
		List<World> worlds = Bukkit.getWorlds();
		for (World world : worlds) {
			//make a new markerset for this world
			MarkerSet markerSet = new MarkerSet("Chat Markers");
			markerSet.setDefaultHidden(false);
			markerSet.setToggleable(true);

			//add the markerset to all BlueMap maps of this world
			api.getWorld(world).ifPresent(bmWorld -> {
				for (BlueMapMap map : bmWorld.getMaps()) {
					map.getMarkerSets().put("chat-markers", markerSet);
				}
			});

			//add the markerset to the global world,markerset collection, so it can be accessed later
			markerSets.put(world.getUID(), markerSet);

			//make a marker that the loads in the style to be used for all the chat markers
			HtmlMarker marker = new HtmlMarker("MarkerStyleLoader", new Vector3d(0, 0, 0), "<style>" + style + "</style>");

			markerSet.getMarkers().put("style-loader", marker);
		}
	};

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		BlueMapAPI.getInstance().ifPresent(api -> {
			Location location = event.getPlayer().getLocation();

			HtmlMarker marker = new HtmlMarker(event.getPlayer().getName() + ": " + event.getMessage(),
					new Vector3d(location.getX(), location.getY(), location.getZ()),
					"<div class='chatMarker'>" + event.getMessage() + "</div>");

			markerSets.get(event.getPlayer().getWorld().getUID()).getMarkers()
					.put(String.valueOf(event.hashCode()), marker);

			//wait seconds and remove the marker
			Bukkit.getScheduler().runTaskLater(this, () ->
				markerSets.get(event.getPlayer().getWorld().getUID()).getMarkers()
						.remove(String.valueOf(event.hashCode())),
				20 * seconds);
		});
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
		BlueMapAPI.unregisterListener(onEnableListener);
	}
}
