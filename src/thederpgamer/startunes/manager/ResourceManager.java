package thederpgamer.startunes.manager;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.schema.schine.graphicsengine.core.Controller;
import thederpgamer.startunes.StarTunes;
import thederpgamer.startunes.data.TrackData;
import thederpgamer.startunes.data.TrackSettings;
import thederpgamer.startunes.utils.DataUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
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
        MAIN_THEME("Main Theme", "Daniel Tusjak"),
        NEW_HORIZONS("New Horizons", "Daniel Tusjak"),
        COLLAPSE("Collapse", "Daniel Tusjak"),
        TRAILER_THEME("Trailer Theme", "Daniel Tusjak"),
        DRIFTING_THROUGH_THE_NEBULA("Drifting Through The Nebula", "Daniel Tusjak"),
        VOID("Void", "Daniel Tusjak"),
        STARLIGHT("Starlight", "Daniel Tusjak"),
        PRE_ACTION("Pre-action", "Daniel Tusjak"),
        DETECTION("Detection", "Daniel Tusjak"),
        CIVILIZATIONS("Civilizations", "Daniel Tusjak"),
        A_SPACE_BETWEEN_WORLDS("A Space Between Worlds", "Daniel Tusjak"),
        SERENITY("Serenity", "Daniel Tusjak"),
        VOYAGE("Voyage", "Daniel Tusjak"),
        GREEN("Green", "Jontyfreack"),
        MOCHUR("Mochur", "Jontyfreack"),
        WANDERER("Wanderer", "Jontyfreack"),
        YGOLLLL_IV("Ygollll IV", "Jontyfreack");

        public final String name;
        public final String artist;

        Music(String name, String artist) {
            this.name = name;
            this.artist = artist;
        }
    }

    public final static HashMap<String, TrackData> musicMap = new HashMap<>();

    public static void loadResources(StarTunes instance) {
        File musicFolder = new File(DataUtils.getResourcesPath() + "/music");
        if(!musicFolder.exists()) musicFolder.mkdirs();

        File trackDataFile = new File(DataUtils.getResourcesPath() + "/music/trackData.json");
        if(!trackDataFile.exists()) {
            try {
                trackDataFile.createNewFile();
            } catch(IOException exception) {
                exception.printStackTrace();
            }
        }

        if(musicFolder.listFiles() != null && musicFolder.listFiles().length > 0) {
            for(File musicFile : musicFolder.listFiles()) {
                if(musicFile.getName().toLowerCase().endsWith(".wav")) {
                    try {
                        String[] fields = musicFile.getName().substring(0, musicFile.getName().lastIndexOf(".")).split(" - ");
                        if(fields.length != 2) {
                            LogManager.logWarning("Music file \"" + musicFile.getName() + "\" is not correctly formatted and cannot be loaded!", null);
                            continue;
                        }
                        String trackName = musicFile.getName().substring(0, musicFile.getName().lastIndexOf("."));
                        if(!musicMap.containsKey(trackName)) {
                            Controller.getAudioManager().addSound(fields[0], musicFile);
                            musicMap.put(trackName, createTrackData(musicFile));
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
                File exportFile = new File(musicFolder.getPath() + "/" + music.name + " - " + music.artist + ".wav");
                InputStream inputStream = instance.getJarResource("thederpgamer/startunes/resources/music/" + music.name + " - " + music.artist + ".wav");
                FileUtils.copyInputStreamToFile(inputStream, exportFile);
                String[] fields = exportFile.getName().substring(0, exportFile.getName().lastIndexOf(".")).split(" - ");
                if(fields.length != 2) {
                    LogManager.logWarning("Music file \"" + exportFile.getName() + "\" is not correctly formatted and cannot be loaded!\nCorrect format: <name> - <artist>.wav", null);
                    continue;
                }
                String trackName = exportFile.getName().substring(0, exportFile.getName().lastIndexOf("."));
                if(!musicMap.containsKey(trackName)) {
                    Controller.getAudioManager().addSound(fields[0], exportFile);
                    musicMap.put(trackName, createTrackData(exportFile));
                    LogManager.logInfo("Loaded music file \"" + exportFile.getName() + "\".");
                }
            } catch(IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public static TrackData createTrackData(File musicFile) {
        saveTrackData();
        String track = musicFile.getName().substring(0, musicFile.getName().lastIndexOf("."));
        try {
            TrackSettings trackSettings = (new Gson()).fromJson(DataUtils.getResourcesPath() + "/music/trackData.json", TrackSettings.class);
            return trackSettings.trackDataMap.get(track);
        } catch(Exception exception) {
            TrackData trackData = new TrackData(MusicManager.getTrackName(track), MusicManager.getArtistName(track), getRunTime(musicFile));
            trackData.combat = 1.0f;
            trackData.exploration = 1.0f;
            trackData.building = 1.0f;
            return trackData;
        }
    }

    public static void saveTrackData() {
        TrackSettings trackSettings = new TrackSettings();
        trackSettings.trackDataMap = musicMap;
        File trackDataFile = new File(DataUtils.getResourcesPath() + "/music/trackData.json");
        try {
            FileWriter writer = new FileWriter(trackDataFile);
            writer.write(new Gson().toJson(trackSettings));
            writer.close();
        } catch(IOException exception) {
            exception.printStackTrace();
        }
    }

    public static int getRunTime(File file) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            AudioFormat format = audioInputStream.getFormat();
            long frames = audioInputStream.getFrameLength();
            return (int) ((frames + 0.0) / format.getFrameRate());
        } catch(Exception exception) {
            exception.printStackTrace();
        }
        return -1;
    }
}
