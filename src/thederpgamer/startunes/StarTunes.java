package thederpgamer.startunes;

import api.common.GameClient;
import api.config.BlockConfig;
import api.listener.Listener;
import api.listener.events.gui.GUITopBarCreateEvent;
import api.listener.events.gui.HudCreateEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.utils.gui.ModGUIHandler;
import org.schema.game.client.view.gui.newgui.GUITopBar;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationHighlightCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;
import org.schema.schine.resource.ResourceLoader;
import thederpgamer.startunes.commands.MusicCommand;
import thederpgamer.startunes.drawer.MusicTrackDrawer;
import thederpgamer.startunes.gui.musicplayer.MusicPlayerControlManager;
import thederpgamer.startunes.manager.ConfigManager;
import thederpgamer.startunes.manager.LogManager;
import thederpgamer.startunes.manager.MusicManager;
import thederpgamer.startunes.manager.ResourceManager;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [02/15/2022]
 */
public class StarTunes extends StarMod {

    //Instance
    private static StarTunes instance;
    public static StarTunes getInstance() {
        return instance;
    }
    public static void main(String[] args) {
    }
    public StarTunes() {
        instance = this;
    }

    //GUI
    public MusicTrackDrawer trackDrawer;
    public MusicPlayerControlManager musicControlManager;

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;

        ConfigManager.initialize(this);
        LogManager.initialize();

        registerListeners();
        //registerCommands();
    }

    @Override
    public void onBlockConfigLoad(BlockConfig blockConfig) {
        /*
        //Blocks
        ElementManager.addBlock(new MusicBox()); //Todo: Don't add new elements if mod is client only

        ElementManager.initialize();
         */
    }

    @Override
    public void onResourceLoad(ResourceLoader resourceLoader) {
        ResourceManager.loadResources(this, resourceLoader);
    }

    private void registerListeners() {
        StarLoader.registerListener(HudCreateEvent.class, new Listener<HudCreateEvent>() {
            @Override
            public void onEvent(HudCreateEvent event) {
                event.addElement(trackDrawer = new MusicTrackDrawer(GameClient.getClientState()));
            }
        }, this);

        StarLoader.registerListener(GUITopBarCreateEvent.class, new Listener<GUITopBarCreateEvent>() {
            @Override
            public void onEvent(final GUITopBarCreateEvent event) {
                if(musicControlManager == null) {
                    musicControlManager = new MusicPlayerControlManager();
                    ModGUIHandler.registerNewControlManager(getSkeleton(), musicControlManager);
                    MusicManager.musicVolume = ConfigManager.getMainConfig().getConfigurableInt("music-volume", 5);
                }

                GUITopBar.ExpandedButton dropDownButton = event.getDropdownButtons().get(event.getDropdownButtons().size() - 1);
                dropDownButton.addExpandedButton("MUSIC PLAYER", new GUICallback() {
                    @Override
                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                        if(mouseEvent.pressedLeftMouse()) {
                            GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - enter");
                            GameClient.getClientState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().deactivateAll();
                            musicControlManager.setActive(true);
                        }
                    }

                    @Override
                    public boolean isOccluded() {
                        return false;
                    }
                }, new GUIActivationHighlightCallback() {
                    @Override
                    public boolean isHighlighted(InputState inputState) {
                        return false;
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
        }, this);
    }

    private void registerCommands() {
        StarLoader.registerCommand(new MusicCommand());
    }
}
