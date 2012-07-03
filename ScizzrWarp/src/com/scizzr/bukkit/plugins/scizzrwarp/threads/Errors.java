package com.scizzr.bukkit.plugins.scizzrwarp.threads;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.bukkit.Bukkit;

import com.scizzr.bukkit.plugins.scizzrwarp.Main;
import com.scizzr.bukkit.plugins.scizzrwarp.config.Config;
import com.scizzr.bukkit.plugins.scizzrwarp.util.base64.Base64;

public class Errors implements Runnable {
    String err;
    
    public Errors(String err) {
        this.err = Base64.encode(err);
    }
    
    public void run() {
        postError();
    }
    
    public void postError() {
        String ver = "UNKNOWN";
        
        try {
            ver = Bukkit.getBukkitVersion();
            
            URL url = new URL(
                    //$uuid && $system && $plugname && $plugver && $ver && $err
                    String.format("http://www.scizzr.com/plugins/errors.php")
                );
            
            String data = new String(
                    //$uuid && $system && $plugname && $plugver && $ver && $err
                    String.format("uniqid=" + "%s", URLEncoder.encode(Config.genUniqID, "UTF-8")) + 
                    String.format("&system=" + "%s", URLEncoder.encode(Main.osN, "UTF-8")) +
                    String.format("&plugname=" + "%s", URLEncoder.encode(Main.info.getName(), "UTF-8")) +
                    String.format("&plugver=" + "%s", URLEncoder.encode(Main.info.getVersion(), "UTF-8")) +
                    String.format("&ver=" + "%s", URLEncoder.encode(ver, "UTF-8")) +
                    String.format("&err=" + "%s", URLEncoder.encode(err, "UTF-8"))
                );
            
            URLConnection conn = url.openConnection(); conn.setDoOutput(true);
            
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream()); writer.write(data); writer.flush();
            InputStreamReader stream = new InputStreamReader(conn.getInputStream());
            
            BufferedReader buff = new BufferedReader(stream);
            
            String line = buff.readLine();
            
            while (line != null) {
                // do nothing; just post stats
                line = buff.readLine();
            }
            
            stream.close();
        } catch (Exception ex) {
            Main.log.info(Main.prefixConsole + "Stack trace submission failed.");
            ex.printStackTrace();
        }
    }
}
