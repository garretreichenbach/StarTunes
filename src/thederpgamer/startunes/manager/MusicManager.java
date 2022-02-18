package thederpgamer.startunes.manager;

import api.utils.textures.StarLoaderTexture;
import org.schema.schine.graphicsengine.core.Controller;
import paulscode.sound.Library;
import paulscode.sound.SoundSystem;
import paulscode.sound.Source;
import paulscode.sound.libraries.SourceLWJGLOpenAL;
import thederpgamer.startunes.StarTunes;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [02/16/2022]
 */
public class MusicManager {

    public static final int NAME = 0;
    public static final int ARTIST = 1;
    public static final int RUNTIME = 2;

    public static int currentSort = NAME;
    public static int musicVolume;
    public static SourceLWJGLOpenAL currentSource;
    public static final ConcurrentHashMap<Integer, String> playList = new ConcurrentHashMap<>();
    private static int trackIndex = 0;

    public static boolean stopped = true;
    public static boolean trackLoop = false;
    public static boolean trackShuffle = false;
    public static String currentTrack;
    public static long currentLength = 0;
    private static Timer timer;

    public static float getProgress() {
        if(currentSource == null) return 0;
        else return currentSource.millisecondsPlayed() / currentLength;
    }

    public static void stopMusic() {
        if(currentSource != null) {
            currentSource.stop();
            //currentSource.cleanup();
        }
        Controller.getAudioManager().stopBackgroundMusic();
        currentTrack = null;
        currentLength = 0;
        try {
            timer.cancel();
            timer.purge();
        } catch(Exception exception) {
            exception.printStackTrace();
        }
        stopped = true;
    }

    public static void setShuffle(boolean shuffle) {
        initializePlayList();
        trackShuffle = shuffle;
        if(trackShuffle) {
            ArrayList<String> temp = new ArrayList<>(playList.values());
            Collections.shuffle(temp);
            playList.clear();
            for(int i = 0; i < temp.size(); i ++) playList.put(i, temp.get(i));
            StarTunes.getInstance().musicControlManager.getMenuPanel().recreateTabs();
        }
    }

    public static void setCurrentTrack(String track) {
        if(playList.isEmpty()) initializePlayList();
        stopMusic();
        currentTrack = track;
        stopped = false;
        currentLength = getRunTimeMS(getRunTime(currentTrack));
        try {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(trackLoop) setCurrentTrack(currentTrack);
                    else nextTrack();
                }
            }, currentLength);
        } catch(Exception exception) {
            exception.printStackTrace();
        }

        Controller.getAudioManager().playBackgroundMusic(getTrackName(track), MusicManager.musicVolume);
        StarTunes.getInstance().trackDrawer.setCurrentTrack(getTrackName(track), getArtistName(track));
        updateCurrentTrack();
        LogManager.logInfo("Now playing: " + track);
        StarLoaderTexture.runOnGraphicsThread(new Runnable() {
            @Override
            public void run() {
                StarTunes.getInstance().musicControlManager.getMenuPanel().recreateTabs();
            }
        });
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
        timer = new Timer();
        int i = 0;
        for(String track : ResourceManager.musicMap.keySet()) {
            playList.put(i, track);
            i ++;
        }
        try {
            StarTunes.getInstance().musicControlManager.getMenuPanel().sortPlayList(playList);
        } catch(Exception ignored) {}
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

    public static String getTrackName(String entry) {
        return entry.split(" - ")[0].trim();
    }

    public static String getArtistName(String entry) {
        return entry.split(" - ")[1].trim();
    }

    public static int getRunTime(String entry) {
        return ResourceManager.musicMap.get(entry).runTime;
    }

    public static long getRunTimeMS(int runTime) {
        return runTime * 1000L;
    }
}
