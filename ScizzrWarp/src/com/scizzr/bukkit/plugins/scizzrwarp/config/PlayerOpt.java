package com.scizzr.bukkit.plugins.scizzrwarp.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.scizzr.bukkit.plugins.scizzrwarp.Main;

public class PlayerOpt {
    static YamlConfiguration config = new YamlConfiguration();
    
    public static boolean load() {
        if(!Main.filePlayerOpt.exists()) {
            try {
                Main.filePlayerOpt.createNewFile();
                Main.log.info(Main.prefixConsole + "Blank playerOpt.yml created");
                return true;
            } catch (Exception ex) {
                Main.log.info(Main.prefixConsole + "Failed to make playerOpt.yml");
                Main.suicide(ex);
                return false;
            }
        } else {
            try {
                config.load(Main.filePlayerOpt);
                return true;
            } catch (Exception ex) { /* ex.printStackTrace(); */ return false; }
        }
    }
    
    public static void setOpt(Player p, String o, String v) {
        config.set(p.getName() + "." + o, v);
        
        try {
            config.save(Main.filePlayerOpt);
        } catch (Exception ex) { /*ex.printStackTrace();*/ }
    }
    
    public static String getOpt(Player p, String o) {
        try {
            config.load(Main.filePlayerOpt);
        } catch (Exception ex) { Main.log.info(Main.prefixConsole + "Failed to load playerOpt.yml"); Main.suicide(ex); }
        
        String val = config.getString(p.getName() + "." + o);
        
        return val != null ? val : null;
    }
    
    public static void checkAll(Player p) {
        try {
            config.load(Main.filePlayerOpt);
            
//            checkOption(config, p, "eff-pot-other", "true");
            
            config.save(Main.filePlayerOpt);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    static void checkOption(YamlConfiguration config, Player p, String opt, String def) {
        if (!config.isSet(p.getName() + "." + opt)) {
            config.set(p.getName() + "." + opt, def);
            try { config.save(Main.filePlayerOpt); } catch (Exception ex) { Main.log.info(Main.prefixConsole + "Failed to save playerOpt.yml"); Main.suicide(ex); }
        }
    }
    
    static void editOption(YamlConfiguration config, Player p, String nodeOld, String nodeNew) {
        if (config.isSet(p.getName() + "." + nodeOld)) {
            if (nodeNew != null) {
                config.set(p.getName() + "." + nodeNew, config.get(p.getName() + "." + nodeOld));
            }
            config.set(nodeOld, null);
            try { config.save(Main.filePlayerOpt); } catch (Exception ex) { p.sendMessage(Main.prefix + "Failed to save playerOpt.yml"); Main.suicide(ex); }
        }
    }
}
