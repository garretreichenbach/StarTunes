package thederpgamer.startunes;

import api.common.GameClient;
import api.config.BlockConfig;
import api.listener.Listener;
import api.listener.events.gui.HudCreateEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import org.schema.schine.resource.ResourceLoader;
import thederpgamer.startunes.commands.MusicCommand;
import thederpgamer.startunes.drawer.MusicTrackDrawer;
import thederpgamer.startunes.manager.ConfigManager;
import thederpgamer.startunes.manager.LogManager;
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

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;

        ConfigManager.initialize(this);
        LogManager.initialize();

        registerListeners();
        registerCommands();
    }

    @Override
    public void onBlockConfigLoad(BlockConfig blockConfig) {
        /*
        //Blocks
        ElementManager.addBlock(new MusicBlock()); //Todo: Don't add new elements if mod is client only

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
    }

    private void registerCommands() {
        StarLoader.registerCommand(new MusicCommand());
    }
}
