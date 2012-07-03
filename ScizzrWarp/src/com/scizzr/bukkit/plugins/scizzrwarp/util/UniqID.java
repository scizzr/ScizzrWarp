package com.scizzr.bukkit.plugins.scizzrwarp.util;

import java.util.UUID;
import java.util.prefs.Preferences;

public class UniqID {
    public static String getUniqID() {
        String path = "com.scizzr.bukkit.shared.UUID";
        
        if (Preferences.userRoot().get(path, null) == null) {
            Preferences.userRoot().put(path, UUID.randomUUID().toString());
        }
        
        return Preferences.userRoot().get(path, null);
    }
}
