package thederpgamer.startunes.gui.musicplayer;

import api.common.GameClient;
import api.utils.gui.GUIControlManager;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [02/15/2022]
 */
public class MusicPlayerControlManager extends GUIControlManager {

    public MusicPlayerControlManager() {
        super(GameClient.getClientState());
    }

    @Override
    public MusicPlayerMenuPanel createMenuPanel() {
        return new MusicPlayerMenuPanel(getState());
    }

    @Override
    public MusicPlayerMenuPanel getMenuPanel() {
        return (MusicPlayerMenuPanel) super.getMenuPanel();
    }
}
