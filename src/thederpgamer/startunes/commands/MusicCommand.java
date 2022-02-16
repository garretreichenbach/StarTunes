package thederpgamer.startunes.commands;

import api.common.GameClient;
import api.mod.StarMod;
import api.utils.game.PlayerUtils;
import api.utils.game.chat.CommandInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.graphicsengine.core.Controller;
import thederpgamer.startunes.StarTunes;
import thederpgamer.startunes.manager.ResourceManager;

import javax.annotation.Nullable;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [02/15/2022]
 */
public class MusicCommand implements CommandInterface {

    @Override
    public String getCommand() {
        return "music";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"music"};
    }

    @Override
    public String getDescription() {
        return "Plays the specified music file for the client. Note: This command is for debugging purposes only and will be removed in a future release.\n" +
               "- /%COMMAND% play <name> : Plays the specified music file.\n" +
               "- /%COMMAND% stop : Stops playing music.\n" +
               "- /%COMMAND% list : Lists all tracks.";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public boolean onCommand(PlayerState sender, String[] args) {
        if(GameClient.getClientState() != null) {
            if(args.length >= 1) {
                if(args[0].toLowerCase().equals("play")) {
                    if(args.length != 2) return false;
                    else {
                        for(String s : ResourceManager.musicMap.keySet()) {
                            if(s.toLowerCase().contains(args[1].toLowerCase())) {
                                final String[] fields = s.split(" - ");
                                Controller.getAudioManager().playBackgroundMusic(fields[0], 5.0f);
                                StarTunes.getInstance().trackDrawer.setCurrentTrack(fields[0], fields[1]);
                                break;
                            }
                        }
                    }
                } else if(args.length == 1) {
                    if(args[0].toLowerCase().equals("stop") || args[0].toLowerCase().equals("pause")) Controller.getAudioManager().stopBackgroundMusic();
                    else if(args[0].toLowerCase().equals("list")) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("Available Tracks:\n");
                        for(String s : ResourceManager.musicMap.keySet()) builder.append(s).append("\n");
                        PlayerUtils.sendMessage(sender, builder.toString().trim());
                    } else return false;
                } else return false;
            } else return false;
        }
        return true;
    }

    @Override
    public void serverAction(@Nullable PlayerState sender, String[] args) {

    }

    @Override
    public StarMod getMod() {
        return StarTunes.getInstance();
    }
}
