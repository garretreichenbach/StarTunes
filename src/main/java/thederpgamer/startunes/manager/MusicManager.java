package thederpgamer.startunes.manager;

import api.utils.textures.StarLoaderTexture;
import org.apache.commons.io.FileUtils;
import org.schema.schine.graphicsengine.core.Controller;
import paulscode.sound.Library;
import paulscode.sound.SoundSystem;
import paulscode.sound.Source;
import paulscode.sound.libraries.SourceLWJGLOpenAL;
import thederpgamer.startunes.StarTunes;
import thederpgamer.startunes.data.TrackData;
import thederpgamer.startunes.utils.DataUtils;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
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
    public static final ConcurrentHashMap<Integer, String> playList = new ConcurrentHashMap<>();
    public static int currentSort = NAME;
    public static int musicVolume;
    public static SourceLWJGLOpenAL currentSource;
    public static boolean stopped = true;
    public static boolean trackLoop = false;
    public static boolean trackShuffle = false;
    public static String currentTrack;
    public static long currentLength = 0;
    public static boolean valid = false;
    public static boolean finishedDownloading = true;
    private static int trackIndex = 0;
    private static Timer timer;
    private static int timeOutCounter = 0;
    private static float pauseTimer = 0;

    public static float getProgress() {
        if(currentSource == null) return 0;
        else return currentSource.millisecondsPlayed() / currentLength;
    }

    public static TrackData getTrackData(String entry) {
        return ResourceManager.musicMap.get(entry);
    }

    public static void toggleAutoPlay(String entry) {
        getTrackData(entry).autoPlay = !getTrackData(entry).autoPlay;
        ResourceManager.saveTrackData();
        if(currentTrack != null && currentTrack.equals(entry)) nextTrack();
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
            //timer.purge();
        } catch(Exception exception) {
            exception.printStackTrace();
        }
        stopped = true;
        StarLoaderTexture.runOnGraphicsThread(new Runnable() {
            @Override
            public void run() {
                StarTunes.getInstance().musicControlManager.getMenuPanel().recreateTabs();
            }
        });
    }

    public static void purge() {
        if(currentSource != null) {
            currentSource.stop();
            //currentSource.cleanup();
        }
        Controller.getAudioManager().stopBackgroundMusic();
        stopped = false;
    }

    public static void setShuffle(boolean shuffle) {
        initializePlayList();
        trackShuffle = shuffle;
        if(trackShuffle) {
            ArrayList<String> temp = new ArrayList<>(playList.values());
            Collections.shuffle(temp);
            playList.clear();
            for(int i = 0; i < temp.size(); i++) playList.put(i, temp.get(i));
            StarTunes.getInstance().musicControlManager.getMenuPanel().recreateTabs();
        }
    }

    public static void setCurrentTrack(String track) {
        //stopMusic();
        if(playList.isEmpty()) initializePlayList();
        currentTrack = track;
        //stopped = false;
        currentLength = getRunTimeMS(getRunTime(currentTrack));
        try {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    purge();
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
        String nextAvailable = getNextAvailableTrack(1);
        if(nextAvailable != null) setCurrentTrack(nextAvailable);
        else stopMusic();
    }

    public static void prevTrack() {
        if(playList.isEmpty()) initializePlayList();
        String nextAvailable = getNextAvailableTrack(-1);
        if(nextAvailable != null) setCurrentTrack(nextAvailable);
        else stopMusic();
    }

    private static String getNextAvailableTrack(int skipDir) {
        int attempts = 0;
        int tempIndex = trackIndex;
        while(attempts < playList.size()) {
            if(skipDir < 0) {
                tempIndex--;
                if(tempIndex <= 0) tempIndex = playList.size() - 1;
            } else if(skipDir > 0) {
                tempIndex += skipDir;
                if(tempIndex >= playList.size()) tempIndex = 0;
            }
            if(getTrackData(playList.get(tempIndex)).autoPlay) {
                trackIndex = tempIndex;
                return playList.get(tempIndex);
            }
            attempts++;
        }
        return null;
    }

    public static void reloadPlayList() {
        playList.clear();
        int i = 0;
        for(String track : ResourceManager.musicMap.keySet()) {
            playList.put(i, track);
            i++;
        }
        try {
            StarTunes.getInstance().musicControlManager.getMenuPanel().sortPlayList(playList);
        } catch(Exception ignored) {}
    }

    public static void initializePlayList() {
        timer = new Timer();
        int i = 0;
        for(String track : ResourceManager.musicMap.keySet()) {
            playList.put(i, track);
            i++;
        }
        try {
            StarTunes.getInstance().musicControlManager.getMenuPanel().sortPlayList(playList);
        } catch(Exception ignored) {}
    }

    public static void setLoop(boolean loop) {
        if(currentSource != null) trackLoop = loop;
    }

    public static boolean isPaused() {
        return currentSource != null && currentSource.paused();
    }

    public static void setPaused(boolean pause) {
        if(currentSource != null) {
            if(pause) {
                pauseTimer = currentSource.millisecondsPlayed();
                currentSource.pause();
                timer.cancel();
            } else {
                currentLength = (long) (getRunTimeMS(getRunTime(currentTrack)) - pauseTimer);
                try {
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            purge();
                            if(trackLoop) setCurrentTrack(currentTrack);
                            else nextTrack();
                        }
                    }, currentLength);
                } catch(Exception exception) {
                    exception.printStackTrace();
                }
                currentSource.play(currentSource.channel);
            }
        }
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
        return ResourceManager.musicMap.get(entry).name;
    }

    public static String getArtistName(String entry) {
        return ResourceManager.musicMap.get(entry).artist;
    }

    public static int getRunTime(String entry) {
        return ResourceManager.musicMap.get(entry).runTime;
    }

    public static long getRunTimeMS(int runTime) {
        return runTime * 1000L;
    }

    public static boolean downloadFile(String text, String title, String artist) {
        valid = false;
        final File[] track = {null};
        new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL(text);
                    URLConnection urlConnection = url.openConnection();
                    urlConnection.setRequestProperty("User-Agent", "NING/1.0");
                    InputStream stream = urlConnection.getInputStream();
                    track[0] = new File(DataUtils.getResourcesPath() + "/music/" + title + ".wav");
                    if(track[0].exists()) track[0].delete();
                    track[0].createNewFile();
                    FileUtils.copyInputStreamToFile(stream, track[0]);
                    stream.close();
                } catch(Exception exception) {
                    exception.printStackTrace();
                    if(track[0] != null && track[0].exists()) track[0].delete();
                    track[0] = null;
                }
            }
        }.start();

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(timeOutCounter > 10) {
                    timeOutCounter = 0;
                    finishedDownloading = true;
                    valid = false;
                    cancel();
                }

                if(track[0] != null && track[0].exists() && track[0].getName().toLowerCase().endsWith(".wav")) {
                    ResourceManager.setName(track[0], title);
                    ResourceManager.setArtist(track[0], artist);
                    Controller.getAudioManager().addSound(title, track[0]);
                    //ResourceManager.musicMap.remove(title);
                    ResourceManager.musicMap.put(title, ResourceManager.getTrackData(track[0]));
                    ResourceManager.saveTrackData();
                    reloadPlayList();
                    StarLoaderTexture.runOnGraphicsThread(new Runnable() {
                        @Override
                        public void run() {
                            StarTunes.getInstance().musicControlManager.getMenuPanel().recreateTabs();
                        }
                    });
                    valid = true;
                    finishedDownloading = true;
                    timeOutCounter = 0;
                    StarTunes.getInstance().musicControlManager.getMenuPanel().musicDownloader.deactivate();
                    cancel();
                }
                timeOutCounter++;
            }
        }, 5000, 5000); //Check every 5 seconds
        return finishedDownloading;
    }
}
