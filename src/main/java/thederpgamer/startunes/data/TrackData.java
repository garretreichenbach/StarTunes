package thederpgamer.startunes.data;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [02/16/2022]
 */
public class TrackData {

    public String name;
    public String artist;
    public int runTime;
    public boolean autoPlay;

    public TrackData(String name, String artist, int runTime, boolean autoPlay) {
        this.name = name;
        this.artist = artist;
        this.runTime = runTime;
        this.autoPlay = autoPlay;
    }
}
