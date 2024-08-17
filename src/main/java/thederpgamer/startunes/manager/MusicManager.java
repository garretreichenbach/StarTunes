package thederpgamer.startunes.manager;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.sound.pcode.SoundManager;
import org.schema.schine.sound.pcode.SoundPool;
import org.schema.schine.sound.pcode.SoundPoolEntry;
import paulscode.sound.SoundSystem;
import thederpgamer.startunes.StarTunes;
import thederpgamer.startunes.data.TrackData;
import thederpgamer.startunes.gui.GUIMusicPanel;
import thederpgamer.startunes.utils.DataUtils;

import java.io.File;
import java.lang.reflect.Field;
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
	private boolean shuffle;
	private boolean looping;
	private boolean paused;

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
		try {
			Field field = Controller.getAudioManager().getClass().getDeclaredField("sndSystem");
			field.setAccessible(true);
			return (SoundSystem) field.get(Controller.getAudioManager());
		} catch(Exception exception) {
			StarTunes.getInstance().logException(exception.getMessage(), exception);
			return null;
		}
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
		if(looping) play();
		else {
			if(shuffle) {
				int random = (int) (Math.random() * music.size());
				play(music.get(random));
			} else if(lastPlayed < music.size() - 1) {
				lastPlayed++;
				play(music.get(lastPlayed));
			} else play(music.get(0));
		}
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
		lastPlayed = music.indexOf(trackData);
		try {
			Field soundPoolField = Controller.audioManager.getClass().getDeclaredField("soundPoolMusic");
			soundPoolField.setAccessible(true);
			SoundPool soundPool = (SoundPool) soundPoolField.get(Controller.audioManager);
			SoundPoolEntry soundpoolentry = soundPool.get(trackData.getName());
			if(soundpoolentry != null) {
				getSoundSystem().backgroundMusic("bm", soundpoolentry.soundUrl, soundpoolentry.soundName, false);
				getSoundSystem().setVolume("bm", 0.18f);
			}
		} catch(Exception exception) {
			StarTunes.getInstance().logException(exception.getMessage(), exception);
		}
		(new Thread() {
			@Override
			public void run() {
				while(isPlaying(trackData)) {
					try {
						sleep(100);
					} catch(InterruptedException exception) {
						StarTunes.getInstance().logException(exception.getMessage(), exception);
					}
				}
				if(!isStopped()) next();
			}
		}).start();
	}

	public void play(int index) {
		if(music.isEmpty()) return;
		if(index < music.size()) play(music.get(index));
	}

	public void stop() {
		if(music.isEmpty()) return;
		Objects.requireNonNull(getSoundSystem()).stop("bm");
	}

	public boolean isStopped() {
		if(music.isEmpty()) return true;
		return !isPlaying();
	}

	public boolean isPaused() {
		if(music.isEmpty()) return false;
		return paused;
	}

	public void setPaused(boolean paused) {
		if(music.isEmpty()) return;
		SoundSystem soundSystem = getSoundSystem();
		if(soundSystem != null) {
			if(this.paused) soundSystem.play("bm");
			else soundSystem.pause("bm");
		}
		this.paused = paused;
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
		if(soundSystem != null) return soundSystem.playing("bm");
		return false;
	}

	public boolean isPlaying(TrackData trackData) {
		return isPlaying() && music.get(lastPlayed).equals(trackData);
	}

	public ObjectArrayList<TrackData> getMusic() {
		return music;
	}

	public long getRunTime() {
		if(music.isEmpty()) return 0;
		return Objects.requireNonNull(getSoundSystem()).playing("bm") ? (long) getSoundSystem().millisecondsPlayed("bm") : 0;
	}

	public TrackData getCurrentTrack() {
		if(music.isEmpty()) return null;
		if(lastPlayed < music.size()) return music.get(lastPlayed);
		else return music.get(0);
	}
}
