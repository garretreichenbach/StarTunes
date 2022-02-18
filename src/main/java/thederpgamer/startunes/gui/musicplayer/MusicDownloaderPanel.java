package thederpgamer.startunes.gui.musicplayer;

import api.utils.gui.GUIInputDialogPanel;
import org.schema.schine.common.TextCallback;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.input.InputState;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [02/18/2022]
 */
public class MusicDownloaderPanel extends GUIInputDialogPanel {

    private GUIActivatableTextBar textInput;
    private GUIActivatableTextBar titleInput;
    private GUIActivatableTextBar artistInput;

    public MusicDownloaderPanel(InputState inputState, GUICallback guiCallback) {
        super(inputState, "musicdownloaderpanel", "Music Downloader", "Input direct download link to a .wav file", 750, 400, guiCallback);
    }

    @Override
    public void onInit() {
        super.onInit();
        GUIContentPane contentPane = ((GUIDialogWindow) background).getMainContentPane();
        contentPane.setTextBoxHeightLast(350);

        textInput = new GUIActivatableTextBar(
                        getState(),
                        FontLibrary.FontSize.MEDIUM,
                        128,
                        1,
                        "Direct download link (.wav)",
                        contentPane.getContent(0),
                        new TextCallback() {
                            @Override
                            public String[] getCommandPrefixes() {
                                return null;
                            }

                            @Override
                            public String handleAutoComplete(String s, TextCallback textCallback, String s1) throws PrefixNotFoundException {
                                return null;
                            }

                            @Override
                            public void onFailedTextCheck(String s) {}

                            @Override
                            public void onTextEnter(String s, boolean b, boolean b1) {}

                            @Override
                            public void newLine() {}
                        },
                        null);
        textInput.onInit();
        contentPane.getContent(0).attach(textInput);

        titleInput = new GUIActivatableTextBar(
                getState(),
                FontLibrary.FontSize.MEDIUM,
                128,
                1,
                "Track Title",
                contentPane.getContent(0),
                new TextCallback() {
                    @Override
                    public String[] getCommandPrefixes() {
                        return null;
                    }

                    @Override
                    public String handleAutoComplete(String s, TextCallback textCallback, String s1) throws PrefixNotFoundException {
                        return null;
                    }

                    @Override
                    public void onFailedTextCheck(String s) {}

                    @Override
                    public void onTextEnter(String s, boolean b, boolean b1) {}

                    @Override
                    public void newLine() {}
                },
                null);
        titleInput.onInit();
        contentPane.getContent(0).attach(titleInput);
        titleInput.getPos().y += 30;

        artistInput = new GUIActivatableTextBar(
                getState(),
                FontLibrary.FontSize.MEDIUM,
                128,
                1,
                "Artist Name",
                contentPane.getContent(0),
                new TextCallback() {
                    @Override
                    public String[] getCommandPrefixes() {
                        return null;
                    }

                    @Override
                    public String handleAutoComplete(String s, TextCallback textCallback, String s1) throws PrefixNotFoundException {
                        return null;
                    }

                    @Override
                    public void onFailedTextCheck(String s) {}

                    @Override
                    public void onTextEnter(String s, boolean b, boolean b1) {}

                    @Override
                    public void newLine() {}
                },
                null);
        artistInput.onInit();
        contentPane.getContent(0).attach(artistInput);
        artistInput.getPos().y += 60;
    }

    public String getText() {
        if(textInput.getText() == null) return "";
        else return textInput.getText();
    }

    public void setText(String text) {
        if(text != null) textInput.setTextWithoutCallback(text);
        else textInput.setTextWithoutCallback("");
    }

    public String getTitle() {
        return titleInput.getText();
    }

    public String getArtist() {
        if(artistInput.getText() == null) return "Unknown";
        else return artistInput.getText();
    }
}
