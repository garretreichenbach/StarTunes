package thederpgamer.startunes.manager;

import api.common.GameClient;
import api.listener.Listener;
import api.listener.events.gui.GUITopBarCreateEvent;
import api.listener.events.input.KeyPressEvent;
import api.mod.StarLoader;
import org.schema.game.client.view.gui.newgui.GUITopBar;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationHighlightCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;
import thederpgamer.startunes.StarTunes;
import thederpgamer.startunes.gui.GUIMusicDialog;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class EventManager {

	public static void initialize(StarTunes instance) {
		StarLoader.registerListener(KeyPressEvent.class, new Listener<KeyPressEvent>() {
            @Override
            public void onEvent(KeyPressEvent event) {
                if(event.isKeyDown()) {
                    if(GameClient.getClientState() != null && GameClient.getClientPlayerState() != null) {
                        if(event.getKey() == ConfigManager.OPEN_PLAYER) {
                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                            GameClient.getClientState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().deactivateAll();
                            new GUIMusicDialog().activate();
                        } else if(event.getKey() == ConfigManager.PAUSE_MUSIC) MusicManager.getManager().setPaused(!MusicManager.getManager().isPaused());
                        else if(event.getKey() == ConfigManager.NEXT_TRACK) MusicManager.getManager().next();
                        else if(event.getKey() == ConfigManager.PREVIOUS_TRACK) MusicManager.getManager().previous();
                    }
                }
            }
        }, instance);

        StarLoader.registerListener(GUITopBarCreateEvent.class, new Listener<GUITopBarCreateEvent>() {
            @Override
            public void onEvent(final GUITopBarCreateEvent event) {
                GUITopBar.ExpandedButton dropDownButton = event.getDropdownButtons().get(event.getDropdownButtons().size() - 1);
                dropDownButton.addExpandedButton("MUSIC PLAYER", new GUICallback() {
                    @Override
                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                        if(mouseEvent.pressedLeftMouse()) {
                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                            GameClient.getClientState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().deactivateAll();
                            new GUIMusicDialog().activate();
                        }
                    }

                    @Override
                    public boolean isOccluded() {
                        return false;
                    }
                }, new GUIActivationHighlightCallback() {
                    @Override
                    public boolean isHighlighted(InputState inputState) {
                        return MusicManager.getManager().isPlaying();
                    }

                    @Override
                    public boolean isVisible(InputState inputState) {
                        return true;
                    }

                    @Override
                    public boolean isActive(InputState inputState) {
                        return true;
                    }
                });
            }
        }, instance);
	}
}
