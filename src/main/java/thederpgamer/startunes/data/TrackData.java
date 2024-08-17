package thederpgamer.startunes.data;

import org.apache.commons.io.FileUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.json.JSONArray;
import org.json.JSONObject;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.sound.pcode.SoundPool;
import thederpgamer.startunes.StarTunes;
import thederpgamer.startunes.utils.DataUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Locale;

/**
 * TrackData class.
 *
 * @author TheDerpGamer
 */
public class TrackData implements JSONSerializable, Comparable<TrackData> {

	private String name;
	private String artist;
	private long runTime;

	private TrackData(String name, String artist, int runTime) {
		this.name = name;
		this.artist = artist;
		this.runTime = runTime;
	}

	public TrackData(JSONObject data) {
		fromJSON(data);
	}

	public static TrackData loadFromFile(File file) {
		try {
			String type = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase(Locale.ENGLISH);
			if(type.equals("mp3") || type.equals("wav")) {
				AudioFile audioFile = AudioFileIO.getDefaultAudioFileIO().readFile(file);
				if(audioFile != null) {
					String name = getField(audioFile, FieldKey.TITLE);
					String artist = getField(audioFile, FieldKey.ARTIST);
					int runTime = audioFile.getAudioHeader().getTrackLength();
					int minutes = runTime / 60;
					int seconds = runTime % 60;
					runTime = (minutes * 60 + seconds) * 1000;
					try {
						Field field = Controller.audioManager.getClass().getDeclaredField("soundPoolMusic");
						field.setAccessible(true);
						SoundPool soundPool = (SoundPool) field.get(Controller.audioManager);
						soundPool.addSound("music", file);
					} catch(Exception exception) {
						StarTunes.getInstance().logException(exception.getMessage(), exception);
					}
					return loadTrackInfo(name, artist, runTime);
				}
			} else throw new IllegalArgumentException("Invalid file type " + type + "\nSupported file types: .mp3, .wav");
		} catch(Exception exception) {
			StarTunes.getInstance().logException(exception.getMessage(), exception);
		}
		return null;
	}

	private static TrackData loadTrackInfo(String name, String artist, int runTime) {
		File trackDataJson = new File(DataUtils.getResourcesPath() + "/music/track_data.json");
		try {
			JSONArray data;
			if(!trackDataJson.exists()) {
				data = new JSONArray();
				FileUtils.write(trackDataJson, data.toString(), "UTF-8");
			} else data = new JSONArray(FileUtils.readFileToString(trackDataJson, "UTF-8"));
			for(int i = 0; i < data.length(); i++) {
				JSONObject trackData = data.getJSONObject(i);
				if(trackData.getString("name").equals(name) && trackData.getString("artist").equals(artist) && trackData.getInt("runTime") == runTime) return new TrackData(trackData);
			}
			TrackData trackData = new TrackData(name, artist, runTime);
			data.put(trackData.toJSON());
			FileUtils.write(trackDataJson, data.toString(), "UTF-8");
			return trackData;
		} catch(Exception exception) {
			StarTunes.getInstance().logException(exception.getMessage(), exception);
			return null;
		}
	}

	private static void checkForEmptyTags(AudioFile audioFile) {
		Tag tag = audioFile.getTagOrCreateDefault();
		if(tag.isEmpty() || tag.getFirst(FieldKey.TITLE) == null || tag.getFirst(FieldKey.ARTIST) == null) {
			String title = audioFile.getFile().getName().substring(0, audioFile.getFile().getName().lastIndexOf("."));
			String artist = "Unknown";
			try {
				if(tag.isEmpty()) {
					tag.setField(FieldKey.TITLE, title);
					tag.setField(FieldKey.ARTIST, artist);
				} else {
					if(tag.getFirst(FieldKey.TITLE) == null) tag.setField(FieldKey.TITLE, title);
					if(tag.getFirst(FieldKey.ARTIST) == null) tag.setField(FieldKey.ARTIST, artist);
				}
			} catch(Exception exception) {
				StarTunes.getInstance().logException(exception.getMessage(), exception);
			}
		}
		audioFile.setTag(tag);
	}

	public static String getField(AudioFile file, FieldKey key) {
		checkForEmptyTags(file);
		return file.getTag().getFirst(key);
	}

	@Override
	public JSONObject toJSON() {
		JSONObject data = new JSONObject();
		data.put("name", name);
		data.put("artist", artist);
		data.put("runTime", runTime);
		return data;
	}

	@Override
	public void fromJSON(JSONObject data) {
		name = data.getString("name");
		artist = data.getString("artist");
		runTime = data.getLong("runTime");
	}

	@Override
	public int compareTo(TrackData o) {
		return name.compareTo(o.name);
	}

	public String getName() {
		return name;
	}

	public String getArtist() {
		return artist;
	}

	public long getDuration() {
		return runTime;
	}
}
