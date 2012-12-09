package me.ceramictitan.AfkChecker;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerData {
	
	 public static HashMap<String, Location> locations = new HashMap<String, Location>();
	  public static HashMap<String, Integer> times = new HashMap<String, Integer>();

	  public static Location getLastLocation(Player player) {
	    return (Location)locations.get(player.getName());
	  }

	  public static void setLastLocation(Player player) {
	    locations.put(player.getName(), new Location(player.getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()));
	  }

	  public static void unsetLastLocation(Player player) {
	    locations.put(player.getName(), null);
	  }

	  public static Integer getAFKTime(Player player) {
	    try {
	      return (Integer)times.get(player.getName());
	    }
	    catch (NullPointerException localNullPointerException) {
	    }
	    return null;
	  }

	  public static void setAFKTime(Player player, Integer time) {
	    times.put(player.getName(), time);
	  }

}
