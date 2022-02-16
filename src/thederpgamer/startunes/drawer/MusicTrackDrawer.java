package thederpgamer.startunes.drawer;

import api.common.GameClient;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [02/15/2022]
 */
public class MusicTrackDrawer extends GUIElement {

    private GUITextOverlay nowPlayingOverlay;
    private GUITextOverlay trackOverlay;
    private GUITextOverlay artistOverlay;
    private float fadeTimer;

    public MusicTrackDrawer(InputState inputState) {
        super(inputState);
    }

    @Override
    public void onInit() {

    }

    @Override
    public void draw() {
        if(nowPlayingOverlay != null) nowPlayingOverlay.draw();
        if(trackOverlay != null) trackOverlay.draw();
        if(artistOverlay != null) artistOverlay.draw();
    }

    @Override
    public void update(Timer timer) {
        fadeTimer -= timer.getDelta();
        if(fadeTimer <= 0) {
            if(nowPlayingOverlay != null) nowPlayingOverlay.cleanUp();
            if(trackOverlay != null) trackOverlay.cleanUp();
            if(artistOverlay != null) artistOverlay.cleanUp();
            nowPlayingOverlay = null;
            trackOverlay = null;
            artistOverlay = null;
        } else { //Todo: Add setting to not fade out
            if(nowPlayingOverlay != null) nowPlayingOverlay.getColor().a -= 0.0015f;
            if(trackOverlay != null) trackOverlay.getColor().a -= 0.0015f;
            if(artistOverlay != null) artistOverlay.getColor().a -= 0.0015f;
        }
    }

    @Override
    public void cleanUp() {

    }

    @Override
    public boolean isInvisible() {
        return false;
    }

    public void setCurrentTrack(String name, String artist) {
        if(nowPlayingOverlay != null) nowPlayingOverlay.cleanUp();
        if(trackOverlay != null) trackOverlay.cleanUp();
        if(artistOverlay != null) artistOverlay.cleanUp();
        nowPlayingOverlay = null;
        trackOverlay = null;
        artistOverlay = null;

        nowPlayingOverlay = new GUITextOverlay(10, 10, GameClient.getClientState());
        nowPlayingOverlay.orientate(GUIElement.ORIENTATION_TOP | GUIElement.ORIENTATION_LEFT);
        nowPlayingOverlay.getPos().x += 15;
        nowPlayingOverlay.getPos().y += 10;
        nowPlayingOverlay.onInit();
        nowPlayingOverlay.setFont(FontLibrary.FontSize.MEDIUM.getFont());
        nowPlayingOverlay.setTextSimple("Now Playing:");

        trackOverlay = new GUITextOverlay(10, 10, GameClient.getClientState());
        trackOverlay.orientate(GUIElement.ORIENTATION_TOP | GUIElement.ORIENTATION_LEFT);
        trackOverlay.getPos().x += 15;
        trackOverlay.getPos().y += 30;
        trackOverlay.onInit();
        trackOverlay.setFont(FontLibrary.FontSize.BIG.getFont());
        trackOverlay.setTextSimple(name);
        trackOverlay.getScale().scale(1.2f);

        artistOverlay = new GUITextOverlay(10, 10, GameClient.getClientState());
        artistOverlay.orientate(GUIElement.ORIENTATION_TOP | GUIElement.ORIENTATION_LEFT);
        artistOverlay.getPos().x += 15;
        artistOverlay.getPos().y += 60;
        artistOverlay.onInit();
        artistOverlay.setFont(FontLibrary.FontSize.MEDIUM.getFont());
        artistOverlay.setTextSimple(artist);
        artistOverlay.getScale().scale(1.2f);

        fadeTimer = 15;
    }

    @Override
    public float getWidth() {
        return 0;
    }

    @Override
    public float getHeight() {
        return 0;
    }
}
