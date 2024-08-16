package thederpgamer.startunes.manager;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.sound.pcode.SoundManager;
import paulscode.sound.SoundSystem;
import thederpgamer.startunes.StarTunes;
import thederpgamer.startunes.data.TrackData;
import thederpgamer.startunes.utils.DataUtils;

import java.io.File;
import java.util.Objects;

/**
 * Manages music playback.
 *
 * @author TheDerpGamer
 */
public class MusicManager {

	private static final ObjectArrayList<TrackData> music = new ObjectArrayList<>();
	private static MusicManager manager;
	private int lastPlayed;
	private boolean autoPlay = true;
	private boolean shuffle;
	private boolean looping;
	private Thread musicThread;
	private long runTime;
	private boolean paused;
	private long start;

	public static MusicManager getManager() {
		return manager;
	}

	public static void initialize(StarTunes instance) {
		try {
			File musicFolder = new File(DataUtils.getResourcesPath() + "/music");
			for(File file : Objects.requireNonNull(musicFolder.listFiles())) {
				if(file.getName().endsWith(".json")) continue; //Skip JSON files
				TrackData trackData = TrackData.loadFromFile(file);
				if(trackData != null) music.add(trackData);
			}
			manager = new MusicManager();
		} catch(Exception exception) {
			instance.logException(exception.getMessage(), exception);
		}
	}

	public static SoundSystem getSoundSystem() {
		return SoundManager.sndSystem;
	}

	public void previous() {
		if(music.isEmpty()) return;
		if(lastPlayed > 0) {
			lastPlayed--;
			play(music.get(lastPlayed));
		} else play(music.get(music.size() - 1));
	}

	public void next() {
		if(music.isEmpty()) return;
		if(shuffle) {
			int random = (int) (Math.random() * music.size());
			play(music.get(random));
		} else if(lastPlayed < music.size() - 1) {
			lastPlayed++;
			play(music.get(lastPlayed));
		} else play(music.get(0));
	}

	public void play() {
		if(music.isEmpty()) return;
		if(lastPlayed < music.size()) play(music.get(lastPlayed));
		else play(music.get(0));
	}

	public void play(final TrackData trackData) {
		if(music.isEmpty()) return;
		paused = false;
		stop();
		Controller.audioManager.playBackgroundMusic(trackData.getName(), getVolume());
		lastPlayed = music.indexOf(trackData);

		if(musicThread != null) musicThread.interrupt();
		start = System.currentTimeMillis();
		musicThread = new Thread("MusicThread") {
			@Override
			public void run() {
				runTime = System.currentTimeMillis() - start;
				while(isPlaying(trackData)) {
					if(System.currentTimeMillis() - runTime >= trackData.getDuration()) {
						if(isLooping()) play(trackData);
						else if(autoPlay) next();
						else break;
					}
					try {
						sleep(1000);
					} catch(InterruptedException exception) {
						StarTunes.getInstance().logException(exception.getMessage(), exception);
					}
				}
			}
		};
		musicThread.start();
	}

	public void play(int index) {
		if(music.isEmpty()) return;
		if(index < music.size()) play(music.get(index));
	}

	public void stop() {
		if(music.isEmpty()) return;
		Controller.audioManager.stopBackgroundMusic();
	}

	public boolean isStopped() {
		if(music.isEmpty()) return true;
		return !isPlaying();
	}

	public boolean isPaused() {
		if(music.isEmpty()) return false;
		SoundSystem soundSystem = getSoundSystem();
		if(soundSystem != null) return soundSystem.playing("music") && paused;
		return paused;
	}

	public void setPaused(boolean paused) {
		if(music.isEmpty()) return;
		SoundSystem soundSystem = getSoundSystem();
		if(soundSystem != null) {
			if(paused) play();
			else soundSystem.pause("music");
		}
		this.paused = paused;
	}

	public float getVolume() {
		return Controller.audioManager.getMusicVolume();
	}

	public void setVolume(float volume) {
		Controller.audioManager.setMusicVolume(volume);
	}

	public boolean isLooping() {
		return looping;
	}

	public void setLooping(boolean looping) {
		this.looping = looping;
	}

	public boolean isShuffle() {
		return shuffle;
	}

	public void setShuffle(boolean shuffle) {
		this.shuffle = shuffle;
	}

	public boolean isPlaying() {
		if(music.isEmpty()) return false;
		SoundSystem soundSystem = getSoundSystem();
		if(soundSystem != null) return soundSystem.playing("music");
		return false;
	}

	public boolean isPlaying(TrackData trackData) {
		return isPlaying() && music.get(lastPlayed).equals(trackData);
	}

	public boolean isAutoPlay() {
		return autoPlay;
	}

	public void setAutoPlay(boolean autoPlay) {
		this.autoPlay = autoPlay;
	}

	public ObjectArrayList<TrackData> getMusic() {
		return music;
	}

	public long getRunTime() {
		return runTime;
	}

	public TrackData getCurrentTrack() {
		if(music.isEmpty()) return null;
		if(lastPlayed < music.size()) return music.get(lastPlayed);
		else return music.get(0);
	}
}
