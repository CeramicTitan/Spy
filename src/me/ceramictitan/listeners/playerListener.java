package me.ceramictitan.listeners;

import java.util.HashSet;
import java.util.Set;

import me.ceramictitan.AfkChecker.Schedulers;
import me.ceramictitan.spy.spy;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.ProtocolManager;

public class playerListener implements Listener{
	
	
	public static Set<String> vanished = new HashSet<String>();
	public static spy plugin;
	private static int id;
	public playerListener(spy plugin){
		playerListener.plugin = plugin;
	}
	
	
	@EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player p = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            if (p.getItemInHand().getType() == Material.REDSTONE) {
                if (vanished.contains(p.getName())) {
                    p.sendMessage("Already Vanished");
                    return;
                } else if (!vanished.contains(p.getName())) {
                	if(!p.hasPermission("spy.transform")){
                		p.sendMessage("No permission");
                		return;
                	}
                	vanished.add(p.getName());
                    for(Player play : Bukkit.getOnlinePlayers()){
                        plugin.toggleVisibilityNative(play, p);
                        }
                    Schedulers.getSchedulers().setup(plugin);
                    Schedulers.getSchedulers().startAFKChecker();
                    p.sendMessage("You are now hidden!");
                    p.getWorld().playEffect(p.getLocation(), Effect.STEP_SOUND,
                            Material.SNOW_BLOCK.getId());
 
                    if (vanished.contains(p.getName())) {
                        id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 
                            @Override
                            public void run() {
                            	if (p.getItemInHand().getType() == Material.REDSTONE) {
                                    p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
                                   
                                    if (p.getItemInHand().getType() == Material.REDSTONE) {
                                        if (p.getItemInHand().getAmount() == 1) {
                                            p.setItemInHand(new ItemStack(Material.AIR, 1));
                                        }
                                        p.getWorld().playEffect(p.getLocation(), Effect.SMOKE, 30);
                                        if (p.getItemInHand().getType() != Material.REDSTONE) {
                                        	if(vanished.contains(p.getName())){
                                        		vanished.remove(p.getName());
                                        	for(Player play : Bukkit.getOnlinePlayers()){
                                           plugin.toggleVisibilityNative(play, p);
                                            System.out.println("removed!");
 
                                            stopTask();
                                        }
                                        }
                                        }
                                    }
                            	}
                            }
                        }, 0L, 20L);
                    }
                    }
                }
            }
    }
    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            final Player player = (Player) event.getEntity();
            if (vanished.contains(player.getName()) && plugin.getConfig().getBoolean("Spy.hungerDecay") == false) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
    	Player p = event.getPlayer();
        if (vanished.contains(p.getName()) && plugin.getConfig().getBoolean("Spy.canPickUpItems") == false){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
    if(event.getEntity() instanceof Player){
    	Player p = (Player)event.getEntity();
    	if(vanished.contains(p.getName()) && plugin.getConfig().getBoolean("Mobs.followSpy") == false){
    		event.setCancelled(true);
    	}
    }
    }
    @EventHandler
    public void onItemChange(PlayerItemHeldEvent event) {
        if (event.getPlayer().getItemInHand().getType() == Material.REDSTONE) {
            if (!vanished.contains(event.getPlayer().getName())) {
                return;
            }
           
            event.getPlayer().sendMessage("No longer hidden");
            if(vanished.contains(event.getPlayer().getName())){
            	vanished.remove(event.getPlayer().getName());
                for(Player play : Bukkit.getOnlinePlayers()){
                    plugin.toggleVisibilityNative(play, event.getPlayer());
                    }
            }
                System.out.println("!");
            stopTask();
        }
    }

    public void onPlayerLogin(PlayerLoginEvent event, ProtocolManager protocolManager) {
        for (String playerName : vanished) {
            Player player = Bukkit.getPlayerExact(playerName);
            Player loginPlayer = event.getPlayer();
                plugin.toggleVisibilityNative(loginPlayer, player);           
        }
    }
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event){
    	if(event.getEntity() instanceof Player){
    	Player p = (Player)event.getEntity();
    	if(vanished.contains(p.getName()) && plugin.getConfig().getBoolean("Spy.isInvincible") == true){
        	event.setCancelled(true);
        }
    	}
    }
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
        if(vanished.contains(p.getName()) && plugin.getConfig().getBoolean("Spy.canAttack") == true){
        	event.setCancelled(false);
        }
        }
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (vanished.contains(event.getPlayer().getName())) {
            vanished.remove(event.getPlayer().getName());
        }
    }
    public static void stopTask() {
        Bukkit.getServer().getScheduler().cancelTask(id);
    }


}
