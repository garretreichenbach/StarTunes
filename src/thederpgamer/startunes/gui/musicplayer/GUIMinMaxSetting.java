package thederpgamer.startunes.gui.musicplayer;

import org.schema.game.client.controller.manager.ingame.AbstractSizeSetting;
import thederpgamer.startunes.manager.ConfigManager;
import thederpgamer.startunes.manager.MusicManager;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [02/16/2022]
 */
public class GUIMinMaxSetting extends AbstractSizeSetting {

    private final int min;
    private final int max;

    public GUIMinMaxSetting(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public void dec() {
        setting = Math.max(min, setting - 1);
        MusicManager.musicVolume = setting;
        ConfigManager.getMainConfig().set("music-volume", MusicManager.musicVolume);
        if (guiCallBack != null) guiCallBack.settingChanged(setting);
    }

    @Override
    public void inc() {
        setting = Math.min(max, setting + 1);
        MusicManager.musicVolume = setting;
        ConfigManager.getMainConfig().set("music-volume", MusicManager.musicVolume);
        if (guiCallBack != null) guiCallBack.settingChanged(setting);
    }

    @Override
    public int getMin() {
        return min;
    }

    @Override
    public int getMax() {
        return max;
    }
}