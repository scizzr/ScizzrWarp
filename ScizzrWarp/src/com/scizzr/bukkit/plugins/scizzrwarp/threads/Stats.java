package com.scizzr.bukkit.plugins.scizzrwarp.threads;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.bukkit.Bukkit;

import com.scizzr.bukkit.plugins.scizzrwarp.Main;
import com.scizzr.bukkit.plugins.scizzrwarp.config.Config;

public class Stats implements Runnable {
    public Stats() {
        
    }
    
    public void run() {
        postStats();
    }
    
    public void postStats() {
        String name = null, sip = "0", port = null, max = null, gm = "UNKNOWN", ver = null;
        
        name = Bukkit.getServerName();
        sip = (Bukkit.getIp() != "" ? Bukkit.getIp() : "");
        port = String.valueOf(Bukkit.getPort());
        max = String.valueOf(Bukkit.getMaxPlayers());
        try { gm = Bukkit.getDefaultGameMode().toString(); } catch (Throwable th) { /*th.printStackTrace();*/ } finally {
            ver = Bukkit.getBukkitVersion();
            
            try {
                URL url = new URL(
                    String.format("http://www.scizzr.com/plugins/stats.php?") +
                    String.format("uniqid=" + "%s", URLEncoder.encode(Config.genUniqID, "UTF-8")) + 
                    String.format("&system=" + "%s", URLEncoder.encode(Main.osN, "UTF-8")) +
                    String.format("&plugname=" + "%s", URLEncoder.encode(Main.info.getName(), "UTF-8")) +
                    String.format("&plugver=" + "%s", URLEncoder.encode(Main.info.getVersion(), "UTF-8")) +
                    String.format("&name=" + "%s", URLEncoder.encode(name, "UTF-8")) +
                    String.format("&sip=" + "%s", URLEncoder.encode(sip, "UTF-8")) +
                    String.format("&port=" + "%s", URLEncoder.encode(port, "UTF-8")) +
                    String.format("&max=" + "%s", URLEncoder.encode(max, "UTF-8")) +
                    String.format("&gm=" + "%s", URLEncoder.encode(gm, "UTF-8")) +
                    String.format("&ver=" + "%s", URLEncoder.encode(ver, "UTF-8"))
                );
                
                URLConnection conn = url.openConnection();
                InputStreamReader stream = new InputStreamReader(conn.getInputStream());
                BufferedReader buff = new BufferedReader(stream);
                String line = buff.readLine();
                
                while (line != null) {
                    // do nothing; just post stats
                    line = buff.readLine();
                }
                stream.close();
            } catch (Exception ex) {
                Main.suicide(ex);
            }
        }
    }
}
