package thederpgamer.startunes.manager;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.resource.ResourceLoader;
import thederpgamer.startunes.StarTunes;
import thederpgamer.startunes.data.MusicAutoplaySettings;
import thederpgamer.startunes.data.TrackAutoplaySettings;
import thederpgamer.startunes.utils.DataUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [02/15/2022]
 */
public class ResourceManager {

    public enum Music {
        MAIN_THEME("Main Theme", "Daniel Tusjak", 248),
        NEW_HORIZONS("New Horizons", "Daniel Tusjak", 233),
        COLLAPSE("Collapse", "Daniel Tusjak", 105),
        TRAILER_THEME("Trailer Theme", "Daniel Tusjak", 147),
        DRIFTING_THROUGH_THE_NEBULA("Drifting Through The Nebula", "Daniel Tusjak", 336),
        VOID("Void", "Daniel Tusjak", 352),
        STARLIGHT("Starlight", "Daniel Tusjak", 188),
        PRE_ACTION("Pre-action", "Daniel Tusjak", 111),
        DETECTION("Detection", "Daniel Tusjak", 183),
        CIVILIZATIONS("Civilizations", "Daniel Tusjak", 203),
        A_SPACE_BETWEEN_WORLDS("A Space Between Worlds", "Daniel Tusjak", 290),
        SERENITY("Serenity", "Daniel Tusjak", 186),
        VOYAGE("Voyage", "Daniel Tusjak", 231),
        GREEN("Green", "Jontyfreack", 646),
        MOCHUR("Mochur", "Jontyfreack", 227),
        WANDERER("Wanderer", "Jontyfreack", 297),
        YGOLLLL_IV("Ygollll IV", "Jontyfreack", 166);

        public final String name;
        public final String artist;
        public final int runTime;

        Music(String name, String artist, int runTime) {
            this.name = name;
            this.artist = artist;
            this.runTime = runTime;
        }
    }

    public final static HashMap<String, TrackAutoplaySettings> musicMap = new HashMap<>();

    public static void loadResources(StarTunes instance, ResourceLoader loader) {
        File musicFolder = new File(DataUtils.getResourcesPath() + "/music");
        if(!musicFolder.exists()) musicFolder.mkdirs();

        File autoplayFile = new File(DataUtils.getResourcesPath() + "/music/autoplay.json");
        if(!autoplayFile.exists()) {
            try {
                autoplayFile.createNewFile();
            } catch(IOException exception) {
                exception.printStackTrace();
            }
        }

        if(musicFolder.listFiles() != null && musicFolder.listFiles().length > 0) {
            for(File musicFile : musicFolder.listFiles()) {
                if(musicFile.getName().toLowerCase().endsWith(".wav")) {
                    try {
                        String[] fields = musicFile.getName().substring(0, musicFile.getName().lastIndexOf(".")).split(" - ");
                        if(fields.length != 3) {
                            LogManager.logWarning("Music file \"" + musicFile.getName() + "\" is not correctly formatted and cannot be loaded!", null);
                            continue;
                        }

                        String trackName = musicFile.getName().substring(0, musicFile.getName().lastIndexOf("."));
                        if(!musicMap.containsKey(trackName)) {
                            Controller.getAudioManager().addSound(fields[0], musicFile);
                            musicMap.put(trackName, getAutoplaySettings(musicFile));
                            LogManager.logInfo("Loaded music file \"" + musicFile.getName() + "\".");
                        }
                    } catch(Exception exception) {
                        LogManager.logException("Failed to load music file \"" + musicFile.getName() + "\". Check to make sure the file name is formatted correctly and ends in .wav", exception);
                    }
                }
            }
        }

        for(Music music : Music.values()) {
            try {
                File exportFile = new File(musicFolder.getPath() + "/" + music.name + " - " + music.artist + " - " + music.runTime + ".wav");
                InputStream inputStream = instance.getJarResource("thederpgamer/startunes/resources/music/" + music.name + " - " + music.artist + " - " + music.runTime + ".wav");
                FileUtils.copyInputStreamToFile(inputStream, exportFile);
                String[] fields = exportFile.getName().substring(0, exportFile.getName().lastIndexOf(".")).split(" - ");
                if(fields.length != 3) {
                    LogManager.logWarning("Music file \"" + exportFile.getName() + "\" is not correctly formatted and cannot be loaded!\nCorrect format: <name> - <artist> - <runtimeSeconds>.wav", null);
                    continue;
                }
                String trackName = exportFile.getName().substring(0, exportFile.getName().lastIndexOf("."));
                if(!musicMap.containsKey(trackName)) {
                    Controller.getAudioManager().addSound(fields[0], exportFile);
                    musicMap.put(trackName, getAutoplaySettings(exportFile));
                    LogManager.logInfo("Loaded music file \"" + exportFile.getName() + "\".");
                }
            } catch(IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public static TrackAutoplaySettings getAutoplaySettings(File musicFile) {
        saveAutoplaySettings();
        try {
            MusicAutoplaySettings allAutoplay = (new Gson()).fromJson(DataUtils.getResourcesPath() + "/music/autoplay.json", MusicAutoplaySettings.class);
            String fileName = musicFile.getName().substring(0, musicFile.getName().lastIndexOf("."));
            return allAutoplay.autoplayMap.get(fileName);
        } catch(Exception exception) {
            TrackAutoplaySettings autoplaySettings = new TrackAutoplaySettings();
            autoplaySettings.combat = 1.0f;
            autoplaySettings.exploration = 1.0f;
            autoplaySettings.building = 1.0f;
            return autoplaySettings;
        }
    }

    public static void saveAutoplaySettings() {
        MusicAutoplaySettings autoplaySettings = new MusicAutoplaySettings();
        autoplaySettings.autoplayMap = musicMap;
        File autoplaySettingsFile = new File(DataUtils.getResourcesPath() + "/music/autoplay.json");
        try {
            FileWriter writer = new FileWriter(autoplaySettingsFile);
            writer.write(new Gson().toJson(autoplaySettings));
            writer.close();
        } catch(IOException exception) {
            exception.printStackTrace();
        }
    }
}
