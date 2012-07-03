package com.scizzr.bukkit.plugins.scizzrwarp;

import java.io.File;
import java.util.Calendar;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.scizzr.bukkit.plugins.scizzrwarp.config.Config;
import com.scizzr.bukkit.plugins.scizzrwarp.config.ConfigMain;
import com.scizzr.bukkit.plugins.scizzrwarp.config.PlayerOpt;
import com.scizzr.bukkit.plugins.scizzrwarp.listeners.Players;
import com.scizzr.bukkit.plugins.scizzrwarp.threads.Errors;
import com.scizzr.bukkit.plugins.scizzrwarp.threads.Stats;
import com.scizzr.bukkit.plugins.scizzrwarp.threads.Update;
import com.scizzr.bukkit.plugins.scizzrwarp.util.MoreString;
import com.scizzr.bukkit.plugins.scizzrwarp.util.Vault;

public class Main extends JavaPlugin {
    public static Logger log = Logger.getLogger("Minecraft");
    public static PluginDescriptionFile info;
    public static PluginManager pm;
    public static Plugin plugin;
    
    public static String prefixConsole, prefixMain, prefix;
    
    boolean isScheduled = false;
    int lastTick;
    
    static int exTimer = 0;
    
    public static File fileFolder, fileConfigMain, fileHomes, fileWarps, filePlayerOpt, df;
    
    public static YamlConfiguration config;
    
    public static String osN = System.getProperty("os.name").toLowerCase();
    public static String os = (osN.contains("windows") ? "Windows" :
                                (osN.contains("linux")) ? "Linux" :
                                    (osN.contains("mac")) ? "Macintosh":
                                        osN);
    
    public static String slash = os.equalsIgnoreCase("Windows") ? "\\" : "/";
    
    public static File filePlug = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("\\", "/"));
    public static String jar = filePlug.getAbsolutePath().split("/")[filePlug.getAbsolutePath().split("/").length - 1];
    
    public static int calY, calM, calD, calH, calI, calS;
    public static String calA;
    
    public void onEnable() {
        info = getDescription();
        
        prefixConsole = "[" + info.getName() + "] ";
        prefixMain = ChatColor.LIGHT_PURPLE + "[" + ChatColor.YELLOW + info.getName() + ChatColor.LIGHT_PURPLE + "]" + ChatColor.RESET + " ";
        
        pm = getServer().getPluginManager();
        
        final Players listenerPlayers = new Players(this);
        pm.registerEvents(listenerPlayers, this);
        
        plugin = pm.getPlugin(info.getName());
        
        fileFolder = getDataFolder();
        
        if (!fileFolder.exists()) {
            log.info(prefixConsole + "Creating our plugin's folder.");
            try {
                fileFolder.mkdir();
            } catch (Exception ex) {
                log.info(prefixConsole + "Error making our plugin's folder.");
                suicide(ex);
            }
        }
        
        fileConfigMain = new File(getDataFolder() + slash + "config.yml");
        ConfigMain.main();
        
        df = getDataFolder();
        
        fileHomes = new File(getDataFolder() + slash + "homes.txt");
        Manager.loadHomes();
        
        fileWarps = new File(getDataFolder() + slash + "warps.txt");
        Manager.loadWarps();
        
        filePlayerOpt = new File(getDataFolder() + slash + "playerOpt.yml");
        PlayerOpt.load();
        
        Vault.setupPermissions();
        log.info(prefixConsole + "Permissions has been setup.");
        
        Vault.setupEconomy();
        log.info(prefixConsole + "Economy has been setup.");
        
        new Thread(new Stats()).start();
        
        if (Config.genVerCheck == true) {
            new Thread(new Update("check", null, null)).start();
        }
        
        if (!isScheduled) {
            isScheduled = true;
            Bukkit.getScheduler().scheduleSyncRepeatingTask(this,
                    new Runnable() {
                        public void run() {
                            Calendar cal = Calendar.getInstance();
                            calY = cal.get(Calendar.YEAR);
                            calM = cal.get(Calendar.MONTH) + 1;
                            calD = cal.get(Calendar.DAY_OF_MONTH);
                            calH = cal.get(Calendar.HOUR);
                            calI = cal.get(Calendar.MINUTE);
                            calS = cal.get(Calendar.SECOND);
                            calA = cal.get(Calendar.AM_PM) == 0 ? "AM" : "PM";
                            
                            if (lastTick % 20 == 0) {
                                lastTick = 0;
                            }
                            
                            lastTick++;
                        }
                    }, 0L, 1L);
        }
        
        log.info(prefixConsole + "Plugin Enabled");
    }
    
    public void onDisable() {
        log.info(prefixConsole + "Plugin disabled.");
    }
    
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        Player p = null; boolean enough = false;
        
        if (sender instanceof Player) { p = (Player) sender; } else { sender.sendMessage(prefixConsole + "Only players can use this command."); return true; }
        
        String home = null, warp = null;
        String world = null, name = null;
        
        if (commandLabel.equalsIgnoreCase("home")) {
            if (args.length == 0) {
// home: basic : use
                world = p.getWorld().getName();
                name = "basic";
            } else if (args.length == 1) {
                if (!args[0].equalsIgnoreCase("help")) {
                    if (!args[0].startsWith("w:")) {
// home: advanced : use (current world)
                        world = p.getWorld().getName();
                        name = args[0];
                    } else {
// home: basic : use (another world)
                        world = args[0].substring(2);
                        name = "basic";
                    }
                }
            } else if (args.length == 2) {
// home: advanced : use (another world)
                if (args[0].startsWith("w:")) {
                    world = p.getWorld().getName();
                    name = args[1];
                }
            }
            
            home = Manager.homeExists(p.getName(), world, name);
            
            if (home != null) {
                String type = (name == "basic") ? "basic" : "advanced";
                if (Vault.hasPermission(p, "homes." + type + ".use")) {
                    if (type.equalsIgnoreCase("basic")) { if (Vault.hasMoney(p, Config.econHomeBasUse, Config.econHomeBasEnab, "homes.basic.nocost.use")) { enough = true; } }
                    if (type.equalsIgnoreCase("advanced")) { if (Vault.hasMoney(p, Config.econHomeAdvUse, Config.econHomeAdvEnab, "homes.advanced.nocost.use")) { enough = true; } }
                    if (enough == true) {
                        Manager.homeUse(p, world, name);
                    } else {
                        p.sendMessage(prefix + "You don't have enough money to use a" + (type == "basic" ? " " : "n ") + type + " home.");
                    }
                } else {
                    p.sendMessage(prefix + "You don't have permission to use " + type + " homes");
                }
            } else {
                p.sendMessage(prefix + ((name == "basic") ? "You haven't set a basic home yet" : "You haven't set that home yet"));
            }
            return true;
        } else if (commandLabel.equalsIgnoreCase("sethome")) {
            if (args.length == 0) {
// home: basic : set
                world = p.getWorld().getName();
                name = "basic";
            } else if (args.length == 1) {
// home: advanced : set
                if (args[0].equalsIgnoreCase("help")) {
                    p.sendMessage(prefix + "Homes cannot be named '" + args[0] + "'");
                    return true;
                } else if (!args[0].matches("[a-zA-Z0-9]{1,12}")) {
                    p.sendMessage(prefix + "Home names must be 1 to 12 alphanumeric characters");
                    return true;
                }
                world = p.getWorld().getName();
                name = args[0];
            }
            String type = (name == "basic") ? "basic" : "advanced";
            if (Vault.hasPermission(p, "homes." + type + ".set")) {
                if (type.equalsIgnoreCase("basic")) { if (Vault.hasMoney(p, Config.econHomeBasSet, Config.econHomeBasEnab, "homes.basic.nocost.set")) { enough = true; } }
                if (type.equalsIgnoreCase("advanced")) { if (Vault.hasMoney(p, Config.econHomeAdvSet, Config.econHomeAdvEnab, "homes.advanced.nocost.set")) { enough = true; } }
                if (enough == true) {
                    Manager.homeSet(p, world, name);
                } else {
                    p.sendMessage(prefix + "You don't have enough money to set " + type + " homes.");
                }
            } else {
                p.sendMessage(prefix + "You don't have permission to set " + type + " homes");
            }
            return true;
        } else if (commandLabel.equalsIgnoreCase("delhome")) {
            if (args.length == 0) {
// home: basic : del
                world = p.getWorld().getName();
                name = "basic";
            } else if (args.length == 1) {
                if (!args[0].startsWith("w:")) {
// home: advanced : del (current world)
                    world = p.getWorld().getName();
                    name = args[0];
                } else {
// home: basic : del (another world)
                    world = args[0].substring(2);
                    name = "basic";
                }
            } else if (args.length == 2) {
// home: advanced : del (another world)
                if (args[0].startsWith("w:")) {
                    world = p.getWorld().getName();
                    name = args[1];
                }
            }
            
            home = Manager.homeExists(p.getName(), world, name);
            
            if (home != null) {
                String type = (name == "basic") ? "basic" : "advanced";
                if (Vault.hasPermission(p, "homes." + type + ".del")) {
                    Manager.homeDel(p, world, name);
                } else {
                    p.sendMessage(prefix + "You don't have permission to delete " + type + " homes");
                }
            } else {
                p.sendMessage(prefix + ((name == "basic") ? "You haven't set a basic home yet" : "You haven't set that home yet"));
            }
            return true;
        } else if (commandLabel.equalsIgnoreCase("homes")) {
// home: list
            if (Vault.hasPermission(p, "homes.list")) {
                Manager.homeList(p, p.getName().toLowerCase());
            } else {
                p.sendMessage(prefix + "You don't have permission to list your homes.");
            }
            return true;
        }
        
        
        
        
        
        if (commandLabel.equalsIgnoreCase("warp")) {
            if (args.length == 0) {
                p.chat("/warps");
                return true;
            } else if (args.length == 1) {
/*
// warp: list
                warpList(p, p.getName());
                return true;
            } else if (args.length == 1) {
*/
                if (!args[0].equalsIgnoreCase("help")) {
                    if (!args[0].startsWith("w:")) {
// warp: basic : use (current world)
                        world = p.getWorld().getName();
                        name = args[0];
                    } else {
// warp: use (another world)
                        world = args[0].substring(2);
                        name = args[0];
                    }
                } else {
// warp: help
                    p.sendMessage("Help here!");
                    return true;
                }
            } else if (args.length == 2) {
// warp: use (another world)
                if (args[0].startsWith("w:")) {
                    world = p.getWorld().getName();
                    name = args[1];
                }
            }
            
            warp = Manager.warpExists(world, name);
            
            if (warp != null) {
                if (Vault.hasPermission(p, "warps.use")) {
                    if (Vault.hasMoney(p, Config.econWarpUse, Config.econWarpEnab, "warps.nocost.use")) { enough = true; }
                    if (enough == true) {
                        Manager.warpUse(p, world, name);
                    } else {
                        p.sendMessage(prefix + "You don't have enough money to use a warp.");
                    }
                } else {
                    p.sendMessage(prefix + "You don't have permission to use warps.");
                }
            } else {
                p.sendMessage(prefix + "That warp hasn't been set yet");
            }
            return true;
        } else if (commandLabel.equalsIgnoreCase("setwarp")) {
            if (args.length == 0) {
                p.sendMessage(prefix + "You must give the warp a name.");
                return true;
            } else if (args.length == 1) {
// warp: set
                if (args[0].equalsIgnoreCase("help")) {
                    p.sendMessage(prefix + "Warps cannot be named '" + args[0] + "'");
                    return true;
                } else if (!args[0].matches("[a-zA-Z0-9]{1,12}")) {
                    p.sendMessage(prefix + "Warp names must be 1 to 12 alphanumeric characters");
                    return true;
                }
                world = p.getWorld().getName();
                name = args[0];
            }
            if (Vault.hasPermission(p, "warps.set")) {
                if (Vault.hasMoney(p, Config.econWarpSet, Config.econWarpEnab, "warps.nocost.set")) { enough = true; }
                if (enough == true) {
                    Manager.warpSet(p, world, name);
                } else {
                    p.sendMessage(prefix + "You don't have enough money to set a warp.");
                }
            } else {
                p.sendMessage(prefix + "You don't have permission to set warps");
            }
            return true;
        } else if (commandLabel.equalsIgnoreCase("delwarp")) {
            if (args.length == 0) {
                p.sendMessage(prefix + "You must specify a warp to delete.");
                return true;
            } else if (args.length == 1) {
                if (!args[0].startsWith("w:")) {
// warp: del (current world)
                    world = p.getWorld().getName();
                    name = args[0];
                }
            } else if (args.length == 2) {
// warp: del (another world)
                if (args[0].startsWith("w:")) {
                    world = p.getWorld().getName();
                    name = args[1];
                }
            }
            
            warp = Manager.warpExists(world, name);
            
            if (warp != null) {
                if (Vault.hasPermission(p, "warps.del")) {
                    Manager.warpDel(p, world, name);
                } else {
                    p.sendMessage(prefix + "You don't have permission to delete warps");
                }
            } else {
                p.sendMessage(prefix + "That warp hasn't been set yet");
            }
            return true;
        } else if (commandLabel.equalsIgnoreCase("warps")) {
// warp: list
            if (Vault.hasPermission(p, "warps.list")) {
                Manager.warpList(p, p.getName().toLowerCase());
            } else {
                p.sendMessage(prefix + "You don't have permission to list the warps.");
            }
            return true;
        }
        
        
        
        
        
        if (commandLabel.equalsIgnoreCase("sw")) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("ver") || args[0].equalsIgnoreCase("version")) {
                    p.sendMessage(prefix + "Version " + info.getVersion() + " on " + osN); return true;
                } else if (args[0].equalsIgnoreCase("reload")) {
                    
//sw reload
                    p.chat("/sw reload config");
                    p.chat("/sw reload homes");
                    p.chat("/sw reload warps");
                    return true;
                }
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (args[1].equalsIgnoreCase("all")) {
                        p.chat("/sw reload");
                        return true;
                    }
                    if (args[1].equalsIgnoreCase("config")) {
//sw reload config
                        if (Vault.hasPermission(p, "admin.reload.config")) {
                            try {
                                ConfigMain.main();
                                p.sendMessage(prefix + "Config reloaded");
                                return true;
                            } catch (Exception ex) {
                                /* ex.printStackTrace(); */
                            }
                        } else {
                            p.sendMessage(prefix + "You don't have permission to reload the config");
                        }
                        return true;
                    }
                    if (args[1].equalsIgnoreCase("homes")) {
//sw reload homes
                        if (Vault.hasPermission(p, "admin.reload.homes")) {
                            Manager.loadHomes();
                            p.sendMessage(prefix + "Homes list reloaded");
                        } else {
                            p.sendMessage(prefix + "You don't have permission to reload the homes");
                        }
                        return true;
                    }
                    if (args[1].equalsIgnoreCase("warps")) {
//sw reload warps
                        if (Vault.hasPermission(p, "admin.reload.homes")) {
                            Manager.loadWarps();
                            p.sendMessage(prefix + "Warps list reloaded");
                            return true;
                        } else {
                            p.sendMessage(prefix + "You don't have permission to reload the warps");
                        }
                        return true;
                    }
                }
            } else if (args.length == 4) {
                if (args[0].equalsIgnoreCase("import")) {
                    if (args[1].equalsIgnoreCase("homes")) {
                        if (Vault.hasPermission(p, "admin.import.homes")) {
//sw import homes scizzrhome scizzrhomes.txt
                            if (Manager.importHomes(args[2], args[3])) {
                                p.sendMessage(prefix + "Homes imported from ScizzrHome.");
                            } else {
                                p.sendMessage(prefix + "Failed to import homes from ScizzrHome.");
                            }
                        } else {
                            p.sendMessage(prefix + "You don't have permission to import homes");
                        }
                        return true;
                    }
                }
            }
        }
        
        p.sendMessage(prefix + ChatColor.YELLOW + "/sw help : " + ChatColor.RESET + "Display ScizzrWarp commands and usage");
        
        p.sendMessage(prefix + ChatColor.YELLOW + "/sw version : " + ChatColor.RESET + "Show ScizzrWarp version");
        
        p.sendMessage(prefix + ChatColor.YELLOW + "/home [<name>] : " + ChatColor.RESET + "Teleport to a home you have set");
        p.sendMessage(prefix + ChatColor.YELLOW + "/sethome [p:<who>] [<name>] : " + ChatColor.RESET + "Set a home; without <name> sets default");
        p.sendMessage(prefix + ChatColor.YELLOW + "/delhome [<name>] : " + ChatColor.RESET + "Delete a home; without <name> clears default");
        p.sendMessage(prefix + ChatColor.YELLOW + "/homes : " + ChatColor.RESET + "List homes you have set");
        
        p.sendMessage(prefix + ChatColor.YELLOW + "/warp [<name>] : " + ChatColor.RESET + "Teleport to a home you have set");
        p.sendMessage(prefix + ChatColor.YELLOW + "/setwarp [p:<who>] [<name>] : " + ChatColor.RESET + "Set a home; without <name> sets default");
        p.sendMessage(prefix + ChatColor.YELLOW + "/delwarp : " + ChatColor.RESET + "Delete a warp");
        p.sendMessage(prefix + ChatColor.YELLOW + "/warps : " + ChatColor.RESET + "List warps");
        
        p.sendMessage(prefix + ChatColor.YELLOW + "/sw reload [config|homes|warps] : " + ChatColor.RESET + "Reload config, homes, or warps; if none given, reload all");
        
        return true;
    }
    
    public static void suicide(Exception ex) {
        int i = 60;
        
        if (Config.genErrorWeb == true) {
            if (exTimer == 0) {
                log.info(prefixConsole + "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
                log.info(prefixConsole + "You submitted a stack trace for further review. Thank");
                log.info(prefixConsole + "you for enabling this as it allows me to fix problems.");
                log.info(prefixConsole + "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
                new Thread(new Errors(MoreString.stackToString(ex))).start();
                exTimer = i;
            } else {
                log.info(prefixConsole + "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
                log.info(prefixConsole + "An error occurred but it was not posted to my website");
                log.info(prefixConsole + "because you recently posted one " + (i-exTimer) + " seconds ago.");
                log.info(prefixConsole + "If errors continue to occur, please post a message on");
                log.info(prefixConsole + "this page: http://dev.bukkit.org/server-mods/" + info.getName().toLowerCase());
                log.info(prefixConsole + "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
            }
        } else {
            ex.printStackTrace();
        }
    }
}
