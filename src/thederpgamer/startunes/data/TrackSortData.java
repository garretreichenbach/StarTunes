package thederpgamer.startunes.data;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [02/17/2022]
 */
public class TrackSortData implements Comparable<TrackSortData> {

    public String track;
    public int runTime;

    public TrackSortData(String track, int runTime) {
        this.track = track;
        this.runTime = runTime;
    }

    @Override
    public int compareTo(TrackSortData o) {
        return Integer.compare(runTime, o.runTime);
    }
}