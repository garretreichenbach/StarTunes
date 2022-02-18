package thederpgamer.startunes.manager;

import com.google.gson.Gson;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.schema.schine.graphicsengine.core.Controller;
import thederpgamer.startunes.data.TrackData;
import thederpgamer.startunes.data.TrackSettings;
import thederpgamer.startunes.utils.DataUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

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

        public static Music getTrack(String fileName) {
            for(Music music : Music.values()) {
                if(music.name.equals(fileName)) return music;
            }
            return null;
        }
    }

    public final static HashMap<String, TrackData> musicMap = new HashMap<>();

    public static void loadResources() {
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
                        String trackName = getName(musicFile);
                        if(!musicMap.containsKey(trackName)) {
                            Controller.getAudioManager().addSound(trackName, musicFile);
                            musicMap.put(trackName, getTrackData(musicFile));
                            LogManager.logInfo("Loaded music file \"" + musicFile.getName() + "\".");
                        }
                    } catch(Exception exception) {
                        LogManager.logException("Failed to load music file \"" + musicFile.getName() + "\". Check to make sure the file name ends in .wav", exception);
                    }
                }
            }
            saveTrackData();
        }
    }

    public static TrackData getTrackData(File musicFile) {
        TrackData trackData;
        try {
            TrackSettings trackSettings = (new Gson()).fromJson(DataUtils.getResourcesPath() + "/music/trackData.json", TrackSettings.class);
            trackData = trackSettings.trackDataMap.get(getName(musicFile));
        } catch(Exception exception) {
            trackData = new TrackData(getName(musicFile), getArtist(musicFile), getRunTime(musicFile), true);
        }
        return trackData;
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
            long audioFileLength = file.length();
            int frameSize = format.getFrameSize();
            float frameRate = format.getFrameRate();
            return (int) (audioFileLength / (frameSize * frameRate));
        } catch(Exception exception1) {
            exception1.printStackTrace();
        }
        return -1;
    }

    public static String getName(File file) {
        try {
            AudioFile audioFile = AudioFileIO.getDefaultAudioFileIO().readFile(file);
            checkForEmptyTags(audioFile);
            String title = audioFile.getTag().getFirst(FieldKey.TITLE);
            if(title == null || title.isEmpty()) return file.getName().substring(0, file.getName().lastIndexOf("."));
            else return title;
        } catch(Exception exception) {
            exception.printStackTrace();
            return file.getName().substring(0, file.getName().lastIndexOf("."));
        }
    }

    public static void setName(File file, String name) {
        try {
            AudioFile audioFile = AudioFileIO.getDefaultAudioFileIO().readFile(file);
            checkForEmptyTags(audioFile);
            Tag tag = audioFile.getTag();
            tag.setField(FieldKey.TITLE, name);
            audioFile.setTag(tag);
        } catch(Exception exception) {
            exception.printStackTrace();
        }
    }

    public static String getArtist(File file) {
        try {
            AudioFile audioFile = AudioFileIO.getDefaultAudioFileIO().readFile(file);
            checkForEmptyTags(audioFile);
            String artist = audioFile.getTag().getFirst(FieldKey.ARTIST);
            if(artist == null || artist.isEmpty()) return "Unknown";
            else return artist;
        } catch(Exception exception) {
            exception.printStackTrace();
            return "Unknown";
        }
    }

    public static void setArtist(File file, String artist) {
        try {
            AudioFile audioFile = AudioFileIO.getDefaultAudioFileIO().readFile(file);
            checkForEmptyTags(audioFile);
            Tag tag = audioFile.getTag();
            tag.setField(FieldKey.ARTIST, artist);
            audioFile.setTag(tag);
        } catch(Exception exception) {
            exception.printStackTrace();
        }
    }

    private static void checkForEmptyTags(AudioFile audioFile) {
        Tag tag = audioFile.getTagOrCreateDefault();
        String filename = audioFile.getFile().getName().substring(0, audioFile.getFile().getName().lastIndexOf("."));
        if(Music.getTrack(filename) != null) {
            String title = Objects.requireNonNull(Music.getTrack(filename)).name;
            String artist = Objects.requireNonNull(Music.getTrack(filename)).artist;
            try {
                if(!Objects.equals(tag.getFirst(FieldKey.TITLE), title)) tag.setField(FieldKey.TITLE, title);
                if(!Objects.equals(tag.getFirst(FieldKey.ARTIST), artist)) tag.setField(FieldKey.ARTIST, artist);
            } catch(FieldDataInvalidException e) {
                e.printStackTrace();
            }
        }
        audioFile.setTag(tag);
    }
}
