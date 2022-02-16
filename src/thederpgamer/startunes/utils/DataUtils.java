package thederpgamer.startunes.utils;

import api.common.GameCommon;
import api.mod.ModSkeleton;
import thederpgamer.startunes.StarTunes;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [02/15/2022]
 */
public class DataUtils {

    private static final ModSkeleton instance = StarTunes.getInstance().getSkeleton();

    public static String getResourcesPath() {
        return instance.getResourcesFolder().getPath().replace('\\', '/');
    }

    public static String getWorldDataPath() {
        return getResourcesPath() + "/data/" + GameCommon.getUniqueContextId();
    }
}
