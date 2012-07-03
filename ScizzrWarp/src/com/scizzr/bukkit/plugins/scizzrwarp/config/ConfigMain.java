package com.scizzr.bukkit.plugins.scizzrwarp.config;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.scizzr.bukkit.plugins.scizzrwarp.Main;

public class ConfigMain extends JavaPlugin {
    File file = new File(getDataFolder() + "configMain.yml");
    
    static boolean changed = false;
    
    Main plugin;
    
    public ConfigMain (Main plugin) {
        this.plugin = plugin;
    }
    
    public static void main() {
        File file = new File(Main.fileConfigMain.getAbsolutePath());
        
        if (!file.exists()) {
            try {
                file.createNewFile();
                Main.log.info(Main.prefixConsole + "Blank configMain.yml created");
            } catch (IOException ex) {
                Main.log.info(Main.prefixConsole + "Failed to make configMain.yml");
                Main.suicide(ex);
            }
        }
        
        load();
    }
    
    static void load() {
        YamlConfiguration config = new YamlConfiguration();
        File file = Main.fileConfigMain;
        
        try {
            config.load(file);
        } catch (Exception ex) {
            Main.log.info(Main.prefixConsole + "There was a problem loading configMain.yml");
            Main.suicide(ex);
        }
        
        editOption(config, "general.uuid", null);
        
        checkOption(config, "general.prefix", Config.genPrefix);                    Config.genPrefix = config.getBoolean("general.prefix");
        checkOption(config, "general.stats", Config.genStats);                      Config.genStats = config.getBoolean("general.stats");
        checkOption(config, "general.uniqid", Config.genUniqID);                    Config.genUniqID = config.getString("general.uniqid");
        checkOption(config, "general.vercheck", Config.genVerCheck);                Config.genVerCheck = config.getBoolean("general.vercheck");
        checkOption(config, "general.autoupdate", Config.genAutoUpdate);            Config.genAutoUpdate = config.getBoolean("general.autoupdate");
        checkOption(config, "general.errorweb", Config.genErrorWeb);                Config.genErrorWeb = config.getBoolean("general.errorweb");
        
        checkOption(config, "economy.homes.basic.enabled", Config.econHomeBasEnab); Config.econHomeBasEnab = config.getBoolean("economy.homes.basic.enabled");
        checkOption(config, "economy.homes.basic.use", Config.econHomeBasUse);      Config.econHomeBasUse = config.getDouble("economy.homes.basic.use");
        checkOption(config, "economy.homes.basic.set", Config.econHomeBasSet);      Config.econHomeBasSet = config.getDouble("economy.homes.basic.set");
        checkOption(config, "economy.homes.adv.enabled", Config.econHomeAdvEnab);   Config.econHomeAdvEnab = config.getBoolean("economy.homes.adv.enabled");
        checkOption(config, "economy.homes.adv.use", Config.econHomeAdvUse);        Config.econHomeAdvUse = config.getDouble("economy.homes.adv.use");
        checkOption(config, "economy.homes.adv.set", Config.econHomeAdvSet);        Config.econHomeAdvSet = config.getDouble("economy.homes.adv.set");
        checkOption(config, "economy.warps.enabled", Config.econWarpEnab);          Config.econWarpEnab = config.getBoolean("economy.warps.enabled");
        checkOption(config, "economy.warps.use", Config.econWarpUse);               Config.econWarpUse = config.getDouble("economy.warps.use");
        checkOption(config, "economy.warps.set", Config.econWarpSet);               Config.econWarpSet = config.getDouble("economy.warps.set");
        
        checkOption(config, "permissions.allowops", Config.permAllowOps);           Config.permAllowOps = config.getBoolean("permissions.allowops");
        
        Main.prefix = (Config.genPrefix == true) ? Main.prefixMain : "";
        
        if (changed) {
            config.options().header(
                "Base Configuration - Main"
            );
            try { config.save(file); } catch (Exception ex) { Main.log.info(Main.prefixConsole + "Failed to save configMain.yml");
            Main.suicide(ex); }
        }
    }
    
    static void checkOption(YamlConfiguration config, String node, Object def) {
        if (!config.isSet(node)) {
            config.set(node, def);
            changed = true;
        }
    }
    
    static void editOption(YamlConfiguration config, String nodeOld, String nodeNew) {
        if (config.isSet(nodeOld)) {
            if (nodeNew != null) {
                config.set(nodeNew, config.get(nodeOld));
            }
            config.set(nodeOld, null);
            changed = true;
        }
    }
}
