package thederpgamer.startunes.gui.musicplayer;

import api.utils.gui.GUIInputDialog;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import thederpgamer.startunes.manager.MusicManager;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [02/18/2022]
 */
public class MusicDownloaderDialog extends GUIInputDialog {

    @Override
    public MusicDownloaderPanel createPanel() {
        return new MusicDownloaderPanel(getState(), this);
    }

    @Override
    public MusicDownloaderPanel getInputPanel() {
        return (MusicDownloaderPanel) super.getInputPanel();
    }

    @Override
    public void callback(GUIElement callingElement, MouseEvent mouseEvent) {
        if(!isOccluded() && mouseEvent.pressedLeftMouse()) {
            switch((String) callingElement.getUserPointer()) {
                case "X":
                case "CANCEL":
                    MusicManager.finishedDownloading = false;
                    deactivate();
                    break;
                case "OK":
                    if(getInputPanel().getTitle() != null && !getInputPanel().getTitle().isEmpty()) {
                        MusicManager.downloadFile(getInputPanel().getText(), getInputPanel().getTitle(), getInputPanel().getArtist());
                        if(MusicManager.finishedDownloading) {
                            if(MusicManager.valid) {
                                MusicManager.finishedDownloading = false;
                                MusicManager.valid = false;
                                deactivate();
                            } else {
                                PlayerOkCancelInput playerInput = new PlayerOkCancelInput("CONFIRM", getState(), 500, 300, Lng.str("Error"), "The provided link is not a valid .wav file or the entry already exists in database.") {
                                    @Override
                                    public void pressedOK() {
                                        getState().getController().queueUIAudio("0022_menu_ui - error 2");
                                        deactivate();
                                    }
                                    @Override
                                    public void onDeactivate() { }
                                };
                                playerInput.getInputPanel().onInit();
                                playerInput.getInputPanel().setCancelButton(false);
                                playerInput.getInputPanel().background.setPos(470.0F, 35.0F, 0.0F);
                                playerInput.getInputPanel().background.setWidth((float)(GLFrame.getWidth() - 435));
                                playerInput.getInputPanel().background.setHeight((float)(GLFrame.getHeight() - 70));
                                playerInput.activate();
                            }
                        }
                    }
                    break;
            }
        }
    }
}
