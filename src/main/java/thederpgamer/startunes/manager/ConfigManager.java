package thederpgamer.startunes.manager;

import api.mod.config.FileConfiguration;
import org.lwjgl.input.Keyboard;
import thederpgamer.startunes.StarTunes;

/**
 * Manages mod config files and values.
 *
 * @author TheDerpGamer
 * @version 1.0 - [02/15/2022]
 */
public class ConfigManager {

    //Main Config
    private static FileConfiguration mainConfig;
    public static final String[] defaultMainConfig = {};

    //Key Config
    private static FileConfiguration keyConfig;
    public static final String[] defaultKeyConfig = {
            "open-music-player: ADD",
            "pause-music: PAUSE",
            "previous-track: PRIOR",
            "next-track: NEXT"
    };
    public static int OPEN_PLAYER;
    public static int PAUSE_MUSIC;
    public static int PREVIOUS_TRACK;
    public static int NEXT_TRACK;

    public static void initialize(StarTunes instance) {
        mainConfig = instance.getConfig("config");
        mainConfig.saveDefault(defaultMainConfig);

        keyConfig = instance.getConfig("key-bindings");
        keyConfig.saveDefault(defaultKeyConfig);

        OPEN_PLAYER = Keyboard.getKeyIndex(keyConfig.getString("open-music-player"));
        PAUSE_MUSIC = Keyboard.getKeyIndex(keyConfig.getString("pause-music"));
        PREVIOUS_TRACK = Keyboard.getKeyIndex(keyConfig.getString("previous-track"));
        NEXT_TRACK = Keyboard.getKeyIndex(keyConfig.getString("next-track"));
    }

    public static FileConfiguration getMainConfig() {
        return mainConfig;
    }

    public static FileConfiguration getKeyConfig() {
        return keyConfig;
    }

    public static String getDefaultValue(String field) {
        if(mainConfig.getKeys().contains(field)) {
            for(String s : defaultMainConfig) {
                String fieldName = s.substring(0, s.lastIndexOf(":") - 1).trim().toLowerCase();
                if(fieldName.equals(field.toLowerCase().trim())) return s.substring(s.lastIndexOf(":") + 1).trim();
            }
        }
        return null;
    }

    public static char getKeyBinding(String field) {
        String binding = keyConfig.getString(field);
        if(binding != null && !binding.toUpperCase().equals("NONE")) return binding.toUpperCase().charAt(0);
        else return '\0';
    }
}
