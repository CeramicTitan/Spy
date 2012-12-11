package me.ceramictitan.AfkChecker;




import me.ceramictitan.listeners.playerListener;
import me.ceramictitan.spy.spy;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;




public class Schedulers {
	
	private spy plugin;
	static Schedulers instance = new Schedulers();
	Integer afkchecker;
	public void setup(spy plugin){
		this.plugin=plugin;
	}
	public static Schedulers getSchedulers()
	  {
	    return instance;
	  }
	public void startAFKChecker() {
	    final Integer afklimit = this.plugin.getConfig().getInt("afk-timer");
	    this.afkchecker = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable()
	    {

			@Override
			public void run() {
				for(String playerName : playerListener.vanished){
				Player player = Bukkit.getPlayerExact(playerName);
				Integer afktime = PlayerData.getAFKTime(player);
	            Location lastloc = PlayerData.getLastLocation(player);
	            Location currentloc = new Location(player.getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
	            if (lastloc != null) {
	            	System.out.println("Last location not null");
	                if ((lastloc.getWorld().getName() == currentloc.getWorld().getName()) && (lastloc.getBlockX() == currentloc.getBlockX()) && (lastloc.getBlockY() == currentloc.getBlockY()) && (lastloc.getBlockZ() == currentloc.getBlockZ()))
	                {
	                  if (afktime == null){
	                	  System.out.println("afktime null");
	                    PlayerData.setAFKTime(player, Integer.valueOf(1));
	                  }else {
	                	 System.out.println("afktime not null");
	                    PlayerData.setAFKTime(player, Integer.valueOf(afktime.intValue() + 1));
	                  }

	                  if (afklimit == afktime) {
	                	  System.out.println("afklimit == afktime");
	                    player.sendMessage("You have been forced visible due to AFK!");
	                    if(playerListener.vanished.contains(player.getName())){
	                    	playerListener.vanished.remove(player.getName());
	                    for(Player play : Bukkit.getOnlinePlayers()){
	                       play.showPlayer(player);
	                        }
	                    }
	                        playerListener.stopTask();
	                    PlayerData.setAFKTime(player, null);
	                    PlayerData.unsetLastLocation(player);
	                  }
	                } else {
	                	System.out.println("Location null");
	                  PlayerData.setAFKTime(player, null);
	                  PlayerData.unsetLastLocation(player);
	                }
	                PlayerData.setLastLocation(player);
	              } else {
	            	  System.out.println("Last Location null");
	                PlayerData.setLastLocation(player);
	              }
	          }
			}
				}, 20L, 20L);
	}
	public void stopScheduler(){
		Bukkit.getScheduler().cancelTask(this.afkchecker);
	}
}