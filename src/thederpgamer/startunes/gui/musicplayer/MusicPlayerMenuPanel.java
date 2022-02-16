package thederpgamer.startunes.gui.musicplayer;

import api.utils.gui.GUIMenuPanel;
import org.schema.game.client.view.gui.buildtools.GUIBuildToolSettingSelector;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalProgressBar;
import org.schema.schine.input.InputState;
import thederpgamer.startunes.manager.ConfigManager;
import thederpgamer.startunes.manager.LogManager;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [02/15/2022]
 */
public class MusicPlayerMenuPanel extends GUIMenuPanel {

    public String currentTrack;
    public long trackLength;
    public long runTimer;

    private GUIMinMaxSetting volumeSlider;

    public MusicPlayerMenuPanel(InputState inputState) {
        super(inputState, "MusicPlayerMenu", GLFrame.getWidth() / 2, GLFrame.getHeight() / 2);
    }

    @Override
    public void update(Timer timer) {
        super.update(timer);
        if(runTimer <= 0) {
            Controller.getAudioManager().stopBackgroundMusic();
            currentTrack = null;
            runTimer = 0;
            trackLength = 0;
            recreateTabs();
        } else runTimer --;
    }

    @Override
    public void recreateTabs() {
        int lastTab = guiWindow.getSelectedTab();
        if(guiWindow.getTabs().size() > 0) guiWindow.clearTabs();
        final GUIContentPane contentPane = guiWindow.addTab("MUSIC");
        contentPane.setTextBoxHeightLast(40);
        contentPane.addNewTextBox((GLFrame.getHeight() / 2) + 67);

        GUITextOverlay currentTrackOverlay = new GUITextOverlay(30, 30, getState());
        currentTrackOverlay.onInit();
        currentTrackOverlay.setFont(FontLibrary.FontSize.BIG.getFont());
        currentTrackOverlay.orientate(ORIENTATION_LEFT);
        currentTrackOverlay.getPos().x += 5;
        //Todo: Clear when track is finished playing
        if(currentTrack == null) currentTrackOverlay.setTextSimple("No current music");
        else {
            String[] fields = currentTrack.split(" - ");
            currentTrackOverlay.setTextSimple("Now Playing: " + fields[0] + " - " + fields[1]);
        }
        contentPane.getContent(0).attach(currentTrackOverlay);

        GUIAncor progressBarAnchor = new GUIAncor(getState(), guiWindow.getInnerWidth() - 26, 24);
        GUIHorizontalProgressBar progressBar = new GUIHorizontalProgressBar(getState(), progressBarAnchor) {
            @Override
            public float getValue() {
                return getProgress();
            }
        };
        progressBar.onInit();
        progressBarAnchor.attach(progressBar);
        contentPane.getContent(0).attach(progressBarAnchor);
        progressBarAnchor.getPos().x += 2;
        progressBarAnchor.getPos().y += currentTrackOverlay.getHeight() + 2;
        contentPane.setTextBoxHeight(0, (int) (progressBar.getHeight() + currentTrackOverlay.getHeight() + 8));

        (new TrackScrollableList(getState(), contentPane.getContent(1), this)).onInit();
        contentPane.addNewTextBox(54);

        GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 2, 1, contentPane.getContent(2));
        buttonPane.onInit();
        buttonPane.addButton(0, 0, "STOP", GUIHorizontalArea.HButtonColor.ORANGE, new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if(mouseEvent.pressedLeftMouse()) {
                    if(currentTrack != null && runTimer > 0) {
                        Controller.getAudioManager().stopBackgroundMusic();
                        currentTrack = null;
                        runTimer = 0;
                        trackLength = 0;
                        LogManager.logInfo("Stopped playing music");
                        recreateTabs();
                    }
                }
            }

            @Override
            public boolean isOccluded() {
                return currentTrack == null || runTimer <= 0;
            }
        }, new GUIActivationCallback() {
            @Override
            public boolean isVisible(InputState inputState) {
                return true;
            }

            @Override
            public boolean isActive(InputState inputState) {
                return currentTrack != null && runTimer > 0;
            }
        });
        buttonPane.addButton(1, 0, "AUTOPLAY SETTINGS", GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if(mouseEvent.pressedLeftMouse()) {
                    //Todo
                }
            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        }, new GUIActivationCallback() {
            @Override
            public boolean isVisible(InputState inputState) {
                return true;
            }

            @Override
            public boolean isActive(InputState inputState) {
                return true;
            }
        });
        GUITextOverlay volumeOverlay = new GUITextOverlay(10, 10, getState());
        volumeOverlay.onInit();
        volumeOverlay.setFont(FontLibrary.FontSize.MEDIUM.getFont());
        volumeOverlay.setTextSimple("Volume: ");

        volumeSlider = new GUIMinMaxSetting(0, 10);
        GUIBuildToolSettingSelector volumeSelector = new GUIBuildToolSettingSelector(getState(), volumeSlider);
        volumeSelector.onInit();
        volumeSlider.set(ConfigManager.getMainConfig().getConfigurableInt("music-volume", 5));
        contentPane.getContent(0).attach(volumeSelector);
        contentPane.getContent(0).attach(volumeOverlay);
        contentPane.getContent(2).attach(buttonPane);
        contentPane.setTextBoxHeight(1, (GLFrame.getHeight() / 2) + 41);
        contentPane.setTextBoxHeight(2, (int) (buttonPane.getHeight() + 15));
        volumeOverlay.orientate(ORIENTATION_HORIZONTAL_MIDDLE);
        volumeSelector.orientate(ORIENTATION_HORIZONTAL_MIDDLE);
        volumeSelector.getPos().x += 150;
        guiWindow.setSelectedTab(lastTab);
    }

    private float getProgress() {
        float lengthSeconds = trackLength / 1000.0f;
        float runTimerSeconds = runTimer / 1000.0f;
        return lengthSeconds / runTimerSeconds;
    }
}
