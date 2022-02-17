package thederpgamer.startunes.manager;

import org.schema.schine.graphicsengine.core.Controller;
import paulscode.sound.Library;
import paulscode.sound.SoundSystem;
import paulscode.sound.Source;
import paulscode.sound.libraries.SourceLWJGLOpenAL;
import thederpgamer.startunes.StarTunes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [02/16/2022]
 */
public class MusicManager {

    public static int musicVolume;
    public static SourceLWJGLOpenAL currentSource;
    public static final ConcurrentHashMap<Integer, String> playList = new ConcurrentHashMap<>();
    private static int trackIndex = 0;
    public static boolean trackLoop;

    public static void setShuffle(boolean shuffle) {
        initializePlayList();
        if(shuffle) {
            ArrayList<String> temp = new ArrayList<>(playList.values());
            Collections.shuffle(temp);
            playList.clear();
            for(int i = 0; i < temp.size(); i ++) playList.put(i, temp.get(i));
        }
    }

    public static String getCurrentTrack() {
        return StarTunes.getInstance().musicControlManager.getMenuPanel().currentTrack;
    }

    public static void setCurrentTrack(String track) {
        if(playList.isEmpty()) initializePlayList();
        Controller.getAudioManager().stopBackgroundMusic();
        Controller.getAudioManager().playBackgroundMusic(getTrackName(track), MusicManager.musicVolume);
        StarTunes.getInstance().trackDrawer.setCurrentTrack(getTrackName(track), getArtistName(track));
        StarTunes.getInstance().musicControlManager.getMenuPanel().currentTrack = track;
        StarTunes.getInstance().musicControlManager.getMenuPanel().runTimer = getRunTimeMS(getRunTime(track));
        StarTunes.getInstance().musicControlManager.getMenuPanel().trackLength = getRunTimeMS(getRunTime(track));
        updateCurrentTrack();
        LogManager.logInfo("Now playing: " + track);
        StarTunes.getInstance().musicControlManager.getMenuPanel().recreateTabs();
    }

    public static void nextTrack() {
        if(playList.isEmpty()) initializePlayList();
        trackIndex ++;
        if(trackIndex >= playList.size()) trackIndex = 0;
        setCurrentTrack(playList.get(trackIndex));
    }

    public static void prevTrack() {
        if(playList.isEmpty()) initializePlayList();
        trackIndex --;
        if(trackIndex <= 0) trackIndex = playList.size() - 1;
        setCurrentTrack(playList.get(trackIndex));
    }

    public static void initializePlayList() {
        int i = 0;
        for(String track : ResourceManager.musicMap.keySet()) {
            playList.put(i, track);
            i ++;
        }
    }

    public static void setLoop(boolean loop) {
        if(currentSource != null) trackLoop = loop;
    }

    public static void setPaused(boolean pause) {
        if(currentSource != null) {
            if(pause) currentSource.pause();
            else currentSource.play(currentSource.channel);
        }
    }

    public static boolean isPaused() {
        return currentSource != null && currentSource.paused();
    }

    public static void setMusicTime(long time) {
        if(currentSource != null) {
            //currentSource.
        }
    }

    public static void updateCurrentTrack() {
        try {
            Field soundSystemField = Controller.getAudioManager().getClass().getDeclaredField("sndSystem");
            soundSystemField.setAccessible(true);
            SoundSystem soundSystem = (SoundSystem) soundSystemField.get(Controller.getAudioManager());

            Field libraryField = soundSystem.getClass().getDeclaredField("soundLibrary");
            libraryField.setAccessible(true);
            Library library = (Library) libraryField.get(soundSystem);
            for(Map.Entry<String, Source> entry : library.getSources().entrySet()) {
                if(entry.getValue() instanceof SourceLWJGLOpenAL && entry.getKey().equals("bm")) {
                    currentSource = (SourceLWJGLOpenAL) entry.getValue();
                    return;
                }
            }
        } catch(Exception exception) {
            exception.printStackTrace();
        }
    }

    private static String getTrackName(String entry) {
        return entry.split(" - ")[0].trim();
    }

    private static String getArtistName(String entry) {
        return entry.split(" - ")[1].trim();
    }

    private static int getRunTime(String entry) {
        return Integer.parseInt(entry.split(" - ")[2].trim());
    }

    private static long getRunTimeMS(int runTime) {
        return runTime * 1000L;
    }
}
