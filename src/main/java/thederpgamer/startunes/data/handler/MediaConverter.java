package thederpgamer.startunes.data.handler;

import thederpgamer.startunes.data.TrackData;

import java.io.InputStream;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public interface MediaConverter {

	InputStream createStream(TrackData trackData);
}
