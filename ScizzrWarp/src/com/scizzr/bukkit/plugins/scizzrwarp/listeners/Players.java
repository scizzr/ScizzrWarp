package com.scizzr.bukkit.plugins.scizzrwarp.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import com.scizzr.bukkit.plugins.scizzrwarp.Main;
import com.scizzr.bukkit.plugins.scizzrwarp.config.Config;
import com.scizzr.bukkit.plugins.scizzrwarp.config.PlayerOpt;
import com.scizzr.bukkit.plugins.scizzrwarp.threads.Update;

public class Players implements Listener {
    Main plugin;
    
    public Players(Main instance) {
        plugin = instance;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(final PlayerLoginEvent e) {
        PlayerOpt.checkAll(e.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent e) {
        Player p = e.getPlayer();
        
        if (Config.genVerCheck == true) {
            new Thread(new Update("check", p, null)).start();
        }
    }
}
