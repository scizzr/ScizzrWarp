package com.scizzr.bukkit.plugins.scizzrwarp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.scizzr.bukkit.plugins.scizzrwarp.config.Config;

@SuppressWarnings({"unchecked", "unused"})
public class Manager {
    private static HashMap<String, Location> map_Homes = new HashMap<String, Location>();
    private static HashMap<String, Location> map_Warps = new HashMap<String, Location>();
    
    public static boolean loadHomes() {
        HashMap<String, Location> map_Tmp = (HashMap<String, Location>) map_Homes.clone();
        try {
            map_Homes.clear();
            BufferedReader reader = new BufferedReader(new FileReader(Main.fileHomes));
            String line = reader.readLine();
            while (line != null) {
                String[] values = line.split(";");
                if (values.length == 2) {
                    String[] info = values[0].split(":");
                    String[] loc = values[1].split(":");
                    
                    if(info.length == 3 && loc.length == 4) {
                        World world = Bukkit.getServer().getWorld(info[1]);
                        
                        if (world != null) {
                            double X = Double.parseDouble(loc[0]);
                            double Y = Double.parseDouble(loc[1]);
                            double Z = Double.parseDouble(loc[2]);
                            float yaw = Float.parseFloat(loc[3]);
                            map_Homes.put(values[0], new Location(world, X, Y, Z, yaw, 0));
                        }
                    }
                }
                line = reader.readLine();
            }
            return true;
        } catch (Exception ex) {
            map_Homes = (HashMap<String, Location>) map_Tmp.clone();
            return false;
        }
    }
    
    public static boolean saveHomes() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(Main.fileHomes));
            for (Entry<String,Location> entry : map_Homes.entrySet()) {
                Location loc = entry.getValue();
                if ( loc != null ) {
                    writer.write(entry.getKey() + ";" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + loc.getYaw() );
                    writer.newLine();
                }
            }
            writer.close();
            return true;
        } catch (Exception ex) {
            Main.suicide(ex);
            return false;
        }
    }
    
    public static boolean loadWarps() {
        HashMap<String, Location> map_Tmp = (HashMap<String, Location>) map_Warps.clone();
        try {
            map_Warps.clear();
            BufferedReader reader = new BufferedReader(new FileReader(Main.fileWarps));
            String line = reader.readLine();
            while (line != null) {
                String[] values = line.split(";");
                if (values.length == 2) {
                    String[] info = values[0].split(":");
                    String[] loc = values[1].split(":");
                    
                    if(info.length == 2 && loc.length == 4) {
                        World world = Bukkit.getServer().getWorld(info[0]);
                        
                        if (world != null) {
                            double X = Double.parseDouble(loc[0]);
                            double Y = Double.parseDouble(loc[1]);
                            double Z = Double.parseDouble(loc[2]);
                            float yaw = Float.parseFloat(loc[3]);
                            map_Warps.put(values[0], new Location(world, X, Y, Z, yaw, 0));
                        }
                    }
                }
                line = reader.readLine();
            }
            return true;
        } catch (Exception ex) {
            map_Warps = (HashMap<String, Location>) map_Tmp.clone();
            return false;
        }
    }
    
    public static boolean saveWarps() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(Main.fileWarps));
            for (Entry<String,Location> entry : map_Warps.entrySet()) {
                Location loc = entry.getValue();
                if ( loc != null ) {
                    writer.write(entry.getKey() + ";" + loc.getX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ() + ":" + loc.getYaw() );
                    writer.newLine();
                }
            }
            writer.close();
            return true;
        } catch (Exception ex) {
            Main.suicide(ex);
            return false;
        }
    }
    
    public static String homeExists(String who, String world, String name) {
        for (String item : map_Homes.keySet()) {
            String[] info = item.split(":");
            if (info[0].equalsIgnoreCase(who)) {
                if (info[1].equalsIgnoreCase(world)) {
                    if (info[2].equalsIgnoreCase(name)) {
                        return info[2];
                    }
                }
            }
        }
        return null;
    }
    
    public static void homeList(Player player, String who) {
        String msg = "";
        for (String item : map_Homes.keySet()) {
            String[] info = item.split(":");
            if (info[0].equalsIgnoreCase(who)) {
                msg += ChatColor.YELLOW + info[2].toLowerCase() + ChatColor.RESET + "(" + info[1] + ChatColor.RESET + ")" + ", ";
            }
        }
        if (msg == "") {
            if (player.getName() == who) { msg = "You do not have any available homes."; } else { msg = ChatColor.YELLOW + who + " does not have any available homes."; }
        } else {
            if (player.getName() == who) { msg = "Your homes : " + msg; } else { msg = ChatColor.YELLOW + who + ChatColor.RESET + "'s homes : " + msg; }
            msg = msg.substring(0, msg.length()-2);
        }
        
        player.sendMessage(Main.prefix + msg);
    }
    
    public static void homeUse(Player p, String world, String name) {
        Location loc = null;
        for (String item : map_Homes.keySet()) {
            String[] info = item.split(":");
            if (info[0].equalsIgnoreCase(p.getName())) {
                if(info[2].equalsIgnoreCase(name)) {
                    loc = map_Homes.get(p.getName().toLowerCase() + ":" + world.toLowerCase() + ":" + name.toLowerCase());
                    Location diff = loc.clone();
                    
                    for (int i = (int) loc.getY(); i <= 127; i++) {
                        Block block1 = diff.getChunk().getBlock((int) diff.getX(), (int) diff.getY(), (int) diff.getZ());
                        Block block2 = diff.getChunk().getBlock((int) diff.getX(), (int) diff.getY()+1, (int) diff.getZ());
                        if (block1.getType() != Material.AIR) {
                            p.sendMessage(Main.prefix + "Avoided " + ChatColor.YELLOW + block1.getType().toString() + ChatColor.WHITE + " at " + "(" + 
                                            ChatColor.YELLOW + diff.getBlockX() + ChatColor.WHITE + ", " + 
                                            ChatColor.YELLOW + diff.getBlockY() + ChatColor.WHITE + ", " +
                                            ChatColor.YELLOW + diff.getBlockZ() + ChatColor.WHITE + ")");
                            diff.setY(diff.getY()+1);
                        } else if (block2.getType() != Material.AIR) {
                            p.sendMessage(Main.prefix + "Avoided " + ChatColor.YELLOW + block2.getType().toString() + ChatColor.WHITE + " at " + "(" + 
                                            ChatColor.YELLOW + diff.getBlockX() + ChatColor.WHITE + ", " + 
                                            ChatColor.YELLOW + (diff.getBlockY()+1) + ChatColor.WHITE + ", " +
                                            ChatColor.YELLOW + diff.getBlockZ() + ChatColor.WHITE + ")");
                            diff.setY(diff.getY()+2);
                        }
                    }
                    
                    for (int i = (int) diff.getY(); i >= 1; i--) {
                        Block block = diff.getChunk().getBlock((int) diff.getX(), (int) diff.getY()-1, (int) diff.getZ());
                        if (block.getType() == Material.AIR) {
                            diff.setY(diff.getY()-1);
                        }
                    }
                    
                    if (diff.getY() == 0) {
                        Bukkit.getWorld(info[1].toString()).getBlockAt(diff).setType(Material.GLASS);
                        diff.setY(1);
                        p.sendMessage(Main.prefix + ChatColor.YELLOW + "Warning " + ChatColor.WHITE + ": This home is located above the void");
                    } else if (diff.getY() < loc.getY()) {
                        p.sendMessage(Main.prefix + ChatColor.YELLOW + "Warning " + ChatColor.WHITE + ": This home is located in the air");
                    }
                    
                    p.teleport(diff.add(0.5, 0, 0.5));
                    p.sendMessage(Main.prefix + "Whoosh!");
                }
            }
        }
        return;
    }
    
    public static void homeSet(Player p, String worldname, String name) {
        Location loc = p.getLocation();
        
        World world = p.getLocation().getWorld();
        double X = loc.getBlockX();
        double Y = loc.getBlockY();
        double Z = loc.getBlockZ();
        float yaw = loc.getYaw();
        
        Y = (Y > 127) ? 127 : Y;
        
        Location dest = new Location(world, X, Y, Z, yaw, 0);
        map_Homes.put(p.getName().toLowerCase() + ":" + worldname.toLowerCase() + ":" + name.toLowerCase(), dest);
        saveHomes();
        p.sendMessage(Main.prefix + "Home saved (" + (Math.floor(X)+0.5) + ", " + Math.floor(Y) + ", "+ (Math.floor(Z)+0.5) + ")");
    }
    
    public static void homeDel(Player p, String worldname, String name) {
        map_Homes.remove(p.getName().toLowerCase() + ":" + worldname .toLowerCase()+ ":" + name.toLowerCase());
        saveHomes();
        p.sendMessage(Main.prefix + "Home deleted");
    }

    public static String warpExists(String world, String name) {
        for (String item : map_Warps.keySet()) {
            String[] info = item.split(":");
            
            if (info[0].equalsIgnoreCase(world)) {
                if (info[1].equalsIgnoreCase(name)) {
                    return info[1];
                }
            }
/*
            if (info[2].startsWith(name)) {
                return info[2];
            }
*/
        }
        return null;
    }
    
    public static void warpList(Player player, String who) {
        String msg = "";
        
        for (String item : map_Warps.keySet()) {
            String[] info = item.split(":");
            msg += ChatColor.YELLOW + info[1].toLowerCase() + ChatColor.WHITE + ", ";
        }
        
        if (msg == "") {
            msg = "There are no warps defined";
        } else {
            msg = "Available warps : " + msg;
            msg = msg.substring(0, msg.length()-2);
        }
        player.sendMessage(Main.prefix + msg);
    }
    
    public static void warpUse(Player p, String world, String name) {
        Location loc = null;
        for (String item : map_Warps.keySet()) {
            String[] info = item.split(":");
            if(info[1].equalsIgnoreCase(name)) {
                loc = map_Warps.get(world.toLowerCase() + ":" + name.toLowerCase());
                Location diff = loc.clone();
                
                for (int i = (int) loc.getY(); i <= 127; i++) {
                    Block block1 = diff.getChunk().getBlock((int) diff.getX(), (int) diff.getY(), (int) diff.getZ());
                    Block block2 = diff.getChunk().getBlock((int) diff.getX(), (int) diff.getY()+1, (int) diff.getZ());
                    if (block1.getType() != Material.AIR) {
                        p.sendMessage(Main.prefix + "Avoided " + ChatColor.YELLOW + block1.getType().toString() + ChatColor.WHITE + " at " + "(" + 
                                        ChatColor.YELLOW + diff.getBlockX() + ChatColor.WHITE + ", " + 
                                        ChatColor.YELLOW + diff.getBlockY() + ChatColor.WHITE + ", " +
                                        ChatColor.YELLOW + diff.getBlockZ() + ChatColor.WHITE + ")");
                        diff.setY(diff.getY()+1);
                    } else if (block2.getType() != Material.AIR) {
                        p.sendMessage(Main.prefix + "Avoided " + ChatColor.YELLOW + block2.getType().toString() + ChatColor.WHITE + " at " + "(" + 
                                        ChatColor.YELLOW + diff.getBlockX() + ChatColor.WHITE + ", " + 
                                        ChatColor.YELLOW + (diff.getBlockY()+1) + ChatColor.WHITE + ", " +
                                        ChatColor.YELLOW + diff.getBlockZ() + ChatColor.WHITE + ")");
                        diff.setY(diff.getY()+2);
                    }
                }
                
                for (int i = (int) diff.getY(); i >= 1; i--) {
                    Block block = diff.getChunk().getBlock((int) diff.getX(), (int) diff.getY()-1, (int) diff.getZ());
                    if (block.getType() == Material.AIR) {
                        diff.setY(diff.getY()-1);
                    }
                }
                
                if (diff.getY() == 0) {
                    Bukkit.getWorld(info[1].toString()).getBlockAt(diff).setType(Material.GLASS);
                    diff.setY(1);
                    p.sendMessage(Main.prefix + ChatColor.YELLOW + "Warning " + ChatColor.WHITE + ": This warp is located above the void");
                } else if (diff.getY() < loc.getY()) {
                    p.sendMessage(Main.prefix + ChatColor.YELLOW + "Warning " + ChatColor.WHITE + ": This warp is located in the air");
                }
                
                p.teleport(diff.add(0.5, 0, 0.5));
                p.sendMessage(Main.prefix + "Whoosh!");
            }
        }
        return;
    }
    
    public static void warpSet(Player p, String worldname, String name) {
        Location loc = p.getLocation();
        
        World world = p.getLocation().getWorld();
        double X = loc.getBlockX();
        double Y = loc.getBlockY();
        double Z = loc.getBlockZ();
        float yaw = loc.getYaw();
        
        Y = (Y > 127) ? 127 : Y;
        
        Location dest = new Location(world, X, Y, Z, yaw, 0);
        map_Warps.put(worldname.toLowerCase() + ":" + name.toLowerCase(), dest);
        saveWarps();
        p.sendMessage(Main.prefix + "Warp saved (" + (Math.floor(X)+0.5) + ", " + Math.floor(Y) + ", "+ (Math.floor(Z)+0.5) + ")");
    }
    
    public static void warpDel(Player p, String worldname, String name) {
        map_Warps.remove(worldname .toLowerCase()+ ":" + name.toLowerCase());
        saveWarps();
        p.sendMessage(Main.prefix + "Warp deleted");
    }
    
    public static boolean importHomes(String plugin, String file) {
        HashMap<String, Location> map_Tmp = (HashMap<String, Location>) map_Homes.clone();
        try {
            map_Homes.clear();
            BufferedReader reader = new BufferedReader(new FileReader(Main.df + Main.slash + file));
            String line = reader.readLine();
            while (line != null) {
                String[] values = line.split(";");
                if (values.length == 7) {
                    double X = Double.parseDouble(values[1]);
                    double Y = Double.parseDouble(values[2]);
                    double Z = Double.parseDouble(values[3]);
                    float yaw = Float.parseFloat(values[5]);
    
                    World world = Bukkit.getServer().getWorld(values[6]);
                    if (world != null) {
                        map_Homes.put(values[0].split(":")[0] + ":" + world.getName() + ":" + values[0].split(":")[1], new Location(world, X, Y, Z, yaw, 0));
//                        map_Homes.put(values[0], new Location(world, X, Y, Z, yaw, pitch));
                    }
                }
                line = reader.readLine();
            }
            saveHomes();
            return true;
        } catch (Exception ex) {
            map_Homes = (HashMap<String, Location>) map_Tmp.clone();
            ex.printStackTrace();
            return false;
        }
    }
}
