package thederpgamer.startunes.data.handler;

import thederpgamer.startunes.StarTunes;
import thederpgamer.startunes.data.TrackData;
import thederpgamer.startunes.utils.AudioUtils;
import thederpgamer.startunes.utils.DataUtils;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Converts .mp3 media files to .wav format.
 *
 * @author TheDerpGamer
 */
public class MP3Converter implements MediaConverter {

	@Override
	public InputStream createStream(TrackData trackData) {
		try {
			InputStream baseStream = Files.newInputStream(Paths.get(DataUtils.getResourcesPath() + "/music/" + trackData.getName() + ".mp3"));
			ByteBuffer buffer = ByteBuffer.allocate(baseStream.available());
			while(baseStream.available() > 0) buffer.put((byte) baseStream.read());
			byte[] bytes = buffer.array();
			final byte[] converted = AudioUtils.convertAudioBytesToWave(bytes);
			return new InputStream() {
				private int index;

				@Override
				public int read() {
					if(index < converted.length) {
						int i = converted[index] & 0xFF;
						index++;
						return i;
					} else return -1;
				}
			};
		} catch(Exception exception) {
			StarTunes.getInstance().logException(exception.getMessage(), exception);
			return null;
		}
	}
}
