package me.ceramictitan.spy;











import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
 
import java.io.IOException;
import java.util.logging.Logger;
 

import me.ceramictitan.listeners.playerListener;
import me.ceramictitan.util.MetricsLite;
import me.ceramictitan.util.UpdateChecker;

 
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
 
public class spy extends JavaPlugin implements Listener {
 
    protected UpdateChecker updateChecker;
    private String prefix = "[SPY]";
    private playerListener pl = new playerListener(this);
    public ProtocolManager protocolManager;
    
    	public void onLoad() {
    	    protocolManager = ProtocolLibrary.getProtocolManager();
    	}
 
 
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this.pl, this);
        loadConfig();
        loadMetrics();
        this.updateChecker = new UpdateChecker(this, "http://dev.bukkit.org/server-mods/enchantbroadcaster/files.rss");
        if(this.updateChecker.updateNeeded() && getConfig().getBoolean("Settings.updateCheck") == true){
                Bukkit.getConsoleSender().sendMessage(ChatColor.WHITE + prefix + ChatColor.RED+" A new Version of spy " +ChatColor.AQUA+"v"+ this.updateChecker.getVersion() + ChatColor.RED+" is now available!");
                Bukkit.getConsoleSender().sendMessage(ChatColor.WHITE + prefix + ChatColor.RED+" Download it from BukkitDev: " +ChatColor.AQUA+ this.updateChecker.getLink());
                Logger.getLogger("Minecraft").warning(prefix + " Please note: That the update checker will be fixed next update");
             // This is where the magic happens
                protocolManager.getAsynchronousManager().registerAsyncHandler(
                        new PacketAdapter(this, ConnectionSide.CLIENT_SIDE, Packets.Server.ARM_ANIMATION) {
                            
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        final int ATTACK_REACH = 4;
                        Player observer = event.getPlayer();
                        Location observerPos = observer.getEyeLocation();
                        Vector3D observerDir = new Vector3D(observerPos.getDirection());
                        
                        Vector3D observerStart = new Vector3D(observerPos);
                        Vector3D observerEnd = observerStart.add(observerDir.multiply(ATTACK_REACH));
                        
                        Player hit = null;
                        
                        // Get nearby entities
                        for (Player target : protocolManager.getEntityTrackers(observer)) {
                            // No need to simulate an attack if the player is already visible
                            if (!observer.canSee(target)) {
                                // Bounding box of the given player
                                Vector3D targetPos = new Vector3D(target.getLocation());
                                Vector3D minimum = targetPos.add(-0.5, 0, -0.5); 
                                Vector3D maximum = targetPos.add(0.5, 1.67, 0.5); 
            
                                if (hasIntersection(observerStart, observerEnd, minimum, maximum)) {
                                    if (hit == null || hit.getLocation().distanceSquared(observerPos) > target.getLocation().distanceSquared(observerPos)) {
                                        hit = target;
                                    }
                                }
                            }
                        }
                        
                        // Simulate a hit against the closest player
                        if (hit != null) {
                            PacketContainer useEntity = protocolManager.createPacket(Packets.Client.USE_ENTITY, false);
                            useEntity.getIntegers().
                                write(0, observer.getEntityId()).
                                write(1, hit.getEntityId()).
                                write(2, 1 /* LEFT_CLICK */);
                            	hit.getWorld().playEffect(hit.getLocation(), Effect.STEP_SOUND, Material.LAVA.getId());
                            	hit.sendMessage("You have been spotted!");
                            	if(playerListener.vanished.contains(hit.getName())){
                                	playerListener.vanished.remove(hit.getName());
                                toggleVisibilityNative(observer, hit);
                                playerListener.stopTask();
                            	}
                            try {
                                protocolManager.recieveClientPacket(event.getPlayer(), useEntity);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    
                    // Get entity trackers is not thread safe
                }).syncStart();
            }
    }

			// Source:
            //    http://www.gamedev.net/topic/338987-aabb---line-segment-intersection-test/
            private boolean hasIntersection(Vector3D p1, Vector3D p2, Vector3D min, Vector3D max) {
                final double epsilon = 0.0001f;
                
                Vector3D d = p2.subtract(p1).multiply(0.5);
                Vector3D e = max.subtract(min).multiply(0.5);
                Vector3D c = p1.add(d).subtract(min.add(max).multiply(0.5));
                Vector3D ad = d.abs();

                if (Math.abs(c.x) > e.x + ad.x)
                    return false;
                if (Math.abs(c.y) > e.y + ad.y)
                    return false;
                if (Math.abs(c.z) > e.z + ad.z)
                    return false;
              
                if (Math.abs(d.y * c.z - d.z * c.y) > e.y * ad.z + e.z * ad.y + epsilon)
                    return false;
                if (Math.abs(d.z * c.x - d.x * c.z) > e.z * ad.x + e.x * ad.z + epsilon)
                    return false;
                if (Math.abs(d.x * c.y - d.y * c.x) > e.x * ad.y + e.y * ad.x + epsilon)
                    return false;
                        
                return true;
            }
            
           public void toggleVisibilityNative(Player observer, Player target) {
        	   if(observer.canSee(target)){
                	  observer.hidePlayer(target);
     
                } else {
                    observer.showPlayer(target);
                }
            }
            
            @Override
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                Player observer = null;
                Player target = null;
                
                // Get the target argument
                if (args.length > 0) {
                    target = getServer().getPlayerExact(args[0]);
                } else {
                    sender.sendMessage(ChatColor.RED + "This command requires at least one argument.");
                    return true;
                }
                
                // Get the observer argument
                if (args.length == 2) {
                    observer = getServer().getPlayerExact(args[1]);
                } else {
                    if (sender instanceof Player) {
                        observer = (Player) sender;
                    } else {
                        sender.sendMessage(ChatColor.RED + "Optional parameter is only valid for player commands.");
                        return true;
                    }
                }
                
                toggleVisibilityNative(observer, target);
                return true;
            }

	private void loadConfig() {
        getConfig().addDefault("afk-timer", 10);// done
        getConfig().addDefault("Mobs.followSpy", false);// done
        getConfig().addDefault("Spy.canAttack", true);// done
        getConfig().addDefault("Spy.isInvincible", true);// done
        getConfig().addDefault("Spy.canPickUpItems", true);// done
        getConfig().addDefault("Spy.hungerDecay", true);// done
        getConfig().addDefault("Settings.updateCheck", true);// done
        getConfig().addDefault("Settings.metricsEnabled", true);// done
        getConfig().options().copyDefaults(true);
        saveConfig();
		
	}
	private void loadMetrics() {
  	  if (getConfig().getBoolean("Settings.metricsEnabled") == true) {
            try {
              Logger.getLogger("Minecraft").info("Successfully started plugin metrics! More info at mcstats.org");
                MetricsLite metrics = new MetricsLite(this);
                metrics.start();
            } catch (IOException e){
        }
        }
  }

}