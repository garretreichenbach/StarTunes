package thederpgamer.startunes.manager;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import thederpgamer.startunes.StarTunes;
import thederpgamer.startunes.data.TrackData;
import thederpgamer.startunes.gui.GUIMusicPanel;
import thederpgamer.startunes.utils.DataUtils;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
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
	private boolean shuffle;
	private boolean looping;
	private boolean paused;
	private Clip clip;

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

	public void previous() {
		if(music.isEmpty()) return;
		if(lastPlayed > 0) {
			lastPlayed--;
			play(music.get(lastPlayed));
		} else play(music.get(music.size() - 1));
	}

	public void next() {
		if(music.isEmpty() || isStopped()) return;
		if(shuffle) {
			if(music.size() == 1) {
				play(music.get(0));
				return;
			}
			int random = (int) (Math.random() * music.size());
			while(random == lastPlayed) random = (int) (Math.random() * music.size());
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

	public void play(TrackData trackData) {
		if(music.isEmpty()) return;
		paused = false;
		stop();
		lastPlayed = music.indexOf(trackData);
		try {
			clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(trackData.getFile()));
			clip.addLineListener(new LineListener() {
				@Override
				public void update(LineEvent event) {
					if(event.getType() == LineEvent.Type.STOP) {
						if(!isStopped() && !isPaused()) {
							if(looping) play();
							else next();
						}
					}
				}
			});
			clip.start();
			GUIMusicPanel.redraw();
		} catch(Exception exception) {
			StarTunes.getInstance().logException(exception.getMessage(), exception);
		}
	}

	public void play(int index) {
		if(music.isEmpty()) return;
		if(index < music.size()) play(music.get(index));
	}

	public void stop() {
		if(music.isEmpty() || clip == null) return;
		try {
			clip.stop();
			clip = null;
			GUIMusicPanel.redraw();
		} catch(Exception exception) {
			StarTunes.getInstance().logException(exception.getMessage(), exception);
		}
	}

	public boolean isStopped() {
		if(music.isEmpty()) return true;
		return !isPlaying() && !isPaused();
	}

	public boolean isPaused() {
		if(music.isEmpty()) return false;
		return paused;
	}

	public void setPaused(boolean paused) {
		if(music.isEmpty() || isStopped()) return;
		try {
			if(paused) clip.stop();
			else clip.start();
			this.paused = paused;
			GUIMusicPanel.redraw();
		} catch(Exception exception) {
			StarTunes.getInstance().logException(exception.getMessage(), exception);
		}
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
		return clip != null && !isPaused();
	}

	public boolean isPlaying(TrackData trackData) {
		return isPlaying() && music.get(lastPlayed).equals(trackData);
	}

	public ObjectArrayList<TrackData> getMusic() {
		return music;
	}

	public long getRunTime() {
		if(music.isEmpty() || isStopped() || clip == null) return 0;
		try {
			return clip.getMicrosecondPosition() / 1000;
		} catch(Exception exception) {
			StarTunes.getInstance().logException(exception.getMessage(), exception);
			return 0;
		}
	}

	public TrackData getCurrentTrack() {
		if(music.isEmpty() || isStopped()) return null;
		if(lastPlayed < music.size()) return music.get(lastPlayed);
		else return music.get(0);
	}
}
