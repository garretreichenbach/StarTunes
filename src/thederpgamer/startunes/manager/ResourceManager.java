package thederpgamer.startunes.manager;

import org.apache.commons.io.FileUtils;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.resource.ResourceLoader;
import thederpgamer.startunes.StarTunes;
import thederpgamer.startunes.utils.DataUtils;

import java.io.File;
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

    public static final int NONE = 0;
    public static final int COMBAT = 1;
    public static final int EXPLORATION = 2;
    public static final int BUILDING = 3;

    public enum Music {
        GREEN("Green", "Jontyfreack", COMBAT),
        MOCHUR("Mochur", "Jontyfreack", COMBAT),
        WANDERER("Wanderer", "Jontyfreack", COMBAT, EXPLORATION),
        YGOLLLL_IV("Ygollll IV", "Jontyfreack", COMBAT);

        public final String name;
        public final String artist;
        public final int[] autoplay;

        Music(String name, String artist, int... autoplay) {
            this.name = name;
            this.artist = artist;
            this.autoplay = autoplay;
        }
    }

    public final static HashMap<String, int[]> musicMap = new HashMap<String, int[]>();

    public static void loadResources(StarTunes instance, ResourceLoader loader) {
        File musicFolder = new File(DataUtils.getWorldDataPath() + "/music");
        if(!musicFolder.exists()) musicFolder.mkdirs();

        if(musicFolder.listFiles() != null && musicFolder.listFiles().length > 0) {
            for(File musicFile : musicFolder.listFiles()) {
                if(musicFile.getName().toLowerCase().endsWith(".wav") || musicFile.getName().toLowerCase().endsWith(".ogg")) {
                    try {
                        String[] fields = musicFile.getName().substring(0, musicFile.getName().lastIndexOf(".")).split(" - ");
                        Controller.getAudioManager().addSound(fields[0], musicFile);
                        musicMap.put(musicFile.getName().substring(0, musicFile.getName().lastIndexOf(".")), new int[] {NONE}); //Todo: Allow tag arguments in name somehow
                    } catch(Exception exception) {
                        LogManager.logException("Failed to load music file \"" + musicFile.getName() + "\". Check to make sure the file name is formatted correctly and ends in .wav or .ogg", exception);
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
                Controller.getAudioManager().addSound(fields[0], exportFile);
                musicMap.put(exportFile.getName().substring(0, exportFile.getName().lastIndexOf(".")), music.autoplay);
            } catch(IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}
