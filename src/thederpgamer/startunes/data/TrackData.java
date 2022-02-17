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
    public float combat;
    public float exploration;
    public float building;

    public TrackData(String name, String artist, int runTime) {
        this.name = name;
        this.artist = artist;
        this.runTime = runTime;
    }
}
